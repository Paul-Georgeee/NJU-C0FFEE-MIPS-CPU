package Core

import chisel3._
import chisel3.util._
import Common.ISA._


class Forward extends Module {
    val io = IO(new Bundle{
        val ExIsLoad = Input(Bool())
        val ExWr1 = Input(Bool())
        val ExWr2 = Input(Bool())
        val MbWr1 = Input(Bool())
        val MbWr2 = Input(Bool())
        val ExRd1 = Input(UInt(RegNUM_WIDTH.W))
        val ExRd2 = Input(UInt(RegNUM_WIDTH.W))
        val MbRd1 = Input(UInt(RegNUM_WIDTH.W))
        val MbRd2 = Input(UInt(RegNUM_WIDTH.W))
        val ExData1 = Input(UInt(XLEN.W))
        val ExData2 = Input(UInt(XLEN.W))
        val MbData1 = Input(UInt(XLEN.W))
        val MbData2 = Input(UInt(XLEN.W))
        val IdNd1 = Input(Bool())
        val IdNd2 = Input(Bool())
        val IdRs1 = Input(UInt(RegNUM_WIDTH.W))
        val IdRs2 = Input(UInt(RegNUM_WIDTH.W))
        val IdData1 = Input(UInt(XLEN.W))
        val IdData2 = Input(UInt(XLEN.W))

        val LoadUseStall = Output(Bool())
        val FwData1 = Output(UInt(XLEN.W))
        val FwData2 = Output(UInt(XLEN.W))
    })

    val depRs1 = io.IdNd1 && io.ExIsLoad && (io.ExRd1 === io.IdRs1)
    val depRs2 = io.IdNd2 && io.ExIsLoad && (io.ExRd1 === io.IdRs2)

    io.LoadUseStall := depRs1 || depRs2
    io.FwData1 := MuxCase(io.IdData1, Seq(
        (io.ExWr1 && (io.ExRd1 === io.IdRs1)) -> io.ExData1,
        (io.ExWr2 && (io.ExRd2 === io.IdRs1)) -> io.ExData2,
        (io.MbWr1 && (io.MbRd1 === io.IdRs1)) -> io.MbData1,
        (io.MbWr2 && (io.MbRd2 === io.IdRs1)) -> io.MbData2
    ))

    io.FwData2 := MuxCase(io.IdData2, Seq(
        (io.ExWr1 && (io.ExRd1 === io.IdRs2)) -> io.ExData1,
        (io.ExWr2 && (io.ExRd2 === io.IdRs2)) -> io.ExData2,
        (io.MbWr1 && (io.MbRd1 === io.IdRs2)) -> io.MbData1,
        (io.MbWr2 && (io.MbRd2 === io.IdRs2)) -> io.MbData2
    ))
//    printf("Forward:\n")
//    printf("ExWr1:%x, ExWr2:%x, MbWr1:%x, MbWr2:%x\n",io.ExWr1, io.ExWr2,io.MbWr1,io.MbWr2)
//    printf("ExData1:%d, ExData2:%d, MbData1:%d, MbData2:%d\n",io.ExData1, io.ExData2,io.MbData1,io.MbData2)
//    printf("FOFWARD1:%d, FOFWARD2:%d\n", (io.FwData2=/=io.IdData2), (io.FwData1=/=io.IdData1))
}

