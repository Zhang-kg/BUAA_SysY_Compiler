package IR.SymbolTableForIR;

import java.util.HashMap;

public class SymbolTableForIR {
    private HashMap<String, SymbolForIR> symbols;
    private SymbolTableForIR prevSymbolTable;

    public SymbolTableForIR() {
        symbols = new HashMap<>();
        prevSymbolTable = null;
    }

    public SymbolTableForIR(SymbolTableForIR prevSymbolTable) {
        this.prevSymbolTable = prevSymbolTable;
    }
}
