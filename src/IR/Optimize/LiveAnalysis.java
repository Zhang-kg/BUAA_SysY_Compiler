package IR.Optimize;

import IR.Module;
import IR.Values.BasicBlock;
import IR.Values.ConstantIR.ConstantInteger;
import IR.Values.Function;
import IR.Values.InstructionIR.*;
import IR.Values.InstructionIR.TerminatorIR.BrInst;
import IR.Values.InstructionIR.TerminatorIR.CallInst;
import IR.Values.InstructionIR.TerminatorIR.RetInst;
import IR.Values.Value;
import IR.types.VoidType;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

public class LiveAnalysis { // * 活跃变量分析
    private HashMap<BasicBlock, HashSet<String>> liveIn = new HashMap<>();
    private HashMap<BasicBlock, HashSet<String>> liveOut = new HashMap<>();
    private HashMap<BasicBlock, HashSet<String>> def = new HashMap<>();
    private HashMap<BasicBlock, HashSet<String>> use = new HashMap<>();
    private HashMap<BasicBlock, HashSet<BasicBlock>> successorBB;

    public LiveAnalysis(Function function) {
        // 准备进行活跃变量分析
        prepareLiveAnalysis(function);
        // 获得每个基本块的use和def集合
        analysisDefUse(function);
        // 根据程序流图和每个基本块的def和use集合, 计算基本块的in和out集合
        DomTreeAnalysis domTreeAnalysis = new DomTreeAnalysis(function, false);
        successorBB = domTreeAnalysis.getSuccessorBB();
        analysisInOut(function);
    }

    public LiveAnalysis(Function function, HashMap<BasicBlock, HashSet<BasicBlock>> successorBB) {
        // 准备进行活跃变量分析
        prepareLiveAnalysis(function);
        // 获得每个基本块的use和def集合
        analysisDefUse(function);
        // 根据程序流图和每个基本块的def和use集合, 计算基本块的in和out集合
//        DomTreeAnalysis domTreeAnalysis = new DomTreeAnalysis(function);
        this.successorBB = successorBB;
        analysisInOut(function);
    }

