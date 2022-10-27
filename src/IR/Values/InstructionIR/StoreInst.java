package IR.Values.InstructionIR;

import IR.Values.BasicBlock;
import IR.Values.Value;

public class StoreInst extends Instruction {

    public StoreInst(BasicBlock fatherBasicBlock, Value pointer, Value value) {
        super(fatherBasicBlock);
        addOperand(value);
        addOperand(pointer);
    }

    @Override
    public String toString() {
        // * store i32 %0, i32* %1
        StringBuilder sb = new StringBuilder();
        sb.append("store ");
        sb.append(getOperands().get(0).getType().toString());
        sb.append("%").append(getOperands().get(0).getName()).append(", ");
        sb.append(getOperands().get(1).getType().toString());
        sb.append("%").append(getOperands().get(1).getName());
        return sb.toString();
    }
}
