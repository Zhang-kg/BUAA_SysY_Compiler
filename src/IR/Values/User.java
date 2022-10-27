package IR.Values;

import IR.types.Type;

import java.util.ArrayList;

public class User extends Value {
    private ArrayList<Value> operands;

    public User(Type type, String name) {
        super(type, name);
        operands = new ArrayList<>();
    }

    public void addOperand(Value operand) {
        operands.add(operand);
    }

    public ArrayList<Value> getOperands() {
        return this.operands;
    }
}
