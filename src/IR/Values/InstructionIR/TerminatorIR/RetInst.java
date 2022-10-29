package IR.Values.InstructionIR.TerminatorIR;

import IR.Values.BasicBlock;
import IR.Values.InstructionIR.Instruction;
import IR.Values.InstructionIR.InstructionType;
import IR.Values.Value;
import IR.types.Type;
import IR.types.VoidType;

public class RetInst extends TerminatorInst {
    private static int RET_INST_NUM = 0;
    private boolean expReturn = true;
    public RetInst(BasicBlock fatherBasicBlock, Value value) {
        super(fatherBasicBlock, InstructionType.RET, value.getType(), allocName());
        expReturn = true;
        addOperand(value);
    }

    public RetInst(BasicBlock fatherBasicBlock) {
        super(fatherBasicBlock, InstructionType.RET, VoidType.voidType, allocName());
        expReturn = false;

    }

    private static String allocName() {
        return "%RET_INST_NO_" + RET_INST_NUM++;
    }

    @Override
    public String toString() {
        if (expReturn) {
            return "ret " + getOperands().get(0).getType() + " " + getOperands().get(0).getName();
        } else {
            return "ret void";
        }
    }
}
