package IR.Optimize;

import IR.Values.BasicBlock;
import IR.Values.InstructionIR.Instruction;
import IR.Values.InstructionIR.InstructionType;
import IR.Values.Value;
import IR.types.Type;

public class MoveInst extends Instruction {
    private Value src;
    private Value dest;

    public MoveInst(BasicBlock fatherBasicBlock, Type type, String name, Value src, Value dest) {
        super(fatherBasicBlock, InstructionType.MOVE, type, name);
        this.src = src;
        this.dest = dest;
    }

    public Value getSrc() {
        return src;
    }

    public Value getDest() {
        return dest;
    }

    @Override
    public String toString() {
        return dest.getName() + " = add " + dest.getType() + " " + src.getName() + ", 0";
    }
}
