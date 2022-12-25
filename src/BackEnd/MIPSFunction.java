package BackEnd;

import BackEnd.SymbolTableForMIPS.SymbolTypeForMIPS;
import IR.Module;
import IR.Values.Value;

import java.util.ArrayList;
import java.util.HashMap;

public class MIPSFunction {
    private String name;
    private ArrayList<MIPSBlock> mipsBlocks = new ArrayList<>();
    private int allocSize = 0;
    private RegAllocation regAllocation = null;
    private ArrayList<Value> arguments = null;

    public MIPSFunction(String name) {
        this.name = name;

    }

    public void setArguments(ArrayList<Value> arguments) {
        this.arguments = arguments;
    }

    public void addMIPSBasicBlock(MIPSBlock mipsBlock) {
        this.mipsBlocks.add(mipsBlock);
    }

    public ArrayList<MIPSBlock> getMipsBlocks() {
        return mipsBlocks;
    }

    public void setRegAllocation(RegAllocation regAllocation) {
        this.regAllocation = regAllocation;
    }

    public String genMIPSFromMIPSFunction(HashMap<String, VirtualReg> globalStringVirtualRegHashMap) {
        StringBuilder sb = new StringBuilder();
        sb.append(name + ":\n");
//        ArrayList<VirtualReg> inputParams = regAllocation.getInputParams();
        sb.append("addi $sp, $sp, " + (-regAllocation.getStackAllocSize()) + "\n");
        if (!Module.isNoColor()) {
            sb.append(loadParams());
        }
        for (MIPSBlock mipsBlock : mipsBlocks) {
            sb.append(mipsBlock.genMIPSFromMIPSBlock(globalStringVirtualRegHashMap, regAllocation));
        }
        return sb.toString();
    }

    private String loadParams() {
        // 由于将大部分分配了，所以需要将它从传参的部分load下来
        if (arguments == null) {
            return "";
        }
        int paramSize = arguments.size();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paramSize; i++) {
            String paramName = arguments.get(i).getName().substring(1);
            VirtualReg virtualReg = regAllocation.getVirtualRegByName(paramName);
            int paramOffset = 4 * i + regAllocation.getStackAllocSize();    // 当前已经给函数内部需要存储在栈上的部分分配空间了，
                                                                            // 同时加上参数相对传参部分的偏移4*i
            if (virtualReg.getSymbolType() == SymbolTypeForMIPS.PhysicsReg) {
                // 对应参数分配到了物理寄存器，所以直接取出到物理寄存器
                sb.append("lw " + regAllocation.getVirToPhi().get(virtualReg.getName()) + ", " + paramOffset + "($sp)\n");
            } else if (virtualReg.getSymbolType() == SymbolTypeForMIPS.SpillReg) {
                // 这里仿照MIPSSw中对于存入Spill的格式，将函数参数存入SpillReg
                sb.append("lw $t1, " + paramOffset + "($sp)\n");
                int offset = virtualReg.getStackOffset();
//                sb.append("lw $t2, " + offset + "($sp)\n");
                sb.append("sw $t1, " + offset + "($sp)\n");
            } else if (virtualReg.getSymbolType() == SymbolTypeForMIPS.StackReg) {
                // 同样仿照MIPSSw中对于存入StackReg的形式
                sb.append("lw $t1, " + paramOffset + "($sp)\n");
                int offset = virtualReg.getStackOffset();
                sb.append("sw $t1, " + offset + "($sp)\n");
            }
        }
        return sb.toString();
    }
}
