package Core

import Common.DecodeSignal._
import Common.ISA._
import chisel3._
import chisel3.util._
import Decoder._
import IO._

class ID extends Module{
  val io = IO(new Bundle() {
    // 输出到流水线寄存器
    val out = Flipped(new Stage_Registers_PortIO)

    // 接收来自IF模块的指令以及PC
    val Instr = Input(UInt(INSTR_SIZE.W))
    val PC    = Input(UInt(PC_SIZE.W))

    // 寄存器读取编号以及读出的数据(Ra和Rb是编号; Busa和Busb是读出的数据)
    val Ra   = Output(UInt(RegNUM_WIDTH.W))
    val Rb   = Output(UInt(RegNUM_WIDTH.W))

    // 跳转信号以及PC 输出
    val Valid_Branch    = Output(Bool())
    val TargetPC_Branch = Output(UInt(PC_SIZE.W))
    val Valid_J         = Output(Bool())
    val TargetPC_J      = Output(UInt(PC_SIZE.W))
    val Valid_EPC       = Output(Bool())
    val TargetPC_EPC    = Output(UInt(PC_SIZE.W))

    //
    val InstrErrorAddr  = Output(UInt(PC_SIZE.W))

    //从转发模块获取的“干净”数据
    val DataA = Input(UInt(XLEN.W))
    val DataB = Input(UInt(XLEN.W))

    // 是否是分支延迟槽指令
    val DelaySlot_in = Input(Bool())
    val DelaySlot    = Output(Bool())
    // IF阶段出现的软件异常
    val Exception_in  = Input(UInt(Exception_WIDTH.W))
  })
  val DelaySlot  = RegInit(false.B)
  DelaySlot     := io.DelaySlot_in
  io.DelaySlot  := DelaySlot

  val InstrErrorAddr = RegInit(0.U(PC_SIZE.W))
  io.InstrErrorAddr  := InstrErrorAddr

  // 初步译码
  val instr = io.Instr
  val pc = io.PC
  val imm = instr(15, 0)
  val rt = instr(20, 16)
  val rs = instr(25, 21)
  val sa = instr(10, 6)
  val rd = instr(15, 11)
  val func6 = instr(5, 0)
  val func5 = instr(20, 16)
  val opcode = instr(31, 26)
  val code = instr(25, 6)
  val sel = instr(2, 0)
  val pc4 = pc+4.U(32.W)
  val pc8 = pc+8.U(32.W)
  val instr_index = instr(25, 0)

  // 控制信号规范译码 配置好所有需要的控制信号
  val Decoder = Module(new ID_Decoder)
  Decoder.io.instr := io.Instr
  val DstRegAddrA  = Decoder.io.DstRegAddrA
  val DstRegAddrB  = Decoder.io.DstRegAddrB
  val WriteEnA     = Decoder.io.WriteEnA
  val WriteEnB     = Decoder.io.WriteEnB
  val SrcRegAddrA  = Decoder.io.SrcRegAddrA
  val SrcRegAddrB  = Decoder.io.SrcRegAddrB
  val ALUAsrc      = Decoder.io.ALUAsrc
  val ALUBsrc      = Decoder.io.ALUBsrc
  val InstrType    = Decoder.io.InstrType
  val MemWrite     = Decoder.io.MemWrite
  val MemtoReg     = Decoder.io.MemtoReg
  val MemOP        = Decoder.io.MemOP
  val ALUCtrl      = Decoder.io.ALUCtrl
  val EXTCtrl      = Decoder.io.EXTCtrl

  // 从寄存器读取数据
  io.Ra := SrcRegAddrA
  io.Rb := SrcRegAddrB

  // 立即数拓展模块
  val EXTDecoder = Module(new ID_EXTDecoder)
  EXTDecoder.io.imm := imm
  EXTDecoder.io.EXTCtrl := EXTCtrl
  EXTDecoder.io.sa := sa
  val ImmExpand = EXTDecoder.io.ImmExpand

