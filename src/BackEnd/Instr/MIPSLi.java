package BackEnd.Instr;

import BackEnd.MIPSInstruction;
import BackEnd.RegAllocation;
import BackEnd.SymbolTableForMIPS.SymbolTypeForMIPS;
import BackEnd.VirtualReg;

import java.util.HashMap;

public class MIPSLi extends MIPSInstruction {

    public MIPSLi(VirtualReg target, int imm) {
        super(InstructionType.li);
        setRd(target);
        setImm(imm);
    }

    public String genMIPSFromMIPSInst(HashMap<String, VirtualReg> globalStringVirtualRegHashMap,
                                      RegAllocation regAllocation) {
        StringBuilder sb = new StringBuilder();
        StringBuilder sbBack = new StringBuilder();
        VirtualReg rd = getRd();
        int imm = getImm();
        String rdPhiRegName = "";
        if (rd.getSymbolType() == SymbolTypeForMIPS.PhysicsReg) {
            rdPhiRegName = regAllocation.getVirToPhi().get(rd.getName());
        } else if (rd.getSymbolType() == SymbolTypeForMIPS.SpillReg) {
            int offset = rd.getStackOffset();
            sbBack.append("sw $t1, " + offset + "($sp)\n");
            rdPhiRegName = "$t1";
        } else {
            System.out.println("WRONG: LI RD IS NOT PHI REG OR SPILL REG");
            return "";
        }
        sb.append("li " + rdPhiRegName + ", " + imm + "\n");
        return sb.toString() + sbBack.toString();
    }
}
