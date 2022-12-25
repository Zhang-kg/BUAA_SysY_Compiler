package BackEnd.Optimize;

import BackEnd.Instr.*;
import BackEnd.MIPSBlock;
import BackEnd.MIPSFunction;
import BackEnd.MIPSInstruction;
import IR.Values.BasicBlock;
import IR.Values.ConstantIR.ConstantInteger;
import IR.Values.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

public class MIPSLiveAnalysis {
    private HashMap<MIPSBlock, HashSet<String>> liveIn = new HashMap<>();
    private HashMap<MIPSBlock, HashSet<String>> liveOut = new HashMap<>();
    private HashMap<MIPSBlock, HashSet<String>> def = new HashMap<>();
    private HashMap<MIPSBlock, HashSet<String>> use = new HashMap<>();
    private HashMap<MIPSBlock, HashSet<MIPSBlock>> successorBB;

    private ArrayList<String> stackVariablesName;


    // 指令级别冲突Set集合
    private ArrayList<HashSet<String>> conflictArray = new ArrayList<>();

    public MIPSLiveAnalysis(MIPSFunction function, HashMap<MIPSBlock, HashSet<MIPSBlock>> successorBB,
                            ArrayList<String> stackVariablesName) {
        this.stackVariablesName = stackVariablesName;
        this.successorBB = successorBB;
        prepareLiveAnalysis(function);
        analysisDefUse(function);
        analysisInOut(function);
        allVariableAsGlobal(function);
    }

    public ArrayList<HashSet<String>> getConflictArray() {
        return conflictArray;
    }

    public void prepareLiveAnalysis(MIPSFunction function) {
        for (MIPSBlock mipsBlock : function.getMipsBlocks()) {
            liveIn.put(mipsBlock, new HashSet<>());
            liveOut.put(mipsBlock, new HashSet<>());
            use.put(mipsBlock, new HashSet<>());
            def.put(mipsBlock, new HashSet<>());
        }
    }

    public void analysisDefUse(MIPSFunction mipsFunction) {
        for (MIPSBlock basicBlock : mipsFunction.getMipsBlocks()) {
            for (MIPSInstruction instruction : basicBlock.getMipsInstructions()) {
                if (instruction instanceof MIPSBinary || instruction instanceof MIPSComp) {
                    String rsName = instruction.getRs().getName();
                    addVarToUseSet(basicBlock, rsName);
                    if (instruction.isHasRt()) {
                        String rtName = instruction.getRt().getName();
                        addVarToUseSet(basicBlock, rtName);
                    }
                    String rdName = instruction.getRd().getName();
                    addVarToDefSet(basicBlock, rdName);
                } else if (instruction instanceof MIPSBranch) {
                    if (instruction.isHasRs()) {
                        String rsName = instruction.getRs().getName();
                        addVarToUseSet(basicBlock, rsName);
                    }
                    ArrayList<Value> arguments = ((MIPSBranch) instruction).getArguments();
                    if (arguments == null) continue;
                    for (Value value : arguments) {
                        if (!(value instanceof ConstantInteger)) {
                            String useName = value.getName().substring(1);
                            addVarToUseSet(basicBlock, useName);
                        }
                    }
                } else if (instruction instanceof MIPSLa) {
//                    String rsName = instruction.getRs().getName();
//                    addVarToUseSet(basicBlock, rsName);
                    String rdName = instruction.getRd().getName();
                    addVarToDefSet(basicBlock, rdName);
                } else if (instruction instanceof MIPSLi) {
                    String rdName = instruction.getRd().getName();
                    addVarToDefSet(basicBlock, rdName);
                } else if (instruction instanceof MIPSLw) {
                    String rsName = instruction.getRs().getName();
                    addVarToUseSet(basicBlock, rsName);
                    String rdName = instruction.getRd().getName();
                    addVarToDefSet(basicBlock, rdName);
                } else if (instruction instanceof MIPSMove) {
                    String rsName = instruction.getRs().getName();
                    addVarToUseSet(basicBlock, rsName);
                    String rdName = instruction.getRd().getName();
                    addVarToDefSet(basicBlock, rdName);
                } else if (instruction instanceof MIPSSw) {
                    String rsName = instruction.getRs().getName();
                    addVarToUseSet(basicBlock, rsName);
                    String rdName = instruction.getRd().getName();
                    addVarToDefSet(basicBlock, rdName);
                }
            }
        }
    }

    public void addVarToUseSet(MIPSBlock mipsBlock, String useVar) {
        if (def.get(mipsBlock).contains(useVar)) return;
        // 表示useVar是全局变量或者是全局的字符串
        if (stackVariablesName.contains(useVar)) return;
        use.get(mipsBlock).add(useVar);
    }

    public void addVarToDefSet(MIPSBlock mipsBlock, String defVar) {
        if (use.get(mipsBlock).contains(defVar)) return;
        // 表示defVar是全局变量或者全局字符串
        if (stackVariablesName.contains(defVar)) return;
        def.get(mipsBlock).add(defVar);
    }