  // 用单独的译码器对分支跳转指令额外译码 判断是否需要跳转以及跳转地址
  val BranchDecoder = Module(new ID_BranchDecoder)
  BranchDecoder.io.opcode := opcode
  BranchDecoder.io.busa   := io.DataA
  BranchDecoder.io.busb   := io.DataB
  BranchDecoder.io.func5  := func5
  BranchDecoder.io.pc4    := pc4
  BranchDecoder.io.ImmExpand := ImmExpand
  val Valid_Branch    = BranchDecoder.io.Valid_Branch
  val TargetPC_Branch = BranchDecoder.io.TargetPC_Branch

  // 用单独的译码器对无条件跳转指令额外译码 判断是否需要跳转以及跳转地址
  val JDecoder = Module(new ID_JDecoder)
  JDecoder.io.instr_index := instr_index
  JDecoder.io.opcode := opcode
  JDecoder.io.func6  := func6
  JDecoder.io.sa     := sa
  JDecoder.io.rd     := rd
  JDecoder.io.rt     := rt
  JDecoder.io.pc4    := pc4
  JDecoder.io.Busa   := io.DataA
  val Valid_J    = JDecoder.io.Valid_J
  val TargetPC_J = JDecoder.io.TargetPC_J

  // 判断异常
  val Exception = MuxCase(Decoder.io.Exception,
    Array(
      //软中断
      (io.Exception_in === Exception_Soft)                             -> io.Exception_in,
      //地址错
      (io.Exception_in === Exception_InstrAddr)                             -> io.Exception_in,
    )
  )

  InstrErrorAddr := Mux(Exception === Exception_InstrAddr, io.PC, 0.U(32.W))


  // 跳转信号赋值
  io.TargetPC_J := TargetPC_J
  io.Valid_J    := Valid_J
  io.TargetPC_Branch := TargetPC_Branch
  io.Valid_Branch    := Valid_Branch
  io.Valid_EPC := (InstrType === EretType)
  io.TargetPC_EPC := io.DataA


  // 对接流水线寄存器模块赋值
  io.out.DstRegAddrA := DstRegAddrA
  io.out.DstRegAddrB := DstRegAddrB
  io.out.DstDataA    := 0.U(XLEN.W)
  io.out.DstDataB    := 0.U(XLEN.W)
  io.out.ValidA      := false.B
  io.out.ValidB      := false.B
  io.out.WriteEnA    := WriteEnA
  io.out.WriteEnB    := WriteEnB
  io.out.SrcRegAddrA := SrcRegAddrA
  io.out.SrcRegAddrB := SrcRegAddrB
  io.out.DataA       := io.DataA
  io.out.DataB       := io.DataB

  io.out.InstrType   := InstrType
  io.out.MemWrite    := MemWrite
  io.out.MemtoReg    := MemtoReg
  io.out.MemOP       := MemOP
  io.out.Exception   := Exception
  io.out.ALUCtrl     := ALUCtrl
  io.out.ALUAsrc     := ALUAsrc
  io.out.ALUBsrc     := ALUBsrc

  io.out.Imm         := ImmExpand
  io.out.PC          := pc
  io.out.PC4         := pc4
  io.out.PC8         := pc8

  //
  io.out.ALUResA     := DontCare
  io.out.ALUResB     := DontCare


  //
  io.out.flush       := DontCare
  io.out.Stall       := DontCare

  printf("ID:\n")
  printf("Instr: 0x%x, PC: 0x%x, NextPC: 0x%x\n", instr, io.out.PC, io.TargetPC_J)
  printf("InstrType:%d MemWrite:%d MemtoReg:%d MemOP:%d Exception:%d ALUCtrl:%d ALUAsrc:%d ALUBsrc:%d EXTCtrl:%d\n",
    io.out.InstrType ,
    io.out.MemWrite,
    io.out.MemtoReg,
    io.out.MemOP,
    io.out.Exception,
    io.out.ALUCtrl,
    io.out.ALUAsrc,
    io.out.ALUBsrc,
    EXTCtrl
  )
  printf("SrcRa: %d, SrcRb: %d  DstRa: %d  DstRb: %d WrenA: %d WrenB: %d\n",
    io.out.SrcRegAddrA,
    io.out.SrcRegAddrB,
    io.out.DstRegAddrA,
    io.out.DstRegAddrB,
    io.out.WriteEnA,
    io.out.WriteEnB
  )

}


object main_ID extends App {
  println(
    new (chisel3.stage.ChiselStage).emitVerilog(
      new ID
    )
  )
}
