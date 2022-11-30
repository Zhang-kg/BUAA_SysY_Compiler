package BackEnd.Instr;

public enum InstructionType {
    j, jr, jal, beqz,
    addiu, addu, subu, mul, div, srem,
    move, la, li,
    seq, sne, sgt, sge, slt, slti, sle,
    lw,
    sw,
}
