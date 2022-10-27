package IR.Values.ConstantIR;

import IR.Values.Value;
import IR.types.Type;

public class Constant extends Value {
    public Constant(Type type, String name) {
        super(type, name);
    }
}
