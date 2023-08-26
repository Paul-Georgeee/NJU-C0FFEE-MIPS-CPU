#do not need stall
addi $s0, $zero, 32 #s0 = 32
addi $s1, $zero, 32 
addi $s1, $zero, 64 #s1 = 64
add  $s2, $s1, $s0  #s2 = 96


addi $s0, $zero, 32 #s0 = 32
addi $s1, $zero, 32
addi $s1, $zero, 64 #s1 = 64
sub  $s2, $s1, $s0  #s2 = 32

#load use
addi $s0, $zero, 32 #s0 = 32
lh $s1, ($s0)
add $s2, $s1, $s0

lw $s1, ($s0)
add $s2, $s1, $s0

lb $s1, ($s0)
add $s2, $s1, $s0

lhu $s1, ($s0)
add $s2, $s1, $s0

lbu $s1, ($s0)
add $s2, $s1, $s0


mult $s1, $s0
mfhi $s2
