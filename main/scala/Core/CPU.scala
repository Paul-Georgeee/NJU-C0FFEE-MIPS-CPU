package Core

import chisel3._
import chisel3.util._
import Common.ISA._
import Common.DecodeSignal._
import Common.RegistersName._
import IO.Stage_Registers_PortIO
import Core.Map
import AXI.AXIBridge

class test_IO extends Bundle
{
  val addr  = Input(UInt(RegNUM_WIDTH.W))
  val data  = Output(UInt(32.W))
  val dataS = Output(SInt(32.W))
  val IDReg = Flipped(new Stage_Registers_PortIO)
  val EXReg = Flipped(new Stage_Registers_PortIO)
  //reg write enable
  val WriteEnA         = Output(Bool())
  val WriteEnB         = Output(Bool())

  //destination register
  val DstRegAddrA           = Output(UInt(6.W))
  val DstRegAddrB           = Output(UInt(6.W))

  //data to be written into register
  val DstDataA      = Output(UInt(32.W))
  val DstDataB      = Output(UInt(32.W))

  //forward
  val LoadUseStall = Output(Bool())
  val FwData1 = Output(UInt(XLEN.W))
  val FwData2 = Output(UInt(XLEN.W))

  //mul div stall
  val MulDivStall = Output(Bool())
}


class CPU extends Module{
  val io = IO(new Bundle() {
    val ext_int = Input(UInt(6.W))    //外部中断

    val arid    = Output(UInt(4.W))
    val araddr  = Output(UInt(32.W))
    val arlen   = Output(UInt(8.W))
    val arsize  = Output(UInt(3.W))
    val arburst = Output(UInt(2.W))
    val arlock  = Output(UInt(2.W))
    val arcache = Output(UInt(4.W))
    val arprot  = Output(UInt(3.W))
    val arvalid = Output(Bool())
    val arready = Input(Bool())

    val rid    = Input(UInt(4.W))
    val rdata  = Input(UInt(32.W))
    val rresp  = Input(UInt(2.W))
    val rlast  = Input(Bool())
    val rvalid = Input(Bool())
    val rready = Output(Bool())

    val awid    = Output(UInt(4.W))
    val awaddr  = Output(UInt(32.W))
    val awlen   = Output(UInt(8.W))
    val awsize  = Output(UInt(3.W))
    val awburst = Output(UInt(2.W))
    val awlock  = Output(UInt(2.W))
    val awcache = Output(UInt(4.W))
    val awprot  = Output(UInt(3.W))
    val awvalid = Output(Bool())
    val awready = Input(Bool())

    val wid    = Output(UInt(4.W))
    val wdata  = Output(UInt(32.W))
    val wstrb  = Output(UInt(4.W))
    val wlast  = Output(Bool())
    val wvalid = Output(Bool())
    val wready = Input(Bool())

    val bid    = Input(UInt(4.W))
    val bresp  = Input(UInt(2.W))
    val bvalid = Input(Bool())
    val bready = Output(Bool())

    val debug_wb_pc       = Output(UInt(ADDR_SIZE.W))
    val debug_wb_rf_wen   = Output(UInt(4.W))
    val debug_wb_rf_wnum  = Output(UInt(5.W))
    val debug_wb_rf_wdata = Output(UInt(XLEN.W))
  })

    val IF        = Module(new IF)
    val ID        = Module(new ID)
    val EX        = Module(new EX)
    val MB        = Module(new MB)
    val Forward   = Module(new Forward)
    val Registers = Module(new Registers)
    val ID_EX     = Module(new Stage_Registers)
    val EX_MB     = Module(new Stage_Registers)
    val AXI       = Module(new AXIBridge)

    val InstrAddrMap  = Module(new Map)
    val DataAddrMap   = Module(new Map)

    InstrAddrMap.io.in  := IF.io.InstrAddr
    DataAddrMap.io.in   := MB.io.memAddr

