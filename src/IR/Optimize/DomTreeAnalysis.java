package IR.Optimize;

import IR.Module;
import IR.Values.BasicBlock;
import IR.Values.Function;
import IR.Values.InstructionIR.Instruction;
import IR.Values.InstructionIR.InstructionType;
import IR.Values.InstructionIR.TerminatorIR.BrInst;
import IR.Values.Value;

import java.util.*;

public class DomTreeAnalysis {
    private Module llvmModule = Module.getMyModule();
    // label to basic block for a function
    private HashMap<Value, BasicBlock> label2BasicBlock4function;
    // predecessor ans successor basic block
    private HashMap<BasicBlock, HashSet<BasicBlock>> successorBB;
    private HashMap<BasicBlock, HashSet<BasicBlock>> predecessorBB;
    // the basic block in the hash set (value) is dominating the basic block (key)
    private HashMap<BasicBlock, HashSet<BasicBlock>> domBB;
    // the basic block in the 'value' position instance dominate the block in the 'key' position
    private HashMap<BasicBlock, BasicBlock> idomBB;
    // the basic blocks in the arraylist is the son of the basic block in the 'key' position in a dom tree
    private HashMap<BasicBlock, ArrayList<BasicBlock>> domTree;
    // dominating frontier
    private HashMap<BasicBlock, HashSet<BasicBlock>> DF;

    private boolean mem2reg;

    public DomTreeAnalysis(Function function, boolean mem2reg) {
        // 这里发现不能所有的支配树分析都删除基本块，因为phi中也涉及到基本块，有些基本块必须保留
        // 所以如果mem2reg是True则删除，否则不删
        readyForDomAnalysis(function);
        genSuccPredBB(function);
        genDomBB(function);
        getIdomBB(function);
        getDF(function);
        this.mem2reg = mem2reg;
    }

    public HashMap<Value, BasicBlock> getLabel2BasicBlock4function() {
        return label2BasicBlock4function;
    }

    public HashMap<BasicBlock, HashSet<BasicBlock>> getSuccessorBB() {
        return successorBB;
    }

    public HashMap<BasicBlock, HashSet<BasicBlock>> getPredecessorBB() {
        return predecessorBB;
    }

    public HashMap<BasicBlock, HashSet<BasicBlock>> getDomBB() {
        return domBB;
    }

    public HashMap<BasicBlock, BasicBlock> getIdomBB() {
        return idomBB;
    }

    public HashMap<BasicBlock, ArrayList<BasicBlock>> getDomTree() {
        return domTree;
    }

    public HashMap<BasicBlock, HashSet<BasicBlock>> getDF() {
        return DF;
    }

