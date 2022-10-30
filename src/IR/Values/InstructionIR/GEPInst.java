package IR.Values.InstructionIR;

import IR.Values.BasicBlock;
import IR.Values.ConstantIR.ConstantInteger;
import IR.Values.Value;
import IR.types.ArrayType;
import IR.types.PointerType;

public class GEPInst extends Instruction {
    private static int GEP_NUM = 0;
    private int num2 = 0;
    private boolean isArray = false;
    private boolean constDim = false;

    // * pointer Value type [2 x [3 x i32]]*
    // * pointer Value type get Inner Value Type [2 x [3 x i32]]
    // * ...get Inner Value Type.get Element Type [3 x i32]
    public GEPInst(BasicBlock fatherBasicBlock, Value pointerValue) {
        super(fatherBasicBlock, InstructionType.GEP,
                ((PointerType) pointerValue.getType()).getInnerValueType(),
                allocName());
        addOperand(pointerValue);
    }

    public GEPInst(BasicBlock fatherBasicBlock, Value pointerValue, Value num2) {
        super(fatherBasicBlock, InstructionType.GEP,
                new PointerType(((ArrayType)((PointerType) pointerValue.getType()).getInnerValueType()).getElementType()),
                allocName());
        addOperand(pointerValue);
        this.isArray = true;
        if (num2 instanceof ConstantInteger) {
            constDim = true;
            this.num2 = ((ConstantInteger) num2).getValue();
        } else {
            addOperand(num2);
        }
    }

    private static String allocName() {
        return "%GEP_NO_" + GEP_NUM++;
    }

    @Override
    public String toString() {
        if (!isArray) {
            return getName() + " = getelementptr " +
                    ((PointerType) getOperands().get(0).getType()).getInnerValueType() + ", " +
                    getOperands().get(0).getType() + " " + getOperands().get(0).getName() + ", " +
                    "i32 0, i32 0";
        } else if (constDim) {
            return getName() + " = getelementptr " +
                    ((PointerType) getOperands().get(0).getType()).getInnerValueType() + ", " +
                    getOperands().get(0).getType() + " " + getOperands().get(0).getName() + ", " +
                    "i32 0, i32 " + num2;
        } else {
            return getName() + " = getelementptr " +
                    ((PointerType) getOperands().get(0).getType()).getInnerValueType() + ", " +
                    getOperands().get(0).getType() + " " + getOperands().get(0).getName() + ", " +
                    "i32 0, i32 " + getOperands().get(1).getName();
        }
    }
}
