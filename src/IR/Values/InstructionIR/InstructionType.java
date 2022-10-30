package IR.Values.InstructionIR;

public enum InstructionType {
    // Binary type
//    BINARY_OP_BEGIN,
    ADD,
    SUB,
    MUL,
    DIV,
    SREM,
    SHL,
    SHR,
    AND,
    OR,
    XOR,
//    BINARY_OP_END,
//    ICMP_OP_BEGIN,
    // ICMP type
    EQ,
    NE,
    SGT,
    SGE,
    SLT,
    SLE,
//    ICMP_OP_END,
    ZEXT,
    // memory type
    ALLOCA,
    LOAD,
    STORE,
    GEP,
    // terminator type
    BR,
    CALL,
    RET,
    // not type
    NOT
}
