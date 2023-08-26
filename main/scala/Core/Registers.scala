package Core

import chisel3._
import Common.ISA._
import Common.RegistersName._

class Registers extends Module {
  val io = IO(new Bundle() {
    val SrcRegAddrA = Input(UInt(RegNUM_WIDTH.W))
    val SrcRegAddrB = Input(UInt(RegNUM_WIDTH.W))
//    val TestRegAddr = Input(UInt(RegNUM_WIDTH.W))
//    val TestData = Output(UInt(XLEN.W))
    val DataA       = Output(UInt(XLEN.W))
    val DataB       = Output(UInt(XLEN.W))
    val DstRegAddrA = Input(UInt(RegNUM_WIDTH.W))
    val DstRegAddrB = Input(UInt(RegNUM_WIDTH.W))
    val DstDataA    = Input(UInt(XLEN.W))
    val DstDataB    = Input(UInt(XLEN.W))
    val WriteEnA    = Input(Bool())
    val WriteEnB    = Input(Bool())

    // IF专用读口
    val CauseRead  = Output(UInt(XLEN.W))
    // EX异常处理专用读口
    val StatusRead = Output(UInt(XLEN.W))
    // EX异常处理专用写口
    val ExceptionWrite = Input(Bool()) // 判断是否出现一下三种异常
    val CauseData      = Input(UInt(XLEN.W))
    val EPCData        = Input(UInt(XLEN.W))
    val StatusData     = Input(UInt(XLEN.W))
    val AddrError      = Input(Bool())
    val BadVAddrData   = Input(UInt(XLEN.W))
  })

  // 初始化了39个寄存器  具体编号可以在consts.scala中查看
  val RegistersFile_init = Seq.fill(39){0.U(XLEN.W)}
  val RegistersFile = RegInit(VecInit(RegistersFile_init))

  io.DataA := RegistersFile(io.SrcRegAddrA)
  io.DataB := RegistersFile(io.SrcRegAddrB)
//  io.TestData := RegistersFile(io.TestRegAddr)
  // Status寄存器的读取
  io.StatusRead := RegistersFile(Status) | "h00400000".U(32.W)
  io.CauseRead  := RegistersFile(Cause)

  // 不写入r0寄存器
  // 处理异常时，写入的优先级最高
  when(io.WriteEnA &&
    io.DstRegAddrA =/= 0.U(RegNUM_WIDTH.W) &&
    ((io.ExceptionWrite && (io.DstRegAddrA === Cause || io.DstRegAddrA === EPC || io.DstRegAddrA === Status)) === false.B)
    )
  {
    RegistersFile(io.DstRegAddrA) := io.DstDataA
  }

  // 当同时写入两个相同的寄存器时，只保留第一个写入的数据，第二个不写入
  // 不写入r0寄存器
  // 处理异常时，写入的优先级最高
  when(io.WriteEnB &&
    io.DstRegAddrB =/= 0.U(RegNUM_WIDTH.W) &&
    ((io.DstRegAddrB === io.DstRegAddrA && io.WriteEnA === false.B) || io.DstRegAddrB =/= io.DstRegAddrA) &&
    ((io.ExceptionWrite && (io.DstRegAddrB === Cause || io.DstRegAddrB === EPC || io.DstRegAddrB === Status)) === false.B)
    )
  {
    RegistersFile(io.DstRegAddrB) := io.DstDataB
  }

  // 发生异常时，写入三个寄存器
  when(io.ExceptionWrite)
  {
    RegistersFile(Cause)  := io.CauseData
    RegistersFile(EPC)    := io.EPCData
    RegistersFile(Status) := io.StatusData
  }

  when(io.AddrError)
  {
    RegistersFile(BadVAddr) := io.BadVAddrData
  }

  //printf("Regs: BadVaddrData 0x%x\n", RegistersFile(BadVAddr))
}


object main_Registers extends App{
  println(
    new(chisel3.stage.ChiselStage).emitVerilog(
      new Registers
    )
  )
}

