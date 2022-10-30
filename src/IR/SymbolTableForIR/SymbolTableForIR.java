package IR.SymbolTableForIR;


import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTableForIR extends SymbolRecordForIR {
    private ArrayList<SymbolRecordForIR> symbols;
    private SymbolTableForIR fatherTable;
    private int indexInFatherTable;

    public SymbolTableForIR(SymbolTableForIR fatherTable, int index) {
        symbols = new ArrayList<>();
        this.indexInFatherTable = index;
        this.fatherTable = fatherTable;
    }

    public void addItem(SymbolRecordForIR symbolRecordForIR) {
        this.symbols.add(symbolRecordForIR);
    }

    public boolean findIdentInCurrentTable(String ident) {
        for (SymbolRecordForIR symbolRecode : symbols) {
            if (symbolRecode instanceof SymbolForIR) {
                if (((SymbolForIR) symbolRecode).getName().equals(ident)) {
                    return true;
                }
            }
        }
        return false;
    }

    public SymbolForIR findIdentInAllTable(String ident) {
        SymbolForIR targetItem = null;
        for (SymbolRecordForIR symbolRecord : symbols) {
            if (symbolRecord instanceof SymbolForIR && ((SymbolForIR) symbolRecord).getName().equals(ident)) {
                targetItem = (SymbolForIR) symbolRecord;
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

    public int getIndex() {
        return symbols.size();
    }
}
