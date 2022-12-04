package IR.Values.InstructionIR;

import IR.Values.BasicBlock;
import IR.Values.Value;
import IR.types.Type;

public class ZextInst extends Instruction {
    private static int ZEXT_NUM = 0;
    private final Type targetType;

    public ZextInst(BasicBlock fatherBasicBlock, Value fromValue, Type targetType) {
        super(fatherBasicBlock, InstructionType.ZEXT, targetType, allocName());
        addOperand(fromValue);
        fromValue.addUser(this);
        this.targetType = targetType;
    }

    private static String allocName() {
        return "%ZEXT_NO_" + ZEXT_NUM++;
    }

    @Override
    public String toString() {
        return getName() + " = zext " +
                getOperands().get(0).getType().toString() + " " + getOperands().get(0).getName() +
                " to " + targetType.toString();
    }
}
