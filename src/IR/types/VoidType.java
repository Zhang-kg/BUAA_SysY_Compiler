package IR.types;

public class VoidType extends Type {
    private static final VoidType voidType = new VoidType();

    public static VoidType getVoidType() {
        return voidType;
    }

    @Override
    public boolean isVoidType() {
        return true;
    }

    @Override
    public String toString() {
        return "void";
    }
}
