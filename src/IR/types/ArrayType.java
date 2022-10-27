package IR.types;

public class ArrayType extends Type {
    private Type elementType;
    private int num;

    public ArrayType(Type elementType, int num) {
        this.elementType = elementType;
        this.num = num;
    }

    @Override
    public boolean isArrayType() {
        return true;
    }

    @Override
    public String toString() {
        // TODO: Check the actually format
        return "[" + num + " x " + elementType.toString() + "]";
    }
}
