package BackEnd.Instr;

import BackEnd.MIPSInstruction;
import BackEnd.RegAllocation;
import BackEnd.SymbolTableForMIPS.SymbolTypeForMIPS;
import BackEnd.VirtualReg;

import java.util.HashMap;

public class MIPSLw extends MIPSInstruction {
    public MIPSLw(VirtualReg rd, VirtualReg rs) {
        super(InstructionType.lw);
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
            System.out.println("WRONG: LW RD IS NOT PHI REG OR STACK REG");
        }
        if (rs.getSymbolType() == SymbolTypeForMIPS.PhysicsReg) {
            // ! 这里是有可能从PhysicsReg中取出值的
            // ! 对于GEP，将内存地址加起来之后，之后的load直接从变量中取
            String rsPhiRegName = regAllocation.getVirToPhi().get(rs.getName());
            sb.append("lw " + rdPhiRegName + ", " + "(" + rsPhiRegName + ")\n");
        } else if (rs.getSymbolType() == SymbolTypeForMIPS.SpillReg) {
            // ! 对于SpillReg（分配寄存器时溢出），load两次
            int offset = rs.getStackOffset();
            sb.append("lw $t2, " + offset + "($sp)\n");
            sb.append("lw " + rdPhiRegName + ", ($t2)\n");
        } else if (rs.getSymbolType() == SymbolTypeForMIPS.StackReg) {
            // ! 对于StackReg（alloc出来的寄存器），load一次
            int offset = rs.getStackOffset();
            sb.append("lw " + rdPhiRegName + ", " + offset + "($sp)\n");
        } else if (rs.getSymbolType() == SymbolTypeForMIPS.GlobalString ||
                rs.getSymbolType() == SymbolTypeForMIPS.GlobalVariable) {
            sb.append("lw " + rdPhiRegName + ", " + rs.getName() + "\n");
        } else {
            System.out.println("WRONG: LW RS IS NOT PHI REG, STACK REG, GLOBAL STRING OR GLOBAL VARIABLE");
        }
        return sb.toString() + sbBack.toString();
    }
}
