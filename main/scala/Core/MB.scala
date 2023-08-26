package Core

import Common.MEMWRITEDECODE._
import Common.MEMREADDECODE._
import chisel3._
import chisel3.util._
import IO._


class MemDecode extends Module{
  val io = IO(new Bundle() {
    val memOp                 = Input(UInt(3.W))
    val memAddr               = Input(UInt(32.W))
    val memWriteData          = Input(UInt(32.W))
    val memReadData           = Input(UInt(32.W))
    val byteEna               = Output(UInt(4.W))
    val memWriteDataModified  = Output(UInt(32.W))
    val memReadDataModified   = Output(UInt(32.W))
  })


  val writeRes = ListLookup(Cat(io.memAddr(1, 0), io.memOp),
    List(0.U(4.W), 0.U(32.W)),
    Array(
      oneByte0  -> List("b0001".U(4.W), Cat(Fill(24, 0.U(1.W)), io.memWriteData(7, 0))),
      oneByte1  -> List("b0010".U(4.W), Cat(Fill(16, 0.U(1.W)), io.memWriteData(7, 0), Fill(8, 0.U(1.W)))),
      oneByte2  -> List("b0100".U(4.W), Cat(Fill(8, 0.U(1.W)), io.memWriteData(7, 0), Fill(16, 0.U(1.W)))),
      oneByte3  -> List("b1000".U(4.W), Cat(io.memWriteData(7, 0), Fill(24, 0.U(1.W)))),
      twoByte0  -> List("b0011".U(4.W), Cat(Fill(16, 0.U(1.W)), io.memWriteData(15, 0))),
      twoByte2  -> List("b1100".U(4.W), Cat(io.memWriteData(15, 0), Fill(16, 0.U(1.W)))),
      allByte   -> List("b1111".U(4.W), io.memWriteData)
    )
  )
  io.byteEna := writeRes(0)
  io.memWriteDataModified := writeRes(1)

  val readRes = ListLookup(Cat(io.memAddr(1, 0), io.memOp),
    List(0.U(32.W)),
    Array(
      oneByte0U -> List(Cat(Fill(24, 0.U(1.W)), io.memReadData(7, 0))),
      oneByte1U -> List(Cat(Fill(24, 0.U(1.W)), io.memReadData(15, 8))),
      oneByte2U -> List(Cat(Fill(24, 0.U(1.W)), io.memReadData(23, 16))),
      oneByte3U -> List(Cat(Fill(24, 0.U(1.W)), io.memReadData(31, 24))),

      oneByte0S -> List(Cat(Fill(24, io.memReadData(7)), io.memReadData(7, 0))),
      oneByte1S -> List(Cat(Fill(24, io.memReadData(15)), io.memReadData(15, 8))),
      oneByte2S -> List(Cat(Fill(24, io.memReadData(23)), io.memReadData(23, 16))),
      oneByte3S -> List(Cat(Fill(24, io.memReadData(31)), io.memReadData(31, 24))),

      twoByte0U -> List(Cat(Fill(16, 0.U(1.W)), io.memReadData(15, 0))),
      twoByte2U -> List(Cat(Fill(16, 0.U(1.W)), io.memReadData(31, 16))),

      twoByte0S -> List(Cat(Fill(16, io.memReadData(15)), io.memReadData(15, 0))),
      twoByte2S -> List(Cat(Fill(16, io.memReadData(31)), io.memReadData(31, 16))),

      allByte0  -> List(io.memReadData)
    )
  )
  io.memReadDataModified := readRes.apply(0)
}

class MB extends Module {
  val io = IO(new Bundle() {
    val in            = new Stage_Registers_PortIO()
    //data read from mem
    val memReadData   = Input(UInt(32.W))
    //data to be write into mem
    val memWriteData  = Output(UInt(32.W))
    //mem address
    val memAddr       = Output(UInt(32.W))
    //mem write enable
    val memWren       = Output(Bool())
    //byte enable
    val byteEna       = Output(UInt(4.W))

    //reg write enable
    val WriteEnA         = Output(Bool())
    val WriteEnB         = Output(Bool())

    //destination register
    val DstRegAddrA           = Output(UInt(6.W))
    val DstRegAddrB           = Output(UInt(6.W))

    //data to be written into register
    val DstDataA      = Output(UInt(32.W))
    val DstDataB      = Output(UInt(32.W))

    //for forward
    val validA         = Output(Bool())
    val validB         = Output(Bool())

    val PC             = Output(UInt(32.W))
  })

  io.PC := io.in.PC
  val decode = Module(new MemDecode)
  decode.io.memOp := io.in.MemOP
  decode.io.memAddr := io.in.ALUResA
  decode.io.memReadData := io.memReadData
  decode.io.memWriteData := io.in.DataB

  //mem input
  io.byteEna := decode.io.byteEna
  io.memAddr := io.in.ALUResA
  io.memWren := io.in.MemWrite
  io.memWriteData := decode.io.memWriteDataModified

  //reg write back
  io.DstRegAddrA := io.in.DstRegAddrA
  io.WriteEnA := io.in.WriteEnA
  io.DstDataA := Mux(io.in.MemtoReg.asBool, decode.io.memReadDataModified, io.in.ALUResA)

  io.DstRegAddrB := io.in.DstRegAddrB
  io.WriteEnB := io.in.WriteEnB
  io.DstDataB := io.in.DstDataB

  io.validA := true.B
  io.validB := true.B

  printf("MB:pc is 0x%x ,wren is %d ,regid is %d\n", io.PC, io.WriteEnA, io.DstRegAddrA)
  printf("alures 0x%x memaddr 0x%x MemtoReg %d memwren %d byteena %d memreadata 0x%x memwritedata 0x%x\n", io.in.ALUResA, io.memAddr, io.in.MemtoReg, io.in.MemWrite, io.byteEna, io.memReadData, io.memWriteData)
}

object hello extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new MB(), Array("--target-dir", "build"))
}
