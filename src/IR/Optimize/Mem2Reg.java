package IR.Optimize;

import IR.GenerateModule;
import IR.Module;
import IR.Values.BasicBlock;
import IR.Values.ConstantIR.ConstantInteger;
import IR.Values.Function;
import IR.Values.InstructionIR.*;
import IR.Values.InstructionIR.TerminatorIR.BrInst;
import IR.Values.InstructionIR.TerminatorIR.CallInst;
import IR.Values.User;
import IR.Values.Value;
import IR.types.IntType;

import java.util.*;

public class Mem2Reg {
    private Module llvmModule = Module.getMyModule();
    // * label to basic block for a function
    private HashMap<Value, BasicBlock> label2BasicBlock4function;   // 用label获取BasicBlock
    private HashMap<BasicBlock, HashSet<BasicBlock>> successorBB;   // 一个BasicBlock的后继基本块
    private HashMap<BasicBlock, HashSet<BasicBlock>> predecessorBB; // 一个BasicBlock的前驱基本块
    private HashMap<BasicBlock, HashSet<BasicBlock>> domBB;         // 一个BasicBlock的dom基本块，dom集合中的基本块都dominate这个基本块
    private HashMap<BasicBlock, BasicBlock> idomBB;                 // 一个基本块的idom，后者直接支配前者
    private HashMap<BasicBlock, ArrayList<BasicBlock>> domTree;     // dom Tree顺序，正序关系，得到的是domTree上基本块的子基本块
    private HashMap<BasicBlock, HashSet<BasicBlock>> DF;            // DF
    private ArrayList<String> allocVariable;                        // 这是所有alloc的变量的名字
    private HashMap<String, HashSet<BasicBlock>> defVariableBB;     // 这是定义variable的所有基本块，可以从名字找到所有定义这个变量的基本块
//    private HashMap<String, Value> reachingDef;                      // reachingDef关系，v-r 关系，表示最后一次对v定义的value是r
    private HashMap<Value, BasicBlock> valueBBMap;                  // 表示定义的一个value对应的BasicBlock，用来判断需要回退多少的
    // * renaming阶段遍历时判断是否遍历到
    private HashMap<BasicBlock, Boolean> visited;

    private HashSet<String> globalIdentSet = GenerateModule.getGlobalIdentSet();
    private HashSet<String> bareIdentSet = GenerateModule.getBareIdentSet();

    public String addName(String name) {
        String bareName = name.substring(1);
        String aftName = name + "_m2g";
        String aftBareName = bareName + "_m2g";
        if (globalIdentSet.contains(aftName) || bareIdentSet.contains(aftBareName)) {
            int i = 1;
            while (globalIdentSet.contains(aftName) || bareIdentSet.contains(aftBareName)) {
                aftName = name + "_m2g" + i;
                aftBareName = bareName + "_m2g" + i;
                i++;
            }

        }
        globalIdentSet.add(aftName);
        bareIdentSet.add(aftBareName);
        return aftName;
    }

    public Mem2Reg() {
        for (Function function : llvmModule.getFunctions()) {
            readyForMem2Reg(function);
            genSuccPredBB(function);

            genDomBB(function);
            getIdomBB(function);
            getDF(function);
            getAllocVariable(function);
            getDefVariableBB(function);
            insertPhiInst(function);
            variablesRenaming(function);
        }
    }



