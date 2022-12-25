package BackEnd;

import java.util.ArrayList;
import java.util.HashMap;

public class MIPSBlock {
    private String name;
    private ArrayList<MIPSInstruction> mipsInstructions;

    public MIPSBlock(String name) {
        this.name = name;
        this.mipsInstructions = new ArrayList<>();
    }

    public void addInstruction(MIPSInstruction instruction) {
        this.mipsInstructions.add(instruction);
    }

    public String getName() {
        return name;
    }

    public ArrayList<MIPSInstruction> getMipsInstructions() {
        return mipsInstructions;
    }

    public String genMIPSFromMIPSBlock(HashMap<String, VirtualReg> globalStringVirtualRegHashMap,
                                       RegAllocation regAllocation) {
        StringBuilder sb = new StringBuilder();
        sb.append(name + ":\n");
//        System.out.println(name);
        for (MIPSInstruction instruction : mipsInstructions) {
            sb.append(instruction.genMIPSFromMIPSInst(globalStringVirtualRegHashMap, regAllocation));
        }
        return sb.toString();
    }
}
