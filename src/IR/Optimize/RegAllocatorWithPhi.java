package IR.Optimize;

import IR.Module;
import IR.Values.BasicBlock;
import IR.Values.Function;
import IR.Values.InstructionIR.PhiInst;

import java.util.ArrayList;
import java.util.HashMap;

public class RegAllocatorWithPhi {
    private static HashMap<String, String> var2reg = new HashMap<>();
    private static HashMap<String, String> reg2var = new HashMap<>();

    public RegAllocatorWithPhi() {


        for (Function function : Module.getMyModule().getFunctions()) {
            DomTreeAnalysis domTreeAnalysis = new DomTreeAnalysis(function, false);
            LiveAnalysis liveAnalysis = new LiveAnalysis(function, domTreeAnalysis.getSuccessorBB());
            HashMap<BasicBlock, ArrayList<BasicBlock>> domTree = domTreeAnalysis.getDomTree();

        }
    }

    public void regAllocate(BasicBlock basicBlock, LiveAnalysis liveAnalysis) {
        for (PhiInst phiInst : basicBlock.getPhiInstructions()) {
            String varDef = phiInst.getName();

        }
    }
}
