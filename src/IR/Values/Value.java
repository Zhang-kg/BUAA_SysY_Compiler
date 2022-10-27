package IR.Values;

import IR.Use;
import IR.types.Type;

import java.util.ArrayList;

public class Value {
    private Type type;
    private String name;
    private ArrayList<Use> useArrayList;
    public static int reg_number = 0;

    public Value(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
