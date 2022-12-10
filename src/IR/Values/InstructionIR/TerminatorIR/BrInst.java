package IR.Values.InstructionIR.TerminatorIR;

import IR.Values.BasicBlock;
import IR.Values.InstructionIR.Instruction;
import IR.Values.InstructionIR.InstructionType;
import IR.Values.Value;
import IR.types.IntType;
import IR.types.Type;

public class BrInst extends TerminatorInst {
    private static int BR_INST_NUM = 0;
    private boolean conditionalBranch;

    /**
     *
     * @param fatherBasicBlock
     * @param condition 跳转条件
     * @param label1 为True时跳转到本Label
     * @param label2 为False时跳转到本Label
     */
    public BrInst(BasicBlock fatherBasicBlock, Value condition, Value label1, Value label2) {
        super(fatherBasicBlock, InstructionType.BR, IntType.i1, allocName());
        conditionalBranch = true;
        assert condition.getType() == IntType.i1;
        addOperand(condition);
        addOperand(label1);
        addOperand(label2);
    }

    public BrInst(BasicBlock fatherBasicBlock, Value label1) {
        super(fatherBasicBlock, InstructionType.BR, IntType.i1, allocName());
        conditionalBranch = false;
        addOperand(label1);
    }

    private static String allocName() {
        return "%BR_INST_NO_" + BR_INST_NUM++;
    }

    public boolean isConditionalBranch() {
        return conditionalBranch;
    }

    public void setConditionalBranch(boolean conditionalBranch) {
        this.conditionalBranch = conditionalBranch;
    }

    @Override
    public String toString() {
        if (conditionalBranch) {
            return "br i1 " + getOperands().get(0).getName() + ", " +
                    getOperands().get(1).getType() + " " + getOperands().get(1).getName() + ", " +
                    getOperands().get(2).getType() + " " + getOperands().get(2).getName();
        } else {
            return "br " + getOperands().get(0).getType() + " " + getOperands().get(0).getName();
        }
    }
}
