package BackEnd.Optimize;

import BackEnd.MIPSBlock;
import BackEnd.MIPSFunction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

public class MIPSDomTreeAnalysis {
    private HashMap<String, MIPSBlock> label2Block4function;
    private HashMap<MIPSBlock, HashSet<MIPSBlock>> successorBB;
    private HashMap<MIPSBlock, HashSet<MIPSBlock>> predecessorBB;

    private HashMap<MIPSBlock, HashSet<MIPSBlock>> domBB;
    private HashMap<MIPSBlock, MIPSBlock> idomBB;
    private HashMap<MIPSBlock, ArrayList<MIPSBlock>> domTree;

    private HashMap<MIPSBlock, HashSet<MIPSBlock>> DF;

    public MIPSDomTreeAnalysis(MIPSFunction function) {
        readyForDomAnalysis(function);
        genDomBB(function);
        getIdomBB(function);
        getDF(function);
    }

    public HashMap<MIPSBlock, HashSet<MIPSBlock>> getDomBB() {
        return domBB;
    }

    public HashMap<MIPSBlock, MIPSBlock> getIdomBB() {
        return idomBB;
    }

    public HashMap<MIPSBlock, ArrayList<MIPSBlock>> getDomTree() {
        return domTree;
    }

    public HashMap<MIPSBlock, HashSet<MIPSBlock>> getDF() {
        return DF;
    }

    public void readyForDomAnalysis(MIPSFunction function) {
        MIPSCFGAnalysis mipscfgAnalysis = new MIPSCFGAnalysis(function, false);
        label2Block4function = mipscfgAnalysis.getLabel2Block4function();
        successorBB = mipscfgAnalysis.getSuccessorBB();
        predecessorBB = mipscfgAnalysis.getPredecessorBB();
        domBB = new HashMap<>();
        idomBB = new HashMap<>();
        domTree = new HashMap<>();
        DF = new HashMap<>();
        for (MIPSBlock basicBlock : function.getMipsBlocks()) {
            domBB.put(basicBlock, new HashSet<>());
            idomBB.put(basicBlock, basicBlock);
            domTree.put(basicBlock, new ArrayList<>());
            DF.put(basicBlock, new HashSet<>());
        }
    }

    public void genDomBB(MIPSFunction function) {
        MIPSBlock firstBB = function.getMipsBlocks().get(0);
        domBB.get(firstBB).add(firstBB);
        for (int i = 1; i < function.getMipsBlocks().size(); i++) {
            MIPSBlock basicBlock = function.getMipsBlocks().get(i);
            domBB.get(basicBlock).addAll(function.getMipsBlocks());
        }
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 1; i < function.getMipsBlocks().size(); i++) {
                HashSet<MIPSBlock> temp = new HashSet<>(function.getMipsBlocks());
                MIPSBlock basicBlockI = function.getMipsBlocks().get(i);
                for (MIPSBlock basicBlockJ : predecessorBB.get(basicBlockI)) {
                    temp.retainAll(domBB.get(basicBlockJ));
                }
                temp.add(basicBlockI);
                temp.stream().sorted(Comparator.comparing(MIPSBlock::hashCode));
                domBB.get(basicBlockI).stream().sorted(Comparator.comparing(MIPSBlock::hashCode));
                if (!temp.toString().equals(domBB.get(basicBlockI).toString())) {
                    changed = true;
                    domBB.put(basicBlockI, temp);
                }
            }
        }
    }

    public void getIdomBB(MIPSFunction function) {
        for (int y = 1; y < function.getMipsBlocks().size(); y++) {
            MIPSBlock basicBlockY = function.getMipsBlocks().get(y);
            HashSet<MIPSBlock> basicBlockYDomSet = new HashSet<>(domBB.get(basicBlockY));
            basicBlockYDomSet.remove(basicBlockY);
            int minSize = -1;
            MIPSBlock minSizeBasicBlock = basicBlockYDomSet.iterator().next();
            for (MIPSBlock basicBlockX : basicBlockYDomSet) {
                if (domBB.get(basicBlockX).size() > minSize) {
                    minSizeBasicBlock = basicBlockX;
                    minSize = domBB.get(basicBlockX).size();
                }
            }
            idomBB.put(basicBlockY, minSizeBasicBlock);
            domTree.get(minSizeBasicBlock).add(basicBlockY);
        }
    }

    public void getDF(MIPSFunction function) {
        for (MIPSBlock basicBlockA : function.getMipsBlocks()) {
            for (MIPSBlock basicBlockB : successorBB.get(basicBlockA)) {
                MIPSBlock basicBlockX = basicBlockA;
                HashSet<MIPSBlock> strictDomB = new HashSet<>(domBB.get(basicBlockB));
                strictDomB.remove(basicBlockB);
                while (!strictDomB.contains(basicBlockX)) {
                    DF.get(basicBlockX).add(basicBlockB);
                    basicBlockX = idomBB.get(basicBlockX);
                }
            }
        }
    }
}
