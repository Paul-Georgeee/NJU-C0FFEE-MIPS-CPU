import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import Core._
import Common.RegistersName._
/*
class Test2 extends AnyFlatSpec with ChiselScalatestTester
{

    it should "PASSED ALL INSTRS TESTS" in 
    {
        test(new CPU) {c => 
            c.io.Instr.poke("h2008001f".U(32.W))
            c.clock.step(1)
            c.io.Instr.poke(0.U(32.W))
            c.io.Instr.poke("h20090001".U(32.W))
            c.clock.step(1)
            c.io.Instr.poke(0.U(32.W))
            c.io.Instr.poke("h20100001".U(32.W))
            c.clock.step(1)
            c.io.Instr.poke(0.U(32.W))
            c.io.Instr.poke("h34110001".U(32.W))
            c.clock.step(1)
            c.io.Instr.poke(0.U(32.W))
            c.io.Instr.poke("h02285024".U(32.W))
            c.clock.step(1)
            c.io.Instr.poke(0.U(32.W))
            c.io.Instr.poke("h100a0001".U(32.W))
            c.clock.step(1)
            c.io.Instr.poke(0.U(32.W))
            c.io.Instr.poke("h00000000".U(32.W))
            c.clock.step(1)
            c.io.Instr.poke(0.U(32.W))
            c.io.Instr.poke("h01308004".U(32.W))
            c.clock.step(1)
            c.io.Instr.poke(0.U(32.W))
            c.io.Instr.poke("h2108ffff".U(32.W))
            c.clock.step(1)
            c.io.Instr.poke(0.U(32.W))
            c.io.Instr.poke("h1500fffa".U(32.W))
            c.clock.step(1)
            c.io.Instr.poke(0.U(32.W))
            c.io.Instr.poke("h00000000".U(32.W))
            c.clock.step(1)
            c.io.Instr.poke(0.U(32.W))
            c.io.Instr.poke("h00000000".U(32.W))
            c.clock.step(1)
            c.io.Instr.poke(0.U(32.W))
            c.clock.step(100)
        }     
    }
}
*/