package BackEnd.SymbolTableForMIPS;

public enum SymbolTypeForMIPS {
    GlobalVariable,
    VirtualReg, // * 需要进行寄存器分配的部分
    PhysicsReg, // * 虚拟寄存器分配物理寄存器的部分
    SpillReg,   // * 虚拟寄存器溢出到栈内的部分
    StackReg,   // * 本来就分配到栈上的寄存器 (alloc出来的虚拟寄存器)
    GlobalString,
    BlockLabel,
    FunctionParam
}