    val inst_addr = RegInit(0.U(32.W))
    val inst_pc   = RegInit(0.U(32.W))
    val inst_data = RegInit(0.U(32.W))
    val inst_temp = AXI.io.doInst && AXI.io.inst_data_ok && AXI.io.data_req
    val inst_done = AXI.io.inst_addr === inst_pc
    inst_addr := Mux(AXI.io.inst_addr_ok, AXI.io.inst_addr, inst_addr)
    inst_pc   := Mux(inst_temp, inst_addr, inst_pc)
    inst_data := Mux(inst_temp, AXI.io.inst_rdata, inst_data)
    val inst_stall = AXI.io.inst_req && !inst_done &&
                     (!AXI.io.inst_data_ok || 
                      (AXI.io.inst_data_ok && (AXI.io.inst_addr =/= inst_addr)))

    AXI.io.inst_req   := !Forward.io.LoadUseStall && !EX.io.MulDivStall && !inst_done
    AXI.io.inst_wr    := false.B
    AXI.io.inst_size  := 2.U
    AXI.io.inst_addr  := InstrAddrMap.io.out
    AXI.io.inst_wdata := 0.U

    AXI.io.data_req   := (MB.io.in.InstrType === Load) || (MB.io.in.InstrType === Store)
    AXI.io.data_wr    := MB.io.memWren
    AXI.io.data_size  := MB.io.in.MemOP(2, 1)
    AXI.io.data_addr  := DataAddrMap.io.out
    AXI.io.data_wdata := MB.io.memWriteData

    AXI.io.arready := io.arready
    AXI.io.rid     := io.rid
    AXI.io.rdata   := io.rdata
    AXI.io.rresp   := io.rresp
    AXI.io.rlast   := io.rlast
    AXI.io.rvalid  := io.rvalid
    AXI.io.awready := io.awready
    AXI.io.wready  := io.wready
    AXI.io.bid     := io.bid
    AXI.io.bresp   := io.bresp
    AXI.io.bvalid  := io.bvalid
    io.arid    := AXI.io.arid
    io.araddr  := AXI.io.araddr
    io.arlen   := AXI.io.arlen
    io.arsize  := AXI.io.arsize
    io.arburst := AXI.io.arburst
    io.arlock  := AXI.io.arlock
    io.arcache := AXI.io.arcache
    io.arprot  := AXI.io.arprot
    io.arvalid := AXI.io.arvalid
    io.rready  := AXI.io.rready
    io.awid    := AXI.io.awid
    io.awaddr  := AXI.io.awaddr
    io.awlen   := AXI.io.awlen
    io.awsize  := AXI.io.awsize
    io.awburst := AXI.io.awburst
    io.awlock  := AXI.io.awlock
    io.awcache := AXI.io.awcache
    io.awprot  := AXI.io.awprot
    io.awvalid := AXI.io.awvalid
    io.wid     := AXI.io.wid
    io.wdata   := AXI.io.wdata
    io.wstrb   := AXI.io.wstrb
    io.wlast   := AXI.io.wlast
    io.wvalid  := AXI.io.wvalid
    io.bready  := AXI.io.bready

    //test
    val wb_pc       = RegNext(MB.io.PC, 0.U)
    val wb_rf_wdata = RegNext(MB.io.DstDataA, 0.U)
    val wb_rf_wnum  = RegNext(Mux(EX_MB.io.in.Stall, 0.U, MB.io.DstRegAddrA(4, 0)), 0.U)
    val wb_rf_wen   = RegNext(Mux(EX_MB.io.in.Stall, 0.U, Mux(MB.io.DstRegAddrA(5).asBool, 0.U(4.W), Fill(4, MB.io.WriteEnA))), 0.U)
    io.debug_wb_pc        := wb_pc
    io.debug_wb_rf_wdata  := wb_rf_wdata
    io.debug_wb_rf_wnum   := wb_rf_wnum
    io.debug_wb_rf_wen    := wb_rf_wen
    // printf("pc:0x%x, wdata:0x%x, wnum:%d, wen:%d\n", wb_pc, wb_rf_wdata, wb_rf_wnum, wb_rf_wen)
    // printf("type:%d, req:%d, stall:%d\n", ID.io.out.InstrType, AXI.io.data_req, EX_MB.io.in.Stall)

