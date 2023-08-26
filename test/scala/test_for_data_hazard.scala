import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import Core._
import Common.RegistersName._
class Test2 extends AnyFlatSpec with ChiselScalatestTester
{

  it should "PASSED ALL INSTRS TESTS" in
    {
      test(new CPU) {c =>
        c.io.memReadData.poke("h1234abcd".U(32.W))
        c.io.Instr.poke("h20100020".U(32.W)) //addi $s0, $zero, 32 #s0 = 32

        c.clock.step(1)
        c.io.Instr.poke("h20110020".U(32.W)) //addi $s1, $zero, 32

        c.clock.step()
        c.io.Instr.poke("h20110040".U(32.W)) //addi $s1, $zero, 64 #s1 = 64

        c.clock.step(1)
        c.io.Instr.poke("h02309020".U(32.W)) //add  $s2, $s1, $s0  #s2 = 96
        c.io.testio.EXReg.ALUResA.expect(32.U(32.W))
        c.io.testio.WriteEnA.expect(true.B)
        c.io.testio.DstRegAddrA.expect("h10".U)
        c.io.testio.DstDataA.expect(32.U)

        c.clock.step(1)
        c.io.Instr.poke("h20100020".U(32.W)) //addi $s0, $zero, 32 #s0 = 32

        c.io.testio.EXReg.ALUResA.expect(32.U(32.W))
        c.io.testio.WriteEnA.expect(true.B)
        c.io.testio.DstRegAddrA.expect("h11".U)
        c.io.testio.DstDataA.expect(32.U)
        c.io.testio.FwData1.expect(64.U)

        c.clock.step(1)
        c.io.Instr.poke("h20110020".U(32.W)) //addi $s1, $zero, 32
        c.io.testio.EXReg.ALUResA.expect(64.U(32.W))
        c.io.testio.EXReg.DstRegAddrA.expect("h11".U)
        c.io.testio.IDReg.InstrType.expect(0.U)
        c.io.testio.IDReg.SrcRegAddrA.expect("h11".U)
        c.io.testio.IDReg.SrcRegAddrB.expect("h10".U)
        c.io.testio.IDReg.DataA.expect(64.U)
        c.io.testio.IDReg.DataB.expect(32.U)

        c.clock.step(1)
        c.io.Instr.poke("h20110040".U(32.W)) //addi $s1, $zero, 64 #s1 = 64
        c.io.testio.EXReg.DataB.expect(32.U(32.W))
        c.io.testio.EXReg.ALUResA.expect(96.U(32.W))

        c.clock.step(1)
        c.io.Instr.poke("h02309022".U(32.W))  //sub  $s2, $s1, $s0  #s2 = 32

        c.clock.step(1)
        c.io.Instr.poke(0.U)

        c.clock.step(1)

        c.clock.step(1)
        c.io.testio.DstDataA.expect(32.U)
        c.io.Instr.poke("h20100020".U(32.W)) //addi $s0, $zero, 32 #s0 = 32

        c.clock.step(1)
        c.io.Instr.poke("h86110000".U(32.W)) //lh $s1, ($s0)

        c.clock.step(1)
        c.io.Instr.poke("h02309020".U(32.W))  //add $s2, $s1, $s0

        c.clock.step(1)
        c.io.Instr.poke("h8e110000".U(32.W)) //lw $s1, ($s0)
        c.io.testio.LoadUseStall.expect(true.B)


        c.clock.step(1)
        c.io.Instr.poke("h8e110000".U(32.W)) //lw $s1, ($s0)
        c.io.testio.IDReg.DataB.expect(0.U)
        c.io.testio.IDReg.SrcRegAddrA.expect(0.U)
        c.io.testio.DstDataA.expect("hffffabcd".U(32.W))
        c.io.testio.DstRegAddrA.expect("h11".U)

        c.clock.step(1)
        c.io.Instr.poke("h02309020".U(32.W)) //add $s2, $s1, $s0
        c.io.testio.IDReg.DataA.expect("hffffabcd".U(32.W))
        c.io.testio.IDReg.DataB.expect(32.U(32.W))

        c.clock.step(1)
        c.io.Instr.poke("h82110000".U(32.W)) //lb $s1, ($s0)
        c.io.testio.LoadUseStall.expect(true.B)
        c.io.testio.DstDataA.expect("hffffabed".U(32.W))

        c.clock.step(1)
        c.io.Instr.poke("h82110000".U(32.W)) //lb $s1, ($s0)
        c.io.testio.IDReg.DataB.expect(0.U)
        c.io.testio.IDReg.DataA.expect(0.U)

        c.clock.step(1)
        c.io.Instr.poke("h02309020".U(32.W)) //add $s2, $s1, $s0
        c.io.testio.IDReg.DataA.expect("h1234abcd".U(32.W))
        c.io.testio.IDReg.DataB.expect(32.U)

        c.clock.step(1)
        c.io.Instr.poke("h96110000".U(32.W)) //lhu $s1, ($s0)
        c.io.testio.DstDataA.expect("h1234abed".U(32.W))
        c.io.testio.LoadUseStall.expect(true.B)


        c.clock.step(1)
        c.io.Instr.poke("h96110000".U(32.W)) //lhu $s1, ($s0)
        c.io.testio.IDReg.DataB.expect(0.U)
        c.io.testio.IDReg.DataA.expect(0.U)

        c.clock.step(1)
        c.io.Instr.poke("h02309020".U(32.W)) //add $s2, $s1, $s0
        c.io.testio.IDReg.DataA.expect("hffffffcd".U(32.W))
        c.io.testio.IDReg.DataB.expect(32.U)

        c.clock.step(1)
        c.io.Instr.poke("h92110000".U(32.W)) //lbu $s1, ($s0)
        c.io.testio.DstDataA.expect("hffffffed".U(32.W))
        c.io.testio.LoadUseStall.expect(true.B)

        c.clock.step(1)
        c.io.Instr.poke("h92110000".U(32.W)) //lbu $s1, ($s0)
        c.io.testio.IDReg.DataB.expect(0.U)
        c.io.testio.IDReg.DataA.expect(0.U)

        c.clock.step(1)
        c.io.Instr.poke("h02309020".U(32.W)) //add $s2, $s1, $s0
        c.io.testio.IDReg.DataA.expect("h0000abcd".U(32.W))
        c.io.testio.IDReg.DataB.expect(32.U)

        c.clock.step(1)
        c.io.Instr.poke(0.U)
        c.io.testio.DstDataA.expect("habed".U(32.W))
        c.io.testio.LoadUseStall.expect(true.B)

        c.clock.step(1)
        c.io.testio.IDReg.DataB.expect(0.U)
        c.io.testio.IDReg.DataA.expect(0.U)

        c.clock.step(1)
        c.io.testio.IDReg.DataA.expect("h000000cd".U(32.W))
        c.io.testio.IDReg.DataB.expect(32.U)

        c.clock.step(1)
        c.io.testio.DstDataA.expect("hed".U(32.W))
        c.io.testio.LoadUseStall.expect(false.B)



        c.clock.step(1)
        c.io.Instr.poke("h02300018".U(32.W)) //mult $s1, $s0

        c.clock.step(1)
        c.io.Instr.poke("h00009010".U(32.W)) //mfhi $s2

        c.clock.step(1)
        c.io.Instr.poke(0.U(32.W))
        c.io.testio.MulDivStall.expect(true.B)

        c.clock.step(1)
        c.io.testio.IDReg.DataA.expect("hcd".U(32.W))
        c.io.testio.IDReg.DataB.expect(32.U(32.W))
        c.io.testio.EXReg.ALUResA.expect(0.U)

        c.clock.step(1)
        c.io.testio.IDReg.DataA.expect("hcd".U(32.W))
        c.io.testio.IDReg.DataB.expect(32.U(32.W))
        c.io.testio.EXReg.ALUResA.expect(0.U)

        c.clock.step(6)
        c.io.testio.MulDivStall.expect(true.B)

        c.clock.step(1)
        c.io.testio.MulDivStall.expect(false.B)
        c.io.testio.IDReg.DataA.expect("hcd".U(32.W))
        c.io.testio.IDReg.DataB.expect(32.U(32.W))
        c.io.testio.EXReg.ALUResA.expect(0.U)

        c.clock.step(1)
        c.io.testio.DstDataA.expect(6560.U)
        c.io.testio.DstDataB.expect(0.U)
        c.io.testio.DstRegAddrB.expect(32.U)

        c.clock.step(100)
      }
    }
}
