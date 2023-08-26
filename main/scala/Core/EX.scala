package Core
import chisel3._
import chisel3.util._
import IO._
import Common.ALUOP._
import Common.MDUOP._
import Common.DecodeSignal._
import Common.ISA._
/*
ALUAsrc 
ALUBsrc 
*/
class EX extends Module
{
    val io = IO(new Bundle{
        val in = new Stage_Registers_PortIO
        val out = Flipped(new Stage_Registers_PortIO)
        val DelaySlot  = Input(Bool())
        val MulDivStall = Output(Bool())
        val StatusRead = Input(UInt(XLEN.W))
        val StatusData = Output(UInt(XLEN.W))
        val CauseData = Output(UInt(XLEN.W))
        val EPCData = Output(UInt(XLEN.W))
        val BadVAddrData  = Output(UInt(XLEN.W))
        val ExceptionFlush = Output(Bool())
        val ExceptionWrite = Output(Bool())
        val AddrError   = Output(Bool())
        val InstrErrorAddr = Input(UInt(PC_SIZE.W))
    })
    val ALUAsrc = io.in.ALUAsrc
    val ALUBsrc = io.in.ALUBsrc
    val PC = io.in.PC
    val PC4 = io.in.PC4
    val PC8 = io.in.PC8
    val Imm = io.in.Imm

    val DataA = MuxCase(0.U,
        Array
        (
            (ALUAsrc === From_Ra_Asrc)  -> io.in.DataA,
            (ALUAsrc === From_PC4_Asrc) -> io.in.PC4,
            (ALUAsrc === From_PC8_Asrc) -> io.in.PC8,
            (ALUAsrc === From_NO_Asrc)  -> 0.U(XLEN.W)
        ))

    val DataB = MuxCase(0.U,
        Array
        (
            (ALUBsrc === From_Imm_Bsrc) -> Imm,
            (ALUBsrc === From_Rb_Bsrc)  -> io.in.DataB,
            (ALUBsrc === From_NO_Bsrc)  -> 0.U(XLEN.W)
        ))

    
    val ALUCtrl = Wire(UInt(ALU_WIDTH.W))
    ALUCtrl := io.in.ALUCtrl

    val resA = Wire(UInt(32.W))
    val resB = Wire(UInt(32.W))
    // val CondValid = Wire(Bool())
    resA := 0.U
    resB := 0.U
    // 暂存乘除法的结果
    val res1 = Wire(SInt(64.W))
    val res2 = Wire(UInt(64.W))
    res1 := 0.S
    res2 := 0.U

    val MoreRes = Wire(SInt(33.W)) // 专门用于检测溢出异常
    val CauseEx = Wire(UInt(Exception_WIDTH.W))
    val DetectEx = Wire(Bool())
    MoreRes := 0.S(33.W)
    DetectEx := false.B

    //溢出异常检测
    switch(ALUCtrl)
    {
        is(ALU_ADD)
        {
            MoreRes := Cat(DataA(31), DataA).asSInt + Cat(DataB(31), DataB).asSInt
        }
        is(ALU_SUB)
        {
            MoreRes := Cat(DataA(31), DataA).asSInt - Cat(DataB(31), DataB).asSInt
        }
    }

    // ALU计算
    switch(ALUCtrl)
    {
        is(ALU_ADD)  {resA := (DataA.asSInt + DataB.asSInt).asUInt}
        is(ALU_ADDU) {resA := DataA + DataB }
        is(ALU_SUB)  {resA := (DataA.asSInt - DataB.asSInt).asUInt}
        is(ALU_SUBU) {resA := DataA - DataB }
        is(ALU_AND)  {resA := DataA & DataB}
        is(ALU_OR)   {resA := DataA | DataB}
        is(ALU_XOR)  {resA := DataA ^ DataB}
        is(ALU_NOR)  {resA := ~(DataA | DataB) }
        is(ALU_SLL)  {resA := DataA << DataB(4,0)}
        is(ALU_SRL)  {resA := DataA >> DataB(4,0)}
        is(ALU_SRA)  {resA := (DataA.asSInt >> DataB(4,0)).asUInt}
        is(ALU_SLT)  {resA := Mux(DataA.asSInt < DataB.asSInt, 1.U(XLEN.W), 0.U(XLEN.W)) }
        is(ALU_SLTU) {resA := Mux(DataA < DataB, 1.U(XLEN.W), 0.U(XLEN.W)) }
        is(ALU_COPY1) {resA := DataA}
        is(ALU_COPY2) {resA := DataB}
        is(ALU_LUI) {resA := Cat(DataB(15, 0), Fill(16, 0.U(1.W)))}
        is(MDU_MULT)
        {
            res1 := DataA.asSInt * DataB.asSInt
            resA := res1(31, 0) // resA is Lo
            resB := res1(63, 32) // resB is Hi
        }
        is(MDU_MULTU)
        {
            res2 := DataA * DataB
            resA := res2(31, 0) // resA is Lo
            resB := res2(63, 32) // resB is Hi
        }
        is(MDU_DIVT)
        {
            resA := (DataA.asSInt / DataB.asSInt).asUInt
            resB := (DataA.asSInt % DataB.asSInt).asUInt
        }
        is(MDU_DIVTU)
        {
            resA := DataA / DataB
            resB := DataA % DataB
        }
    }

    // 传递基本信号
    io.out <> io.in   
    io.out.ALUResA := resA
    io.out.ALUResB := resB
    io.out.DstDataA := resA
    io.out.DstDataB := resB

    io.out.ValidA := Mux(io.in.InstrType =/= Load, true.B, false.B)
    io.out.ValidB := Mux(io.in.InstrType =/= Load, true.B, false.B)
   