    public void prepareLiveAnalysis(Function function) {
        // 活跃变量分析的准备函数, 内部将每个基本块的活跃变量的in集合, out集合, def集合, use集合
        // 初始化为空集
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            liveIn.put(basicBlock, new HashSet<>());
            liveOut.put(basicBlock, new HashSet<>());
            use.put(basicBlock, new HashSet<>());
            def.put(basicBlock, new HashSet<>());
        }
    }

    public void analysisDefUse(Function function) {
        // 计算每个基本块的use和def集合, 对于其中所有变量进行扫描
        // 如果定义先于使用, 则放到def集合中
        // 如果使用先于定义, 则放到use集合中
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            // 这里时刻要注意常数的问题
            for (PhiInst phiInst : basicBlock.getPhiInstructions()) {
                String defName = phiInst.getName();
                addVarToDefSet(basicBlock, defName);
                for (Value operand : phiInst.getOperands()) {
                    // 这里有可能是常数, 需要进行检验
                    if (!(operand instanceof ConstantInteger)) {
                        String useName = operand.getName();
                        addVarToUseSet(basicBlock, useName);
                    }
                }
            }
            for (Instruction i : basicBlock.getInstructions()) {
                if (i instanceof BrInst) {
                    if (((BrInst) i).isConditionalBranch()) {
                        // 这里不太可能是常数
                        String useName = i.getOperands().get(0).getName();
                        addVarToUseSet(basicBlock, useName);
                    }
                } else if (i instanceof CallInst) {
                    if (i.getType() != VoidType.voidType) {
                        String defName = i.getName();
                        addVarToDefSet(basicBlock, defName);
                    }
                    ArrayList<Value> arguments = ((CallInst) i).getArguments();
                    if (arguments == null) continue;
                    for (Value argument : arguments) {
                        // 这里有可能是常数
                        if (!(argument instanceof ConstantInteger)) {
                            String useName = argument.getName();
                            addVarToUseSet(basicBlock, useName);
                        }
                    }
                } else if (i instanceof RetInst) {
                    if (((RetInst) i).isExpReturn()) {
                        // 这里有可能是常数
                        if (!(i.getOperands().get(0) instanceof ConstantInteger)) {
                            String useName = i.getOperands().get(0).getName();
                            addVarToUseSet(basicBlock, useName);
                        }

                    }
                } else if (i instanceof AllocaInst) {
                    String defName = i.getName();
                    addVarToDefSet(basicBlock, defName);
                } else if (i instanceof BinaryOperator || i instanceof IcmpInst) {
                    // 这里的use都有可能是常数
                    if (!(i.getOperands().get(0) instanceof ConstantInteger)) {
                        String useName1 = i.getOperands().get(0).getName();
                        addVarToUseSet(basicBlock, useName1);
                    }
                    if (!(i.getOperands().get(1) instanceof ConstantInteger)) {
                        String useName2 = i.getOperands().get(1).getName();
                        addVarToUseSet(basicBlock, useName2);
                    }
                    String defName = i.getName();
                    addVarToDefSet(basicBlock, defName);
                } else if (i instanceof GEPInst) {
                    String useName1 = i.getOperands().get(0).getName();
                    String defName = i.getName();
                    if (((GEPInst) i).isSpecial()) {
                        Value value2 = i.getOperands().get(1);
                        if (value2 instanceof ConstantInteger) {
                            addVarToUseSet(basicBlock, useName1);
                            addVarToDefSet(basicBlock, defName);
                            continue;
                        }
                        String useName2 = value2.getName();
                        addVarToUseSet(basicBlock, useName1);
                        addVarToUseSet(basicBlock, useName2);
                        addVarToDefSet(basicBlock, defName);
                    } else if (((GEPInst) i).isArray()) {
                        if (((GEPInst) i).isConstDim()) {
                            addVarToUseSet(basicBlock, useName1);
                            addVarToDefSet(basicBlock, defName);
                            continue;
                        }
                        String useName2 = i.getOperands().get(1).getName();
                        addVarToUseSet(basicBlock, useName1);
                        addVarToUseSet(basicBlock, useName2);
                        addVarToDefSet(basicBlock, defName);
                    } else {
                        String useName = i.getOperands().get(0).getName();
                        addVarToUseSet(basicBlock, useName);
                        addVarToDefSet(basicBlock, defName);
                    }
                }  else if (i instanceof LoadInst) {
                    String useName = i.getOperands().get(0).getName();
                    addVarToUseSet(basicBlock, useName);
                    String defName = i.getName();
                    addVarToDefSet(basicBlock, defName);
                } else if (i instanceof StoreInst) {
                    // 这里的use可能是常数
                    if (!(i.getOperands().get(0) instanceof ConstantInteger)) {
                        String useName = i.getOperands().get(0).getName();
                        addVarToUseSet(basicBlock, useName);
                    }
                    String defName = i.getOperands().get(1).getName();
                    addVarToDefSet(basicBlock, defName);
                } else if (i instanceof ZextInst) {
                    // 这里的use可能是常数
                    if (!(i.getOperands().get(0) instanceof ConstantInteger)) {
                        String useName = i.getOperands().get(0).getName();
                        addVarToUseSet(basicBlock, useName);
                    }
                    String defName = i.getName();
                    addVarToDefSet(basicBlock, defName);
                } else if (i instanceof MoveInst) {
                    if (!(((MoveInst) i).getSrc() instanceof ConstantInteger)) {
                        String useName = ((MoveInst) i).getSrc().getName();
                        addVarToUseSet(basicBlock, useName);
                    }
                    String defName = i.getName();
                    addVarToDefSet(basicBlock, defName);
                }
            }
        }
    }

    public void addVarToUseSet(BasicBlock basicBlock, String useVar) {
        if (def.get(basicBlock).contains(useVar)) return;
        use.get(basicBlock).add(useVar);
    }

    public void addVarToDefSet(BasicBlock basicBlock, String defVar) {
        if (use.get(basicBlock).contains(defVar)) return;
        def.get(basicBlock).add(defVar);
    }

    public void analysisInOut(Function function) {
        printDefUseSetForDebug(function);
        boolean changed = true;
        while (changed) {
            changed = false;
            // 思路就是重复不变的遍历所有的基本块, 首先计算每个基本块的out集合
            // out集合是由该基本块的所有后继基本块的in集合取并集而来,
            // 接着计算in集合, in = use U (out - def)
            // 这里面如果out或者in和上一轮计算结果不同, 则changed都要变成true, 再进行一轮计算
            // 最终一定会结束, 因为每一轮计算in和out只增不减, 而上界存在.
            for (BasicBlock basicBlock : function.getBasicBlocks()) {
                // 临时out集合
                HashSet<String> tempOut = new HashSet<>();
                for (BasicBlock sucBB : successorBB.get(basicBlock)) {
                    tempOut.addAll(liveIn.get(sucBB));
                }
                changed = isChanged(changed, basicBlock, tempOut, liveOut);
                HashSet<String> tempIn = new HashSet<>(use.get(basicBlock));
                HashSet<String> intersect = new HashSet<>(tempOut);
                intersect.retainAll(def.get(basicBlock));
                tempIn.addAll(tempOut);
                tempIn.removeAll(intersect);
                changed = isChanged(changed, basicBlock, tempIn, liveIn);
            }
        }
    }

    public void printDefUseSetForDebug(Function function) {
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            System.out.println(basicBlock.getLabel().getName());
            System.out.println("\tDef:");
            for (String s : def.get(basicBlock)) {
                System.out.println("\t\t" + s);
            }
            System.out.println("\tUse:");
            for (String s : use.get(basicBlock)) {
                System.out.println("\t\t" + s);
            }
        }
    }

    private boolean isChanged(boolean changed, BasicBlock basicBlock, HashSet<String> tempSet,
                              HashMap<BasicBlock, HashSet<String>> liveSet) {
//        tempSet.stream().sorted();
//        liveSet.get(basicBlock).stream().sorted();
        String stream1 = tempSet.stream().sorted().collect(Collectors.joining());
        String stream2 = liveSet.get(basicBlock).stream().sorted().collect(Collectors.joining());
        if (!stream1.equals(stream2)) {
            {   // print for debug;
                System.out.println("\n\n1" + basicBlock.getLabel().getName());
                System.out.println(liveSet.get(basicBlock).toString());
                System.out.println(tempSet.toString());
            }
            changed = true;
            liveSet.get(basicBlock).clear();
            liveSet.get(basicBlock).addAll(tempSet);
        }
        return changed;
    }

    public HashMap<BasicBlock, HashSet<String>> getLiveIn() {
        return liveIn;
    }

    public HashMap<BasicBlock, HashSet<String>> getLiveOut() {
        return liveOut;
    }

    public HashMap<BasicBlock, HashSet<String>> getDef() {
        return def;
    }

    public HashMap<BasicBlock, HashSet<String>> getUse() {
        return use;
    }
}
