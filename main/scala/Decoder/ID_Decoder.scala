package Decoder

import chisel3._
import chisel3.util._
import Common.Instrcutions._
import Common.ALUOP._
import Common.MDUOP._
import Common.DecodeSignal._
import Common.RegistersName._
import Common.ISA._


class ID_Decoder extends RawModule{
  val io = IO(new Bundle() {
    val instr       = Input(UInt(INSTR_SIZE.W))

    val DstRegAddrA = Output(UInt(RegNUM_WIDTH.W))       // 目的寄存器地址A/B
    val DstRegAddrB = Output(UInt(RegNUM_WIDTH.W))
    val WriteEnA    = Output(Bool())                     // 寄存器使能端
    val WriteEnB    = Output(Bool())
    val SrcRegAddrA = Output(UInt(RegNUM_WIDTH.W))       // 源寄存器地址A/B
    val SrcRegAddrB = Output(UInt(RegNUM_WIDTH.W))
    val ALUAsrc     = Output(UInt(ALUAsrc_WIDTH.W))
    val ALUBsrc     = Output(UInt(ALUBsrc_WIDTH.W))


    val InstrType   = Output(UInt(InstrType_WIDTH.W))
    val MemWrite    = Output(UInt(1.W))
    val MemtoReg    = Output(UInt(1.W))
    val MemOP       = Output(UInt(MEMOP_WIDTH.W))
    val ALUCtrl     = Output(UInt(ALU_WIDTH.W))
    val EXTCtrl     = Output(UInt(EXTCtrl_WIDTH.W))
    val Exception   = Output(UInt(Exception_WIDTH.W))
  })

  val rs = Cat(0.U(1.W), io.instr(25, 21))
  val rt = Cat(0.U(1.W), io.instr(20, 16))
  val rd = Cat(0.U(1.W), io.instr(15, 11))
  val sa = io.instr(10, 6)
  val cp0 = MuxCase(r0,
    Array(
      (rd === 8.U(6.W)) ->  BadVAddr,
      (rd === 9.U(6.W)) ->  Count,
      (rd === 12.U(6.W)) -> Status,
      (rd === 13.U(6.W)) -> Cause,
      (rd === 14.U(6.W)) -> EPC,
    )
  )


  // 以下分成两组译码


