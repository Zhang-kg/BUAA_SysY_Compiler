package IR.Values.InstructionIR;

import IR.Values.BasicBlock;
import IR.Values.User;
import IR.Values.Value;
import IR.types.Type;

import java.util.ArrayList;

public class Instruction extends User {
    private BasicBlock fatherBasicBlock;
    private final InstructionType instructionType;
    private static int INST_NUM = 0;

    public Instruction(BasicBlock fatherBasicBlock, InstructionType instructionType, Type type, String name) {
        super(type, name);
        this.fatherBasicBlock = fatherBasicBlock;
        this.fatherBasicBlock.addInstruction(this);
        this.instructionType = instructionType;
    }

    public InstructionType getInstructionType() {
        return instructionType;
    }

    private static String allocName() {
        return "INST_" + INST_NUM++;
    }
}
