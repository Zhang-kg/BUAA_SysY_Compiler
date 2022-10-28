package IR.Values.InstructionIR.TerminatorIR;

import IR.Values.BasicBlock;
import IR.Values.InstructionIR.Instruction;
import IR.Values.InstructionIR.InstructionType;
import IR.types.Type;

public class TerminatorInst extends Instruction {

    public TerminatorInst(BasicBlock fatherBasicBlock, InstructionType instructionType, Type type, String name) {
        super(fatherBasicBlock, instructionType, type, name);
    }
}
