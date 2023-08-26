package IO

import chisel3._

import Common.ISA._

class RamIO extends Bundle{
  //true: 有读写请求
  val req     = Output(Bool())
  //true: 写请求
  val wr      = Output(Bool())
  //0: 1byte；1: 2bytes；2: 4bytes 和byteena功能类似
  val size    = Output(UInt(3.W))
  val addr    = Output(UInt(ADDR_SIZE.W))
  val wdata   = Output(UInt(XLEN.W))

  val rdata   = Input(UInt(XLEN.W))
  val addr_ok = Input(Bool())
  val data_ok = Input(Bool())
}
