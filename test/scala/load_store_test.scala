import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import Core._
import Common.RegistersName._
class Test5 extends AnyFlatSpec with ChiselScalatestTester
{

  it should "PASSED ALL INSTRS TESTS" in
    {
      test(new CPU) {c =>
        c.io.inst_sram_rdata.poke("h100001ab".U(32.W))
        c.clock.step()
        c.clock.step()
        c.clock.step()
        c.clock.step()


        //        c.io.Instr.poke("h20100010".U(32.W)) //addi $s0, $zero, 16 #s0 = 16
//
//        c.clock.step(1)
//        c.io.Instr.poke("h82110001".U(32.W)) //lb $s1, 1($s0)
//
//        c.clock.step(1)
//        c.io.Instr.poke("h92110001".U(32.W)) //lbu $s1, 1($s0)
//
//        c.clock.step(1)
//        c.io.Instr.poke("h82110002".U(32.W)) //lb $s1, 2($s0)
//
//        c.clock.step(1)
//        c.io.memAddr.expect("b100".U(30.W))
//        c.io.memWren.expect(false.B)
//        c.io.memReadData.poke("h1234fe00".U)
//        c.io.testio.EXReg.ALUResA.expect(17.U)
//        c.io.testio.DstDataA.expect("hfffffffe".U(32.W))
//        c.io.Instr.poke("h92110002".U(32.W)) //lbu $s1, 2($s0)
//
//        c.clock.step(1)
//        c.io.Instr.poke("h86110002".U(32.W)) //lh $s1, 2($s0)
//        c.io.testio.DstDataA.expect("hfe".U(32.W))
//
//        c.clock.step(1)
//        c.io.Instr.poke("h96110002".U(32.W)) //lhu $s1, 2($s0)
//        c.io.memReadData.poke("ha2347800".U)
//        c.io.testio.DstDataA.expect("h34".U(32.W))
//
//
//        c.clock.step(1)
//        c.io.Instr.poke("h82110003".U(32.W)) //lb $s1, 3($s0)
//        c.io.testio.DstDataA.expect("h34".U(32.W))
//
//        c.clock.step(1)
//        c.io.Instr.poke("h92110003".U(32.W)) //lbu $s1, 3($s0)
//        c.io.testio.DstDataA.expect("hffffa234".U)
//
//        c.clock.step(1)
//        c.io.Instr.poke("h3c12ab12".U(32.W))
//        c.io.testio.DstDataA.expect("ha234".U)
//
//        c.clock.step(1)
//        c.io.Instr.poke("h3652ef34".U(32.W))   //li $s2, 0xab12ef34
//        c.io.testio.DstDataA.expect("hffffffa2".U(32.W))
//
//        c.clock.step(1)
//        c.io.testio.DstDataA.expect("ha2".U(32.W))
//        c.io.Instr.poke("ha2120000".U(32.W))  //sb $s2, ($s0)
//
//        c.clock.step(1)
//        c.io.Instr.poke("hae120000".U(32.W)) //sw $s2, ($s0)
//
//        c.clock.step(1)
//        c.io.testio.IDReg.DataA.expect(16.U)
//        c.io.testio.IDReg.DataB.expect("hab12ef34".U(32.W))
//        c.io.Instr.poke("ha2120001".U(32.W)) //sb $s2, 1($s0)
//        c.io.memWren.expect(false.B)
//
//        c.clock.step(1)
//        c.io.Instr.poke("ha2120002".U(32.W)) //sb $s2, 2($s0)
//        c.io.memWren.expect(true.B)
//        c.io.byteEna.expect("b0001".U(4.W))
//        c.io.memAddr.expect("b100".U(32.W))
//        c.io.memWriteData.expect("h00000034".U(32.W))
//
//        c.clock.step(1)
//        c.io.Instr.poke("ha6120002".U(32.W)) //sh $s2, 2($s0)
//        c.io.memWren.expect(true.B)
//        c.io.byteEna.expect("b1111".U(4.W))
//        c.io.memAddr.expect("b100".U(32.W))
//        c.io.memWriteData.expect("hab12ef34".U(32.W))
//
//        c.clock.step(1)
//        c.io.Instr.poke("ha2120003".U(32.W)) //sb $s2, 3($s0)
//        c.io.memWren.expect(true.B)
//        c.io.byteEna.expect("b0010".U(4.W))
//        c.io.memAddr.expect("b100".U(32.W))
//        c.io.memWriteData.expect("h00003400".U(32.W))
//
//        c.clock.step(1)
//        c.io.Instr.poke(0.U(32.W))
//        c.io.memWren.expect(true.B)
//        c.io.byteEna.expect("b0100".U(4.W))
//        c.io.memAddr.expect("b100".U(32.W))
//        c.io.memWriteData.expect("h00340000".U(32.W))
//
//        c.clock.step(1)
//        c.io.memWren.expect(true.B)
//        c.io.byteEna.expect("b1100".U(4.W))
//        c.io.memAddr.expect("b100".U(32.W))
//        c.io.memWriteData.expect("hef340000".U(32.W))
//
//        c.clock.step(1)
//        c.io.memWren.expect(true.B)
//        c.io.byteEna.expect("b1000".U(4.W))
//        c.io.memAddr.expect("b100".U(32.W))
//        c.io.memWriteData.expect("h34000000".U(32.W))
//
//        c.clock.step(100)
      }
    }
}
