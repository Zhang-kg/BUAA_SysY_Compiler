package BackEnd.Instr;

import BackEnd.MIPSInstruction;
import BackEnd.RegAllocation;
import BackEnd.SymbolTableForMIPS.SymbolTypeForMIPS;
import BackEnd.VirtualReg;

import java.util.HashMap;
import java.util.Vector;

public class MIPSBinary extends MIPSInstruction {
    public MIPSBinary(InstructionType instructionType, VirtualReg rd, VirtualReg rs, VirtualReg rt) {
        super(instructionType);
        this.setRd(rd);
        this.setRs(rs);
        this.setRt(rt);
    }

    public MIPSBinary(InstructionType instructionType, VirtualReg rd, VirtualReg rs, int imm) {
        super(instructionType);
        this.setRd(rd);
        this.setRs(rs);
        this.setImm(imm);
    }

    public String genMIPSFromMIPSInst(HashMap<String, VirtualReg> globalStringVirtualRegHashMap,
                                      RegAllocation regAllocation) {
        StringBuilder sb = new StringBuilder();
        StringBuilder sbBack = new StringBuilder();
        VirtualReg rd = getRd();
        VirtualReg rs = getRs();
        String rdPhiRegName = "";
        String rsPhiRegName = "";
        if (rd.getSymbolType() == SymbolTypeForMIPS.PhysicsReg) {
            rdPhiRegName = regAllocation.getVirToPhi().get(rd.getName());
        } else if (rd.getSymbolType() == SymbolTypeForMIPS.SpillReg) {
            // * 这里不仅要取出，而且最后要放回去，使用sbBack表示最后进行的操作
            int offsetRd = rd.getStackOffset();
//            sb.append("lw $t1, " + offsetRd + "($sp)\n");
            sbBack.append("sw $t1, " + offsetRd + "($sp)\n");   // * 最后进行的操作
            rdPhiRegName = "$t1";
        } else if (rd.getSymbolType() == SymbolTypeForMIPS.GlobalVariable) {
            sb.append("la $t1, " + rd.getName() + "\n");
            rsPhiRegName = "$t1";
        } else {    // ! wrong
            System.out.println("WRONG: BINARY RD NOT A PHYSICAL REG OR A SPILL REG OR GLOBAL VARIABLE");
            return "";
        }

        if (rs.getSymbolType() == SymbolTypeForMIPS.PhysicsReg) {
            rsPhiRegName = regAllocation.getVirToPhi().get(rs.getName());
        } else if (rs.getSymbolType() == SymbolTypeForMIPS.SpillReg) {
            int offsetRs = rs.getStackOffset();
            sb.append("lw $t2, " + offsetRs + "($sp)\n");
            sbBack.append("sw $t2, " + offsetRs + "($sp)\n");
            rsPhiRegName = "$t2";
        } else if (rs.getSymbolType() == SymbolTypeForMIPS.StackReg) {
            int offsetRs = rs.getStackOffset();
            sb.append("addi $t2, $sp, " + offsetRs + "\n");
            rsPhiRegName = "$t2";
        } else if (rs.getSymbolType() == SymbolTypeForMIPS.GlobalVariable) {
            sb.append("la $t2, " + rs.getName() + "\n");
            rsPhiRegName = "$t2";
        } else {    // ! wrong
            System.out.println("WRONG: BINARY RS NOT A PHYSICAL REG, A SPILL REG OR GLOBAL VARIABLE");
            System.out.println(rs.getSymbolType());
        }
        if (isHasImm()) {
            int imm = getImm();
            if (getInstructionType() == InstructionType.srem) {
                sb.append("div " + rdPhiRegName + ", " + rsPhiRegName + ", " + imm + "\n");
//                sb.append("divu " + rdPhiRegName + ", " + rsPhiRegName + ", " + imm + "\n");
//                sb.append("divu " + rsPhiRegName + ", " + imm + "\n");
                sb.append("mfhi " + rdPhiRegName + "\n");
            } else {
                sb.append(getInstructionType().toString() + " " + rdPhiRegName + ", " + rsPhiRegName + ", " + imm + "\n");
            }

            return sb.toString() + sbBack.toString();
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
                System.out.println("WRONG: BINARY RT NOT A PHYSICAL REG OR A SPILL REG");
            }
            if (getInstructionType() == InstructionType.srem) {
                sb.append("div " + rdPhiRegName + ", " + rsPhiRegName + ", " + rtPhiRegName + "\n");
//                sb.append("divu " + rdPhiRegName + ", " + rsPhiRegName + ", " + rtPhiRegName + "\n");
//                sb.append("divu " +  rsPhiRegName + ", " + rtPhiRegName + "\n");
                sb.append("mfhi " + rdPhiRegName + "\n");
            } else {
                sb.append(getInstructionType().toString() + " " + rdPhiRegName + ", " + rsPhiRegName + ", " + rtPhiRegName + "\n");
            }
            return sb.toString() + sbBack.toString();
        }
        // ! Wrong
        return "WRONG BINARY DONT HAVE IMM OR RT\n";
    }


}
