package Decoder

import chisel3._
import chisel3.util._
import Common.ISA._
import Common.DecodeSignal._

class ID_EXTDecoder extends RawModule{
  val io = IO(new Bundle() {
    val imm = Input(UInt(16.W))
    val sa = Input(UInt(5.W))
    val EXTCtrl = Input(UInt(EXTCtrl_WIDTH.W))
    val ImmExpand = Output(UInt(XLEN.W))
  })

  val ImmExpand = MuxCase(0.U(32.W),
    Array(
      (io.EXTCtrl === 0.U(3.W)) -> Cat(0.U(16.W), io.imm),
      (io.EXTCtrl === 1.U(3.W)) -> Cat(Fill(16, io.imm(15)), io.imm),
      (io.EXTCtrl === 2.U(3.W)) -> Cat(Fill(14, io.imm(15)), io.imm, 0.U(2.W)),
      (io.EXTCtrl === 3.U(3.W)) -> Cat(io.imm, 0.U(16.W)),
      (io.EXTCtrl === 4.U(3.W)) -> Cat(0.U(27.W), io.sa)
    )
  )

  io.ImmExpand := ImmExpand
}
