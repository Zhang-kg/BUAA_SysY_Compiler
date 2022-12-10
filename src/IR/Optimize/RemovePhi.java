package IR.Optimize;

import IR.Module;
import SysYTokens.Block;

import java.util.HashMap;
import java.util.HashSet;

public class RemovePhi {
    // 消除phi
    private Module llvmModule = Module.getMyModule();
    private HashMap<Block, HashSet<String>> liveIn;
    private HashMap<Block, HashSet<String>> liveOut;
    private HashMap<Block, HashSet<String>> def;
    private HashMap<Block, HashSet<String>> use;


    public RemovePhi() {

    }

    public void ReplacePhiNodes() {

    }

    public void insertCopies() {

    }

    public void schedule_copies() {

    }
}
