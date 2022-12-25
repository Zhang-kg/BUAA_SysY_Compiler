package BackEnd;

import BackEnd.Optimize.MIPSCFGAnalysis;
import BackEnd.Optimize.MIPSLiveAnalysis;
import BackEnd.SymbolTableForMIPS.SymbolTypeForMIPS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class RegAllocationColor extends RegAllocation {
    // 这个寄存器分配方案采用了图着色
    // 最基础的，根据书上介绍的方法实现的
    private HashMap<MIPSBlock, HashSet<String>> liveIn;
    private HashMap<MIPSBlock, HashSet<String>> liveOut;

    private ArrayList<HashSet<String>> conflictArray;
    // 这些是MIPS中全局变量或者全局字符串的名字, 这些不参与分配物理寄存器
    private ArrayList<String> stackVariablesName;
    public RegAllocationColor() {
        super();
    }

    public void setStackVariablesName(ArrayList<String> stackVariablesName) {
        this.stackVariablesName = stackVariablesName;
    }

    public void allocateReg() {
        MIPSFunction function = getMipsFunction();
        // 活跃变量分析，获得out集合
        MIPSCFGAnalysis mipscfgAnalysis = new MIPSCFGAnalysis(function,
                false);
        MIPSLiveAnalysis mipsLiveAnalysis = new MIPSLiveAnalysis(function,
                mipscfgAnalysis.getSuccessorBB(), stackVariablesName);
        // 构建冲突图
        liveOut = mipsLiveAnalysis.getLiveOut();
        liveIn = mipsLiveAnalysis.getLiveIn();
        conflictArray = mipsLiveAnalysis.getConflictArray();
        HashMap<String, HashSet<String>> conflictGraph = new HashMap<>();
        getConflictGraph(conflictGraph);
        // 加入 K 是全局寄存器个数，则不断寻找连接点数目小于K的节点，将它从图中移走
        int K = getAvailableRegs().size();
        ArrayList<String> allocGlobalVar = new ArrayList<>();
        ArrayList<String> stackVar = new ArrayList<>();
        while (conflictGraph.keySet().size() != 0) {
            String removeVar = "";
            int find = 0;
            for (String s : conflictGraph.keySet()) {
                if (conflictGraph.get(s).size() < K) {
                    find = 1;
                    removeVar = s;
                    break;
                }
            }
            if (find == 0) {
                removeVar = new ArrayList<String>(conflictGraph.keySet()).get(0);
                stackVar.add(removeVar);
            } else {
                allocGlobalVar.add(removeVar);
            }
            for (String otherS : conflictGraph.get(removeVar)) {
                conflictGraph.get(otherS).remove(removeVar);
            }
            conflictGraph.remove(removeVar);
        }
        // allocGlobalVar 是可以分配的全局变量
        for (String inPhiReg : allocGlobalVar) {
            VirtualReg virtualReg = getNameToRegMap().get(inPhiReg);
            if (virtualReg == null) {
                System.out.println("图着色virtual Reg null");
            }
            if (virtualReg.getSymbolType() != SymbolTypeForMIPS.StackReg) {
                virtualReg.setInPhysicReg();
//                System.out.println("图着色分配：" + );
            }
        }
        ArrayList<String> availableRegs = getAvailableRegs();
        getConflictGraph(conflictGraph);
        HashMap<String, HashSet<String>> colorGraph = new HashMap<>();
        for (int i = allocGlobalVar.size() - 1; i >= 0; i--) {
            String varName = allocGlobalVar.get(i);
            if (!colorGraph.containsKey(varName)) {
                colorGraph.put(varName, new HashSet<>());
            }
            // 对当前节点着色
            int find = 0;
            String phiReg = "";
            for (String reg : availableRegs) {
                if (!colorGraph.get(varName).contains(reg)) {
                    find = 1;
                    phiReg = reg;
                    System.out.println("图着色分配：" + varName + " --> " + phiReg);
                    break;
                }
            }
            // 将当前节点着色结果发送到其他节点
            if (find == 1) {
               for (String neighbour : conflictGraph.get(varName)) {
                   if (!colorGraph.containsKey(neighbour)) {
                       colorGraph.put(neighbour, new HashSet<>());
                   }
                   colorGraph.get(neighbour).add(phiReg);
//                   if (phiReg.equals("$a2")) {
//                       System.out.println("phi reg a2");
//                   }
                   for (int j = 0; j < availableRegs.size(); j++) {
                       if (availableRegs.get(j).equals(phiReg)) {
                           if (getTotalReg() < j + 1) {
                               setTotalReg(j + 1);
                               break;
                           }
                       }
                   }
               }
               getVirToPhi().put(varName, phiReg); // 将虚拟寄存器名称与物理寄存器名称对应起来，便于后续翻译
            } else System.out.println("\n\n\n!!!!!!!!!!图着色错误，出现了无法分配的情况!!!!!!!!!!\n\n\n");
        }
        // stackVar 是spill到栈上的全局变量
        for (String inStackVar : stackVar) {
            if (getNameToRegMap().get(inStackVar).getSymbolType() == SymbolTypeForMIPS.VirtualReg ||
                getNameToRegMap().get(inStackVar).getSymbolType() == SymbolTypeForMIPS.FunctionParam) {
                getNameToRegMap().get(inStackVar).setInStack();
            }
        }
        int stackAllocSize = 0;
        for (Map.Entry<String, VirtualReg> entry : getNameToRegMap().entrySet()) {
            if (entry.getValue().getSymbolType() == SymbolTypeForMIPS.StackReg ||
                    entry.getValue().getSymbolType() == SymbolTypeForMIPS.SpillReg) {
                entry.getValue().setStackOffset(stackAllocSize);
                stackAllocSize += entry.getValue().getSize();
            }
        }
        setStackAllocSize(stackAllocSize);
    }

    public void getConflictGraph(HashMap<String, HashSet<String>> conflictGraph) {
        for (HashSet<String> s : conflictArray) {
            ArrayList<String> vars = new ArrayList<>(s);
            for (int i = 0; i < vars.size(); i++) {
                if (vars.get(i).length() == 0 || vars.get(i).charAt(0) == '$') continue;
                if (getNameToRegMap().get(vars.get(i)).getSymbolType() == SymbolTypeForMIPS.SpillReg ||
                    getNameToRegMap().get(vars.get(i)).getSymbolType() == SymbolTypeForMIPS.StackReg) continue;
                if (!conflictGraph.containsKey(vars.get(i))) {
                    conflictGraph.put(vars.get(i), new HashSet<>());
                }
                for (int j = i + 1; j < vars.size(); j++) {
                    if (vars.get(j).length() == 0 || vars.get(j).charAt(0) == '$') continue;
                    if (getNameToRegMap().get(vars.get(j)).getSymbolType() == SymbolTypeForMIPS.SpillReg ||
                            getNameToRegMap().get(vars.get(j)).getSymbolType() == SymbolTypeForMIPS.StackReg) continue;
                    if (!conflictGraph.containsKey(vars.get(j))) {
                        conflictGraph.put(vars.get(j), new HashSet<>());
                    }
                    conflictGraph.get(vars.get(i)).add(vars.get(j));
                    conflictGraph.get(vars.get(j)).add(vars.get(i));
                }
            }
        }
    }
}
