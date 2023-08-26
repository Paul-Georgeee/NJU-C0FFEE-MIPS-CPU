package Core

import chisel3._
import chisel3.util._
import Common.ISA._

class Map extends Module {
  val io = IO(new Bundle() {
    val in  = Input(UInt(ADDR_SIZE.W))
    val out = Output(UInt(ADDR_SIZE.W))
  })
  when(io.in(31,28) >= "he".U(4.W))
  {
    io.out := io.in
  }.elsewhen(io.in(31, 28) >= "hc".U(4.W))
  {
    io.out := io.in
  }.elsewhen(io.in(31, 28) >= "ha".U(4.W))
  {
    io.out := Cat(io.in(31, 28) - "ha".U(4.W), io.in(27, 0))
  }.otherwise{
    io.out := Cat(io.in(31, 28) - "h8".U(4.W), io.in(27, 0))
  }
}

object test extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new Map(), Array("--target-dir", "build"))
}
