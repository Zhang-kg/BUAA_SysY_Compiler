package IR.Values.InstructionIR;

import IR.Values.BasicBlock;
import IR.Values.Value;
import IR.types.PointerType;

public class GEPInst extends Instruction {
    private static int GEP_NUM = 0;

    public GEPInst(BasicBlock fatherBasicBlock, Value pointerValue) {
        super(fatherBasicBlock, InstructionType.GEP, ((PointerType) pointerValue.getType()).getInnerValueType(), allocName());
        addOperand(pointerValue);
    }

    private static String allocName() {
        return "GEP_NO_" + GEP_NUM++;
    }

    @Override
    public String toString() {
        return getName() + " = getelementptr " +
                ((PointerType) getOperands().get(0).getType()).getInnerValueType() + ", " +
                getOperands().get(0).getType() + " " + getOperands().get(0).getName() + " " +
                "i32  0, i32 0";
    }
}
