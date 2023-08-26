addi $t0, $zero, 31
addi $t1, $zero, 1
addi $s0, $zero, 1
ori $s1, $zero, 0x1
loop:
    and $t2, $s1, $t0
    beq $zero, $t2, Do_Sth
Do_Sth:
    sllv $s0, $s0, $t1
    addi $t0, $t0, -1
    bne		$t0, $zero, loop	# if $t0 != $r0 then target

# With Branch and Loop 