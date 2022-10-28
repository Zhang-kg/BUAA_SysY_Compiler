package IR.Values;

import IR.types.ArrayType;
import IR.types.Type;

import java.util.ArrayList;

public class InitValue extends Value {
    private boolean isArray;
    private ArrayList<InitValue> midArrayInitVal = null;
    private Value value;

    public InitValue(Value value) {
        super(value.getType(), value.getName());
        this.isArray = false;
        this.value = value;
    }

    public InitValue(ArrayList<InitValue> arrayInit) {
        super(new ArrayType(arrayInit.get(0).getType(), arrayInit.size()), "ARRAY_INIT_VALUE");
        this.midArrayInitVal = arrayInit;
        this.isArray = true;
    }


    public Type getType() {
        if (!isArray) {
            return value.getType();
        } else {
            return new ArrayType(midArrayInitVal.get(0).getType(), midArrayInitVal.size());
        }
    }
}
