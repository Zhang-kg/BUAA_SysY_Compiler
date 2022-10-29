package IR.Values.InstructionIR;

import IR.Values.BasicBlock;
import IR.Values.ConstantIR.ConstantInteger;
import IR.Values.Value;
import IR.types.IntType;

public class StoreInst extends Instruction {
    private static int STORE_NUM = 0;

    public StoreInst(BasicBlock fatherBasicBlock, Value pointer, Value value) {
        super(fatherBasicBlock, InstructionType.STORE, IntType.i1, allocName());
        addOperand(value);
        addOperand(pointer);
    }

    @Override
    public String toString() {
        // * store i32 %0, i32* %1
        return "store " +
                getOperands().get(0).getType() + " " + getOperands().get(0).getName() + ", " +
                getOperands().get(1).getType() + " " + getOperands().get(1).getName();
//        if (getOperands().get(0) instanceof ConstantInteger) {
//            string += getOperands().get(0).getType() + " " + getOperands().get(0).getName() + ", ";
//        } else {
//            string += getOperands().get(0).getType() + " %" + getOperands().get(0).getName() + ", ";
//        }
//        if (getOperands().get(1) instanceof ConstantInteger) {
//            string += getOperands().get(1).getType() + " " + getOperands().get(1).getName();
//        } else {
//            string += getOperands().get(1).getType() + " %" + getOperands().get(1).getName();
//        }
    }

    private static String allocName() {
        return "%STORE_NO_" + STORE_NUM++;
    }
}
