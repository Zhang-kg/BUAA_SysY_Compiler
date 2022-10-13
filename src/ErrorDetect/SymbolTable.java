package ErrorDetect;

import java.util.ArrayList;

public class SymbolTable extends SymbolRecord {
    private ArrayList<SymbolRecord> symbolRecords;
    private SymbolTable fatherTable;
    private int index;

    public SymbolTable(SymbolTable fatherTable, int index) {
        this.symbolRecords = new ArrayList<>();
        this.fatherTable = fatherTable;
        this.index = index;
    }


}
