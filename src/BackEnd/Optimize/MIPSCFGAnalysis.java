package BackEnd.Optimize;

import BackEnd.Instr.MIPSBranch;
import BackEnd.MIPSBlock;
import BackEnd.MIPSFunction;
import BackEnd.MIPSInstruction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class MIPSCFGAnalysis {
    private HashMap<String, MIPSBlock> label2Block4function;
    private HashMap<MIPSBlock, HashSet<MIPSBlock>> successorBB;
    private HashMap<MIPSBlock, HashSet<MIPSBlock>> predecessorBB;

    private boolean removeRedundant;

    public MIPSCFGAnalysis(MIPSFunction function, boolean removeRedundant) {
        this.removeRedundant = removeRedundant;
        readyForCFGAnalysis(function);
        genSuccPredBB(function);
    }

    public void readyForCFGAnalysis(MIPSFunction function) {
        label2Block4function = new HashMap<>();
        successorBB = new HashMap<>();
        predecessorBB = new HashMap<>();
        for (MIPSBlock block : function.getMipsBlocks()) {
            label2Block4function.put(block.getName(), block);
            successorBB.put(block, new HashSet<>());
            predecessorBB.put(block, new HashSet<>());
        }
    }

    public void genSuccPredBB(MIPSFunction function) {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (MIPSBlock block : function.getMipsBlocks()) {
                Iterator<MIPSInstruction> instIterator = block.getMipsInstructions().iterator();
                while (instIterator.hasNext()) {
                    MIPSInstruction lastInst = instIterator.next();
                    if (lastInst instanceof MIPSBranch) {
                        String targetLabelName = ((MIPSBranch) lastInst).getTargetLabelName();
                        if (!label2Block4function.containsKey(targetLabelName)) continue;
                        MIPSBlock block1 = label2Block4function.get(targetLabelName);
                        predecessorBB.get(block1).add(block);
                        successorBB.get(block).add(block1);
                    }
                }
            }

            Iterator<MIPSBlock> iterator = function.getMipsBlocks().iterator();
            while (iterator.hasNext()) {
                MIPSBlock block = iterator.next();
                if (predecessorBB.get(block).isEmpty() && block != function.getMipsBlocks().get(0)) {
                    changed = true;
                    iterator.remove();
                }
            }

            iterator = function.getMipsBlocks().iterator();
            while (iterator.hasNext() && removeRedundant) {
                MIPSBlock block = iterator.next();
                // 先不去除了
            }

            if (changed) {
                for (MIPSBlock block : function.getMipsBlocks()) {
                    predecessorBB.get(block).clear();
                    successorBB.get(block).clear();
                }
            }
        }
    }

    public HashMap<MIPSBlock, HashSet<MIPSBlock>> getPredecessorBB() {
        return predecessorBB;
    }

    public HashMap<MIPSBlock, HashSet<MIPSBlock>> getSuccessorBB() {
        return successorBB;
    }

    public HashMap<String, MIPSBlock> getLabel2Block4function() {
        return label2Block4function;
    }
}
