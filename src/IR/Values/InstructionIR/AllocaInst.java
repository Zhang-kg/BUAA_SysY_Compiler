package IR.Values.InstructionIR;

import IR.Values.BasicBlock;

import IR.types.PointerType;
import IR.types.Type;

public class AllocaInst extends Instruction {
    private boolean isConst;
    private Type allocatedType;
    private String name;    // * 这是符号表的中的符号的Name
    private static int ALLOC_INST_NUM = 0;

    public AllocaInst(BasicBlock fatherBasicBlock, String name, boolean isConst,
                      Type allocatedType) {
        super(fatherBasicBlock, InstructionType.ALLOCA, new PointerType(allocatedType), name);
        this.name = name;
        this.isConst = isConst;
        this.allocatedType = allocatedType;
    }

    public String toString() {
        return name +
                " = alloca " +
                allocatedType.toString();
    }

    public Type getAllocatedType() {
        return allocatedType;
    }

    private static String allocName() {
        return "%ALLOC_INST_NO_" + ALLOC_INST_NUM++;
    }
}
