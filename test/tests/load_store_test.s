addi $s0, $zero, 16 #s0 = 16
lb $s1, 1($s0)
lbu $s1, 1($s0)

lb $s1, 2($s0)
lbu $s1, 2($s0)
lh $s1, 2($s0)
lhu $s1, 2($s0)

lb $s1, 3($s0)
lbu $s1, 3($s0)

li $s2, 0xab12ef34
sb $s2, ($s0)
sw $s2, ($s0)

sb $s2, 1($s0)

sb $s2, 2($s0)
sh $s2, 2($s0)

sb $s2, 3($s0)
