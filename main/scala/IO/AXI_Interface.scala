package IO
import chisel3._

import Common.ISA._

class AXI_interface extends Bundle {

  //address read
  val arid    = Input(UInt(4.W))
  val araddr  = Input(UInt(ADDR_SIZE.W))
  val arlen   = Input(UInt(8.W))
  val arsize  = Input(UInt(3.W))
  val arburst = Input(UInt(2.W))
  val arlock  = Input(UInt(2.W))
  val arcache = Input(UInt(4.W))
  val arprot  = Input(UInt(3.W))
  val arvalid = Input(Bool())
  val arready = Output(Bool())

  //read data
  val rid     = Output(UInt(4.W))
  val rdata   = Output(UInt(XLEN.W))
  val rresp   = Output(UInt(2.W))
  val rlast   = Output(Bool())
  val rvalid  = Output(Bool())
  val rready  = Input(Bool())

  //address write
  val awid    = Input(UInt(4.W))
  val awaddr  = Input(UInt(ADDR_SIZE.W))
  val awlen   = Input(UInt(8.W))
  val awsize  = Input(UInt(3.W))
  val awburst = Input(UInt(2.W))
  val awlock  = Input(UInt(2.W))
  val awcache = Input(UInt(4.W))
  val awprot  = Input(UInt(3.W))
  val awvalid = Input(Bool())
  val awready = Output(Bool())

  //write data
  val wid     = Input(UInt(4.W))
  val wdata   = Input(UInt(XLEN.W))
  val wstrb   = Input(UInt(4.W))
  val wlast   = Input(Bool())
  val wvalid  = Input(Bool())
  val wready  = Output(Bool())

  //write finish
  val bid     = Output(UInt(4.W))
  val bresp   = Output(UInt(2.W))
  val bvalid  = Output(Bool())
  val bready  = Input(Bool())

}

