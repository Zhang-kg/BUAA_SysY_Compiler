package BackEnd.Instr;

import BackEnd.MIPSInstruction;
import BackEnd.RegAllocation;
import BackEnd.SymbolTableForMIPS.SymbolTypeForMIPS;
import BackEnd.VirtualReg;

import java.util.HashMap;

public class MIPSComp extends MIPSInstruction {

    public MIPSComp(InstructionType instructionType, VirtualReg rd, VirtualReg rs, VirtualReg rt) {
        super(instructionType);
        setRd(rd);
        setRs(rs);
        setRt(rt);
    }

    public MIPSComp(InstructionType instructionType, VirtualReg rd, VirtualReg rs, int imm) {
        super(instructionType);
        setRd(rd);
        setRs(rs);
        setImm(imm);
    }

    public String genMIPSFromMIPSInst(HashMap<String, VirtualReg> globalStringVirtualRegHashMap,
                                      RegAllocation regAllocation) {
        StringBuilder sb = new StringBuilder();
        StringBuilder sbBack = new StringBuilder();
        String rdPhiRegName = "";
        String rsPhiRegName = "";
        VirtualReg rd = getRd();
        VirtualReg rs = getRs();
        if (rd.getSymbolType() == SymbolTypeForMIPS.PhysicsReg) {
            rdPhiRegName = regAllocation.getVirToPhi().get(rd.getName());
        } else if (rd.getSymbolType() == SymbolTypeForMIPS.SpillReg) {
            int offsetRd = rd.getStackOffset();
            sb.append("lw $t1, " + offsetRd + "($sp)\n");
            sbBack.append("sw $t1, " + offsetRd + "($sp)\n");
            rdPhiRegName = "$t1";
        } else {    // ! wrong
            System.out.println("WRONG: COMP RD NOT A PHYSICAL REG OR A SPILL REG");
            return "";
        }
        if (rs.getSymbolType() == SymbolTypeForMIPS.PhysicsReg) {
            rsPhiRegName = regAllocation.getVirToPhi().get(rs.getName());
        } else if (rs.getSymbolType() == SymbolTypeForMIPS.SpillReg) {
            int offsetRs = rs.getStackOffset();
            sb.append("lw $t2, " + offsetRs + "($sp)\n");
            sbBack.append("sw $t2, " + offsetRs + "($sp)\n");
            rsPhiRegName = "$t2";
        } else {    // ! wrong
            System.out.println("WRONG: COMP RS NOT A PHYSICAL REG OR A SPILL REG");
        }
        if (isHasImm()) {
            int imm = getImm();
            if (imm > 32768 && getInstructionType() == InstructionType.slti) {
                sb.append("li $t0, " + imm + "\n");
                sb.append("slt " + rdPhiRegName + ", " + rsPhiRegName + ", $t0\n");
            } else {
                sb.append(getInstructionType().toString() + " " + rdPhiRegName + ", " + rsPhiRegName + ", " + imm + "\n");
            }

            return sb.toString() + sbBack;
        }
        if (isHasRt()) {
            VirtualReg rt = getRt();
            String rtPhiRegName = "";
            if (rt.getSymbolType() == SymbolTypeForMIPS.PhysicsReg) {
                rtPhiRegName = regAllocation.getVirToPhi().get(rt.getName());
            } else if (rt.getSymbolType() == SymbolTypeForMIPS.SpillReg) {
                int offsetRt = rt.getStackOffset();
                sb.append("lw $t3, " + offsetRt + "($sp)\n");
                sbBack.append("sw $t3, " + offsetRt + "($sp)\n");
                rtPhiRegName = "$t3";
            } else {    // ! wrong
                System.out.println("WRONG: COMP RT NOT A PHYSICAL REG OR A SPILL REG");
            }
            sb.append(getInstructionType().toString() + " " + rdPhiRegName + ", " + rsPhiRegName + ", " + rtPhiRegName + "\n");
            return sb.toString() + sbBack.toString();
        }
        // ! Wrong
        return "WRONG COMP DONT HAVE IMM OR RT\n";
    }
}
