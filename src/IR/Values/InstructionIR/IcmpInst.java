package IR.Values.InstructionIR;

import IR.Values.BasicBlock;
import IR.Values.Value;
import IR.types.IntType;

public class IcmpInst extends Instruction {
    private static int ICMP_INST_NUM = 0;

    public IcmpInst(BasicBlock fatherBasicBlock, InstructionType instructionType, Value value1, Value value2) {
        super(fatherBasicBlock, instructionType, IntType.i1, allocIcmpInstName());
        this.addOperand(value1);
        this.addOperand(value2);
    }

    @Override
    public String toString() {
        return getName() + " = icmp i32 " +
                this.getOperands().get(0).getType().toString() + " " +
                this.getOperands().get(0).getName() + ", " +
                this.getOperands().get(1).getName();
    }

    private static String allocIcmpInstName() {
        return "ICMP_INST_" + ICMP_INST_NUM++;
    }
}
