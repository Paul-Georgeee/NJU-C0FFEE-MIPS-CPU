package Core

import chisel3._
import IO._
import Common.ISA._
import Common.DecodeSignal._

class Stage_Registers extends Module{
  val io = IO(new Bundle() {
    val in  = new Stage_Registers_PortIO
    val out = Flipped(new Stage_Registers_PortIO)
  })
  val DstRegAddrA = RegInit(0.U(RegNUM_WIDTH.W))
  val DstRegAddrB = RegInit(0.U(RegNUM_WIDTH.W))
  val DstDataA    = RegInit(0.U(XLEN.W))
  val DstDataB    = RegInit(0.U(XLEN.W))
  val ValidA      = RegInit(0.U(1.W))
  val ValidB      = RegInit(0.U(1.W))
  val WriteEnA    = RegInit(0.U(1.W))
  val WriteEnB    = RegInit(0.U(1.W))
  val SrcRegAddrA = RegInit(0.U(RegNUM_WIDTH.W))
  val SrcRegAddrB = RegInit(0.U(RegNUM_WIDTH.W))
  val DataA       = RegInit(0.U(XLEN.W))
  val DataB       = RegInit(0.U(XLEN.W))
  val InstrType   = RegInit(0.U(InstrType_WIDTH.W))
  val MemWrite    = RegInit(0.U(1.W))
  val MemtoReg    = RegInit(0.U(1.W))
  val MemOP       = RegInit(0.U(MEMOP_WIDTH.W))
  val Exception   = RegInit(0.U(Exception_WIDTH.W))
  val ALUCtrl     = RegInit(0.U(ALU_WIDTH.W))
  val ALUAsrc     = RegInit(0.U(ALUAsrc_WIDTH.W))
  val ALUBsrc     = RegInit(0.U(ALUBsrc_WIDTH.W))
  val Imm         = RegInit(0.U(XLEN.W))
  val PC          = RegInit(0.U(PC_SIZE.W))
  val PC4         = RegInit(0.U(PC_SIZE.W))
  val PC8         = RegInit(0.U(PC_SIZE.W))
  val ALUResA     = RegInit(0.U(XLEN.W))
  val ALUResB     = RegInit(0.U(XLEN.W))

    when(io.in.Stall) {
      DstRegAddrA := DstRegAddrA
      DstRegAddrB := DstRegAddrB
      DstDataA    := DstDataA
      DstDataB    := DstDataB
      ValidA      := ValidA
      ValidB      := ValidB
      WriteEnA    := WriteEnA
      WriteEnB    := WriteEnB
      SrcRegAddrA := SrcRegAddrA
      SrcRegAddrB := SrcRegAddrB
      DataA       := DataA
      DataB       := DataB
      InstrType   := InstrType
      MemWrite    := MemWrite
      MemtoReg    := MemtoReg
      MemOP       := MemOP
      Exception   := Exception
      ALUCtrl     := ALUCtrl
      ALUAsrc     := ALUAsrc
      ALUBsrc     := ALUBsrc
      Imm         := Imm
      PC          := PC
      PC4         := PC4
      PC8         := PC8
      ALUResA     := ALUResA
      ALUResB     := ALUResB
    }
    .elsewhen(io.in.flush){
      DstRegAddrA := 0.U(RegNUM_WIDTH.W)
      DstRegAddrB := 0.U(RegNUM_WIDTH.W)
      DstDataA    := 0.U(XLEN.W)
      DstDataB    := 0.U(XLEN.W)
      ValidA      := 0.U(1.W)
      ValidB      := 0.U(1.W)
      WriteEnA    := 0.U(1.W)
      WriteEnB    := 0.U(1.W)
      SrcRegAddrA := 0.U(RegNUM_WIDTH.W)
      SrcRegAddrB := 0.U(RegNUM_WIDTH.W)
      DataA       := 0.U(XLEN.W)
      DataB       := 0.U(XLEN.W)
      InstrType   := 0.U(InstrType_WIDTH.W)
      MemWrite    := 0.U(1.W)
      MemtoReg    := 0.U(1.W)
      MemOP       := 0.U(MEMOP_WIDTH.W)
      Exception   := 0.U(Exception_WIDTH.W)
      ALUCtrl     := 0.U(ALU_WIDTH.W)
      ALUAsrc     := 0.U(ALUAsrc_WIDTH.W)
      ALUBsrc     := 0.U(ALUBsrc_WIDTH.W)
      Imm         := 0.U(XLEN.W)
      PC          := 0.U(PC_SIZE.W)
      PC4         := 0.U(PC_SIZE.W)
      PC8         := 0.U(PC_SIZE.W)
      ALUResA     := 0.U(XLEN.W)
      ALUResB     := 0.U(XLEN.W)

    }.elsewhen(io.in.Stall === false.B){
    DstRegAddrA := io.in.DstRegAddrA
    DstRegAddrB := io.in.DstRegAddrB
    DstDataA    := io.in.DstDataA
    DstDataB    := io.in.DstDataB
    ValidA      := io.in.ValidA
    ValidB      := io.in.ValidB
    WriteEnA    := io.in.WriteEnA
    WriteEnB    := io.in.WriteEnB
    SrcRegAddrA := io.in.SrcRegAddrA
    SrcRegAddrB := io.in.SrcRegAddrB
    DataA       := io.in.DataA
    DataB       := io.in.DataB
    InstrType   := io.in.InstrType
    MemWrite    := io.in.MemWrite
    MemtoReg    := io.in.MemtoReg
    MemOP       := io.in.MemOP
    Exception   := io.in.Exception
    ALUCtrl     := io.in.ALUCtrl
    ALUAsrc     := io.in.ALUAsrc
    ALUBsrc     := io.in.ALUBsrc
    Imm         := io.in.Imm
    PC          := io.in.PC
    PC4         := io.in.PC4
    PC8         := io.in.PC8
    ALUResA     := io.in.ALUResA
    ALUResB     := io.in.ALUResB
  }

  io.out.DstRegAddrA := DstRegAddrA
  io.out.DstRegAddrB := DstRegAddrB
  io.out.DstDataA    := DstDataA
  io.out.DstDataB    := DstDataB
  io.out.ValidA      := ValidA
  io.out.ValidB      := ValidB
  io.out.WriteEnA    := WriteEnA
  io.out.WriteEnB    := WriteEnB
  io.out.SrcRegAddrA := SrcRegAddrA
  io.out.SrcRegAddrB := SrcRegAddrB
  io.out.DataA       := DataA
  io.out.DataB       := DataB
  io.out.InstrType   := InstrType
  io.out.MemWrite    := MemWrite
  io.out.MemtoReg    := MemtoReg
  io.out.MemOP       := MemOP
  io.out.Exception   := Exception
  io.out.ALUCtrl     := ALUCtrl
  io.out.ALUAsrc     := ALUAsrc
  io.out.ALUBsrc     := ALUBsrc
  io.out.Imm         := Imm
  io.out.PC          := PC
  io.out.PC4         := PC4
  io.out.PC8         := PC8
  io.out.ALUResA     := ALUResA
  io.out.ALUResB     := ALUResB

  io.out.Stall       := DontCare
  io.out.flush       := DontCare
}

object main_Stage_Registers extends App {
  println(
    new (chisel3.stage.ChiselStage).emitVerilog(
      new Stage_Registers
    )
  )
}