  val CtrlSignalList_0 = ListLookup(io.instr,
    //   DstA  DstB  WriteEnA          WriteEnB           SrcA  SrcB ALUAsrc       ALUBsrc
    List(r0,   r0,   RegWrite_Disable, RegWrite_Disable,  r0,   r0,  From_NO_Asrc, From_NO_Bsrc),
    Array(
      //                DstA  DstB  WriteEnA          WriteEnB           SrcA  SrcB ALUAsrc        ALUBsrc
      NOP      ->  List(r0,   r0,   RegWrite_Disable, RegWrite_Disable,  r0,   r0,  From_NO_Asrc,  From_NO_Bsrc),
      ADD      ->  List(rd,   r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   rt,  From_Ra_Asrc,  From_Rb_Bsrc),
      ADDI     ->  List(rt,   r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   r0,  From_Ra_Asrc,  From_Imm_Bsrc),
      ADDU     ->  List(rd,   r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   rt,  From_Ra_Asrc,  From_Rb_Bsrc),
      ADDIU    ->  List(rt,   r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   r0,  From_Ra_Asrc,  From_Imm_Bsrc),
      SUB      ->  List(rd,   r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   rt,  From_Ra_Asrc,  From_Rb_Bsrc),
      SUBU     ->  List(rd,   r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   rt,  From_Ra_Asrc,  From_Rb_Bsrc),
      SLT      ->  List(rd,   r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   rt,  From_Ra_Asrc,  From_Rb_Bsrc),
      SLTI     ->  List(rt,   r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   r0,  From_Ra_Asrc,  From_Imm_Bsrc),
      SLTU     ->  List(rd,   r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   rt,  From_Ra_Asrc,  From_Rb_Bsrc),
      SLTIU    ->  List(rt,   r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   r0,  From_Ra_Asrc,  From_Imm_Bsrc),
      DIV      ->  List(LO,   HI,   RegWrite_Enable,  RegWrite_Enable,   rs,   rt,  From_Ra_Asrc,  From_Rb_Bsrc),
      DIVU     ->  List(LO,   HI,   RegWrite_Enable,  RegWrite_Enable,   rs,   rt,  From_Ra_Asrc,  From_Rb_Bsrc),
      MULT     ->  List(LO,   HI,   RegWrite_Enable,  RegWrite_Enable,   rs,   rt,  From_Ra_Asrc,  From_Rb_Bsrc),
      MULTU    ->  List(LO,   HI,   RegWrite_Enable,  RegWrite_Enable,   rs,   rt,  From_Ra_Asrc,  From_Rb_Bsrc),
      AND      ->  List(rd,   r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   rt,  From_Ra_Asrc,  From_Rb_Bsrc),
      ANDI     ->  List(rt,   r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   r0,  From_Ra_Asrc,  From_Imm_Bsrc),
      LUI      ->  List(rt,   r0,   RegWrite_Enable,  RegWrite_Disable,  r0,   r0,  From_NO_Asrc,  From_Imm_Bsrc),
      NOR      ->  List(rd,   r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   rt,  From_Ra_Asrc,  From_Rb_Bsrc),
      OR       ->  List(rd,   r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   rt,  From_Ra_Asrc,  From_Rb_Bsrc),
      ORI      ->  List(rt,   r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   r0,  From_Ra_Asrc,  From_Imm_Bsrc),
      XOR      ->  List(rd,   r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   rt,  From_Ra_Asrc,  From_Rb_Bsrc),
      XORI     ->  List(rt,   r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   r0,  From_Ra_Asrc,  From_Imm_Bsrc),
      SLLV     ->  List(rd,   r0,   RegWrite_Enable,  RegWrite_Disable,  rt,   rs,  From_Ra_Asrc,  From_Rb_Bsrc),
      SLL      ->  List(rd,   r0,   RegWrite_Enable,  RegWrite_Disable,  rt,   r0,  From_Ra_Asrc,  From_Imm_Bsrc),
      SRAV     ->  List(rd,   r0,   RegWrite_Enable,  RegWrite_Disable,  rt,   rs,  From_Ra_Asrc,  From_Rb_Bsrc),
      SRA      ->  List(rd,   r0,   RegWrite_Enable,  RegWrite_Disable,  rt,   r0,  From_Ra_Asrc,  From_Imm_Bsrc),
      SRLV     ->  List(rd,   r0,   RegWrite_Enable,  RegWrite_Disable,  rt,   rs,  From_Ra_Asrc,  From_Rb_Bsrc),
      SRL      ->  List(rd,   r0,   RegWrite_Enable,  RegWrite_Disable,  rt,   r0,  From_Ra_Asrc,  From_Imm_Bsrc),
      BEQ      ->  List(r0,   r0,   RegWrite_Disable, RegWrite_Disable,  rs,   rt,  From_NO_Asrc,  From_NO_Bsrc),
      BNE      ->  List(r0,   r0,   RegWrite_Disable, RegWrite_Disable,  rs,   rt,  From_NO_Asrc,  From_NO_Bsrc),
      BGEZ     ->  List(r0,   r0,   RegWrite_Disable, RegWrite_Disable,  rs,   r0,  From_NO_Asrc,  From_NO_Bsrc),
      BGTZ     ->  List(r0,   r0,   RegWrite_Disable, RegWrite_Disable,  rs,   r0,  From_NO_Asrc,  From_NO_Bsrc),
      BLEZ     ->  List(r0,   r0,   RegWrite_Disable, RegWrite_Disable,  rs,   r0,  From_NO_Asrc,  From_NO_Bsrc),
      BLTZ     ->  List(r0,   r0,   RegWrite_Disable, RegWrite_Disable,  rs,   r0,  From_NO_Asrc,  From_NO_Bsrc),
      BGEZAL   ->  List(r31,  r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   r0,  From_PC8_Asrc, From_NO_Bsrc),
      BLTZAL   ->  List(r31,  r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   r0,  From_PC8_Asrc, From_NO_Bsrc),
      J        ->  List(r0,   r0,   RegWrite_Disable, RegWrite_Disable,  r0,   r0,  From_NO_Asrc,  From_NO_Bsrc),
      JAL      ->  List(r31,  r0,   RegWrite_Enable,  RegWrite_Disable,  r0,   r0,  From_PC8_Asrc, From_NO_Bsrc),
      JR       ->  List(r0,   r0,   RegWrite_Disable, RegWrite_Disable,  rs,   r0,  From_NO_Asrc,  From_NO_Bsrc),
      JALR     ->  List(rd,   r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   r0,  From_PC8_Asrc, From_NO_Bsrc),
      MFHI     ->  List(rd,   r0,   RegWrite_Enable,  RegWrite_Disable,  HI,   r0,  From_Ra_Asrc,  From_NO_Bsrc),
      MFLO     ->  List(rd,   r0,   RegWrite_Enable,  RegWrite_Disable,  LO,   r0,  From_Ra_Asrc,  From_NO_Bsrc),
      MTHI     ->  List(HI,   r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   r0,  From_Ra_Asrc,  From_NO_Bsrc),
      MTLO     ->  List(LO,   r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   r0,  From_Ra_Asrc,  From_NO_Bsrc),
      BREAK    ->  List(r0,   r0,   RegWrite_Disable, RegWrite_Disable,  r0,   r0,  From_NO_Asrc,  From_NO_Bsrc),
      SYSCALL  ->  List(r0,   r0,   RegWrite_Disable, RegWrite_Disable,  r0,   r0,  From_NO_Asrc,  From_NO_Bsrc),
      LB       ->  List(rt,   r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   r0,  From_Ra_Asrc,  From_Imm_Bsrc),
      LBU      ->  List(rt,   r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   r0,  From_Ra_Asrc,  From_Imm_Bsrc),
      LH       ->  List(rt,   r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   r0,  From_Ra_Asrc,  From_Imm_Bsrc),
      LHU      ->  List(rt,   r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   r0,  From_Ra_Asrc,  From_Imm_Bsrc),
      LW       ->  List(rt,   r0,   RegWrite_Enable,  RegWrite_Disable,  rs,   r0,  From_Ra_Asrc,  From_Imm_Bsrc),
      SB       ->  List(r0,   r0,   RegWrite_Disable, RegWrite_Disable,  rs,   rt,  From_Ra_Asrc,  From_Imm_Bsrc),
      SH       ->  List(r0,   r0,   RegWrite_Disable, RegWrite_Disable,  rs,   rt,  From_Ra_Asrc,  From_Imm_Bsrc),
      SW       ->  List(r0,   r0,   RegWrite_Disable, RegWrite_Disable,  rs,   rt,  From_Ra_Asrc,  From_Imm_Bsrc),

      // 暂时不解码
      ERET     ->  List(r0,   r0,   RegWrite_Disable, RegWrite_Disable,  EPC,   r0, From_NO_Bsrc,  From_NO_Bsrc),

      MFC0     ->  List(rt,  r0,    RegWrite_Enable,  RegWrite_Disable,  cp0,  r0,  From_Ra_Asrc,  From_NO_Bsrc),
      MTC0     ->  List(cp0, r0,    RegWrite_Enable,  RegWrite_Disable,  rt,   r0,  From_Ra_Asrc,  From_NO_Bsrc),
    )
  )

