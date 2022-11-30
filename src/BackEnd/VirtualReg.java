package BackEnd;

import BackEnd.SymbolTableForMIPS.SymbolTypeForMIPS;

public class VirtualReg implements Comparable {
    private String name;
    private int start;
    private int end;
    private SymbolTypeForMIPS symbolType;
    private int stackOffset;
    private int size = 4;

    public VirtualReg(String name) {
        this.name = name;
    }

    public VirtualReg(int start, String name) {
        this.start = start;
        this.name = name;
    }

    public VirtualReg(String name, SymbolTypeForMIPS symbolType) {
        this.name = name;
        this.symbolType = symbolType;
    }

    public SymbolTypeForMIPS getSymbolType() {
        return symbolType;
    }

    public void setInPhysicReg() {
        symbolType = SymbolTypeForMIPS.PhysicsReg;
    }

    public void setInStack() {
        symbolType = SymbolTypeForMIPS.SpillReg;
    }

    public void setStackOffset(int stackOffset) {
        this.stackOffset = stackOffset;
    }

    public int getStackOffset() {
        return stackOffset;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getName() {
        return name;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    @Override
    public int compareTo(Object o) {
        VirtualReg virtualReg = (VirtualReg) o;
        if (this.getEnd() > virtualReg.getEnd()) {
            return 1;
        } else if (this.getEnd() < virtualReg.getEnd()) {
            return -1;
        } else {
            return Integer.compare(this.getStart(), virtualReg.getStart());
        }
    }
}
