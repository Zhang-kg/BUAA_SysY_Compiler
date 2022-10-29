package ErrorDetect;

import IR.Values.Value;

import java.util.ArrayList;

public class SymbolTableItem extends SymbolRecord {
    private SymbolType symbolType = SymbolType.VARIABLE;  // Function, Variable, Array, FuncParam, Attributes
    private String name = "";    // ident, or a[1], b[0][1]
    private String type = "";    // int, void

    private boolean isConst = false;    // true or false
//    private int value;          // number
//    private ArrayList<Integer> values;  // for array [2][3] = {{1, 2, 3}, {4, 5, 6}} ==> [1, 2, 3, 4, 5, 6]
//    private ArrayList<Integer> dimensions; // for array [2][3] ==> [2, 3]
    private int dimensions = 0;
//    private ArrayList<SymbolTableItem> arrayInitVal = new ArrayList<>();
    // for function
    private ArrayList<SymbolTableItem> funcParams = new ArrayList<>();


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
    //    public void setValue(int value) {
//        this.value = value;
//    }
//
//    public int getValue() {
//        return value;
//    }

//    public void addArrayInitVal(SymbolTableItem initValAttributes) {
//        this.arrayInitVal.add(initValAttributes);
//    }

    public void setDimensions(int dimensions) {
        this.dimensions = dimensions;
    }

    public int getDimensions() {
        return dimensions;
    }

    public ArrayList<SymbolTableItem> getFuncParams() {
        return funcParams;
    }

    public void addFuncParam(SymbolTableItem funcParam) {
        funcParams.add(funcParam);
    }
    public void setFuncParams(ArrayList<SymbolTableItem> funcParams) {
        this.funcParams = funcParams;
    }

    public void setConst(boolean aConst) {
        isConst = aConst;
    }

    public boolean isConst() {
        return isConst;
    }

    public void setSymbolType(SymbolType symbolType) {
        this.symbolType = symbolType;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public void cloneAttributes(SymbolTableItem symbolTableItem) {
        this.setSymbolType(symbolTableItem.getSymbolType());
        this.setName(symbolTableItem.getName());
        this.setDimensions(symbolTableItem.getDimensions());
        this.setConst(symbolTableItem.isConst());
    }
    public void combineAttributes(SymbolTableItem symbolTableItem) {
        this.setDimensions(Math.max(dimensions, symbolTableItem.getDimensions()));
        if (type.equals("int") || symbolTableItem.getType().equals("int")) {
            type = "int";
        }
    }

    public String toString() {
        return this.type +
                dimensions;
    }

    public int checkFuncParams(SymbolTableItem funcParamsAttributes) {
        if (this.funcParams.size() != funcParamsAttributes.getFuncParams().size()) {
            return 1;
        }
        ArrayList<SymbolTableItem> callFuncParams = funcParamsAttributes.getFuncParams();
        for (int i = 0; i < funcParams.size(); i++) {
            if (!funcParams.get(i).toString().equals(callFuncParams.get(i).toString())) {
                return 2;
            }
        }
        return 0;
    }

}