  io.DstRegAddrA := CtrlSignalList_0(0)
  io.DstRegAddrB := CtrlSignalList_0(1)
  io.WriteEnA    := CtrlSignalList_0(2)
  io.WriteEnB    := CtrlSignalList_0(3)
  io.SrcRegAddrA := CtrlSignalList_0(4)
  io.SrcRegAddrB := CtrlSignalList_0(5)
  io.ALUAsrc     := CtrlSignalList_0(6)
  io.ALUBsrc     := CtrlSignalList_0(7)

  val CtrlSignalList_1 = ListLookup(io.instr,
    //   InstrType  MemWrite          MemtoReg  MemOP   ALUCtrl     EXTCtrl         Exception
    List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_ADD,    Zero_Expansion, Exception_Error),
    Array(
      //                InstrType  MemWrite          MemtoReg  MemOP   ALUCtrl     EXTCtrl         Exception
      NOP      ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_ADD,    Zero_Expansion, Exception_No),
      ADD      ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_ADD,    Zero_Expansion, Exception_No),
      ADDI     ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_ADD,    Symb_Expansion, Exception_No),
      ADDU     ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_ADDU,   Zero_Expansion, Exception_No),
      ADDIU    ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_ADDU,   Symb_Expansion, Exception_No),
      SUB      ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_SUB,    Zero_Expansion, Exception_No),
      SUBU     ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_SUBU,   Zero_Expansion, Exception_No),
      SLT      ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_SLT,    Zero_Expansion, Exception_No),
      SLTI     ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_SLT,    Symb_Expansion, Exception_No),
      SLTU     ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_SLTU,   Zero_Expansion, Exception_No),
      SLTIU    ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_SLTU,   Symb_Expansion, Exception_No),
      DIV      ->  List(MulDiv,    MemWrite_Disable, From_ALU, Ubyte,  MDU_DIVT,   Zero_Expansion, Exception_No),
      DIVU     ->  List(MulDiv,    MemWrite_Disable, From_ALU, Ubyte,  MDU_DIVTU,  Zero_Expansion, Exception_No),
      MULT     ->  List(MulDiv,    MemWrite_Disable, From_ALU, Ubyte,  MDU_MULT,   Zero_Expansion, Exception_No),
      MULTU    ->  List(MulDiv,    MemWrite_Disable, From_ALU, Ubyte,  MDU_MULTU,  Zero_Expansion, Exception_No),
      AND      ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_AND,    Zero_Expansion, Exception_No),
      ANDI     ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_AND,    Zero_Expansion, Exception_No),
      LUI      ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_COPY2,  Shif_Expansion, Exception_No),
      NOR      ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_NOR,    Zero_Expansion, Exception_No),
      OR       ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_OR,     Zero_Expansion, Exception_No),
      ORI      ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_OR,     Zero_Expansion, Exception_No),
      XOR      ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_XOR,    Zero_Expansion, Exception_No),
      XORI     ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_XOR,    Zero_Expansion, Exception_No),
      SLLV     ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_SLL,    Zero_Expansion, Exception_No),
      SLL      ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_SLL,    SA_Z_Expansion, Exception_No),
      SRAV     ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_SRA,    Zero_Expansion, Exception_No),
      SRA      ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_SRA,    SA_Z_Expansion, Exception_No),
      SRLV     ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_SRL,    Zero_Expansion, Exception_No),
      SRL      ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_SRL,    SA_Z_Expansion, Exception_No),
      BEQ      ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_ADD,    Addr_Expansion, Exception_No),
      BNE      ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_ADD,    Addr_Expansion, Exception_No),
      BGEZ     ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_ADD,    Addr_Expansion, Exception_No),
      BGTZ     ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_ADD,    Addr_Expansion, Exception_No),
      BLEZ     ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_ADD,    Addr_Expansion, Exception_No),
      BLTZ     ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_ADD,    Addr_Expansion, Exception_No),
      BGEZAL   ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_COPY1,  Addr_Expansion, Exception_No),
      BLTZAL   ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_COPY1,  Addr_Expansion, Exception_No),
      J        ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_ADD,    Zero_Expansion, Exception_No),
      JAL      ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_COPY1,  Zero_Expansion, Exception_No),
      JR       ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_ADD,    Zero_Expansion, Exception_No),
      JALR     ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_COPY1,  Zero_Expansion, Exception_No),
      MFHI     ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_COPY1,  Zero_Expansion, Exception_No),
      MFLO     ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_COPY1,  Zero_Expansion, Exception_No),
      MTHI     ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_COPY1,  Zero_Expansion, Exception_No),
      MTLO     ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_COPY1,  Zero_Expansion, Exception_No),
      BREAK    ->  List(BreakPoint,MemWrite_Disable, From_ALU, Ubyte,  ALU_ADD,    Zero_Expansion, Exception_BreakPoint),
      SYSCALL  ->  List(SystemCall,MemWrite_Disable, From_ALU, Ubyte,  ALU_ADD,    Zero_Expansion, Exception_SystemCall),
      LB       ->  List(Load,      MemWrite_Disable, From_MEM, Sbyte,  ALU_ADDU,   Symb_Expansion, Exception_No),
      LBU      ->  List(Load,      MemWrite_Disable, From_MEM, Ubyte,  ALU_ADDU,   Symb_Expansion, Exception_No),
      LH       ->  List(Load,      MemWrite_Disable, From_MEM, Sdbyte, ALU_ADDU,   Symb_Expansion, Exception_No),
      LHU      ->  List(Load,      MemWrite_Disable, From_MEM, Udbyte, ALU_ADDU,   Symb_Expansion, Exception_No),
      LW       ->  List(Load,      MemWrite_Disable, From_MEM, word,   ALU_ADDU,   Symb_Expansion, Exception_No),
      SB       ->  List(Store,     MemWrite_Enable,  From_ALU, Ubyte,  ALU_ADDU,   Symb_Expansion, Exception_No),
      SH       ->  List(Store,     MemWrite_Enable,  From_ALU, Udbyte, ALU_ADDU,   Symb_Expansion, Exception_No),
      SW       ->  List(Store,     MemWrite_Enable,  From_ALU, word,   ALU_ADDU,   Symb_Expansion, Exception_No),
      ERET     ->  List(EretType,  MemWrite_Disable, From_ALU, Ubyte,  ALU_ADD,    Zero_Expansion, Exception_No),
      MFC0     ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_COPY1,  Zero_Expansion, Exception_No),
      MTC0     ->  List(NoType,    MemWrite_Disable, From_ALU, Ubyte,  ALU_COPY1,  Zero_Expansion, Exception_No)
    )
  )

  //   InstrType  MemWrite          MemtoReg  MemOP   ALUCtrl     EXTCtrl         Exception

  io.InstrType := CtrlSignalList_1(0)
  io.MemWrite  := CtrlSignalList_1(1)
  io.MemtoReg  := CtrlSignalList_1(2)
  io.MemOP     := CtrlSignalList_1(3)
  io.ALUCtrl   := CtrlSignalList_1(4)
  io.EXTCtrl   := CtrlSignalList_1(5)
  io.Exception := CtrlSignalList_1(6)
}
