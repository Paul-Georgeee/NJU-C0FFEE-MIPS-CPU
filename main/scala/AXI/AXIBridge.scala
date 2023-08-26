package AXI

import chisel3._
import chisel3.util._
import Common.ISA._
import IO.AXI_interface


class AXIBridge extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle() {
        val clock = Input(Clock())
        val reset = Input(Reset())

        // inst sram-like
        val inst_req     = Input(Bool())
        val inst_wr      = Input(Bool())
        val inst_size    = Input(UInt(2.W))
        val inst_addr    = Input(UInt(32.W))
        val inst_wdata   = Input(UInt(32.W))
        val inst_rdata   = Output(UInt(32.W))
        val inst_addr_ok = Output(Bool())
        val inst_data_ok = Output(Bool())

        // data sram-like
        val data_req      = Input(Bool())
        val data_wr       = Input(Bool())
        val data_size     = Input(UInt(2.W))
        val data_addr     = Input(UInt(32.W))
        val data_wdata    = Input(UInt(32.W))
        val data_rdata    = Output(UInt(32.W))
        val data_addr_ok  = Output(Bool())
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

  setInline("cpu_axi_interface.v",
    s"""
       |module cpu_axi_interface
       |(
       |    input         clock,
       |    input         reset,
       |
       |    //inst sram-like
       |    input         inst_req     ,
       |    input         inst_wr      ,
       |    input  [1 :0] inst_size    ,
       |    input  [31:0] inst_addr    ,
       |    input  [31:0] inst_wdata   ,
       |    output [31:0] inst_rdata   ,
       |    output        inst_addr_ok ,
       |    output        inst_data_ok ,
       |
       |    //data sram-like
       |    input         data_req     ,
       |    input         data_wr      ,
       |    input  [1 :0] data_size    ,
       |    input  [31:0] data_addr    ,
       |    input  [31:0] data_wdata   ,
       |    output [31:0] data_rdata   ,
       |    output        data_addr_ok ,
       |    output        data_data_ok ,
       |
       |    //axi
       |    //ar
       |    output [3 :0] arid         ,
       |    output [31:0] araddr       ,
       |    output [7 :0] arlen        ,
       |    output [2 :0] arsize       ,
       |    output [1 :0] arburst      ,
       |    output [1 :0] arlock        ,
       |    output [3 :0] arcache      ,
       |    output [2 :0] arprot       ,
       |    output        arvalid      ,
       |    input         arready      ,
       |    //r
       |    input  [3 :0] rid          ,
       |    input  [31:0] rdata        ,
       |    input  [1 :0] rresp        ,
       |    input         rlast        ,
       |    input         rvalid       ,
       |    output        rready       ,
       |    //aw
       |    output [3 :0] awid         ,
       |    output [31:0] awaddr       ,
       |    output [7 :0] awlen        ,
       |    output [2 :0] awsize       ,
       |    output [1 :0] awburst      ,
       |    output [1 :0] awlock       ,
       |    output [3 :0] awcache      ,
       |    output [2 :0] awprot       ,
       |    output        awvalid      ,
       |    input         awready      ,
       |    //w
       |    output [3 :0] wid          ,
       |    output [31:0] wdata        ,
       |    output [3 :0] wstrb        ,
       |    output        wlast        ,
       |    output        wvalid       ,
       |    input         wready       ,
       |    //b
       |    input  [3 :0] bid          ,
       |    input  [1 :0] bresp        ,
       |    input         bvalid       ,
       |    output        bready       ,
       |
       |    output        doInst       ,
       |    output        doData
       |);
       |//addr
       |reg do_req;
       |reg do_req_or; //req is inst or data;1:data,0:inst
       |reg        do_wr_r;
       |reg [1 :0] do_size_r;
       |reg [31:0] do_addr_r;
       |reg [31:0] do_wdata_r;
       |wire data_back;
       |
       |assign inst_addr_ok = !do_req&&!data_req;
       |assign data_addr_ok = !do_req;
       |always @(posedge clk)
       |begin
       |    do_req     <= reset                       ? 1'b0 :
       |                  (inst_req||data_req)&&!do_req ? 1'b1 :
       |                  data_back                     ? 1'b0 : do_req;
       |    do_req_or  <= reset ? 1'b0 :
       |                  !do_req ? data_req : do_req_or;
       |
       |    do_wr_r    <= data_req&&data_addr_ok ? data_wr :
       |                  inst_req&&inst_addr_ok ? inst_wr : do_wr_r;
       |    do_size_r  <= data_req&&data_addr_ok ? data_size :
       |                  inst_req&&inst_addr_ok ? inst_size : do_size_r;
       |    do_addr_r  <= data_req&&data_addr_ok ? data_addr :
       |                  inst_req&&inst_addr_ok ? inst_addr : do_addr_r;
       |    do_wdata_r <= data_req&&data_addr_ok ? data_wdata :
       |                  inst_req&&inst_addr_ok ? inst_wdata :do_wdata_r;
       |end
       |
       |//inst sram-like
       |assign inst_data_ok = do_req&&!do_req_or&&data_back;
       |assign data_data_ok = do_req&& do_req_or&&data_back;
       |assign inst_rdata   = rdata;
       |assign data_rdata   = rdata;
       |
       |//---axi
       |reg addr_rcv;
       |reg wdata_rcv;
       |
       |assign data_back = addr_rcv && (rvalid&&rready||bvalid&&bready);
       |always @(posedge clk)
       |begin
       |    addr_rcv  <= reset          ? 1'b0 :
       |                 arvalid&&arready ? 1'b1 :
       |                 awvalid&&awready ? 1'b1 :
       |                 data_back        ? 1'b0 : addr_rcv;
       |    wdata_rcv <= reset        ? 1'b0 :
       |                 wvalid&&wready ? 1'b1 :
       |                 data_back      ? 1'b0 : wdata_rcv;
       |end
       |//ar
       |assign arid    = 4'd0;
       |assign araddr  = do_addr_r;
       |assign arlen   = 8'd0;
       |assign arsize  = do_size_r;
       |assign arburst = 2'd0;
       |assign arlock  = 2'd0;
       |assign arcache = 4'd0;
       |assign arprot  = 3'd0;
       |assign arvalid = do_req&&!do_wr_r&&!addr_rcv;
       |//r
       |assign rready  = 1'b1;
       |
       |//aw
       |assign awid    = 4'd0;
       |assign awaddr  = do_addr_r;
       |assign awlen   = 8'd0;
       |assign awsize  = do_size_r;
       |assign awburst = 2'd0;
       |assign awlock  = 2'd0;
       |assign awcache = 4'd0;
       |assign awprot  = 3'd0;
       |assign awvalid = do_req&&do_wr_r&&!addr_rcv;
       |//w
       |assign wid    = 4'd0;
       |assign wdata  = do_wdata_r;
       |assign wstrb  = do_size_r==2'd0 ? 4'b0001<<do_addr_r[1:0] :
       |                do_size_r==2'd1 ? 4'b0011<<do_addr_r[1:0] : 4'b1111;
       |assign wlast  = 1'd1;
       |assign wvalid = do_req&&do_wr_r&&!wdata_rcv;
       |//b
       |assign bready  = 1'b1;
       |
       |assign doInst  = do_req && !do_req_or;
       |assign doData  = do_req &&  do_req_or;
       |
       |endmodule
       |

       """.stripMargin)
}

/*
class AXIBridge extends Module {
  val io = IO(new Bundle() {
        // inst sram-like
        val inst_req     = Input(Bool())
        val inst_wr      = Input(Bool())
        val inst_size    = Input(UInt(2.W))
        val inst_addr    = Input(UInt(32.W))
        val inst_wdata   = Input(UInt(32.W))
        val inst_rdata   = Output(UInt(32.W))
        val inst_addr_ok = Output(Bool())
        val inst_data_ok = Output(Bool())

        // data sram-like
        val data_req      = Input(Bool())
        val data_wr       = Input(Bool())
        val data_size     = Input(UInt(2.W))
        val data_addr     = Input(UInt(32.W))
        val data_wdata    = Input(UInt(32.W))
        val data_rdata    = Output(UInt(32.W))
        val data_addr_ok  = Output(Bool())
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
