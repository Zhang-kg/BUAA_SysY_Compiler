package IR.types;

import java.util.ArrayList;

public class FunctionType extends Type {
    private ArrayList<Type> argumentsType;
    private Type returnType;
    private String typeCache = null;

    public FunctionType(ArrayList<Type> argumentsType, Type returnType) {
        this.argumentsType = argumentsType;
        this.returnType = returnType;
    }

    @Override
    public boolean isFunctionType() {
        return true;
    }

    public Type getReturnType() {
        return returnType;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public void setArgumentsType(ArrayList<Type> argumentsType) {
        this.argumentsType = argumentsType;
    }

    public ArrayList<Type> getArgumentsType() {
        return argumentsType;
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
