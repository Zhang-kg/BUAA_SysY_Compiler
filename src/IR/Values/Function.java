package IR.Values;

import IR.types.FunctionType;
import IR.types.Type;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.util.ArrayList;

public class Function extends Value {
    private ArrayList<BasicBlock> basicBlocks;


    public Function(String name, Type type) {
        super(type, name);
        basicBlocks = new ArrayList<>();
        basicBlocks.add(new BasicBlock());
    }

    public ArrayList<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public void addBasicBlock(BasicBlock basicBlock) {
        this.basicBlocks.add(basicBlock);
    }
}
