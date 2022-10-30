package IR.Values;

import IR.types.Type;

import java.util.ArrayList;

public class Function extends Value {
    private ArrayList<BasicBlock> basicBlocks;
    private ArrayList<Value> arguments;


    public Function(String name, Type type) {
        super(type, name);
        basicBlocks = new ArrayList<>();
        basicBlocks.add(new BasicBlock());
    }

    public void setArguments(ArrayList<Value> arguments) {
        this.arguments = arguments;
    }

    public ArrayList<Value> getArguments() {
        return arguments;
    }

    public ArrayList<Type> getArgumentsType() {
        return new ArrayList<>();
    }

    public ArrayList<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public void addBasicBlock(BasicBlock basicBlock) {
        this.basicBlocks.add(basicBlock);
    }
}
