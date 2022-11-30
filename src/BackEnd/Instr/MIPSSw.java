package BackEnd.Instr;

import BackEnd.MIPSInstruction;
import BackEnd.RegAllocation;
import BackEnd.SymbolTableForMIPS.SymbolTypeForMIPS;
import BackEnd.VirtualReg;

import java.util.HashMap;

public class MIPSSw extends MIPSInstruction {

    public MIPSSw(VirtualReg sourceReg, VirtualReg pointerReg) {
        super(InstructionType.sw);
        setRd(pointerReg);
        setRs(sourceReg);
    }

    public String genMIPSFromMIPSInst(HashMap<String, VirtualReg> globalStringVirtualRegHashMap,
                                      RegAllocation regAllocation) {
        StringBuilder sb = new StringBuilder();
        VirtualReg target = getRd();
        VirtualReg source = getRs();
        String rsPhiRegName = "";
        if (source.getSymbolType() == SymbolTypeForMIPS.PhysicsReg) {
            rsPhiRegName = regAllocation.getVirToPhi().get(source.getName());
        } else if (source.getSymbolType() == SymbolTypeForMIPS.SpillReg) {
            int offset = source.getStackOffset();
            sb.append("lw $t1, " + offset + "($sp)\n");
            rsPhiRegName = "$t1";
        } else if (source.getSymbolType() == SymbolTypeForMIPS.FunctionParam) {
            // ! 函数的参数需要加上本函数中新alloc的栈大小从而获得栈内位置。
            int allocOffset = regAllocation.getStackAllocSize();
            // ! 当前参数对应的偏移量
            int paramStackOffset = source.getStackOffset();
            sb.append("lw $t1, " + (paramStackOffset + allocOffset) + "($sp)\n");
            rsPhiRegName = "$t1";
        } else {
            System.out.println("WRONG: SW source IS NOT PHI REG OR SPILL REG");
            System.out.println(source.getSymbolType().toString());
        }

        if (target.getSymbolType() == SymbolTypeForMIPS.GlobalString) {
            System.out.println("WRONG: SW target IS GLOBAL STRING");
        } else if (target.getSymbolType() == SymbolTypeForMIPS.PhysicsReg) {
            String phiRegName = regAllocation.getVirToPhi().get(target.getName());
            sb.append("sw " + rsPhiRegName + ", (" + phiRegName + ")\n");
        } else if (target.getSymbolType() == SymbolTypeForMIPS.SpillReg) {
            int offset = target.getStackOffset();
            sb.append("lw $t2, " + offset + "($sp)\n");
            sb.append("sw " + rsPhiRegName + ", ($t2)\n");
        } else if (target.getSymbolType() == SymbolTypeForMIPS.StackReg) {
            int offset = target.getStackOffset();
            sb.append("sw " + rsPhiRegName + ", " + offset + "($sp)\n");
        } else if (target.getSymbolType() == SymbolTypeForMIPS.GlobalVariable) {
            sb.append("sw " + rsPhiRegName + ", " + target.getName() + "\n");
        }
        return sb.toString();
    }
}
