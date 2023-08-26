package AXI

import chisel3._
import chisel3.util._
import Common.ISA._

/*
class Cache extends Module {
  val io = IO(new Bundle() {
        val inst_req     = Input(Bool())
        val inst_addr    = Input(UInt(32.W))
        val inst_rdata   = Output(UInt(32.W))
        val inst_data_ok = Output(Bool())

        val data_req      = Input(Bool())
        val data_wr       = Input(Bool())
        val data_size     = Input(UInt(2.W))
        val data_addr     = Input(UInt(32.W))
        val data_wdata    = Input(UInt(32.W))
        val data_rdata    = Output(UInt(32.W))
        val data_data_ok  = Output(Bool())

        val arid    = Output(UInt(4.W))
        val araddr  = Output(UInt(32.W))
        val arlen   = Output(UInt(8.W))
        val arsize  = Output(UInt(3.W))
        val arburst = Output(UInt(2.W))
        val arlock  = Output(UInt(2.W))
        val arcache = Output(UInt(4.W))
        val arprot  = Output(UInt(3.W))
        val arvalid = Output(Bool())
        val arready = Input(Bool())

        val rid    = Input(UInt(4.W))
        val rdata  = Input(UInt(32.W))
        val rresp  = Input(UInt(2.W))
        val rlast  = Input(Bool())
        val rvalid = Input(Bool())
        val rready = Output(Bool())

        val awid    = Output(UInt(4.W))
        val awaddr  = Output(UInt(32.W))
        val awlen   = Output(UInt(8.W))
        val awsize  = Output(UInt(3.W))
        val awburst = Output(UInt(2.W))
        val awlock  = Output(UInt(2.W))
        val awcache = Output(UInt(4.W))
        val awprot  = Output(UInt(3.W))
        val awvalid = Output(Bool())
        val awready = Input(Bool())

        val wid    = Output(UInt(4.W))
        val wdata  = Output(UInt(32.W))
        val wstrb  = Output(UInt(4.W))
        val wlast  = Output(Bool())
        val wvalid = Output(Bool())
        val wready = Input(Bool())

        val bid    = Input(UInt(4.W))
        val bresp  = Input(UInt(2.W))
        val bvalid = Input(Bool())
        val bready = Output(Bool())

        val doInst = Output(Bool())
        val doData = Output(Bool())
    })

    // [153]:valid [152]:dirty [151:128]:tag [127:0]:data
    val cache1_r = RegInit(VecInit(Seq.fill(16){0.U(154.W)}))
    val cache2_r = RegInit(VecInit(Seq.fill(16){0.U(154.W)}))

    val inst_addr_offset = inst_addr(3, 0)
    val inst_addr_index  = inst_addr(7, 4)
    val inst_addr_tag    = inst_addr(31, 8)
    val data_addr_offset = data_addr(3, 0)
    val data_addr_index  = data_addr(7, 4)
    val data_addr_tag    = data_addr(31, 8)
    val addr_offset = Mux(data_req)

    val inst_hit = cache1_r(inst_addr_index)(153) && 
                    (cache1_r(inst_addr_index)(151, 128) === inst_addr_tag) ||
                   cache2_r(inst_addr_index)(153) &&
                    (cache2_r(inst_addr_index)(151, 128) === inst_addr_tag)
    val data_hit = cache1_r(data_addr_index)(153) && 
                    (cache1_r(data_addr_index)(151, 128) === data_addr_tag) ||
                   cache2_r(data_addr_index)(153) &&
                    (cache2_r(data_addr_index)(151, 128) === data_addr_tag)

    //addr
    val do_req     = RegInit(false.B)
    val do_req_or  = RegInit(false.B)
    val do_wr_r    = RegInit(false.B)
    val do_size_r  = RegInit(0.U(2.W))
    val do_addr_r  = RegInit(0.U(32.W))
    val do_wdata_r = RegInit(0.U(32.W))
    val data_back = Wire(Bool())

    io.inst_addr_ok := !do_req && !io.data_req
    io.data_addr_ok := !do_req
    do_req     := Mux((io.inst_req || io.data_req) && !do_req, true.B,
                      Mux(data_back, false.B, do_req))
    do_req_or  := Mux(!do_req, io.data_req, do_req_or)
    do_wr_r    := Mux(io.data_req && io.data_addr_ok, io.data_wr,
                      Mux(io.inst_req && io.inst_addr_ok, io.inst_wr, do_wr_r))
    do_size_r  := Mux(io.data_req && io.data_addr_ok, io.data_size,
                      Mux(io.inst_req && io.inst_addr_ok, io.inst_size, do_size_r))
    do_addr_r  := Mux(io.data_req && io.data_addr_ok, io.data_addr,
                      Mux(io.inst_req && io.inst_addr_ok, io.inst_addr, do_addr_r))
    do_wdata_r := Mux(io.data_req && io.data_addr_ok, io.data_wdata,
                      Mux(io.inst_req && io.inst_addr_ok, io.inst_wdata, do_wdata_r))

    //inst sram-like
    io.inst_data_ok := do_req && !do_req_or && data_back
    io.data_data_ok := do_req &&  do_req_or && data_back
    io.inst_rdata   := io.rdata
    io.data_rdata   := io.rdata

    //---axi
    val addr_rcv  = RegInit(false.B)
    val wdata_rcv = RegInit(false.B)

    data_back := addr_rcv && ((io.rvalid && io.rready) || (io.bvalid && io.bready))
    addr_rcv  := Mux(io.arvalid && io.arready, true.B,
                     Mux(io.awvalid && io.awready, true.B,
                         Mux(data_back, false.B, addr_rcv)))
    wdata_rcv := Mux(io.wvalid && io.wready, true.B,
                     Mux(data_back, false.B, wdata_rcv))

    //ar
    io.arid    := 0.U
    io.araddr  := do_addr_r
    io.arlen   := 0.U
    io.arsize  := do_size_r
    io.arburst := 0.U
    io.arlock  := 0.U
    io.arcache := 0.U
    io.arprot  := 0.U
    io.arvalid := do_req && !do_wr_r && !addr_rcv

    //r
    io.rready  := true.B

    //aw
    io.awid    := 0.U
    io.awaddr  := do_addr_r
    io.awlen   := 0.U
    io.awsize  := do_size_r
    io.awburst := 0.U
    io.awlock  := 0.U
    io.awcache := 0.U
    io.awprot  := 0.U
    io.awvalid := do_req && do_wr_r && !addr_rcv

    //w
    io.wid    := 0.U
    io.wdata  := do_wdata_r
    io.wstrb  := Mux(do_size_r === 0.U, 1.U(4.W) << do_addr_r(1, 0),
                     Mux(do_size_r === 1.U, 3.U(4.W) << do_addr_r(1, 0), 15.U))
    io.wlast  := true.B
    io.wvalid := do_req && do_wr_r && !wdata_rcv

    //b
    io.bready := true.B
    
    io.doInst := do_req && !do_req_or
    io.doData := do_req && do_req_or
}
*/
