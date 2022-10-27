package IR.types;

import java.util.ArrayList;

public class FunctionType extends Type {
    private final ArrayList<Type> argumentsType;
    private final Type returnType;
    private String typeCache = null;

    public FunctionType(ArrayList<Type> argumentsType, Type returnType) {
        this.argumentsType = argumentsType;
        this.returnType = returnType;
    }

    @Override
    public boolean isFunctionType() {
        return true;
    }

    @Override
    public String toString() {
        if (typeCache != null) {
            return typeCache;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(returnType.toString());
        for (Type type : argumentsType) {
            sb.append(type.toString());
        }
        typeCache = sb.toString();
        return typeCache;
    }
}
