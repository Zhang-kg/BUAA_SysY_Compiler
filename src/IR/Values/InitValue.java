package IR.Values;

import java.util.ArrayList;

public class InitValue extends Value {
    private boolean isArray;
    private ArrayList<InitValue> midArrayInitVal;
    private ArrayList<Value> finalArrayInitVal;
    private Value value;

    public InitValue(Value value) {
        super();
        this.value = value;
    }
}
