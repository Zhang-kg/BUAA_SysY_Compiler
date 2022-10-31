package BackEnd;

import java.util.*;

public class RegAllocation {
    private HashMap<String, String> virToPhi = new HashMap<>();           // * virtual reg name -> physical reg name
    private HashMap<String, VirtualReg> nameToRegMap;   // * virtual reg name -> Virtual reg
    private ArrayList<String> availableRegs;
    private int totalReg;

    public RegAllocation() {
        availableRegs = new ArrayList<>();
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
    }

    public void setNameToRegMap(HashMap<String, VirtualReg> nameToRegMap) {
        this.nameToRegMap = nameToRegMap;
    }

    public void allocateReg() {
        ArrayList<VirtualReg> virtualRegs = new ArrayList<>(nameToRegMap.values());
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
                virtualReg.setInPhysicReg(true);
                virtualRegs.remove(virtualReg);
                virToPhi.put(virtualReg.getName(), phyReg);
//                System.out.println(virtualReg.getName() + "\t\tstart from " + virtualReg.getStart() +
//                        " to " + virtualReg.getEnd() + "\t\tallocate to " + phyReg);
            }
        }

        if (virtualRegs.size() != 0) {
            for (VirtualReg virtualReg : virtualRegs) {
                virtualReg.setInStack(true);
            }
            System.out.println("OUT OUT OUT OUT OUT OUT");
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
}
