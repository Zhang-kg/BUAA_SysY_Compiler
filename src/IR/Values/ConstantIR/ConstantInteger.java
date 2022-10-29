package IR.Values.ConstantIR;

import IR.types.IntType;
import IR.types.Type;

public class ConstantInteger extends Constant {
    private int value;
    public static ConstantInteger zero = new ConstantInteger(IntType.i32, "0", 0);

    public ConstantInteger(Type type, String name) {
        super(type, name);
    }

    public ConstantInteger(Type type, String name, int value) {
        super(type, name);
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
