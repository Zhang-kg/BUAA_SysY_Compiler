package IR.Values;

import java.util.ArrayList;

public class Function extends Value {
    private ArrayList<BasicBlock> basicBlocks;

    public Function() {
        super(null, null);
        basicBlocks = new ArrayList<>();
        basicBlocks.add(new BasicBlock());
    }

    public ArrayList<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }
}
