package IO

import chisel3._
import Common.ISA._

class IF_PortIO extends Bundle {
  // 是否选择分支跳转
  val Valid_Branch = Input(Bool())
  // 分支跳转目的PC
  val TargetPC_Branch = Input(UInt(PC_SIZE.W))
  // 是否选择无条件跳转pc(1:有效 0:无效)
  val Valid_J = Input(Bool())
  // 无条件跳转目的PC
  val TargetPC_J = Input(UInt(PC_SIZE.W))
  // 目标指令的地址--用于向存储器获取指令
  val InstrAddr = Output(UInt(ADDR_SIZE.W))
  // 获取到的指令
  val Instr_In = Input(UInt(INSTR_SIZE.W))


  // 输出到ID的指令
  val Instr_Out = Output(UInt(INSTR_SIZE.W))
  //  输出到ID的PC
  val PC = Output(UInt(PC_SIZE.W))
}
