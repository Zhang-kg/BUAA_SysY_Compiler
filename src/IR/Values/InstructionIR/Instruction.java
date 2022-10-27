package IR.Values.InstructionIR;

import IR.Values.BasicBlock;
import IR.Values.User;
import IR.Values.Value;

import java.util.ArrayList;

public class Instruction extends User {
    private BasicBlock fatherBasicBlock;

    public Instruction(BasicBlock fatherBasicBlock) {
        super(null, null);
        this.fatherBasicBlock = fatherBasicBlock;
        // TODO: fatherBasicBlock中添加这个Instruction
    }

}