    public void readyForMem2Reg(Function function) {
        /*
            进行mem2reg的准备操作，包括
            - 初始化label to basicBlock for a function
            - 初始化前驱后继map
            - 初始化支配集合dom
            - 初始化直接支配者idomBasicBlock
            - 初始化DomTree
            - 初始化支配边界DF
            - 初始化获得函数中alloc的变量（不包括数组）
            - 遍历变量，获得初始化所有alloc变量的基本块def(v)
            - reachingDef关系
            - 每个value定义的instruction
         */
        label2BasicBlock4function = new HashMap<>();
        successorBB = new HashMap<>();
        predecessorBB = new HashMap<>();
        domBB = new HashMap<>();
        idomBB = new HashMap<>();
        domTree = new HashMap<>();
        DF = new HashMap<>();
        allocVariable = new ArrayList<>();
        defVariableBB = new HashMap<>();
        valueBBMap = new HashMap<>();
//        reachingDef = new HashMap<>();
        visited = new HashMap<>();
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
            HashSet<BasicBlock> removeBB = new HashSet<>();
            while (iterator.hasNext()) {
                BasicBlock basicBlock = iterator.next();
                if (predecessorBB.get(basicBlock).isEmpty() && basicBlock != function.getBasicBlocks().get(0)) {
                    change = true;
                    removeBB.add(basicBlock);
                    iterator.remove();
                }
            }
            if (change) {
                for (BasicBlock basicBlock : function.getBasicBlocks()) {
                    predecessorBB.get(basicBlock).removeAll(removeBB);
                    successorBB.get(basicBlock).removeAll(removeBB);
                }
            }
        }

    }

    public void genDomBB(Function function) {
        BasicBlock firstBB = function.getBasicBlocks().get(0);
        domBB.get(firstBB).add(firstBB);
        for (int i = 1; i < function.getBasicBlocks().size(); i++) {
            BasicBlock basicBlock = function.getBasicBlocks().get(i);
//            if (predecessorBB.get(basicBlock).isEmpty()) {
//                domBB.get(basicBlock).add(basicBlock);
//            } else {
                domBB.get(basicBlock).addAll(function.getBasicBlocks());
//            }
        }
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 1; i < function.getBasicBlocks().size(); i++) {
//                HashSet<BasicBlock> temp = new HashSet<>();
                HashSet<BasicBlock> temp = new HashSet<>(function.getBasicBlocks());
                BasicBlock basicBlockI = function.getBasicBlocks().get(i);
//                temp.add(basicBlockI);
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
//        for (BasicBlock basicBlock : function.getBasicBlocks()) {
//            if (predecessorBB.get(basicBlock).isEmpty()) {
//
//            }
//        }
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
        // * n2级别算法
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

    public void getAllocVariable(Function function) {
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            for (Instruction instruction : basicBlock.getInstructions()) {
                if (instruction instanceof AllocaInst) {
                    if (!((AllocaInst) instruction).getAllocatedType().isArrayType()) {
                        allocVariable.add(instruction.getName());
                        defVariableBB.put(instruction.getName(), new HashSet<>());
                    }
                }
            }
        }
    }

    public void getDefVariableBB(Function function) {
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            for (Instruction instruction : basicBlock.getInstructions()) {
                if (instruction instanceof StoreInst) {
                    String targetName = instruction.getOperands().get(1).getName();
                    if (allocVariable.contains(targetName)) {
                        defVariableBB.get(targetName).add(basicBlock);
                    }
                }
            }
        }
    }

    public void insertPhiInst(Function function) {
        for (String v : allocVariable) {
            HashSet<BasicBlock> F = new HashSet<>();        // set of basic blocks where phi is added
            HashSet<BasicBlock> W = new HashSet<>(defVariableBB.get(v));   // set of basic blocks that contain definitions of v
            while (W.size() != 0) {
                Iterator<BasicBlock> it = W.iterator();
                BasicBlock X = it.next();
                it.remove();
                for (BasicBlock Y : DF.get(X)) {
                    if (!F.contains(Y)) {
                        // 新创建的Phi指令会插入到Y的phiInstructions数组中
                        new PhiInst(Y, IntType.i32, v);
                        F.add(Y);
                        if (!defVariableBB.get(v).contains(Y)) {
                            W.add(Y);
                        }
                    }
                }
            }
        }
    }

    public void variablesRenaming(Function function) {
        HashMap<String, Value> reachingDef = new HashMap<>();
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            visited.put(basicBlock, false);
            for (Instruction instruction : basicBlock.getInstructions()) {
                if (instruction instanceof AllocaInst) {
                    if (!((AllocaInst)instruction).getAllocatedType().isArrayType()) {
                        reachingDef.put(instruction.getName(), ConstantInteger.zero);
                    }
                } else if (instruction instanceof LoadInst) {
                    if (allocVariable.contains(instruction.getOperands().get(0).getName())) {
                        reachingDef.put(instruction.getOperands().get(0).getName(), ConstantInteger.zero);
                    }
                }
            }
        }

        BasicBlock basicBlock0 = function.getBasicBlocks().get(0);
        renaming(basicBlock0, reachingDef);
        /*
        // 按照支配树前序遍历顺序
        ArrayList<BasicBlock> dfsBB = dfsPreorderTraversal(basicBlock0);
        for (BasicBlock basicBlock : dfsBB) {
            for (PhiInst phiInst : basicBlock.getPhiInstructions()) {
                // 可能在这里被重定义，需要进行重命名
                // 方法和StoreInst相同
                String originName = phiInst.getName();
                if (allocVariable.contains(originName)) {
                    updateReachingDef(originName, phiInst, basicBlock);
                    String aftName = addName(originName);
                    reachingDef.put(aftName, reachingDef.get(originName));
                    reachingDef.put(originName, phiInst);
                    valueBBMap.put(phiInst, basicBlock);
                    phiInst.setName(aftName);
                }
            }
            Iterator<Instruction> iterator = basicBlock.getInstructions().iterator();
            while (iterator.hasNext()) {
                Instruction i = iterator.next();
                if (i instanceof LoadInst) {
                    // load 指令是变量被使用的地方
                    // 因此需要更新reachingDef
                    // 并将此处对于变量v的使用改成v的reachingDef
                    String originName = i.getOperands().get(0).getName();
                    if (allocVariable.contains(originName)) {   // 只有alloc的变量才能继续执行
                        updateReachingDef(originName, i, basicBlock);
                        String loadOriginName = i.getName();
                        ArrayList<User> users = i.getUserArrayList();
                        for (User user : users) {   // 获得使用这个load的所有user
                            // 需要把user中所有使用load的地方改成reachingDef
                            ListIterator<Value> originValueIterator = user.getOperands().listIterator();
                            while (originValueIterator.hasNext()) { // 将user中所有使用到load的地方换成新的value
                                Value originValue = originValueIterator.next();
                                if (originValue.getName().equals(loadOriginName)) {
                                    originValueIterator.set(reachingDef.get(originName));
                                }
                            }
                        }
                        iterator.remove();
                    }
                } else if (i instanceof StoreInst) {
                    // store 指令是变量被重定义的地方
                    // 需要更新reachingDef
                    // 创建新变量v‘
                    // 把对于v的定义改成v’
                    // v‘的reachingDef改成v的reachingDef
                    // v的reachingDef改成v’
                    String originName = i.getOperands().get(1).getName();
                    if (allocVariable.contains(originName)) {   // 同样只有alloc的变量才能继续执行
                        updateReachingDef(originName, i, basicBlock);
                        String aftName = addName(originName);
                        reachingDef.put(aftName, reachingDef.get(originName));
//                        reachingDef.put(originName, phiInst);
//                        phiInst.setName(aftName);
                        reachingDef.put(originName, i.getOperands().get(0));
                        valueBBMap.put(i.getOperands().get(0), basicBlock);
                        iterator.remove();
                    }
                } else if (i instanceof AllocaInst) {
                    iterator.remove();
                }
            }
            // 遍历后继基本块的phi指令
            // 将phi中使用v的地方：先更新v的reachingDef，再将v的使用换成v的reachingDef
            for (BasicBlock succBB : successorBB.get(basicBlock)) {
                for (PhiInst phiInst : succBB.getPhiInstructions()) {
                    String originName = phiInst.getName();
                    if (allocVariable.contains(originName)) {
                        updateReachingDef(originName, phiInst, succBB);
                        phiInst.addOperand(reachingDef.get(originName));
                    }
                }
            }
        }
         */
    }

    public void renaming(BasicBlock basicBlock, HashMap<String, Value> rchDef) {
        if (visited.get(basicBlock)) {
            return;
        } else {
            visited.put(basicBlock, true);
        }
        if (basicBlock.getLabel().getName().equals("%Label_14")) {
            int a = 1;
        }
//        System.out.println(basicBlock.getLabel().getName());
//        if (basicBlock.getLabel().getName().equals("%Label_9")) {
//            System.out.println(1);
//        }
        for (PhiInst phiInst : basicBlock.getPhiInstructions()) {
            String originName = phiInst.getOriginVariableName();
            if (allocVariable.contains(originName)) {
                String aftName = addName(originName);
                rchDef.put(originName, phiInst);
                phiInst.setName(aftName);
            }
        }
        Iterator<Instruction> iterator = basicBlock.getInstructions().iterator();
        while (iterator.hasNext()) {
            Instruction i = iterator.next();
            if (i instanceof LoadInst) {
                // load 指令是变量被使用的地方
                // 因此需要更新reachingDef
                // 并将此处对于变量v的使用改成v的reachingDef
                String originName = i.getOperands().get(0).getName();
                if (allocVariable.contains(originName)) {   // 只有alloc的变量才能继续执行
                    String loadOriginName = i.getName();
                    ArrayList<User> users = i.getUserArrayList();
                    for (User user : users) {   // 获得使用这个load的所有user
                        // 需要把user中所有使用load的地方改成reachingDef
                        ListIterator<Value> originValueIterator = ((Instruction) user instanceof CallInst)?
                                ((CallInst) user).getArguments().listIterator() :
                                user.getOperands().listIterator();
                        while (originValueIterator.hasNext()) { // 将user中所有使用到load的地方换成新的value
                            Value originValue = originValueIterator.next();
                            if (originValue.getName().equals(loadOriginName)) {
                                originValueIterator.set(rchDef.get(originName));
                                rchDef.get(originName).addUser(user);
                            }
                        }
                    }
                    iterator.remove();
                }
            } else if (i instanceof StoreInst) {
                // store 指令是变量被重定义的地方
                // 需要更新reachingDef
                // 创建新变量v‘
                // 把对于v的定义改成v’
                // v‘的reachingDef改成v的reachingDef
                // v的reachingDef改成v’
                String originName = i.getOperands().get(1).getName();
                if (allocVariable.contains(originName)) {   // 同样只有alloc的变量才能继续执行
                    rchDef.put(originName, i.getOperands().get(0));
                    iterator.remove();
                }
            } else if (i instanceof AllocaInst) {
                String originName = i.getName();
                if (allocVariable.contains(originName)) {
                    iterator.remove();
                }
            }
        }
        if (basicBlock.getLabel().getName().equals("%Label_14")) {
            int i = 1;
        }
        for (BasicBlock succBB : successorBB.get(basicBlock)) {
            for (PhiInst phiInst : succBB.getPhiInstructions()) {
                String originName = phiInst.getOriginVariableName();
                if (allocVariable.contains(originName)) {
                    if (rchDef.get(originName) != null) {
                        phiInst.addPhiOperand(rchDef.get(originName), basicBlock);
                        rchDef.get(originName).addUser(phiInst);
                    } else {
                        phiInst.addPhiOperand(ConstantInteger.zero, basicBlock);
                        rchDef.put(originName, ConstantInteger.zero);
                        rchDef.get(originName).addUser(phiInst);
                    }
                }
            }
            HashMap<String , Value> rchDefN = new HashMap<>(rchDef);
            renaming(succBB, rchDefN);
        }

    }

    public ArrayList<BasicBlock> dfsPreorderTraversal(BasicBlock rootBB) {
        ArrayList<BasicBlock> ans = new ArrayList<>();
        ans.add(rootBB);
        for (BasicBlock basicBlock : domTree.get(rootBB)) {
            ans.addAll(dfsPreorderTraversal(basicBlock));
        }
        return ans;
    }

//    public void updateReachingDef(String v, Instruction i, BasicBlock basicBlock) {
//        Value r = reachingDef.get(v);
//        while (!(r == null || domBB.get(basicBlock).contains(valueBBMap.get(r)))) {
//            r = reachingDef.get(r.getName());
//        }
//        reachingDef.put(v, r);
//    }
}
