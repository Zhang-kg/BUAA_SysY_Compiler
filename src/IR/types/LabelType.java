package IR.types;

public class LabelType extends Type {
    private static final LabelType labelType = new LabelType();
    private static int label_num = 0;

    public static LabelType getLabelType() {
        return labelType;
    }

    @Override
    public boolean isLabelType() {
        return true;
    }

    @Override
    public String toString() {
        return "Label_" + label_num++;
    }
}
