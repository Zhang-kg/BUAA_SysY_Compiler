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
    private boolean isSpecial = false;

    // * pointer Value type [2 x [3 x i32]]*
    // * pointer Value type get Inner Value Type [2 x [3 x i32]]
    // * ...get Inner Value Type.get Element Type [3 x i32]
    public GEPInst(BasicBlock fatherBasicBlock, Value pointerValue) {
        super(fatherBasicBlock, InstructionType.GEP,
                ((PointerType) pointerValue.getType()).getInnerValueType(),
                allocName());
        addOperand(pointerValue);
        pointerValue.addUser(this);
    }

    public GEPInst(BasicBlock fatherBasicBlock, Value pointerValue, Value num2) {
        super(fatherBasicBlock, InstructionType.GEP,
                new PointerType(((ArrayType)((PointerType) pointerValue.getType()).getInnerValueType()).getElementType()),
                allocName());
        addOperand(pointerValue);
        pointerValue.addUser(this);
        this.isArray = true;
        if (num2 instanceof ConstantInteger) {
            constDim = true;
            this.num2 = ((ConstantInteger) num2).getValue();
        } else {
            addOperand(num2);
            num2.addUser(this);
        }
    }

    public GEPInst(BasicBlock fatherBasicBlock, Value pointerValue, Value num2, boolean isSpecial) {
        super(fatherBasicBlock, InstructionType.GEP,
                pointerValue.getType(), allocName());
        this.isSpecial = true;
        addOperand(pointerValue);
        pointerValue.addUser(this);
        addOperand(num2);
        num2.addUser(this);
    }

    private static String allocName() {
        return "%GEP_NO_" + GEP_NUM++;
    }

    public boolean isSpecial() {
        return isSpecial;
    }

    public boolean isArray() {
        return isArray;
    }

    public boolean isConstDim() {
        return constDim;
    }

    public int getNum2() {
        return num2;
    }

    @Override
    public String toString() {
        if (isSpecial) {
            return getName() + " = getelementptr " +
                    ((PointerType) getOperands().get(0).getType()).getInnerValueType() + ", " +
                    getOperands().get(0).getType() + " " + getOperands().get(0).getName() + ", " +
                    "i32 " + getOperands().get(1).getName();
        } else {
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
}
