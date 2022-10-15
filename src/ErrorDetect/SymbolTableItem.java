package ErrorDetect;

import java.util.ArrayList;

public class SymbolTableItem extends SymbolRecord {
    private SymbolType symbolType;  // Function, Variable, Array, FuncParam, Attributes
    private String name;    // ident, or a[1], b[0][1]
    private String type;    // int, void
    // for variable
    private boolean isConst;    // true or false
    private int value;          // number
    private ArrayList<Integer> values;  // for array [2][3] = {{1, 2, 3}, {4, 5, 6}} ==> [1, 2, 3, 4, 5, 6]
    private ArrayList<Integer> dimensions; // for array [2][3] ==> [2, 3]
    // for function
    private ArrayList<SymbolTableItem> funcParams;

    public SymbolTableItem() {

    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
