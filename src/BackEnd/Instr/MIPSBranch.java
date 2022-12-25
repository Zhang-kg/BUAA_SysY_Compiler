package BackEnd.Instr;

import BackEnd.MIPSInstruction;
import BackEnd.RegAllocation;
import BackEnd.SymbolTableForMIPS.SymbolTypeForMIPS;
import BackEnd.VirtualReg;
import IR.Values.ConstantIR.ConstantInteger;
import IR.Values.Function;
import IR.Values.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MIPSBranch extends MIPSInstruction {
    private VirtualReg targetLabel = null;
    private Function function;
    private ArrayList<Value> arguments;
    private int envStackSize = 0;

    public MIPSBranch(InstructionType instructionType, VirtualReg cond, VirtualReg label) {
        super(instructionType);
        setRs(cond);
        this.targetLabel = label;
    }

    public MIPSBranch(InstructionType instructionType, VirtualReg label) {
        super(instructionType);
        this.targetLabel = label;
    }

    public void setArguments(ArrayList<Value> arguments) {
        this.arguments = arguments;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public Function getFunction() {
        return function;
    }

    public ArrayList<Value> getArguments() {
        return arguments;
    }

    public String getTargetLabelName() {
        return targetLabel.getName();
    }

    // * 输出call function的时候需要先保存环境、在调jal、再恢复环境
    public String genMIPSFromMIPSInst(HashMap<String, VirtualReg> globalStringVirtualRegHashMap,
                                      RegAllocation regAllocation) {
        StringBuilder sb = new StringBuilder();
        StringBuilder sbBack = new StringBuilder();
        if (isHasRs()) {
            String rsPhiRegName = "";
            VirtualReg rs = getRs();
            if (rs.getSymbolType() == SymbolTypeForMIPS.PhysicsReg) {
                rsPhiRegName = regAllocation.getVirToPhi().get(rs.getName());
            } else if (rs.getSymbolType() == SymbolTypeForMIPS.SpillReg) {
                int rsOffset = rs.getStackOffset();
                sb.append("lw $t1, " + rsOffset + "($sp)\n");
                sbBack.append("sw $t1, " + rsOffset + "($sp)\n");
                rsPhiRegName = "$t1";
            } else {
                System.out.println("WRONG: BRANCH RS IS NOT PHI REG OR SPILL REG");
            }
            sb.append(getInstructionType().toString() + " " + rsPhiRegName + ", " + targetLabel.getName() + "\n");
        } else {
            if (getInstructionType() == InstructionType.jal) {
                // * 保存环境
                sb.append(saveEnv(regAllocation));
                // * 压入参数
                sb.append(saveParams(regAllocation));
                // * 调用函数
                sb.append("jal " + function.getName().substring(1) + "\n");
                // * 恢复参数空间
                sb.append(restoreParamStack());
                // * 恢复环境
                sb.append(loadEnv(regAllocation));
            } else if (getInstructionType() == InstructionType.jr) {
                // * 恢复函数内部alloc的空间
                int size = regAllocation.getStackAllocSize();
//                for (Map.Entry<String, VirtualReg> entry : regAllocation.getNameToRegMap().entrySet()) {
//                    if (entry.getValue().getSymbolType() == SymbolTypeForMIPS.StackReg ||
//                        entry.getValue().getSymbolType() == SymbolTypeForMIPS.SpillReg) {
//                        size += entry.getValue().getSize();
//                    }
//                }
                sb.append("addi $sp, $sp, " + size + "\n");
                sb.append("jr $ra\n");
            } else {
                sb.append(getInstructionType().toString() + " " + targetLabel.getName() + "\n");
            }
        }
        return sb.toString() + sbBack.toString();
    }

    private String saveEnv(RegAllocation regAllocation) {
        StringBuilder sb = new StringBuilder();
        int size = regAllocation.getTotalReg();
        sb.append("addi $sp, $sp, " + (size + 1) * -4 + "\n");
        for (int i = 0; i < size; i++) {
            sb.append("sw " + regAllocation.getAvailableRegs().get(i) + ", " + 4 * i + "($sp)\n");
        }
        sb.append("sw $ra, " + (4 * size) + "($sp)\n");
//        HashMap<String, VirtualReg> nameToRegMap = regAllocation.getNameToRegMap();
//        int size = 0;
//        for (Map.Entry<String, VirtualReg> entry : nameToRegMap.entrySet()) {
//            if (entry.getValue().getSymbolType() == SymbolTypeForMIPS.PhysicsReg) {
//                size++;
//            }
//        }
//        sb.append("addi $sp, $sp, " + (size + 1) * -4 + "\n");
//        int stackOffset = 0;
//        for (Map.Entry<String, VirtualReg> entry : nameToRegMap.entrySet()) {
//            if (entry.getValue().getSymbolType() == SymbolTypeForMIPS.PhysicsReg) {
//                entry.getValue().setStackOffset(stackOffset * 4);
//                String virRegName = entry.getKey();
//                String regName = regAllocation.getVirToPhi().get(virRegName);
//                sb.append("sw " + regName + ", " + (4 * stackOffset) + "($sp)\n");
//                stackOffset++;
//            }
//        }
//        sb.append("sw $ra, " + (4 * stackOffset) + "($sp)\n");
//        stackOffset++;
//        this.envStackSize = stackOffset * 4;
        return sb.toString();
    }

    private String loadEnv(RegAllocation regAllocation) {
        StringBuilder sb = new StringBuilder();
        int size = regAllocation.getTotalReg();
        for (int i = 0; i < size; i++) {
            sb.append("lw " + regAllocation.getAvailableRegs().get(i) + ", " + 4 * i + "($sp)\n");
        }
        sb.append("lw $ra, " + (4 * size) + "($sp)\n");
        sb.append("addi $sp, $sp, " + (size + 1) * 4 + "\n");
//        HashMap<String, VirtualReg> nameToRegMap = regAllocation.getNameToRegMap();
//        int size = 0;
//        for (Map.Entry<String, VirtualReg> entry : nameToRegMap.entrySet()) {
//            if (entry.getValue().getSymbolType() == SymbolTypeForMIPS.PhysicsReg) {
//                size++;
//            }
//        }
//        for (Map.Entry<String, VirtualReg> entry : nameToRegMap.entrySet()) {
//            if (entry.getValue().getSymbolType() == SymbolTypeForMIPS.PhysicsReg) {
//                String virRegName = entry.getKey();
//                String regName = regAllocation.getVirToPhi().get(virRegName);
//                sb.append("lw " + regName + ", " + entry.getValue().getStackOffset() + "($sp)\n");
//            }
//        }
//        sb.append("lw $ra, " + (4 * size) + "($sp)\n");
//        sb.append("addi $sp, $sp, ")
        return sb.toString();
    }

    private String saveParams(RegAllocation regAllocation) {
        StringBuilder sb = new StringBuilder();
        if (arguments != null) {
            int paramSize = arguments.size();
            sb.append("addi $sp, $sp, " + paramSize * -4 + "\n");
            for (int i = 0; i < paramSize; i++) {
                if (!(arguments.get(i) instanceof ConstantInteger)) {
                    String virRegName = arguments.get(i).getName().substring(1);
                    VirtualReg virtualReg = regAllocation.getVirtualRegByName(virRegName);
                    String phiRegName = "";
                    if (virtualReg.getSymbolType() == SymbolTypeForMIPS.PhysicsReg) {
                        phiRegName = regAllocation.getVirToPhi().get(virRegName);
                    } else if (virtualReg.getSymbolType() == SymbolTypeForMIPS.SpillReg) {
                        int offset = virtualReg.getStackOffset() + paramSize * 4;
                        offset += (regAllocation.getTotalReg() + 1) * 4;
                        sb.append("lw $t1, " + offset + "($sp)\n");
                        phiRegName = "$t1";
                    } else {
                        System.out.println("WRONG: BRANCH PARAM NOT A PHYSICAL REG OR A SPILL REG");
                    }
                    sb.append("sw " + phiRegName + ", " + 4 * i + "($sp)\n");
                } else {
                    sb.append("li $t0, " + arguments.get(i).getName() + "\n");
                    sb.append("sw $t0, " + 4 * i + "($sp)\n");
                }
            }
        }
        return sb.toString();
    }

    private String restoreParamStack() {
        StringBuilder sb = new StringBuilder();
        if (arguments != null) {
            int paramSize = arguments.size();
            sb.append("addi $sp, $sp, " + paramSize * 4 + "\n");
        }
        return sb.toString();
    }


}
