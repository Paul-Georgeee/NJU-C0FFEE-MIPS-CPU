package IO

import chisel3._

import Common.ISA._
import Common.DecodeSignal._

// 不需要的信号传0即可
class Stage_Registers_PortIO extends Bundle
{
    // Forwarding
    val DstRegAddrA = Input(UInt(RegNUM_WIDTH.W))       // 目的寄存器地址A/B
    val DstRegAddrB = Input(UInt(RegNUM_WIDTH.W)) 
    val DstDataA    = Input(UInt(XLEN.W))               // 目的寄存器的数据
    val DstDataB    = Input(UInt(XLEN.W)) 
    val ValidA      = Input(Bool())                     // A/B数据是否有效
    val ValidB      = Input(Bool())
    val WriteEnA    = Input(Bool())                     // 寄存器使能端
    val WriteEnB    = Input(Bool())
    val SrcRegAddrA = Input(UInt(RegNUM_WIDTH.W))       // 源寄存器地址A/B
    val SrcRegAddrB = Input(UInt(RegNUM_WIDTH.W))
    val DataA       = Input(UInt(XLEN.W))                     // 从源寄存器地址A/B取出的数据
    val DataB       = Input(UInt(XLEN.W))
    

    // Load/Store, Common(Add, Sub, Mul, MFHI, MFC0 ……), 
    val InstrType = Input(UInt(InstrType_WIDTH.W))
    // ID stage signal
    val MemWrite = Input(UInt(1.W))
    // 写入数据控制信号--决定写入的数据来自于内存还是alu的执行结果
    val MemtoReg = Input(UInt(1.W))
    // 内存写入方式&&读取方式控制信号--决定写入&&读取内存的方式
    val MemOP = Input(UInt(MEMOP_WIDTH.W))
    // 例外控制信号
    val Exception = Input(UInt(Exception_WIDTH.W))
    // ALU工作模式控制信号--决定ALU的工作模式
    val ALUCtrl = Input(UInt(ALU_WIDTH.W))
    // ALU源操作数ra的控制信号--决定ra操作数来自何处
    val ALUAsrc = Input(UInt(ALUAsrc_WIDTH.W))
    // ALU源操作数rb的控制信号--决定rb操作数来自何处
    val ALUBsrc = Input(UInt(ALUBsrc_WIDTH.W))
    
    // 拓展后的立即数imm--后续阶段无需再关注立即数的拓展形式
    val Imm = Input(UInt(XLEN.W))
    // PC的数值
    val PC = Input(UInt(PC_SIZE.W))
    // PC+4的数值(即分支延迟槽中的指令的pc)
    val PC4 = Input(UInt(PC_SIZE.W))
    // PC+8的数值
    val PC8 = Input(UInt(PC_SIZE.W))

    // EX stage signal
    // EX阶段ALU计算的结果
    val ALUResA = Input(UInt(XLEN.W))
    val ALUResB = Input(UInt(XLEN.W))

    // special signal
    val flush = Input(Bool())
    val Stall = Input(Bool())
}