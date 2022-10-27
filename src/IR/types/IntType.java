package IR.types;

public class IntType extends Type {
    private int bit;
    public static final IntType i1 = new IntType(1);
    public static final IntType i32 = new IntType(32);

    public IntType(int bit) {
        this.bit = bit;
    }

    public boolean isIntType() {
        return true;
    }

    @Override
    public String toString() {
        return "i" + bit;
    }
}
