package Decoder

import chisel3._
import chisel3.util._
import Common.ISA._

class ID_JDecoder extends RawModule{
  val io = IO(new Bundle() {
    val instr_index = Input(UInt(26.W))
    val opcode = Input(UInt(6.W))
    val func6 = Input(UInt(6.W))
    val sa = Input(UInt(5.W))
    val rd = Input(UInt(5.W))
    val rt = Input(UInt(5.W))
    val pc4 = Input(UInt(PC_SIZE.W))
    val Busa = Input(UInt(XLEN.W))
    val Valid_J = Output(Bool())
    val TargetPC_J = Output(UInt(PC_SIZE.W))
  })

  val Valid_J= (io.opcode === BitPat("b000011")) ||
    (io.opcode === BitPat("b000010")) ||
    (io.opcode === BitPat("b000000") && io.func6 === BitPat("b001000") && io.sa === 0.U(5.W) && io.rd === 0.U(5.W) && io.rt ===0.U(5.W)) ||
    (io.opcode === BitPat("b000000") && io.func6 === BitPat("b001001") && io.sa === 0.U(5.W) && io.rt ===0.U(5.W))
  val TargetPC_J = MuxCase(0.U(32.W),
    Array(
      (Valid_J && io.opcode =/= BitPat("b000000")) -> Cat(io.pc4(31,28), io.instr_index, 0.U(2.W)),
      (Valid_J && io.opcode === BitPat("b000000")) -> io.Busa
    )
  )

  io.TargetPC_J := TargetPC_J
  io.Valid_J := Valid_J
}