    // Registers
    Registers.io.SrcRegAddrA    := ID.io.Ra
    Registers.io.SrcRegAddrB    := ID.io.Rb
    Registers.io.DstRegAddrA    := MB.io.DstRegAddrA
    Registers.io.DstRegAddrB    := MB.io.DstRegAddrB
    Registers.io.DstDataA       := MB.io.DstDataA
    Registers.io.DstDataB       := MB.io.DstDataB
    Registers.io.WriteEnA       := MB.io.WriteEnA
    Registers.io.WriteEnB       := MB.io.WriteEnB
    Registers.io.EPCData        := EX.io.EPCData
    Registers.io.CauseData      := EX.io.CauseData
    Registers.io.StatusData     := EX.io.StatusData
    Registers.io.ExceptionWrite := EX.io.ExceptionWrite
    Registers.io.AddrError      := EX.io.AddrError
    Registers.io.BadVAddrData   := EX.io.BadVAddrData

    // Forward
    Forward.io.ExIsLoad := (EX.io.out.InstrType === Load)
    Forward.io.ExWr1    := EX.io.out.WriteEnA
    Forward.io.ExWr2    := EX.io.out.WriteEnB
    Forward.io.MbWr1    := MB.io.WriteEnA
    Forward.io.MbWr2    := MB.io.WriteEnB
    Forward.io.ExRd1    := EX.io.out.DstRegAddrA
    Forward.io.ExRd2    := EX.io.out.DstRegAddrB
    Forward.io.MbRd1    := MB.io.DstRegAddrA
    Forward.io.MbRd2    := MB.io.DstRegAddrB
    Forward.io.ExData1  := EX.io.out.DstDataA
    Forward.io.ExData2  := EX.io.out.DstDataB
    Forward.io.MbData1  := MB.io.DstDataA
    Forward.io.MbData2  := MB.io.DstDataB
    Forward.io.IdNd1    := (ID.io.out.SrcRegAddrA =/= r0)
    Forward.io.IdNd2    := (ID.io.out.SrcRegAddrB =/= r0)
    Forward.io.IdRs1    := ID.io.out.SrcRegAddrA
    Forward.io.IdRs2    := ID.io.out.SrcRegAddrB
    Forward.io.IdData1  := Registers.io.DataA
    Forward.io.IdData2  := Registers.io.DataB

    // IF
    IF.io.Instr_In        := Mux(inst_done, inst_data, AXI.io.inst_rdata)
    IF.io.Valid_Branch    := ID.io.Valid_Branch
    IF.io.Valid_J         := ID.io.Valid_J
    IF.io.Vaild_EPC       := ID.io.Valid_EPC
    IF.io.TargetPC_Branch := ID.io.TargetPC_Branch
    IF.io.TargetPC_J      := ID.io.TargetPC_J
    IF.io.TargetPC_EPC    := ID.io.TargetPC_EPC
    IF.io.CauseRead       := Registers.io.CauseRead
    IF.io.EX_PC           := MB.io.PC
    IF.io.flush           := DontCare
    IF.io.ExceptionFlush  := EX.io.ExceptionFlush
    IF.io.stall           := Forward.io.LoadUseStall || EX.io.MulDivStall ||
                            inst_stall ||
                            (AXI.io.data_req && !AXI.io.data_data_ok)

    // ID
    ID.io.Instr        := IF.io.Instr_Out
    ID.io.PC           := IF.io.PC
    ID.io.DelaySlot_in := IF.io.DelaySlot
    ID.io.DataA        := Forward.io.FwData1
    ID.io.DataB        := Forward.io.FwData2
    ID.io.Exception_in := IF.io.Exception

    // ID_EX
    ID.io.out         <> ID_EX.io.in
    ID_EX.io.in.Stall := EX.io.MulDivStall || (AXI.io.data_req && !AXI.io.data_data_ok)
    ID_EX.io.in.flush := Forward.io.LoadUseStall || EX.io.ExceptionFlush || inst_stall

    // EX
    ID_EX.io.out <> EX.io.in
    EX.io.out    <> EX_MB.io.in
    EX.io.InstrErrorAddr := ID.io.InstrErrorAddr
    EX.io.StatusRead  := Registers.io.StatusRead
    EX.io.DelaySlot   := ID.io.DelaySlot
    EX_MB.io.in.Stall := AXI.io.data_req && !AXI.io.data_data_ok
    EX_MB.io.in.flush := (EX.io.MulDivStall || EX.io.ExceptionFlush)

    // MB
    EX_MB.io.out <> MB.io.in
    MB.io.memReadData := AXI.io.data_rdata
    
}

object main_CPU extends App{
  println(
    new(chisel3.stage.ChiselStage).emitVerilog(
      new CPU
    )
  )
}




