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

    public Value getValue() {
        return value;
    }

    public Type getType() {
        if (!isArray) {
            return value.getType();
        } else {
            return new ArrayType(midArrayInitVal.get(0).getType(), midArrayInitVal.size());
        }
    }

    public boolean isArray() {
        return isArray;
    }

    public ArrayList<InitValue> getMidArrayInitVal() {
        return midArrayInitVal;
    }

    @Override
    public String toString() {
        if (isArray) {
            StringBuilder sb = new StringBuilder();
            sb.append(getTypeString()).append(" [");
            for (int i = 0; i < midArrayInitVal.size(); i++) {
                sb.append(midArrayInitVal.get(i).toString());
                if (i != midArrayInitVal.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        } else {
            return value.getType().toString() + " " + value.getName();
        }
    }

    public String getTypeString() {
        if (isArray) {
            return "[" + midArrayInitVal.size() + " x " + midArrayInitVal.get(0).getTypeString() +  "]";
        } else {
            return value.getType().toString();
        }
    }
}
