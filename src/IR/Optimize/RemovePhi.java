package IR.Optimize;

import IR.Module;
import IR.Values.BasicBlock;
import IR.Values.ConstantIR.ConstantInteger;
import IR.Values.Function;
import IR.Values.InstructionIR.Instruction;
import IR.Values.InstructionIR.PhiInst;
import IR.Values.InstructionIR.StoreInst;
import IR.Values.InstructionIR.TerminatorIR.CallInst;
import IR.Values.Value;
import SysYTokens.Block;

import java.util.*;

public class RemovePhi {
    // 消除phi
    private Module llvmModule = Module.getMyModule();
    private HashMap<BasicBlock, HashSet<String>> liveIn;
    private HashMap<BasicBlock, HashSet<String>> liveOut;
    private HashMap<BasicBlock, HashSet<String>> def;
    private HashMap<BasicBlock, HashSet<String>> use;

    private HashSet<Value> allVariablesValue;   // 伪代码主循环中需要的变量信息
    private DomTreeAnalysis domTreeAnalysis;    // 当前函数的domTree分析
    private LiveAnalysis liveAnalysis;          // 当前函数的活跃变量分析

    private HashMap<Value, Stack<Value>> variableStack;   // 论文提出的Stack
    private HashMap<BasicBlock, ArrayList<BasicBlock>> domTree; // 支配树

    private ArrayList<Value> globalPushed;

    private static int nameNum = 0;

    private String tempAllocName() {
        return "temp_mem" + nameNum++;
    }

    public RemovePhi() {
        // 消除phi函数
        for (Function function : llvmModule.getFunctions()) {
            ReplacePhiNodes(function);
        }
    }

    public void ReplacePhiNodes(Function function) {
        // Perform live analysis
        this.domTreeAnalysis = new DomTreeAnalysis(function, false);
        this.liveAnalysis = new LiveAnalysis(function, domTreeAnalysis.getSuccessorBB());
        liveOut = liveAnalysis.getLiveOut();
        liveIn = liveAnalysis.getLiveIn();
        def = liveAnalysis.getDef();
        use = liveAnalysis.getUse();
        this.allVariablesValue = getAllVariables(function);
        // For each variable v
            // Stack[v] <= emptyStack()
        variableStack = new HashMap<>();
        for (Value variable : allVariablesValue) {
            variableStack.put(variable, new Stack<>());
        }
        // insert_copies(start)
        this.domTree = domTreeAnalysis.getDomTree();
        insertCopies(function.getBasicBlocks().get(0));
    }

