# 仅适用于Linux系统，WIN下mipsel-linux-xxx无法正常使用，半自动模板生成
cnt=1
while read name 
do
    testfilename=$name.s
    targetname=$name.o
    objname=$name.txt
    tmpname=tmp_$name.txt
    cd ~/NJU-C0FFEE-MIPS-CPU/src/test/tests
    mipsel-linux-gcc -c $testfilename
    mipsel-linux-objdump -d $targetname > $objname
    awk '/^\s.*([0-9]|[a-f]){8}/ {print $2}' $objname > $tmpname
    testfile=$name.scala
    touch $testfile

    # 简单生成chiseltest模板
    echo "import chisel3._" > $testfile
    echo "import chisel3.util._" >> $testfile
    echo "import chiseltest._" >> $testfile
    echo "import org.scalatest.flatspec.AnyFlatSpec" >> $testfile
    echo "import Core._" >> $testfile
    echo "import Common.RegistersName._" >> $testfile
    echo "class Test${cnt} extends AnyFlatSpec with ChiselScalatestTester" >> $testfile
    echo "{" >> $testfile
    #echo "def TestReg(addr: Int, data: Int, c: CPU): Int =
    # {
    #    val ret = 1
    #    c.io.testio.addr.poke(addr.U(6.W))
    #    c.clock.step(1)
    #    c.io.testio.data.expect(data.U(32.W))
    #    ret
    # }" >> $testfile
    echo '
    it should "PASSED ALL INSTRS TESTS" in 
    {
        test(new CPU) {c => ' >> $testfile
    # 读取tmpname中的指令，一条条执行，
    while read Instr
    do
        hexInstr="\"h${Instr}\".U(32.W)"
        echo "            c.io.Instr.poke(${hexInstr})" >> $testfile
        echo "            c.clock.step(1)" >> $testfile
        # echo "c.io.memReadData.poke()"
    done < $tmpname
    
    echo "            c.io.Instr.poke(0.U(32.W))" >> $testfile
    echo "            c.clock.step(100)" >> $testfile #末尾执行10个周期结束指令
    # 此处自行指定测试结果
    echo "        }     
    }
}" >> $testfile
    mv $testfile ../scala
    let cnt+=1
    rm $tmpname $targetname # 清理残余
done < ~/NJU-C0FFEE-MIPS-CPU/src/test/scripts/tests_list.txt

# c.io.testio.addr.poke(RegAddr)
# c.clock.step(1)
# c.io.testio.data.expect(Data)
# c.io.testio.dataS.expect(DataS) 
# DataS 检测SInt

