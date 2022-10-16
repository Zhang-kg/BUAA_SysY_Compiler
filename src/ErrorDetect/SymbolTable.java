package ErrorDetect;

import java.util.ArrayList;

public class SymbolTable extends SymbolRecord {
    private ArrayList<SymbolRecord> symbolRecords;
    private SymbolTable fatherTable;
    private int indexInFatherTable;

    public SymbolTable(SymbolTable fatherTable, int index) {
        this.symbolRecords = new ArrayList<>();
        this.fatherTable = fatherTable;
        this.indexInFatherTable = index;
    }

    public int addItem(SymbolRecord symbolRecord) {
        this.symbolRecords.add(symbolRecord);
        return symbolRecords.size();    // 1, 2, 3, ...
    }

    public void setFatherTable(SymbolTable fatherTable, int index) {
        this.fatherTable = fatherTable;
        this.indexInFatherTable = index;
    }

    public boolean findIdentInCurrentTable(String ident) {
        for (SymbolRecord symbolRecord : symbolRecords) {
            if (symbolRecord instanceof SymbolTableItem) {
                if (((SymbolTableItem) symbolRecord).getName().equals(ident)) {
                    return true;
                }
            }
        }
        return false;
    }

    public SymbolTableItem findIdentInAllTable(String ident) {
        SymbolTableItem targetItem = null;
        for (SymbolRecord symbolRecord : symbolRecords) {
            if (symbolRecord instanceof SymbolTableItem && ((SymbolTableItem) symbolRecord).getName().equals(ident)) {
                targetItem = (SymbolTableItem) symbolRecord;
            }
        }
        if (targetItem == null) {
            if (fatherTable == null) {
                return null;
            }
            return fatherTable.findIdentInAllTable(ident);
        }
        return targetItem;
    }

    public int getIndexInFatherTable() {
        return indexInFatherTable;
    }

    public int getIndex() {
        return symbolRecords.size();
    }
}
