package IR.Optimize;

import IR.Values.BasicBlock;
import IR.Values.Value;

public class CopyPair {
    Value src;
    Value dest;
    BasicBlock targetBlock;

    public CopyPair(Value src, Value dest) {
        this.src = src;
        this.dest = dest;
    }

    public void setTargetBlock(BasicBlock targetBlock) {
        this.targetBlock = targetBlock;
    }

    public BasicBlock getTargetBlock() {
        return targetBlock;
    }

    public Value getSrc() {
        return src;
    }

    public Value getDest() {
        return dest;
    }
}