    public void readyForDomAnalysis(Function function) {
        label2BasicBlock4function = new HashMap<>();
        successorBB = new HashMap<>();
        predecessorBB = new HashMap<>();
        domBB = new HashMap<>();
        idomBB = new HashMap<>();
        domTree = new HashMap<>();
        DF = new HashMap<>();
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            label2BasicBlock4function.put(basicBlock.getLabel(), basicBlock);
            successorBB.put(basicBlock, new HashSet<>());
            predecessorBB.put(basicBlock, new HashSet<>());
            domBB.put(basicBlock, new HashSet<>());
            idomBB.put(basicBlock, basicBlock);
            domTree.put(basicBlock, new ArrayList<>());
            DF.put(basicBlock, new HashSet<>());
        }
    }

    public void genSuccPredBB(Function function) {
        boolean change = true;
        while (change) {
            change = false;

            for (BasicBlock basicBlock : function.getBasicBlocks()) {
                Iterator<Instruction> instIterator = basicBlock.getInstructions().iterator();
                while (instIterator.hasNext()) {
                    Instruction lastInst = instIterator.next();
                    if (lastInst.getInstructionType() == InstructionType.BR) {
                        // 判断直接跳转还是条件跳转：直接跳转只有一个目的基本块；条件跳转又两个目的基本块
                        if (((BrInst)lastInst).isConditionalBranch()) {
                            Value label1 = lastInst.getOperands().get(1);
                            BasicBlock basicBlock1 = label2BasicBlock4function.get(label1);
                            Value label2 = lastInst.getOperands().get(2);
                            BasicBlock basicBlock2 = label2BasicBlock4function.get(label2);
                            if (label1 == label2) { // 经过删除无用基本块后，可能出现条件跳转指令中两个目标地址相同的情况，此时改成无条件跳转即可
                                Value condition = lastInst.getOperands().get(0);
                                condition.getUserArrayList().remove(lastInst);
                                ((BrInst) lastInst).setConditionalBranch(false);
                                lastInst.getOperands().set(0, label1);
                            }
                            // 添加前驱基本块和后继基本块
                            predecessorBB.get(basicBlock1).add(basicBlock);
                            successorBB.get(basicBlock).add(basicBlock1);
                            predecessorBB.get(basicBlock2).add(basicBlock);
                            successorBB.get(basicBlock).add(basicBlock2);
                        } else {
                            Value label1 = lastInst.getOperands().get(0);
                            BasicBlock basicBlock1 = label2BasicBlock4function.get(label1);
                            // 添加前驱基本块和后继基本块
                            predecessorBB.get(basicBlock1).add(basicBlock);
                            successorBB.get(basicBlock).add(basicBlock1);
                        }
                        while (instIterator.hasNext()) {
                            instIterator.next();
                            instIterator.remove();
                        }
                    }
                }
            }

            Iterator<BasicBlock> iterator = function.getBasicBlocks().iterator();
            while (iterator.hasNext()) {
                BasicBlock basicBlock = iterator.next();
                if (predecessorBB.get(basicBlock).isEmpty() &&
                        basicBlock != function.getBasicBlocks().get(0)) {
                    change = true;
                    iterator.remove();
                }
            }

            iterator = function.getBasicBlocks().iterator();
            while (iterator.hasNext() && mem2reg) {
                BasicBlock basicBlock = iterator.next();
                if (basicBlock.getInstructions().get(0).getInstructionType() == InstructionType.BR &&
                        basicBlock != function.getBasicBlocks().get(0) &&
                        basicBlock.getPhiInstructions().size() == 0) { // only has a branch inst
                    BrInst brInst = (BrInst) basicBlock.getInstructions().get(0);
                    if (!brInst.isConditionalBranch()) {
                        change = true;
                        Value targetLabel = brInst.getOperands().get(0);
                        BasicBlock targetBlock = label2BasicBlock4function.get(targetLabel);
                        for (BasicBlock predBB : predecessorBB.get(basicBlock)) {
                            predecessorBB.get(targetBlock).add(predBB);
                            successorBB.get(predBB).add(targetBlock);
                            Instruction lastInst = predBB.getInstructions().get(predBB.getInstructions().size() - 1);
                            ListIterator<Value> labelIter = lastInst.getOperands().listIterator();
                            while (labelIter.hasNext()) {
                                Value target = labelIter.next();
                                if (target == basicBlock.getLabel()) {
                                    labelIter.set(targetLabel);
                                }
                            }
                        }
                        iterator.remove();
                    }
                }
            }

            if (change) {
                for (BasicBlock basicBlock : function.getBasicBlocks()) {
                    predecessorBB.get(basicBlock).clear();
                    successorBB.get(basicBlock).clear();
                }
            }
        }
    }
    public void genDomBB(Function function) {
        BasicBlock firstBB = function.getBasicBlocks().get(0);
        domBB.get(firstBB).add(firstBB);
        for (int i = 1; i < function.getBasicBlocks().size(); i++) {
            BasicBlock basicBlock = function.getBasicBlocks().get(i);
            domBB.get(basicBlock).addAll(function.getBasicBlocks());
        }
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 1; i < function.getBasicBlocks().size(); i++) {
                HashSet<BasicBlock> temp = new HashSet<>(function.getBasicBlocks());
                BasicBlock basicBlockI = function.getBasicBlocks().get(i);
                for (BasicBlock basicBlockJ : predecessorBB.get(basicBlockI)) {
                    temp.retainAll(domBB.get(basicBlockJ));
                }
                temp.add(basicBlockI);
                temp.stream().sorted(Comparator.comparing(BasicBlock::hashCode));
                domBB.get(basicBlockI).stream().sorted(Comparator.comparing(BasicBlock::hashCode));
                if (!temp.toString().equals(domBB.get(basicBlockI).toString())) {
                    changed = true;
                    domBB.put(basicBlockI, temp);
                }
            }
        }
    }

    public void getIdomBB(Function function) {
        // ! 需要对算法进行修改，改成n2级别的算法
//        for (int y = 1; y < function.getBasicBlocks().size(); y++) {
//            BasicBlock basicBlockY = function.getBasicBlocks().get(y);
//            HashSet<BasicBlock> hashSetY = new HashSet<>(domBB.get(basicBlockY));
//            hashSetY.remove(basicBlockY);
//            for (BasicBlock basicBlockX : hashSetY) {
//                boolean contain = false;
//                for (BasicBlock basicBlockXOther : hashSetY) {
//                    HashSet<BasicBlock> hashSetXOther = new HashSet<>(domBB.get(basicBlockXOther));
//                    hashSetXOther.remove(basicBlockXOther);
//                    if (hashSetXOther.contains(basicBlockX)) {
//                        contain = true;
//                    }
//                }
//                if (!contain) {
//                    idomBB.put(basicBlockY, basicBlockX);
//                    break;
//                }
//            }
//        }
        // * n2级别的算法，用于计算支配树
        for (int y = 1; y < function.getBasicBlocks().size(); y++) {
            BasicBlock basicBlockY = function.getBasicBlocks().get(y);
            HashSet<BasicBlock> basicBlockYDomSet = new HashSet<>(domBB.get(basicBlockY));
            basicBlockYDomSet.remove(basicBlockY);
            int minSize = -1;
            BasicBlock minSizeBasicBlock = basicBlockYDomSet.iterator().next();
            for (BasicBlock basicBlockX : basicBlockYDomSet) {
                if (domBB.get(basicBlockX).size() > minSize) {
                    minSizeBasicBlock = basicBlockX;
                    minSize = domBB.get(basicBlockX).size();
                }
            }
            idomBB.put(basicBlockY, minSizeBasicBlock);
            // 方便之后正序遍历domTree使用
            domTree.get(minSizeBasicBlock).add(basicBlockY);
        }
    }

    public void getDF(Function function) {
        for (BasicBlock basicBlockA : function.getBasicBlocks()) {
            for (BasicBlock basicBlockB : successorBB.get(basicBlockA)) {
                BasicBlock basicBlockX = basicBlockA;
                HashSet<BasicBlock> strictDomB = new HashSet<>(domBB.get(basicBlockB));
                strictDomB.remove(basicBlockB);
                while (!strictDomB.contains(basicBlockX)) {
                    DF.get(basicBlockX).add(basicBlockB);
                    basicBlockX = idomBB.get(basicBlockX);
                }
            }
        }
    }

}
