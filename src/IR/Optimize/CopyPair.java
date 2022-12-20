package IR.Optimize;

import IR.Values.Value;

public class CopyPair {
    Value src;
    Value dest;

    public CopyPair(Value src, Value dest) {
        this.src = src;
        this.dest = dest;
    }

    public Value getSrc() {
        return src;
    }

    public Value getDest() {
        return dest;
    }
}