/*
class CPU extends Module{
  val io = IO(new Bundle() {
    val ext_int                 = Input(UInt(6.W))    //外部中断

    val inst_sram_en            = Output(Bool())
    val inst_sram_wen           = Output(UInt(4.W))
    val inst_sram_addr          = Output(UInt(ADDR_SIZE.W))
    val inst_sram_wdata         = Output(UInt(XLEN.W))
    val inst_sram_rdata         = Input(UInt(INSTR_SIZE.W))

    val data_sram_en            = Output(Bool())
    val data_sram_wen           = Output(UInt(4.W))
    val data_sram_addr          = Output(UInt(ADDR_SIZE.W))
    val data_sram_wdata         = Output(UInt(XLEN.W))
    val data_sram_rdata         = Input(UInt(INSTR_SIZE.W))

    val debug_wb_pc             = Output(UInt(ADDR_SIZE.W))
    val debug_wb_rf_wren        = Output(UInt(4.W))
    val debug_wb_rf_wnum        = Output(UInt(5.W))
    val debug_wb_rf_wdata       = Output(UInt(XLEN.W))
//    val memReadData  = Input(UInt(XLEN.W))
//    val memAddr      = Output(UInt(ADDR_SIZE.W))
//    val byteEna      = Output(UInt(4.W))
//    val memWriteData = Output(UInt(XLEN.W))
//    val memWren      = Output(Bool())
//    val testio       = new test_IO
  })
//  printf("\n\n********************************************\n")

  val IF        = Module(new IF)
  val ID        = Module(new ID)
  val EX        = Module(new EX)
  val MB        = Module(new MB)
  val Forward   = Module(new Forward)
  val Registers = Module(new Registers)
  val ID_EX     = Module(new Stage_Registers)
  val EX_MB     = Module(new Stage_Registers)


  val InstrAddrMap  = Module(new Map)
  val DataAddrMap   = Module(new Map)

  //test
  io.debug_wb_pc        := MB.io.PC
  io.debug_wb_rf_wdata  := MB.io.DstDataA
  io.debug_wb_rf_wnum   := MB.io.DstRegAddrA(4, 0)
  io.debug_wb_rf_wren   := Mux(MB.io.DstRegAddrA(5).asBool, 0.U(4.W), Fill(4, MB.io.WriteEnA))


//  printf("TOP:\n")
//  printf("InstrAddr: %d\n", io.inst_sram_addr)
  // TOP
  InstrAddrMap.io.in  := IF.io.InstrAddr
  io.inst_sram_addr   := InstrAddrMap.io.out
  io.inst_sram_en     := true.B
  io.inst_sram_wen    := 0.U(4.W)
  io.inst_sram_wdata  := 0.U(32.W)

  DataAddrMap.io.in   := MB.io.memAddr
  io.data_sram_addr   := DataAddrMap.io.out
  io.data_sram_en     := true.B
  io.data_sram_wen    := Mux(MB.io.memWren, MB.io.byteEna, 0.U(4.W))
  io.data_sram_wdata  := MB.io.memWriteData
//  io.memAddr  := MB.io.memAddr
//  io.memWren   := MB.io.memWren
//  io.byteEna   := MB.io.byteEna
//  io.memWriteData   := MB.io.memWriteData

  // Registers
  Registers.io.SrcRegAddrA    := ID.io.Ra
  Registers.io.SrcRegAddrB    := ID.io.Rb
  Registers.io.DstRegAddrA    := MB.io.DstRegAddrA
  Registers.io.DstRegAddrB    := MB.io.DstRegAddrB
  Registers.io.DstDataA       := MB.io.DstDataA
  Registers.io.DstDataB       := MB.io.DstDataB
  Registers.io.WriteEnA       := MB.io.WriteEnA
  Registers.io.WriteEnB       := MB.io.WriteEnB
  Registers.io.EPCData        := EX.io.EPCData
  Registers.io.CauseData      := EX.io.CauseData
  Registers.io.StatusData     := EX.io.StatusData
  Registers.io.ExceptionWrite := EX.io.ExceptionWrite
  Registers.io.AddrError      := EX.io.AddrError
  Registers.io.BadVAddrData   := EX.io.BadVAddrData

  // Forward
  Forward.io.ExIsLoad := (EX.io.out.InstrType === Load)
  Forward.io.ExWr1    := EX.io.out.WriteEnA
  Forward.io.ExWr2    := EX.io.out.WriteEnB
  Forward.io.MbWr1    := MB.io.WriteEnA
  Forward.io.MbWr2    := MB.io.WriteEnB
  Forward.io.ExRd1    := EX.io.out.DstRegAddrA
  Forward.io.ExRd2    := EX.io.out.DstRegAddrB
  Forward.io.MbRd1    := MB.io.DstRegAddrA
  Forward.io.MbRd2    := MB.io.DstRegAddrB
  Forward.io.ExData1  := EX.io.out.DstDataA
  Forward.io.ExData2  := EX.io.out.DstDataB
  Forward.io.MbData1  := MB.io.DstDataA
  Forward.io.MbData2  := MB.io.DstDataB
  Forward.io.IdNd1    := (ID.io.out.SrcRegAddrA =/= r0)
  Forward.io.IdNd2    := (ID.io.out.SrcRegAddrB =/= r0)
  Forward.io.IdRs1    := ID.io.out.SrcRegAddrA
  Forward.io.IdRs2    := ID.io.out.SrcRegAddrB
  Forward.io.IdData1  := Registers.io.DataA
  Forward.io.IdData2  := Registers.io.DataB

  // IF
  IF.io.Instr_In        := io.inst_sram_rdata
  IF.io.Valid_Branch    := ID.io.Valid_Branch
  IF.io.Valid_J         := ID.io.Valid_J
  IF.io.Vaild_EPC       := ID.io.Valid_EPC
  IF.io.TargetPC_Branch := ID.io.TargetPC_Branch
  IF.io.TargetPC_J      := ID.io.TargetPC_J
  IF.io.TargetPC_EPC    := ID.io.TargetPC_EPC
  IF.io.CauseRead       := Registers.io.CauseRead
  IF.io.EX_PC           := MB.io.PC
  IF.io.flush           := DontCare
  IF.io.ExceptionFlush  := EX.io.ExceptionFlush
  IF.io.stall           := (Forward.io.LoadUseStall || EX.io.MulDivStall)

  // ID
  ID.io.Instr        := IF.io.Instr_Out
  ID.io.PC           := IF.io.PC
  ID.io.DelaySlot_in := IF.io.DelaySlot
  ID.io.DataA        := Forward.io.FwData1
  ID.io.DataB        := Forward.io.FwData2
  ID.io.Exception_in := IF.io.Exception

  // ID_EX
  ID.io.out         <> ID_EX.io.in
  ID_EX.io.in.Stall := EX.io.MulDivStall
  ID_EX.io.in.flush := (Forward.io.LoadUseStall || EX.io.ExceptionFlush)

  // EX
  ID_EX.io.out <> EX.io.in
  EX.io.out    <> EX_MB.io.in
  EX.io.InstrErrorAddr := ID.io.InstrErrorAddr
  EX.io.StatusRead  := Registers.io.StatusRead
  EX.io.DelaySlot   := ID.io.DelaySlot
  EX_MB.io.in.flush := (EX.io.MulDivStall || EX.io.ExceptionFlush)

  // MB
  EX_MB.io.out <> MB.io.in
  MB.io.memReadData := io.data_sram_rdata

  // testio
  // Only used in test model
//  Registers.io.TestRegAddr := io.testio.addr
//  io.testio.data  := Registers.io.TestData
//  io.testio.dataS := Registers.io.TestData.asSInt
//  io.testio.IDReg <> ID_EX.io.out
//  io.testio.EXReg <> EX_MB.io.out
//
//  io.testio.DstRegAddrA := MB.io.DstRegAddrA
//  io.testio.DstRegAddrB := MB.io.DstRegAddrB
//  io.testio.DstDataA    := MB.io.DstDataA
//  io.testio.DstDataB    := MB.io.DstDataB
//  io.testio.WriteEnA    := MB.io.WriteEnA
//  io.testio.WriteEnB    := MB.io.WriteEnB
//  io.testio.LoadUseStall  := Forward.io.LoadUseStall
//  io.testio.FwData1       := Forward.io.FwData1
//  io.testio.FwData2       := Forward.io.FwData2
//  io.testio.MulDivStall   := EX.io.MulDivStall
}
*/
