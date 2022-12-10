package IR.Optimize;

import FileIO.LLVMTreePrinter;
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
        DomTreeAnalysis domTreeAnalysis = new DomTreeAnalysis(function);
        label2BasicBlock4function = domTreeAnalysis.getLabel2BasicBlock4function();
        successorBB = domTreeAnalysis.getSuccessorBB();
        predecessorBB = domTreeAnalysis.getPredecessorBB();
        domBB = domTreeAnalysis.getDomBB();
        idomBB = domTreeAnalysis.getIdomBB();
        domTree = domTreeAnalysis.getDomTree();
        DF = domTreeAnalysis.getDF();
        allocVariable = new ArrayList<>();
        defVariableBB = new HashMap<>();
        valueBBMap = new HashMap<>();
        visited = new HashMap<>();
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
    }

    public void renaming(BasicBlock basicBlock, HashMap<String, Value> rchDef) {
        if (visited.get(basicBlock)) {
            return;
        } else {
            visited.put(basicBlock, true);
        }
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
}
