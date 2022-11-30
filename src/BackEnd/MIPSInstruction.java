package BackEnd;

import BackEnd.Instr.InstructionType;

import java.util.HashMap;

public class MIPSInstruction {
    private boolean hasRs = false;
    private boolean hasRt = false;
    private boolean hasRd = false;
    private boolean hasOffset = false;
    private boolean hasImm = false;
    private VirtualReg rs;
    private VirtualReg rt;
    private VirtualReg rd;
    private int imm;
    private int offset;
    private InstructionType instructionType;

     public MIPSInstruction(InstructionType instructionType) {
        this.instructionType = instructionType;
     }

    public void setRd(VirtualReg rd) {
        this.rd = rd;
        hasRd = true;
    }

    public void setRs(VirtualReg rs) {
        this.rs = rs;
        hasRs = true;
    }

    public void setRt(VirtualReg rt) {
        this.rt = rt;
        hasRt = true;
    }

    public void setImm(int imm) {
        this.imm = imm;
        hasImm = true;
    }

    public void setOffset(int offset) {
        this.offset = offset;
        hasOffset = true;
    }

    public InstructionType getInstructionType() {
        return instructionType;
    }

    public VirtualReg getRd() {
        return rd;
    }

    public VirtualReg getRs() {
        return rs;
    }

    public VirtualReg getRt() {
        return rt;
    }

    public int getImm() {
        return imm;
    }

    public int getOffset() {
        return offset;
    }

    public boolean isHasImm() {
        return hasImm;
    }

    public boolean isHasRt() {
        return hasRt;
    }

    public boolean isHasRs() {
        return hasRs;
    }

    public String genMIPSFromMIPSInst(HashMap<String, VirtualReg> globalStringVirtualRegHashMap,
                                      RegAllocation regAllocation) {
         return "# MIPS INSTRUCTION\n";
    }

//    public String getPhiRegName(VirtualReg virtualReg, RegAllocation regAllocation, String backupReg) {
//
//    }
}
