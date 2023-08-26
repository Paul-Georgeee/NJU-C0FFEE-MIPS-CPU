/*addi $s0, $zero, 32 # s0 = 32
ori $s1, $zero, 8 # s1 = 8
li $t0, 8 # t0 = 8
xori $t1, $zero, 15 # t1 = 15
li $t2, 20 
addu $s2, $s0, $s1 # s2 = 40
sub $s3, $s1, $s0 # s3 = -24
and $s4, $t0, $t1 # s4 = 8
sll $t0, $t0, 12 # t0 = 2^15 = 0x8000

li $a0, 0xFFFFFFFF
addi $t0, $zero, 0xF0F0
li $s2, 0
li $t1, 16 
slti $t2, $s1, 0x6000 # t2 = 0
# SLT指令是后面比前面小则赋值为1
slt $a1, $a0, $zero # a1 = 0
sltu $a2, $a0, $zero # a2 = 1

nop
nor $s3, $s2, $t0 
lui $a0, 0x4567
sra $a1, $t0, 8
mult	$t0, $t1			# $t0 * $t1 = Hi and Lo registers, lo = FFFF0F00
mflo	$t2					# copy Lo to $t2, hi = FFFFFFFF
*/

li $s0, 32
addi $t0, $s0, 8
add $t0, $t0, $s0 # t0 = 72
xori $s1, $zero, 8
sra $s2, $s1, 2 # s2 = 2
li $s3, 5
sllv $a0, $s2, $s3 # a0 = 32
lui $s3, 0x8000
slt $a1, $s3, $zero # a1 = 0
addi $t0, $t0, 7 # t0 = 79



# Basic Test



