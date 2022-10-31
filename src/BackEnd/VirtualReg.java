package BackEnd;

public class VirtualReg implements Comparable {
    private String name;
    private int start;
    private int end;
    private boolean inPhysicReg;
    private boolean inMemory;
    private int memoryAddress;
    private boolean inStack;
    private int stackOffset;

    public VirtualReg(int start, String name) {
        this.start = start;
        this.name = name;
    }

    public void setInPhysicReg(boolean inPhysicReg) {
        this.inPhysicReg = inPhysicReg;
        if (inPhysicReg) {
            inMemory = false;
            inStack = false;
        }
    }

    public void setInStack(boolean inStack) {
        this.inStack = inStack;
        if (inStack) {
            inPhysicReg = false;
            inMemory = false;
        }
    }

    public void setStackOffset(int stackOffset) {
        this.stackOffset = stackOffset;
    }

    public void setEnd(int end) {
        this.end = end;
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
