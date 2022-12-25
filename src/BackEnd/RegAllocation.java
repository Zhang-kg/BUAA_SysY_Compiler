package BackEnd;

import BackEnd.SymbolTableForMIPS.SymbolTypeForMIPS;

import java.util.*;

public class RegAllocation {
    // 这个是寄存器分配的父类，寄存器在这里分配
    // 本文件中使用的方法不是图着色，而是基于贪心的“活动参加问题”，也没有考虑权重
    private HashMap<String, String> virToPhi = new HashMap<>();           // * virtual reg name -> physical reg name
    private HashMap<String, VirtualReg> nameToRegMap;   // * virtual reg name -> Virtual reg
    private ArrayList<String> availableRegs;
    private int totalReg;
    private int stackAllocSize = 0;
    private ArrayList<VirtualReg> inputParams;

    // 设置一下MIPSFunction，主要是为了图着色前的活跃变量分析
    private MIPSFunction mipsFunction;

    public RegAllocation() {
        virToPhi.put("$v0", "$v0");
        virToPhi.put("$a0", "$a0");
        virToPhi.put("$t0", "$t0");
        virToPhi.put("$t1", "$t1");
        virToPhi.put("$t2", "$t2");
        virToPhi.put("$t3", "$t3");
        virToPhi.put("$ra", "$ra");
        availableRegs = new ArrayList<>();
        availableRegs.add("$v1");
        availableRegs.add("$a1");
        availableRegs.add("$a2");
        availableRegs.add("$a3");
        availableRegs.add("$t4");
        availableRegs.add("$t5");
        availableRegs.add("$t6");
        availableRegs.add("$t7");
        availableRegs.add("$t8");
        availableRegs.add("$t9");
        availableRegs.add("$s0");
        availableRegs.add("$s1");
        availableRegs.add("$s2");
        availableRegs.add("$s3");
        availableRegs.add("$s4");
        availableRegs.add("$s5");
        availableRegs.add("$s6");
        availableRegs.add("$s7");
        availableRegs.add("$k0");
        availableRegs.add("$k1");
    }

    public void setNameToRegMap(HashMap<String, VirtualReg> nameToRegMap) {
        this.nameToRegMap = nameToRegMap;
    }

    public HashMap<String, VirtualReg> getNameToRegMap() {
        return nameToRegMap;
    }

    // 设置和去除MIPSFunction，图着色使用
    public void setMipsFunction(MIPSFunction mipsFunction) {
        this.mipsFunction = mipsFunction;
    }

    public MIPSFunction getMipsFunction() {
        return mipsFunction;
    }

    public void setInputParams(ArrayList<VirtualReg> inputParams) {
        this.inputParams = inputParams;
    }

    public ArrayList<VirtualReg> getInputParams() {
        return inputParams;
    }

    public void allocateReg() {
        ArrayList<VirtualReg> virtualRegs = new ArrayList<>();
        for (Map.Entry entry : nameToRegMap.entrySet()) {
            if (((VirtualReg)entry.getValue()).getSymbolType() == SymbolTypeForMIPS.VirtualReg) {
                virtualRegs.add((VirtualReg) entry.getValue());
            }
        }
        Collections.sort(virtualRegs);
//        System.out.println("abc");
        totalReg = 0;
        for (String phyReg : availableRegs) {
            if (virtualRegs.size() == 0) break;
            totalReg++;
            ArrayList<VirtualReg> virRegsForPhyReg = new ArrayList<>();
            virRegsForPhyReg.add(virtualRegs.get(0));
            int k = 0;
            for (int i = 1; i < virtualRegs.size(); i++) {
                if (virtualRegs.get(i).getStart() >= virtualRegs.get(k).getEnd()) {
                    virRegsForPhyReg.add(virtualRegs.get(i));
                    k = i;

                }
            }
            for (VirtualReg virtualReg : virRegsForPhyReg) {
                virtualReg.setInPhysicReg();
                virtualRegs.remove(virtualReg);
                virToPhi.put(virtualReg.getName(), phyReg);
//                System.out.println(virtualReg.getName() + "\t\tstart from " + virtualReg.getStart() +
//                        " to " + virtualReg.getEnd() + "\t\tallocate to " + phyReg);
            }
        }

        if (virtualRegs.size() != 0) {
            for (VirtualReg virtualReg : virtualRegs) {
                virtualReg.setInStack();
            }
            System.out.println("OUT OUT OUT OUT OUT OUT");
        }
        stackAllocSize = 0;
        for (Map.Entry<String, VirtualReg> entry : nameToRegMap.entrySet()) {
            if (entry.getValue().getSymbolType() == SymbolTypeForMIPS.StackReg ||
                entry.getValue().getSymbolType() == SymbolTypeForMIPS.SpillReg) {
                entry.getValue().setStackOffset(stackAllocSize);
                stackAllocSize += entry.getValue().getSize();
            }
        }
    }

    public HashMap<String, String> getVirToPhi() {
        return virToPhi;
    }

    public ArrayList<String> getAvailableRegs() {
        return availableRegs;
    }

    public int getTotalReg() {
        return totalReg;
    }

    public void setTotalReg(int totalReg) {
        this.totalReg = totalReg;
    }

    public int getStackAllocSize() {
        return stackAllocSize;
    }

    public void setStackAllocSize(int stackAllocSize) {
        this.stackAllocSize = stackAllocSize;
    }

    public VirtualReg getVirtualRegByName(String name) {
        return nameToRegMap.get(name);
    }
}
