package Decoder

import chisel3._
import chisel3.util.BitPat
import Common.ISA._

class ID_BranchDecoder extends RawModule {
  val io = IO(new Bundle() {
    val opcode = Input(UInt(6.W))
    val func5 = Input(UInt(5.W))
    val busa = Input(UInt(XLEN.W))
    val busb = Input(UInt(XLEN.W))
    val ImmExpand = Input(UInt(XLEN.W))
    val pc4 = Input(UInt(PC_SIZE.W))
    val Valid_Branch = Output(Bool())
    val TargetPC_Branch = Output(UInt(PC_SIZE.W))
  })

  io.Valid_Branch := ((io.opcode === BitPat("b000100")) && (io.busa === io.busb)) ||
    ((io.opcode === BitPat("b000101")) && (io.busa =/= io.busb)) ||
    ((io.opcode === BitPat("b000001")) && (io.func5 === BitPat("b00001")) && (io.busa.asSInt >= 0.S(32.W))) ||
    ((io.opcode === BitPat("b000111")) && (io.func5 === BitPat("b00000")) && (io.busa.asSInt > 0.S(32.W)))  ||
    ((io.opcode === BitPat("b000110")) && (io.func5 === BitPat("b00000")) && (io.busa.asSInt <= 0.S(32.W))) ||
    ((io.opcode === BitPat("b000001")) && (io.func5 === BitPat("b00000")) && (io.busa.asSInt < 0.S(32.W)))  ||
    ((io.opcode === BitPat("b000001")) && (io.func5 === BitPat("b10001")) && (io.busa.asSInt >= 0.S(32.W))) ||
    ((io.opcode === BitPat("b000001")) && (io.func5 === BitPat("b10000")) && (io.busa.asSInt < 0.S(32.W)))

  io.TargetPC_Branch := io.ImmExpand + io.pc4
}


