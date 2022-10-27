package IR.Values.InstructionIR;

import IR.Values.BasicBlock;

import IR.types.Type;

public class AllocaInst extends Instruction {
    private boolean isConst;
    private Type allocatedType;
    private String name;

    public AllocaInst(BasicBlock fatherBasicBlock, String name, boolean isConst, Type allocatedType) {
        super(fatherBasicBlock);
        this.name = name;
        this.isConst = isConst;
        this.allocatedType = allocatedType;
    }

    public String toString() {
        return name +
                " = alloca " +
                allocatedType.toString();
    }
}
