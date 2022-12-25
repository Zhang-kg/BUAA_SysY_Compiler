package BackEnd.Instr;

import BackEnd.MIPSInstruction;
import BackEnd.RegAllocation;
import BackEnd.SymbolTableForMIPS.SymbolTypeForMIPS;
import BackEnd.VirtualReg;

import java.util.HashMap;

public class MIPSLa extends MIPSInstruction {
    public MIPSLa(VirtualReg rd, VirtualReg rs) {
        super(InstructionType.la);
        setRd(rd);
        setRs(rs);
    }

    public String genMIPSFromMIPSInst(HashMap<String, VirtualReg> globalStringVirtualRegHashMap,
                                      RegAllocation regAllocation) {
        StringBuilder sb = new StringBuilder();
        StringBuilder sbBack = new StringBuilder();
        VirtualReg rd = getRd();
        VirtualReg rs = getRs();
        String rdPhiRegName = "";
        if (rd.getSymbolType() == SymbolTypeForMIPS.PhysicsReg) {
            rdPhiRegName = regAllocation.getVirToPhi().get(rd.getName());
        } else if (rd.getSymbolType() == SymbolTypeForMIPS.SpillReg) {
            int offset = rd.getStackOffset();
            sbBack.append("sw $t1, " + offset + "($sp)\n");
            rdPhiRegName = "$t1";
        } else {
            System.out.println("WRONG: LA Rd IS NOT PHI REG OR SPILL REG");
        }
        sb.append("la " + rdPhiRegName + ", " + rs.getName() + "\n");
        return sb.toString() + sbBack.toString();
    }
}
