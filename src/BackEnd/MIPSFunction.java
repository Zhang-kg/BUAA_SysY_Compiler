package BackEnd;

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

    public void setRegAllocation(RegAllocation regAllocation) {
        this.regAllocation = regAllocation;
    }

    public String genMIPSFromMIPSFunction(HashMap<String, VirtualReg> globalStringVirtualRegHashMap) {
        StringBuilder sb = new StringBuilder();
        sb.append(name + ":\n");
//        ArrayList<VirtualReg> inputParams = regAllocation.getInputParams();
        sb.append("addi $sp, $sp, " + (-regAllocation.getStackAllocSize()) + "\n");
        for (MIPSBlock mipsBlock : mipsBlocks) {
            sb.append(mipsBlock.genMIPSFromMIPSBlock(globalStringVirtualRegHashMap, regAllocation));
        }
        return sb.toString();
    }

    private String loadParams() {
        if (arguments == null) {
            return "";
        }
        int paramSize = arguments.size();
        for (int i = 0; i < paramSize; i++) {
            String paramName = arguments.get(i).getName().substring(1);
            VirtualReg virtualReg = regAllocation.getVirtualRegByName(paramName);
//            if ()
        }
        return "";
    }
}
