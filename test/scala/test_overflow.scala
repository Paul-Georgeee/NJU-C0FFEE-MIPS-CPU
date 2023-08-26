import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import Core._
import Common.RegistersName._
import Common.ISA._
import Common.DecodeSignal._

class test_overflow extends AnyFlatSpec with ChiselScalatestTester
{

    "it" should "PASSED ALL INSTRS TESTS" in
      {
          test(new CPU) {c =>
              c.io.Instr.poke("h84110001".U(32.W))
              // TODO
              c.clock.step(1)

              c.io.Instr.poke("h0000000d".U(32.W))
              // TODO
              c.clock.step(1)

              c.io.Instr.poke("h0000000d".U(32.W))
              // TODO
              c.clock.step(1)

              c.io.Instr.poke("h42000018".U(32.W))
              // TODO
              c.clock.step(1)

              c.io.Instr.poke("h0000000d".U(32.W))
              // TODO
              c.clock.step(1)

              c.io.Instr.poke("h8c110003".U(32.W))
              // TODO
              c.clock.step(1)

              c.io.Instr.poke("h0000000d".U(32.W))
              // TODO
              c.clock.step(1)

              c.io.Instr.poke("h0000000d".U(32.W))
              // TODO
              c.clock.step(1)

              c.io.Instr.poke("h42000018".U(32.W))
              // TODO
              c.clock.step(1)

              c.io.Instr.poke("h0000000d".U(32.W))
              // TODO
              c.clock.step(1)

              c.io.Instr.poke("h20100001".U(32.W))
              // TODO
              c.clock.step(1)

              c.io.Instr.poke("h86110002".U(32.W))
              // TODO
              c.clock.step(1)

              c.io.Instr.poke("h0000000d".U(32.W))
              // TODO
              c.clock.step(1)

              c.io.Instr.poke("h0000000d".U(32.W))
              // TODO
              c.clock.step(1)

              c.io.Instr.poke("h42000018".U(32.W))
              // TODO
              c.clock.step(1)

              c.io.Instr.poke("h0000000d".U(32.W))
              // TODO
              c.clock.step(1)

              c.io.Instr.poke("h20100002".U(32.W))
              // TODO
              c.clock.step(1)

              c.io.Instr.poke("h8e110004".U(32.W))
              // TODO
              c.clock.step(1)

              c.io.Instr.poke("h0000000d".U(32.W))
              // TODO
              c.clock.step(1)

              c.io.Instr.poke("h0000000d".U(32.W))
              // TODO
              c.clock.step(1)

              c.io.Instr.poke("h42000018".U(32.W))
              // TODO
              c.clock.step(1)

              c.io.Instr.poke("h0000000d".U(32.W))
              // TODO
              c.clock.step(1)


              // END
              c.io.Instr.poke(0.U(32.W))
              c.clock.step(5)
          }
      }
}