   // 表明异常的原因
    CauseEx := MuxCase(io.in.Exception, Array
    (
        (MoreRes(32) =/= MoreRes(31))    -> Exception_OverFlow,
        (io.in.InstrType === BreakPoint) -> Exception_BreakPoint,
        (io.in.InstrType === SystemCall) -> Exception_SystemCall,

        ((io.in.InstrType === Load || io.in.InstrType === Store) &&
          (io.in.MemOP === Udbyte || io.in.MemOP === Sdbyte) &&
          resA(0) =/= 0.U(1.W)) -> Exception_ZeroCheck1,   // 地址错误

        ((io.in.InstrType === Load || io.in.InstrType === Store) &&
          io.in.MemOP === word &&
          resA(1,0) =/= 0.U(2.W)) -> Exception_ZeroCheck2, // 地址错误
    ))

    io.out.Exception := CauseEx
    // 乘除法阻塞
    val mduCntReg = RegInit(0.U(MDU_CNT_WIDTH.W))
    val cntEnd = mduCntReg === (MDU_CYCLE - 1.U)
    val isMulDiv = io.in.InstrType === MulDiv
    when(cntEnd) {
        mduCntReg := 0.U
    }.elsewhen(isMulDiv) {
        mduCntReg := mduCntReg + 1.U
    }
    io.MulDivStall := isMulDiv && !cntEnd

    // 异常处理模块
    val IntExCode      = Cat(Fill(25, 0.U(1.W)), "h0".U(5.W), Fill(2, 0.U(1.W)))
    val RdAddrExCode   = Cat(Fill(25, 0.U(1.W)), "h4".U(5.W), Fill(2, 0.U(1.W)))
    val WrAddrExCode   = Cat(Fill(25, 0.U(1.W)), "h5".U(5.W), Fill(2, 0.U(1.W)))
    val SysExCode      = Cat(Fill(25, 0.U(1.W)), "h8".U(5.W), Fill(2, 0.U(1.W)))
    val BpExCode       = Cat(Fill(25, 0.U(1.W)), "h9".U(5.W), Fill(2, 0.U(1.W)))
    val RIExCode       = Cat(Fill(25, 0.U(1.W)), "ha".U(5.W), Fill(2, 0.U(1.W)))
    val OverFlowExCode = Cat(Fill(25, 0.U(1.W)), "hc".U(5.W), Fill(2, 0.U(1.W)))

    val ExceptionInRd = ((CauseEx === Exception_ZeroCheck1 || CauseEx === Exception_ZeroCheck2) && (io.in.MemWrite === false.B))||
                        (CauseEx === Exception_InstrAddr)
    val ExceptionInWr = (CauseEx === Exception_ZeroCheck1 || CauseEx === Exception_ZeroCheck2) && (io.in.MemWrite === true.B)

    // 用组合逻辑代替时序逻辑
    val SoftCond = (CauseEx === Exception_Soft)
    val RICond   = (CauseEx === Exception_Error)
    val BpCond   = (CauseEx === Exception_BreakPoint)
    val SysCond  = (CauseEx === Exception_SystemCall)
    val OverCond = (CauseEx === Exception_OverFlow)
    val AddrCond = (CauseEx === Exception_ZeroCheck1 || CauseEx === Exception_ZeroCheck2 || CauseEx === Exception_InstrAddr)
    DetectEx := Mux(BpCond || SysCond || OverCond || AddrCond || RICond || SoftCond, true.B, false.B)
    
    val CondExcept = io.StatusRead(1) === 0.U(1.W) && DetectEx

    io.EPCData := MuxCase(DontCare, Array(
        (CondExcept && CauseEx ===Exception_InstrAddr && io.DelaySlot === false.B            )-> io.InstrErrorAddr,
        (CondExcept && CauseEx ===Exception_InstrAddr && io.DelaySlot === true.B            )-> (io.InstrErrorAddr - 4.U(PC_SIZE.W)),
        (CondExcept && (io.DelaySlot === false.B || CauseEx === Exception_Soft) && DetectEx  ) -> PC,
        (CondExcept && io.DelaySlot === true.B  && DetectEx                                  ) -> (PC-4.U(PC_SIZE.W))

    ))

    val tmp = Mux(CondExcept === false.B, 0.U(32.W), MuxCase(0.U(XLEN.W), Array(
        (SoftCond)     -> IntExCode,
        (RICond)       -> RIExCode,
        (ExceptionInRd)-> RdAddrExCode,
        (ExceptionInWr)-> WrAddrExCode,
        (OverCond)     -> OverFlowExCode,
        (BpCond)       -> BpExCode,
        (SysCond)      -> SysExCode
        )))
    io.CauseData := Cat(io.DelaySlot, tmp(30, 0))

    io.StatusData := MuxCase(DontCare, Array(
        (CondExcept) -> (io.StatusRead | (2.U)),
        (io.StatusRead(1) === 1.U(1.W) && (io.in.InstrType === EretType)) -> (io.StatusRead ^ (2.U)),
    )) // Eret需要修改StatusData

    io.AddrError      := ExceptionInRd || ExceptionInWr
    io.BadVAddrData   := Mux(CauseEx === Exception_InstrAddr, io.InstrErrorAddr, resA)
    io.ExceptionFlush := CondExcept
    io.ExceptionWrite := CondExcept | (io.in.InstrType === EretType)
    printf("EX:\n")
    printf("PC: 0x%x, DataA: 0x%x, DataB: 0x%x, ExceptionFlush: %d \n", io.in.PC, DataA, DataB, io.ExceptionFlush)

}

object main_EX extends App {
    println(
        new(chisel3.stage.ChiselStage).emitVerilog(
            new EX
        )
    )
}