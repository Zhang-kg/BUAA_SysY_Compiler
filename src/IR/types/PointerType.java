package IR.types;

public class PointerType extends Type {
    private final Type innerValueType;

    public PointerType(Type valueType) {
        this.innerValueType = valueType;
    }

    @Override
    public boolean isPointerType() {
        return true;
    }

    public String toString() {
        return innerValueType.toString() + "*";
    }

    public Type getInnerValueType() {
        return innerValueType;
    }

}