    public void analysisInOut(MIPSFunction function) {
        printDefUseSetForDebug(function);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (MIPSBlock mipsBlock : function.getMipsBlocks()) {
                HashSet<String> tempOut = new HashSet<>();
                for (MIPSBlock sucBB : successorBB.get(mipsBlock)) {
                    tempOut.addAll(liveIn.get(sucBB));
                }
                changed = isChanged(changed, mipsBlock, tempOut, liveOut);
                HashSet<String> tempIn = new HashSet<>(use.get(mipsBlock));
                HashSet<String> intersect = new HashSet<>(tempOut);
                intersect.retainAll(def.get(mipsBlock));
                tempIn.addAll(tempOut);
                tempIn.removeAll(intersect);
                changed = isChanged(changed, mipsBlock, tempIn, liveIn);
            }
        }
    }

    public void printDefUseSetForDebug(MIPSFunction function) {
        for (MIPSBlock block : function.getMipsBlocks()) {
            System.out.println(block.getName());
            System.out.println("\tDef:");
            for (String s : def.get(block)) {
                System.out.println("\t\t" + s);
            }
            System.out.println("\tUse:");
            for (String s : use.get(block)) {
                System.out.println("\t\t" + s);
            }
        }
    }

    private boolean isChanged(boolean changed, MIPSBlock mipsBlock, HashSet<String> tempSet,
                              HashMap<MIPSBlock, HashSet<String>> liveSet) {
//        tempSet.stream().sorted();
//        liveSet.get(mipsBlock).stream().sorted();
        String stream1 = tempSet.stream().sorted().collect(Collectors.joining());
        String stream2 = liveSet.get(mipsBlock).stream().sorted().collect(Collectors.joining());
        if (!stream1.equals(stream2)) {
            changed = true;
            liveSet.get(mipsBlock).clear();
            liveSet.get(mipsBlock).addAll(tempSet);
        }
        return changed;
    }

    // 图着色中将指令级别的冲突画出来，这样所有寄存器都参与到全局寄存器分配中，
    // 所有寄存器也不区分局部寄存器和全局寄存器。
    public void allVariableAsGlobal(MIPSFunction function) {
        for (MIPSBlock mipsBlock : function.getMipsBlocks()) {
            HashSet<String> outSet = liveOut.get(mipsBlock);
            if (mipsBlock.getName().equals("Label_11")) {
                System.out.println("Debug");
            }
            for (int i = mipsBlock.getMipsInstructions().size() - 1; i >= 0; i--) {
                MIPSInstruction instruction = mipsBlock.getMipsInstructions().get(i);
                HashSet<String> useSet = new HashSet<>();
                HashSet<String> defSet = new HashSet<>();
                if (instruction instanceof MIPSBinary || instruction instanceof MIPSComp) {
                    String rsName = instruction.getRs().getName();
//                    useSet.add(rsName);
                    addToSet(useSet, rsName);
                    if (instruction.isHasRt()) {
                        String rtName = instruction.getRt().getName();
//                        useSet.add(rtName);
                        addToSet(useSet, rtName);
                    }
                    String rdName = instruction.getRd().getName();
//                    defSet.add(rdName);
                    addToSet(defSet, rdName);
                } else if (instruction instanceof MIPSBranch) {
                    if (instruction.isHasRs()) {
                        String rsName = instruction.getRs().getName();
//                        useSet.add(rsName);
                        addToSet(useSet, rsName);
                    }
                    ArrayList<Value> arguments = ((MIPSBranch) instruction).getArguments();
                    if (arguments != null) {
                        for (Value value : arguments) {
                            if (!(value instanceof ConstantInteger)) {
                                String useName = value.getName().substring(1);
//                                useSet.add(useName);
                                addToSet(useSet, useName);
                            }
                        }
                    }
                } else if (instruction instanceof MIPSLa) {
//                    String rsName = instruction.getRs().getName();
//                    useSet.add(rsName);
                    String rdName = instruction.getRd().getName();
//                    defSet.add(rdName);
                    addToSet(defSet, rdName);
                } else if (instruction instanceof MIPSLi) {
                    String rdName = instruction.getRd().getName();
//                    defSet.add(rdName);
                    addToSet(defSet, rdName);
                } else if (instruction instanceof MIPSLw) {
                    String rsName = instruction.getRs().getName();
//                    useSet.add(rsName);
                    addToSet(useSet, rsName);
                    String rdName = instruction.getRd().getName();
//                    defSet.add(rdName);
                    addToSet(defSet, rdName);
                } else if (instruction instanceof MIPSMove) {
                    String rsName = instruction.getRs().getName();
//                    useSet.add(rsName);
                    addToSet(useSet, rsName);
                    String rdName = instruction.getRd().getName();
//                    defSet.add(rdName);
                    addToSet(defSet, rdName);
                } else if (instruction instanceof MIPSSw) {
                    String rsName = instruction.getRs().getName();
//                    useSet.add(rsName);
                    addToSet(useSet, rsName);
                    String rdName = instruction.getRd().getName();
//                    defSet.add(rdName);
                    addToSet(useSet, rdName);
                }
                HashSet<String> intersect = new HashSet<>(outSet);
                intersect.retainAll(defSet);
                HashSet<String> inSet = new HashSet<>(useSet);
                inSet.addAll(outSet);
                inSet.removeAll(intersect);
                HashSet<String> abc = new HashSet<>(inSet);
                abc.addAll(defSet);
                conflictArray.add(abc);
                outSet = new HashSet<>(inSet);
            }
        }
    }

    private void addToSet(HashSet<String> varSet, String var) {
        if (stackVariablesName.contains(var)) return;
        varSet.add(var);
    }

    public HashMap<MIPSBlock, HashSet<String>> getLiveIn() {
        return liveIn;
    }

    public HashMap<MIPSBlock, HashSet<String>> getLiveOut() {
        return liveOut;
    }

    public HashMap<MIPSBlock, HashSet<String>> getDef() {
        return def;
    }

    public HashMap<MIPSBlock, HashSet<String>> getUse() {
        return use;
    }
}
