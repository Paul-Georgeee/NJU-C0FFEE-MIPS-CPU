import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import Core._
import Common.RegistersName._
class Test1 extends AnyFlatSpec with ChiselScalatestTester
{

    it should "DRINK COFFEE" in 
    {
        test(new CPU) {c => 
            c.io.Instr.poke("h24100020".U(32.W))
            c.clock.step(1)
            c.io.Instr.poke("h22080008".U(32.W))
            c.clock.step(1)
            c.io.Instr.poke("h01104020".U(32.W))
            c.clock.step(1)
            c.io.Instr.poke("h38110008".U(32.W))
            c.clock.step(1)
            c.io.Instr.poke("h00119083".U(32.W))
            c.clock.step(1)
            c.io.Instr.poke("h24130005".U(32.W))
            c.clock.step(1)
            c.io.Instr.poke("h02722004".U(32.W))
            c.clock.step(1)
            c.io.Instr.poke("h3c138000".U(32.W))
            c.clock.step(1)
            c.io.Instr.poke("h0260282a".U(32.W))
            c.clock.step(1)
            c.io.Instr.poke("h21080007".U(32.W))
            c.clock.step(1)
            c.io.Instr.poke(0.U(32.W))
            c.clock.step(100)
            c.io.testio.addr.poke(s1) // Then we will test forward
            c.clock.step(1)
            c.io.testio.data.expect("h8".U(32.W))  
            c.io.testio.addr.poke(s2) // Then we will test forward
            c.clock.step(1)
            c.io.testio.data.expect("h2".U(32.W))  
            c.io.testio.addr.poke(a0) // Then we will test forward
            c.clock.step(1)
            c.io.testio.data.expect("h40".U(32.W))  
            c.io.testio.addr.poke(t0) // Then we will test forward
            c.clock.step(1)
            c.io.testio.data.expect("h4f".U(32.W))  
            c.io.testio.addr.poke(a1) // Then we will test forward
            c.clock.step(1)
            c.io.testio.data.expect("h0".U(32.W))  
        }     
    }
}
