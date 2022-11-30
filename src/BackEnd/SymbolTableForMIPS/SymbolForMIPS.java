package BackEnd.SymbolTableForMIPS;

public class SymbolForMIPS {
    private String name;
    private SymbolTypeForMIPS symbolType;

    public SymbolForMIPS(String name, SymbolTypeForMIPS symbolType) {
        this.name = name;
        this.symbolType = symbolType;
    }
}
