package Core

import Chisel.MuxCase
import chisel3._
import chisel3.util._
import Common.ISA._
import Common.Instrcutions._
import Common.DecodeSignal._

class IF extends Module {
  val io = IO(new Bundle() {
    // 是否选择分支跳转
    val Valid_Branch = Input(Bool())
    // 分支跳转目的PC
    val TargetPC_Branch = Input(UInt(PC_SIZE.W))
    // 是否选择无条件跳转pc(1:有效 0:无效)
    val Valid_J = Input(Bool())
    // 无条件跳转目的PC
    val TargetPC_J = Input(UInt(PC_SIZE.W))
    // 异常信号
    val ExceptionFlush = Input(Bool())
    // 是否选择epc
    val Vaild_EPC = Input(Bool())
    // 目的EPC
    val TargetPC_EPC = Input(UInt(PC_SIZE.W))
    // 目标指令的地址--用于向存储器获取指令
    val InstrAddr = Output(UInt(ADDR_SIZE.W))
    // 获取到的指令
    val Instr_In = Input(UInt(INSTR_SIZE.W))
    // 输出到ID的指令
    val Instr_Out = Output(UInt(INSTR_SIZE.W))
    //  输出到ID的PC
    val PC = Output(UInt(PC_SIZE.W))
    // 该条指令是否是延迟槽指令
    val DelaySlot = Output(Bool())
    // 读取的Cause寄存器的数值
    val CauseRead = Input(UInt(XLEN.W))
    // 传递的异常信号
    val Exception = Output(UInt(Exception_WIDTH.W))
    //EX的PC
    val EX_PC = Input(UInt(PC_SIZE.W))
    //
    val flush = Input(Bool())
    //
    val stall = Input(Bool())
  })

  //初始化PC寄存器的值为0
  val NextPC     = RegInit("hbfc00000".U(32.W))
  val PC         = RegInit("hbfc00000".U(32.W))
  val Instr      = RegInit(0.U(32.W))
  val DelaySlot_ = RegInit(false.B)
  val DelaySlot  = RegInit(false.B)
  val Exception  = RegInit(0.U(Exception_WIDTH.W))

  DelaySlot := MuxCase(DelaySlot_,
    Array(
      (io.ExceptionFlush) -> false.B,
      (io.stall         ) -> DelaySlot,
      (io.flush         ) -> false.B
    )
  )
  //处理软件中断 以及 指令地址错
  val IP_1_0   = io.CauseRead(9,8)
  val Exception_tmp = MuxCase(Exception_No,
    Array(
      (io.ExceptionFlush === true.B) -> Exception_No,
      (io.stall          === true.B) -> Exception,
      (io.flush          === true.B) -> Exception_No,
      (IP_1_0 =/= 0.U(2.W))         -> Exception_Soft,
      (NextPC(1, 0) =/= 0.U(2.W))   -> Exception_InstrAddr
    )
  )
  Exception := Exception_tmp
  PC := MuxCase(NextPC,
    Array(
      (io.ExceptionFlush === true.B) -> EXCEPTION_ADDR,
      (io.stall          === true.B) -> PC,
      (io.flush          === true.B) -> 0.U(PC_SIZE),
      (IP_1_0 =/= 0.U(2.W))          -> (NextPC - 4.U),
    )
  )

  NextPC := MuxCase(NextPC + 4.U(32.W),
    Array(
//      (IP_1_0 =/= 0.U(2.W))          -> io.EX_PC,
      (io.ExceptionFlush === true.B) -> EXCEPTION_ADDR,
      (io.stall          === true.B) -> NextPC,
      (io.Vaild_EPC      === true.B) -> io.TargetPC_EPC,
      (io.flush          === true.B) -> 0.U(PC_SIZE),
      (io.Valid_Branch   === true.B) -> io.TargetPC_Branch,
      (io.Valid_J        === true.B) -> io.TargetPC_J,
    )
  )

  val Instr_tmp = MuxCase(io.Instr_In,
    Array(
//      (IP_1_0 =/= 0.U(2.W))          -> 0.U(INSTR_SIZE.W),
      (io.ExceptionFlush === true.B) -> 0.U(INSTR_SIZE.W),
      (io.stall          === true.B) -> Instr,
      (io.Vaild_EPC      === true.B) -> 0.U(INSTR_SIZE.W),
      (io.flush          === true.B) -> 0.U(INSTR_SIZE.W),
    )
  )
  Instr := Instr_tmp
  val SignalList = ListLookup(io.Instr_In,
    List(false.B),
    Array(
      BEQ      ->  List(true.B),
      BNE      ->  List(true.B),
      BGEZ     ->  List(true.B),
      BGTZ     ->  List(true.B),
      BLEZ     ->  List(true.B),
      BLTZ     ->  List(true.B),
      BGEZAL   ->  List(true.B),
      BLTZAL   ->  List(true.B),
      J        ->  List(true.B),
      JAL      ->  List(true.B),
      JR       ->  List(true.B),
      JALR     ->  List(true.B)
    )
  )
  DelaySlot_ := MuxCase(SignalList(0),
    Array(
      (io.ExceptionFlush === true.B) -> false.B,
      (io.stall          === true.B) -> DelaySlot_,
      (io.flush          === true.B) -> false.B
    )
  )

  io.DelaySlot := DelaySlot
  io.InstrAddr := NextPC
  io.Instr_Out := Instr
  io.Exception := Exception
  io.PC := PC

  printf("---------------------------------------------------------------------------------\n")
  printf("IF: PC: 0x%x Instr: 0x%x  Isbranch %d DelaySlot: 0x%x  ExceptionFLush: %d ValidEPC: %d flush: %d stall: %d exception: %d\n",
    NextPC,
    Instr_tmp,
    DelaySlot_,
    io.DelaySlot,
    io.ExceptionFlush,
    io.Vaild_EPC,
    io.flush,
    io.stall,
    Exception_tmp
  )
}

object main_IF extends App{
  println(
    new(chisel3.stage.ChiselStage).emitVerilog(
      new IF
    )
  )
}