    private HashSet<Value> getAllVariables(Function function) {
        // 我从原论文和文中的伪代码感觉，这里只需要统计一下所有的phi指令定义的变量就行
        HashSet<Value> ans = new HashSet<>();
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            ans.addAll(basicBlock.getPhiInstructions());
        }
        return ans;
    }

    public void insertCopies(BasicBlock basicBlock) {
        // pushed <= emptySet
        ArrayList<Value> pushed = new ArrayList<>();
        globalPushed = pushed;
        // For all instruction i in block
            // Replace all uses u with Stacks[u]
        for (PhiInst phiInst : basicBlock.getPhiInstructions()) {
            ListIterator<Value> listIterator = phiInst.getOperands().listIterator();
            while (listIterator.hasNext()) {
                Value operand = listIterator.next();
                if (!(operand instanceof ConstantInteger)) {
                    Value stackV = getStackV(operand);
                    if (operand.getName().equals(stackV.getName())) continue;
                    listIterator.set(stackV);
                    stackV.addUser(phiInst);
                }
            }
        }
        for (Instruction inst : basicBlock.getInstructions()) {
            ListIterator<Value> listIterator;
            if (inst instanceof CallInst) {
                listIterator = ((CallInst) inst).getArguments().listIterator();
            } else {
                listIterator = inst.getOperands().listIterator();
            }
            while (listIterator.hasNext()) {
                Value operand = listIterator.next();
                if (!(operand instanceof ConstantInteger)) {
                    Value stackV = getStackV(operand);
                    if (operand.getName().equals(stackV.getName())) continue;
                    listIterator.set(stackV);
                    stackV.addUser(inst);
                }
            }
        }
        // schedule_copies(block)
        schedule_copies(basicBlock);
        // For each child c of block in the dominator tree
            // insert_copies(c)
        for (BasicBlock c : domTree.get(basicBlock)) {
            insertCopies(c);
        }
        // For each name n in pushed
            // pop(Stack[n])
        for (Value value : pushed) {
            popStackV(value);
        }
    }

    public void schedule_copies(BasicBlock basicBlock) {
        /* Pass One: Initialize the data structures */
        // copy_set <= emptySet
        HashSet<CopyPair> copySet = new HashSet<>();
        ArrayList<CopyPair> workList = new ArrayList<>();
        HashMap<Value, Value> map = new HashMap<>();
        HashMap<Value, Boolean> used_by_another = new HashMap<>();
        // For all successors s of block
            // j <= whichPred(s, block)
            // For each phi-function dest = phi(...) in s
                // src <= j^th operand of phi-function
                // copy_set <= copy_set U <src, dest>
                // map[src] <= src
                // map[dest] <= dest
                // used_by_another[src] <= TRUE
        for (BasicBlock succBB : domTreeAnalysis.getSuccessorBB().get(basicBlock)) {
            for (PhiInst phiInst : succBB.getPhiInstructions()) {
                Value dest = phiInst;
                int j = phiInst.getBbList().indexOf(basicBlock);
                Value src = phiInst.getOperands().get(j);
                copySet.add(new CopyPair(src, dest));
                used_by_another.put(src, true);
            }
        }
        /* Pass Two: Set up the work_list of initial copies */
        // For each copy <src, dest> in copy_set
            // if not used_by_another[dest]
                // workList <= workList U <src, dest>
                // copySet <= copySet - <src, dest>
        Iterator<CopyPair> iterator = copySet.iterator();
        while (iterator.hasNext()) {
            CopyPair copyPair = iterator.next();
            if (!used_by_another.getOrDefault(copyPair.dest, false)) {
                workList.add(copyPair);
                iterator.remove();
            }
        }
        /* Pass Three: Iterate over the workList, inserting copies */



        // While workList != empty or copy_list != empty
        while (workList.size() != 0 || copySet.size() != 0) {
            // While workList != empty
            while (workList.size() != 0) {
                // Pick a <src, dest> from workList
                // workList <= workList - <src, dest>
                CopyPair copyPair = workList.remove(0);
                // if dest in LiveOut(block)
                if (liveOut.get(basicBlock).contains(copyPair.dest.getName())) {
                    // Insert a copy from dest to a new temp t at phi-node defining dest
                    // push(t, Stacks[dest])
                    Value temp = new Value(copyPair.dest.getType(), tempAllocName());
                    MoveInst moveInst = new MoveInst(basicBlock, copyPair.dest.getType(), temp.getName(),
                            copyPair.dest, temp);
                    basicBlock.getInstructions().add(0, moveInst);
                    pushStackV(temp, copyPair.dest);
                }
                // Insert a copy operation from mat[src] to dest at the end of b
                MoveInst moveInst = new MoveInst(basicBlock, copyPair.dest.getType(), copyPair.dest.getName(),
                        map.getOrDefault(copyPair.src, copyPair.src), copyPair.dest);
//                basicBlock.addInstruction(moveInst);
                basicBlock.getInstructions().add(basicBlock.getInstructions().size() - 1, moveInst);
                // map[src] <= dest
                map.put(copyPair.src, copyPair.dest);
                // If src is the name of a destination in copy_set
                // Add that copy to workList
                Iterator<CopyPair> copySetIterator = copySet.iterator();
                while (copySetIterator.hasNext()) {
                    CopyPair copyPairInCopySet = copySetIterator.next();
                    if (copyPairInCopySet.dest.getName().equals(copyPair.src.getName())) {
                        copySetIterator.remove();
                        workList.add(copyPairInCopySet);
                        break;
                    }
                }
            }
            // If copy_set != empty
            if (copySet.size() != 0) {
                // Pick a <src, dest> from copy_set
                // copy_set <= copy_set - <src, dest>
                CopyPair copyPair = copySet.iterator().next();
                copySet.remove(copyPair);
                // Insert a copy from dest to a new temp t at the end of block
                Value temp = new Value(copyPair.dest.getType(), tempAllocName());
                MoveInst moveInst = new MoveInst(basicBlock, copyPair.dest.getType(), temp.getName(), copyPair.dest, temp);
                basicBlock.addInstruction(moveInst);
                // map[dest] <= t
                map.put(copyPair.dest, temp);
                // workList <= workList U <src, dest>
                workList.add(copyPair);
            }
        }
    }

    // 这是对于论文中Stack的实现，llvm比较特殊
    public Value getStackV(Value value) {
        if (variableStack.containsKey(value)) {
            if (variableStack.get(value).isEmpty()) {
                return value;
            }
            return variableStack.get(value).peek();
        } else {
            variableStack.put(value, new Stack<>());
            return value;
        }
    }

    public void pushStackV(Value temp, Value dest) {
        globalPushed.add(dest);
        if (!variableStack.containsKey(dest)) {
            variableStack.put(dest, new Stack<>());
        }
        variableStack.get(dest).push(temp);
    }

    public void popStackV(Value value) {
        if (variableStack.containsKey(value)) {
            if (variableStack.get(value).size() != 0) {
                variableStack.get(value).pop();
            }
        }
    }
}
