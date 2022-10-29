package IR.Values.ConstantIR;

import IR.types.Type;

public class ConstantString extends Constant {
    private String string = "";
    private static int STR_NUM = 0;


    public ConstantString(Type type, String string) {
        super(type, allocName());
        this.string = string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }

    private static String allocName() {
        return "@STRCON_NO_" + STR_NUM++;
    }
}
