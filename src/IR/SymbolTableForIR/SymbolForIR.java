package IR.SymbolTableForIR;

import IR.Values.Value;
import IR.types.Type;

public class SymbolForIR extends SymbolRecordForIR {
    private String name;
    private String aftName;
    private Type type;
    private Value value;
    private boolean isConstant;

    public SymbolForIR() {

    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAftName(String aftName) {
        this.aftName = aftName;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public void setConstant(boolean constant) {
        isConstant = constant;
    }

    public String getName() {
        return name;
    }

    public String getAftName() {
        return aftName;
    }

    public Type getType() {
        return type;
    }

    public Value getValue() {
        return value;
    }

    public boolean isConstant() {
        return isConstant;
    }
}
