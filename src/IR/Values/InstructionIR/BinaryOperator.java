package IR.Values.InstructionIR;

import IR.Values.BasicBlock;
import IR.Values.Value;

public class BinaryOperator extends Instruction {
    private static int BINARY_INST_NUM = 0;

    public BinaryOperator(BasicBlock fatherBasicBlock, InstructionType instructionType, Value value1, Value value2) {
        super(fatherBasicBlock, instructionType, value1.getType(), allocName());
        this.addOperand(value1);
        value1.addUser(this);
        this.addOperand(value2);
        value2.addUser(this);
    }

    @Override
    public String toString() {
//        switch (this.getInstructionType()) {
//            case ADD -> sb.append("add");
//            case SUB -> sb.append("sub");
//            case MUL -> sb.append("mul");
//            case DIV -> sb.append("div");
//            case MOD -> sb.append("")
//        }
        return this.getInstructionType().toString().toLowerCase() + " " +
                this.getType().toString() + " " +
                this.getOperands().get(0).getName() + ", " +
                this.getOperands().get(1).getName();
    }

    private static String allocName() {
        return "BINARY_INST_NO_" + BINARY_INST_NUM++;
    }
}
