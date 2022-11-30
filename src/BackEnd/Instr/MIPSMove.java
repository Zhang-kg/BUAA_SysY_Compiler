package BackEnd.Instr;

import BackEnd.MIPSInstruction;
import BackEnd.RegAllocation;
import BackEnd.SymbolTableForMIPS.SymbolTypeForMIPS;
import BackEnd.VirtualReg;

import java.util.HashMap;

public class MIPSMove extends MIPSInstruction {

    public MIPSMove(VirtualReg target, VirtualReg source) {
        super(InstructionType.move);
        this.setRd(target);
        this.setRs(source);
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
            System.out.println("WRONG: MOVE RD IS NOT PHI REG OR SPILL REG");
        }
        String rsPhiRegName = "";
        if (rs.getSymbolType() == SymbolTypeForMIPS.PhysicsReg) {
            rsPhiRegName = regAllocation.getVirToPhi().get(rs.getName());
        } else if (rs.getSymbolType() == SymbolTypeForMIPS.SpillReg) {
            int offset = rs.getStackOffset();
            sb.append("lw $t2, " + offset + "($sp)\n");
            rsPhiRegName = "$t2";
        } else {
            System.out.println("WRONG: MOVE RS IS NOT PHI REG OR SPILL REG");
        }
        sb.append("move " + rdPhiRegName + ", " + rsPhiRegName + "\n");
        return sb.toString() + sbBack.toString();
    }
}
