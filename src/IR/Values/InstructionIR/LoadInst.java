package IR.Values.InstructionIR;

import IR.Values.BasicBlock;
import IR.Values.Value;
import IR.types.PointerType;

public class LoadInst extends Instruction {
    private static int LOAD_NUM = 0;

    public LoadInst(BasicBlock fatherBasicBlock, Value pointer) {
        super(fatherBasicBlock, InstructionType.LOAD, ((PointerType)pointer.getType()).getInnerValueType(), allocName());
        this.addOperand(pointer);
    }

    @Override
    public String toString() {
        return getName() + " = load " +
                getType().toString() + ", " +
                this.getOperands().get(0).getType() + " " +
                this.getOperands().get(0).getName();


    }

    private static String allocName() {
        return "%LOAD_NO_" + LOAD_NUM++;
    }
}
