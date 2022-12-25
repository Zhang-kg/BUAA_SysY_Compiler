package BackEnd;

import BackEnd.Instr.*;
import BackEnd.Instr.InstructionType;
import BackEnd.SymbolTableForMIPS.SymbolForMIPS;
import BackEnd.SymbolTableForMIPS.SymbolTypeForMIPS;
import FileIO.FilePrinter;
import IR.Module;
import IR.Optimize.MoveInst;
import IR.SymbolTableForIR.SymbolForIR;
import IR.Values.BasicBlock;
import IR.Values.ConstantIR.ConstantInteger;
import IR.Values.ConstantIR.ConstantString;
import IR.Values.Function;
import IR.Values.InstructionIR.*;
import IR.Values.InstructionIR.TerminatorIR.BrInst;
import IR.Values.InstructionIR.TerminatorIR.CallInst;
import IR.Values.InstructionIR.TerminatorIR.RetInst;
import IR.Values.Value;
import IR.types.ArrayType;
import IR.types.PointerType;
import IR.types.Type;
import IR.types.VoidType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GenerateMIPS2 {
    private Module llvmModule = Module.getMyModule();
    private FilePrinter filePrinter = FilePrinter.getFilePrinter();
    private static int instrPosition = 0;
    private HashMap<String, SymbolForMIPS> symbolTable = new HashMap<>();
    private HashMap<String, RegAllocation> functionRegAllocation = new HashMap<>();
    private boolean isGlobal = true;
    private HashMap<String, VirtualReg> globalStringVirtualRegHashMap = new HashMap<>();
    private HashMap<String, VirtualReg> functionStringVirRegHashMap = null;
    public static HashMap<String, VirtualReg> allPhysicalRegs = new HashMap<>();
    private MIPSBlock currentBlock = null;
//    private MIPSFunction currentFunction = null;

    // 全局变量的名称,这些不参与活跃变量分析
    private static ArrayList<String> stackVariablesName = new ArrayList<>();

    public GenerateMIPS2() {
        // * 设置所有物理寄存器
        {
            allPhysicalRegs.put("$0", new VirtualReg("$0", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$at", new VirtualReg("$at", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$v0", new VirtualReg("$v0", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$v1", new VirtualReg("$v1", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$a0", new VirtualReg("$a0", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$a1", new VirtualReg("$a1", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$a2", new VirtualReg("$a2", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$a3", new VirtualReg("$a3", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$t0", new VirtualReg("$t0", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$t1", new VirtualReg("$t1", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$t2", new VirtualReg("$t2", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$t3", new VirtualReg("$t3", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$t4", new VirtualReg("$t4", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$t5", new VirtualReg("$t5", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$t6", new VirtualReg("$t6", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$t7", new VirtualReg("$t7", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$s0", new VirtualReg("$s0", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$s1", new VirtualReg("$s1", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$s2", new VirtualReg("$s2", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$s3", new VirtualReg("$s3", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$s4", new VirtualReg("$s4", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$s5", new VirtualReg("$s5", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$s6", new VirtualReg("$s6", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$s7", new VirtualReg("$s7", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$t8", new VirtualReg("$t8", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$t9", new VirtualReg("$t9", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$k0", new VirtualReg("$k0", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$k1", new VirtualReg("$k1", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$gp", new VirtualReg("$gp", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$sp", new VirtualReg("$sp", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$fp", new VirtualReg("$fp", SymbolTypeForMIPS.PhysicsReg));
            allPhysicalRegs.put("$ra", new VirtualReg("$ra", SymbolTypeForMIPS.PhysicsReg));
        }
        // * 获得所有寄存器，并分配
        getVirtualRegFromIRModule(llvmModule);
        // * 进行指令翻译
        parseIRModule(llvmModule);
        // * 获得结果
        String ans = MIPSModule.getMipsModule().genMIPSFromMIPSModule();
//        System.out.println(ans);
        filePrinter.outPrintlnMIPS(ans);

        for (Map.Entry<String, VirtualReg> entry : globalStringVirtualRegHashMap.entrySet()) {
            System.out.println(entry.getKey() + "\t" + entry.getValue().getSymbolType().toString() + "\t" + entry.getValue().getStackOffset());
        }
    }

    // ! 获得所有的Virtual Reg
    private void getVirtualRegFromIRModule(Module module) {
        MIPSModule mipsModule = MIPSModule.getMipsModule();
        // * Virtual Reg from String
        ArrayList<ConstantString> globalStrings = llvmModule.getConstantStrings();
        for (ConstantString globalString : globalStrings) {
            String stringName = globalString.getName().substring(1);
            getVirtualRegInterface(stringName, SymbolTypeForMIPS.GlobalString);
            // 添加全局变量名称, 这些不分配寄存器
            stackVariablesName.add(stringName);
        }
        // * Virtual Reg from variables
        ArrayList<SymbolForIR> globalVariables = llvmModule.getGlobalVariables();
        for (SymbolForIR symbol : globalVariables) {
            String symbolName = symbol.getAftName().substring(1);
            getVirtualRegInterface(symbolName, SymbolTypeForMIPS.GlobalVariable);
            // 添加全局变量名称, 这些不分配寄存器
            stackVariablesName.add(symbolName);
        }
        // * Virtual Reg from Functions
        for (Function function : module.getFunctions()) {
            isGlobal = false;
            getVirtualRegFromIRFunction(function);
            isGlobal = true;
        }
    }

    private void getVirtualRegFromIRFunction(Function function) {
        // ? 获得Function内部的虚拟寄存器
        // * 函数内部的寄存器分配
        RegAllocation regAllocation;
        if (Module.isNoColor()) {
            regAllocation = new RegAllocation();
        } else {
            regAllocation = new RegAllocationColor();
            ((RegAllocationColor) regAllocation).setStackVariablesName(stackVariablesName);
        }
//        RegAllocation regAllocation = new RegAllocation();
//        RegAllocationColor regAllocation = new RegAllocationColor();
//        regAllocation.setStackVariablesName(stackVariablesName);
        HashMap<String, VirtualReg> stringVirtualRegHashMap = new HashMap<>();
        functionStringVirRegHashMap = stringVirtualRegHashMap;
        // * 获得传参虚拟寄存器
        ArrayList<VirtualReg> inputParams = getVirtualRegFromFuncParams(function);
        regAllocation.setInputParams(inputParams);
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            // * 将解析得到的Block加入Function中
            getVirtualRegFromIRBlock(basicBlock);
        }
        // * 给函数内部的regAllocation传入所有虚拟寄存器对应表
        regAllocation.setNameToRegMap(stringVirtualRegHashMap);
        if (Module.isNoColor()) {
            regAllocation.allocateReg();
        }
//        regAllocation.allocateReg();
        // * 总表中添加本函数的regAllocation
        functionRegAllocation.put(function.getName().substring(1), regAllocation);
    }

    private void getVirtualRegFromIRBlock(BasicBlock basicBlock) {
        // ? 获得Block内部的虚拟寄存器
        for (Instruction i : basicBlock.getInstructions()) {
            if (i instanceof BrInst) {
                getVirtualRegFromBrInst((BrInst) i);
            } else if (i instanceof CallInst) {
                getVirtualRegFromCallInst((CallInst) i);
            } else if (i instanceof RetInst) {
                getVirtualRegFromRetInst((RetInst) i);
            } else if (i instanceof AllocaInst) {
                getVirtualRegFromAllocaInst((AllocaInst) i);
            } else if (i instanceof BinaryOperator) {
                getVirtualRegFromBinaryOperator((BinaryOperator) i);
            } else if (i instanceof GEPInst) {
                getVirtualRegFromGEPInst((GEPInst) i);
            } else if (i instanceof IcmpInst) {
                getVirtualRegFromIcmpInst((IcmpInst) i);
            } else if (i instanceof LoadInst) {
                getVirtualRegFromLoadInst((LoadInst) i);
            } else if (i instanceof StoreInst) {
                getVirtualRegFromStoreInst((StoreInst) i);
            } else if (i instanceof ZextInst) {
                getVirtualRegFromZextInst((ZextInst) i);
            } else if (i instanceof MoveInst) {
                getVirtualRegFromMoveInst((MoveInst) i);
            }
        }
    }

    // ! get virtual regs from llvm ir part
    private ArrayList<VirtualReg> getVirtualRegFromFuncParams(Function function) {
        ArrayList<VirtualReg> inputParams = new ArrayList<>();
        if (function.getArguments() == null) {
            return inputParams;
        }
        instrPosition++;
        int paramSize = function.getArguments().size();
        for (int i = 0; i < paramSize; i++) {
            String paraName = function.getArguments().get(i).getName().substring(1);
            VirtualReg paramVirtualReg = getVirtualRegInterface(paraName, SymbolTypeForMIPS.FunctionParam);
            paramVirtualReg.setStackOffset(i * 4);
            inputParams.add(paramVirtualReg);
        }
        return inputParams;
    }

    private void getVirtualRegFromBrInst(BrInst brInst) {
        instrPosition++;
        if (brInst.isConditionalBranch()) {
            Value cond = brInst.getOperands().get(0);
            Value label1 = brInst.getOperands().get(1);
            Value label2 = brInst.getOperands().get(2);
            getVirtualRegInterface(cond.getName().substring(1), SymbolTypeForMIPS.VirtualReg);
            getVirtualRegInterface(label1.getName().substring(1), SymbolTypeForMIPS.BlockLabel);
            getVirtualRegInterface(label2.getName().substring(1), SymbolTypeForMIPS.BlockLabel);
        } else {
            Value label = brInst.getOperands().get(0);
            getVirtualRegInterface(label.getName().substring(1), SymbolTypeForMIPS.BlockLabel);
        }
    }

    private void getVirtualRegFromCallInst(CallInst callInst) {
        instrPosition++;
        if (callInst.getArguments() != null) {
            for (Value value : callInst.getArguments()) {
                if (value instanceof ConstantInteger) {
                    continue;
                }
                getVirtualRegInterface(value.getName().substring(1), SymbolTypeForMIPS.VirtualReg);
            }
        }
        if (callInst.getType() != VoidType.voidType) {
            String name = callInst.getName().substring(1);
            getVirtualRegInterface(name, SymbolTypeForMIPS.VirtualReg);
        }
    }

    private void getVirtualRegFromRetInst(RetInst retInst) {
        instrPosition++;
        if (retInst.isExpReturn()) {
            Value expValue = retInst.getOperands().get(0);
            if (!(expValue instanceof ConstantInteger)) {
                String name = expValue.getName().substring(1);
                getVirtualRegInterface(name, SymbolTypeForMIPS.VirtualReg);
            }
        }
    }

    private void getVirtualRegFromAllocaInst(AllocaInst allocaInst) {
        instrPosition++;
        String name = allocaInst.getName().substring(1);
        Type allocType = ((PointerType)allocaInst.getType()).getInnerValueType();
        VirtualReg virtualReg = getVirtualRegInterface(name, SymbolTypeForMIPS.StackReg);
        virtualReg.setSize(allocType.getSize());
    }

    private void getVirtualRegFromBinaryOperator(BinaryOperator binaryOperator) {
        instrPosition++;
        String newName = binaryOperator.getName().substring(1);
        getVirtualRegInterface(newName, SymbolTypeForMIPS.VirtualReg);
        Value value1 = binaryOperator.getOperands().get(0);
        Value value2 = binaryOperator.getOperands().get(1);

        if (!(value1 instanceof ConstantInteger)) {
            String valueName = value1.getName().substring(1);
            getVirtualRegInterface(valueName, SymbolTypeForMIPS.VirtualReg);
        }
        if (!(value2 instanceof ConstantInteger)) {
            String valueName = value2.getName().substring(1);
            getVirtualRegInterface(valueName, SymbolTypeForMIPS.VirtualReg);
        }
    }

    private void getVirtualRegFromGEPInst(GEPInst gepInst) {
        instrPosition++;
        String name = gepInst.getName().substring(1);
        getVirtualRegInterface(name, SymbolTypeForMIPS.VirtualReg);

        Value value = gepInst.getOperands().get(0);
        String valueName = value.getName().substring(1);
        getVirtualRegInterface(valueName, SymbolTypeForMIPS.StackReg);
        // 这里不确定是不是VirtualReg，所以使用StackReg，这样保证不会错

        if ((gepInst.isArray() && !gepInst.isConstDim()) ||
                (gepInst.isSpecial() && !(gepInst.getOperands().get(1) instanceof ConstantInteger))) {
            value = gepInst.getOperands().get(1);
            if (value instanceof ConstantInteger) {
                gepInst.setConstDim(true);
                gepInst.setNum2(((ConstantInteger) value).getValue());
                return;
            }
            valueName = value.getName().substring(1);
            getVirtualRegInterface(valueName, SymbolTypeForMIPS.StackReg);
        }
    }

    private void getVirtualRegFromIcmpInst(IcmpInst icmpInst) {
        instrPosition++;
        String newName = icmpInst.getName().substring(1);
        getVirtualRegInterface(newName, SymbolTypeForMIPS.VirtualReg);

        Value value1 = icmpInst.getOperands().get(0);
        Value value2 = icmpInst.getOperands().get(1);
        if (!(value1 instanceof ConstantInteger)) {
            String valueName = value1.getName().substring(1);
            getVirtualRegInterface(valueName, SymbolTypeForMIPS.VirtualReg);
        }
        if (!(value2 instanceof ConstantInteger)) {
            String valueName = value2.getName().substring(1);
            getVirtualRegInterface(valueName, SymbolTypeForMIPS.VirtualReg);
        }
    }

    private void getVirtualRegFromLoadInst(LoadInst loadInst) {
        instrPosition++;
        String newName = loadInst.getName().substring(1);
        getVirtualRegInterface(newName, SymbolTypeForMIPS.VirtualReg);
        String valueName = loadInst.getOperands().get(0).getName().substring(1);
        getVirtualRegInterface(valueName, SymbolTypeForMIPS.StackReg);
    }

    private void getVirtualRegFromStoreInst(StoreInst storeInst) {
        instrPosition++;
        if (!(storeInst.getOperands().get(0) instanceof ConstantInteger)) {
            String name = storeInst.getOperands().get(0).getName().substring(1);
            getVirtualRegInterface(name, SymbolTypeForMIPS.VirtualReg);
        }
        String targetName = storeInst.getOperands().get(1).getName().substring(1);
        getVirtualRegInterface(targetName, SymbolTypeForMIPS.StackReg);
    }

    private void getVirtualRegFromZextInst(ZextInst zextInst) {
        instrPosition++;
        String newName = zextInst.getName().substring(1);
        getVirtualRegInterface(newName, SymbolTypeForMIPS.VirtualReg);

        String valueName = zextInst.getOperands().get(0).getName().substring(1);
        getVirtualRegInterface(valueName, SymbolTypeForMIPS.VirtualReg);
    }

    private void getVirtualRegFromMoveInst(MoveInst moveInst) {
        instrPosition++;
        if (!(moveInst.getSrc() instanceof ConstantInteger)) {
            String sourceName = moveInst.getSrc().getName().substring(1);
            getVirtualRegInterface(sourceName, SymbolTypeForMIPS.VirtualReg);
        }
        String destName = moveInst.getDest().getName().substring(1);
        getVirtualRegInterface(destName, SymbolTypeForMIPS.VirtualReg);
    }

    // ! main parse llvm struct part
    /**
     * 函数作用是处理IRModel，首先处理String和全局变量
     */
    private void parseIRModule(Module module) {
        MIPSModule mipsModule = MIPSModule.getMipsModule();
        mipsModule.setGlobalStringVirtualRegHashMap(globalStringVirtualRegHashMap);
        // * parse global string
        mipsModule.setConstantStrings(module.getConstantStrings());
        // * parse global variables
        mipsModule.setGlobalVariables(module.getGlobalVariables());
        // * parse all functions
        for (Function function : module.getFunctions()) {
            isGlobal = false;
            // * 将解析得到的function加入module中
            MIPSFunction mipsFunction = parseIRFunction(function);
            mipsModule.addMIPSFunction(mipsFunction);
            isGlobal = true;
        }
    }

    private MIPSFunction parseIRFunction(Function function) {
//        currentFunction = function;
        // ? 定义Function
        MIPSFunction mipsFunction = new MIPSFunction(function.getName().substring(1));
        mipsFunction.setArguments(function.getArguments());
        mipsFunction.setRegAllocation(functionRegAllocation.get(function.getName().substring(1)));
        // * 函数内部的寄存器分配
        RegAllocation regAllocation = functionRegAllocation.get(function.getName().substring(1));
        regAllocation.setMipsFunction(mipsFunction);
        functionStringVirRegHashMap = regAllocation.getNameToRegMap();
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            MIPSBlock mipsBlock = parseIRBasicBlock(basicBlock);
            mipsFunction.addMIPSBasicBlock(mipsBlock);
        }
        if (!Module.isNoColor()) {
            regAllocation.allocateReg();
        }
        return mipsFunction;
    }

    private MIPSBlock parseIRBasicBlock(BasicBlock basicBlock) {
        // ? 定义Block
        MIPSBlock mipsBlock = new MIPSBlock(basicBlock.getLabel().getName().substring(1));
        currentBlock = mipsBlock;
        // * 解析基本块
        for (Instruction i : basicBlock.getInstructions()) {
            if (i instanceof BrInst) {
                parseBrInst((BrInst) i);
            } else if (i instanceof CallInst) {
                parseCallInst((CallInst) i);
            } else if (i instanceof RetInst) {
                parseRetInst((RetInst) i);
            } else if (i instanceof AllocaInst) {
                parseAllocaInst((AllocaInst) i);
            } else if (i instanceof BinaryOperator) {
                parseBinaryOperator((BinaryOperator) i);
            } else if (i instanceof GEPInst) {
                parseGEPInst((GEPInst) i);
            } else if (i instanceof IcmpInst) {
                parseIcmpInst((IcmpInst) i);
            } else if (i instanceof LoadInst) {
                parseLoadInst((LoadInst) i);
            } else if (i instanceof StoreInst) {
                parseStoreInst((StoreInst) i);
            } else if (i instanceof ZextInst) {
                parseZextInst((ZextInst) i);
            } else if (i instanceof MoveInst) {
                parseMoveInst((MoveInst) i);
            }
        }
        return mipsBlock;
    }

    private void parseBrInst(BrInst brInst) {
        if (brInst.isConditionalBranch()) {
            Value cond = brInst.getOperands().get(0);
            Value label1 = brInst.getOperands().get(1);
            Value label2 = brInst.getOperands().get(2);
            VirtualReg virCond = globalStringVirtualRegHashMap.get(cond.getName().substring(1));
            VirtualReg virLabel1 = globalStringVirtualRegHashMap.get(label1.getName().substring(1));
            VirtualReg virLabel2 = globalStringVirtualRegHashMap.get(label2.getName().substring(1));
            currentBlock.addInstruction(new MIPSBranch(InstructionType.beqz, virCond, virLabel2));
            currentBlock.addInstruction(new MIPSBranch(InstructionType.j, virLabel1));
        } else {
            Value label = brInst.getOperands().get(0);
            VirtualReg virLabel = globalStringVirtualRegHashMap.get(label.getName().substring(1));
            currentBlock.addInstruction(new MIPSBranch(InstructionType.j, virLabel));
        }
    }

    private void parseCallInst(CallInst callInst) {
        // * 之前没有统计Function的VirtualReg
        MIPSBranch mipsBranch = new MIPSBranch(InstructionType.jal,
                new VirtualReg(callInst.getFunction().getName().substring(1)));
        currentBlock.addInstruction(mipsBranch);
        mipsBranch.setArguments(callInst.getArguments());
        mipsBranch.setFunction(callInst.getFunction());
        if (callInst.getType() != VoidType.voidType) {
            String name = callInst.getName().substring(1);
            VirtualReg virtualReg = globalStringVirtualRegHashMap.get(name);
            currentBlock.addInstruction(new MIPSMove(virtualReg, allPhysicalRegs.get("$v0")));
        }
    }

    private void parseRetInst(RetInst retInst) {
        if (retInst.isExpReturn()) {    // * 有返回值
            Value expValue = retInst.getOperands().get(0);
            if (!(expValue instanceof ConstantInteger)) {   // 不是常数
                String name = expValue.getName().substring(1);
                VirtualReg virtualReg = globalStringVirtualRegHashMap.get(name);
                currentBlock.addInstruction(new MIPSMove(allPhysicalRegs.get("$v0"), virtualReg));
            } else {    // 是常数
                currentBlock.addInstruction(new MIPSLi(allPhysicalRegs.get("$v0"),
                        ((ConstantInteger) expValue).getValue()));
            }
        }
        MIPSBranch mipsBranch = new MIPSBranch(InstructionType.jr, allPhysicalRegs.get("$ra"));
//        mipsBranch.setFunction(currentFunction);
        currentBlock.addInstruction(mipsBranch);
    }

    private void parseAllocaInst(AllocaInst allocaInst) {
        // 所有alloca的和所有溢出的寄存器都会在函数开始的时候分配，所以这里不需要再处理alloca指令了
    }

    private void parseBinaryOperator(BinaryOperator binaryOperator) {
        String newName = binaryOperator.getName().substring(1);
        VirtualReg newNameReg = globalStringVirtualRegHashMap.get(newName);
        Value value1 = binaryOperator.getOperands().get(0);
        Value value2 = binaryOperator.getOperands().get(1);
        IR.Values.InstructionIR.InstructionType instructionType = binaryOperator.getInstructionType();
        VirtualReg value1Reg = null;
        VirtualReg value2Reg = null;
        if (!(value1 instanceof ConstantInteger) && value2 instanceof ConstantInteger) {
            value1Reg = globalStringVirtualRegHashMap.get(value1.getName().substring(1));
            int value2Num = ((ConstantInteger) value2).getValue();
            if (instructionType == IR.Values.InstructionIR.InstructionType.ADD) {
                currentBlock.addInstruction(new MIPSBinary(InstructionType.addiu, newNameReg, value1Reg, value2Num));
            } else if (instructionType == IR.Values.InstructionIR.InstructionType.SUB) {
                currentBlock.addInstruction(new MIPSBinary(InstructionType.addiu, newNameReg, value1Reg, -value2Num));
            } else if (instructionType == IR.Values.InstructionIR.InstructionType.MUL) {
                currentBlock.addInstruction(new MIPSBinary(InstructionType.mul, newNameReg, value1Reg, value2Num));
            } else if (instructionType == IR.Values.InstructionIR.InstructionType.SDIV) {
                currentBlock.addInstruction(new MIPSBinary(InstructionType.div, newNameReg, value1Reg, value2Num));
            } else if (instructionType == IR.Values.InstructionIR.InstructionType.SREM) {
//                currentBlock.addInstruction(new MIPSBinary(InstructionType.div, allPhysicalRegs.get("")));
                currentBlock.addInstruction(new MIPSBinary(InstructionType.srem, newNameReg, value1Reg, value2Num));
            }
        } else if (value1 instanceof ConstantInteger && !(value2 instanceof ConstantInteger)) {
            value2Reg = globalStringVirtualRegHashMap.get(value2.getName().substring(1));
            int value1Num = ((ConstantInteger) value1).getValue();
            if (instructionType == IR.Values.InstructionIR.InstructionType.ADD) {
                currentBlock.addInstruction(new MIPSBinary(InstructionType.addiu, newNameReg, value2Reg, value1Num));
            } else if (instructionType == IR.Values.InstructionIR.InstructionType.SUB) {
                currentBlock.addInstruction(new MIPSLi(allPhysicalRegs.get("$t0"), value1Num));
                currentBlock.addInstruction(new MIPSBinary(InstructionType.subu, newNameReg, allPhysicalRegs.get("$t0"), value2Reg));
            } else if (instructionType == IR.Values.InstructionIR.InstructionType.MUL) {
                currentBlock.addInstruction(new MIPSBinary(InstructionType.mul, newNameReg, value2Reg, value1Num));
            } else if (instructionType == IR.Values.InstructionIR.InstructionType.SDIV) {
                currentBlock.addInstruction(new MIPSLi(allPhysicalRegs.get("$t0"), value1Num));
                currentBlock.addInstruction(new MIPSBinary(InstructionType.div, newNameReg, allPhysicalRegs.get("$t0"), value2Reg));
            } else if (instructionType == IR.Values.InstructionIR.InstructionType.SREM) {
                currentBlock.addInstruction(new MIPSLi(allPhysicalRegs.get("$t0"), value1Num));
                currentBlock.addInstruction(new MIPSBinary(InstructionType.srem, newNameReg, allPhysicalRegs.get("$t0"), value2Reg));
            }
        } else if (value1 instanceof ConstantInteger && value2 instanceof ConstantInteger) {
            int value1Num = ((ConstantInteger) value1).getValue();
            int value2Num = ((ConstantInteger) value2).getValue();
            int ansNum = 0;
            if (instructionType == IR.Values.InstructionIR.InstructionType.ADD) {
                ansNum = value1Num + value2Num;
            } else if (instructionType == IR.Values.InstructionIR.InstructionType.SUB) {
                ansNum = value1Num - value2Num;
            } else if (instructionType == IR.Values.InstructionIR.InstructionType.MUL) {
                ansNum = value1Num * value2Num;
            } else if (instructionType == IR.Values.InstructionIR.InstructionType.SDIV) {
                ansNum = value1Num / value2Num;
            } else if (instructionType == IR.Values.InstructionIR.InstructionType.SREM) {
                ansNum = value1Num % value2Num;
            }
            currentBlock.addInstruction(new MIPSLi(newNameReg, ansNum));
        } else {
            value1Reg = globalStringVirtualRegHashMap.get(value1.getName().substring(1));
            value2Reg = globalStringVirtualRegHashMap.get(value2.getName().substring(1));
            if (instructionType == IR.Values.InstructionIR.InstructionType.ADD) {
                currentBlock.addInstruction(new MIPSBinary(InstructionType.addu, newNameReg, value1Reg, value2Reg));
            } else if (instructionType == IR.Values.InstructionIR.InstructionType.SUB) {
                currentBlock.addInstruction(new MIPSBinary(InstructionType.subu, newNameReg, value1Reg, value2Reg));
            } else if (instructionType == IR.Values.InstructionIR.InstructionType.MUL) {
                currentBlock.addInstruction(new MIPSBinary(InstructionType.mul, newNameReg, value1Reg, value2Reg));
            } else if (instructionType == IR.Values.InstructionIR.InstructionType.SDIV) {
                currentBlock.addInstruction(new MIPSBinary(InstructionType.div, newNameReg, value1Reg, value2Reg));
            } else if (instructionType == IR.Values.InstructionIR.InstructionType.SREM) {
                currentBlock.addInstruction(new MIPSBinary(InstructionType.srem, newNameReg, value1Reg, value2Reg));
            }
        }
    }

    private void parseGEPInst(GEPInst gepInst) {
        String targetName = gepInst.getName().substring(1);
        if (gepInst.isSpecial()) {
            VirtualReg targetReg = globalStringVirtualRegHashMap.get(targetName);
            Value pointerValue = gepInst.getOperands().get(0);
            VirtualReg pointerReg = globalStringVirtualRegHashMap.get(pointerValue.getName().substring(1));
            Value num = gepInst.getOperands().get(1);
            int typeSize = ((PointerType) pointerValue.getType()).getInnerValueType().getSize();
            if (num instanceof ConstantInteger) {
                int offset = ((ConstantInteger) num).getValue() * typeSize;
                currentBlock.addInstruction(new MIPSBinary(InstructionType.addiu, targetReg, pointerReg, offset));
            } else {
                VirtualReg numReg = globalStringVirtualRegHashMap.get(num.getName().substring(1));
                currentBlock.addInstruction(new MIPSBinary(InstructionType.mul, allPhysicalRegs.get("$t0"), numReg, typeSize));
                currentBlock.addInstruction(new MIPSBinary(InstructionType.addu, targetReg, pointerReg, allPhysicalRegs.get("$t0")));
            }
        } else if (gepInst.isArray()) {
            VirtualReg targetReg = globalStringVirtualRegHashMap.get(targetName);
            Value pointerValue = gepInst.getOperands().get(0);
            VirtualReg pointerReg = globalStringVirtualRegHashMap.get(pointerValue.getName().substring(1));
            int typeSize = ((ArrayType)((PointerType) pointerValue.getType()).getInnerValueType()).getElementType().getSize();
            if (!gepInst.isConstDim()) {
                Value num = gepInst.getOperands().get(1);
                VirtualReg numReg = globalStringVirtualRegHashMap.get(num.getName().substring(1));
                currentBlock.addInstruction(new MIPSBinary(InstructionType.mul, allPhysicalRegs.get("$t0"), numReg, typeSize));
                currentBlock.addInstruction(new MIPSBinary(InstructionType.addu, targetReg, pointerReg, allPhysicalRegs.get("$t0")));
            } else {
                int offset = gepInst.getNum2() * typeSize;
                currentBlock.addInstruction(new MIPSBinary(InstructionType.addiu, targetReg, pointerReg, offset));
            }
        } else {
            VirtualReg targetReg = globalStringVirtualRegHashMap.get(targetName);
            String strName = gepInst.getOperands().get(0).getName().substring(1);
            VirtualReg strReg = globalStringVirtualRegHashMap.get(strName);
            currentBlock.addInstruction(new MIPSLa(targetReg, strReg));
        }
    }

    private void parseIcmpInst(IcmpInst icmpInst) {
        String targetName = icmpInst.getName().substring(1);
        Value value1 = icmpInst.getOperands().get(0);
        Value value2 = icmpInst.getOperands().get(1);
        VirtualReg targetReg = globalStringVirtualRegHashMap.get(targetName);
        IR.Values.InstructionIR.InstructionType instructionType = icmpInst.getInstructionType();
        if (value1 instanceof ConstantInteger) {
            if (value2 instanceof ConstantInteger) {    // 都是数字
                int ans = handleIcmpNumNum(instructionType,
                        ((ConstantInteger) value1).getValue(), ((ConstantInteger) value2).getValue());
                currentBlock.addInstruction(new MIPSLi(targetReg, ans));
            } else {                                    // value1 是数字，value2 是寄存器
                instructionType = handleOppositeIcmpType(instructionType);
                int value1Num = ((ConstantInteger) value1).getValue();
                VirtualReg value2Reg = globalStringVirtualRegHashMap.get(value2.getName().substring(1));
                MIPSInstruction compInst = handleIcmpRegNum(instructionType, targetReg, value2Reg, value1Num);
                currentBlock.addInstruction(compInst);
            }
        } else {
            if (value2 instanceof ConstantInteger) {    // value1 是寄存器，value2 是数字
                int value2Num = ((ConstantInteger) value2).getValue();
                VirtualReg value1Reg = globalStringVirtualRegHashMap.get(value1.getName().substring(1));
                MIPSInstruction compInst = handleIcmpRegNum(instructionType, targetReg, value1Reg, value2Num);
                currentBlock.addInstruction(compInst);
            } else {                                    // 都是寄存器
                VirtualReg value1Reg = globalStringVirtualRegHashMap.get(value1.getName().substring(1));
                VirtualReg value2Reg = globalStringVirtualRegHashMap.get(value2.getName().substring(1));
                MIPSInstruction compInst = handleIcmpRegReg(instructionType, targetReg, value1Reg, value2Reg);
                currentBlock.addInstruction(compInst);
            }
        }
    }

    private IR.Values.InstructionIR.InstructionType handleOppositeIcmpType(
            IR.Values.InstructionIR.InstructionType instructionType) {  // * 得到相反操作的比较指令
        if (instructionType == IR.Values.InstructionIR.InstructionType.EQ) {
            return IR.Values.InstructionIR.InstructionType.EQ;
        } else if (instructionType == IR.Values.InstructionIR.InstructionType.NE) {
            return IR.Values.InstructionIR.InstructionType.NE;
        } else if (instructionType == IR.Values.InstructionIR.InstructionType.SGT) {
            return IR.Values.InstructionIR.InstructionType.SLT;
        } else if (instructionType == IR.Values.InstructionIR.InstructionType.SGE) {
            return IR.Values.InstructionIR.InstructionType.SLE;
        } else if (instructionType == IR.Values.InstructionIR.InstructionType.SLT) {
            return IR.Values.InstructionIR.InstructionType.SGT;
        } else if (instructionType == IR.Values.InstructionIR.InstructionType.SLE){    // SLE
            return IR.Values.InstructionIR.InstructionType.SGE;
        }
        return null;
    }

    private int handleIcmpNumNum(IR.Values.InstructionIR.InstructionType instructionType, int x, int y) {
        if (instructionType == IR.Values.InstructionIR.InstructionType.EQ) {    // * 处理两数比较指令
            if (x == y) return 1;
        } else if (instructionType == IR.Values.InstructionIR.InstructionType.NE) {
            if (x != y) return 1;
        } else if (instructionType == IR.Values.InstructionIR.InstructionType.SGT) {
            if (x > y) return 1;
        } else if (instructionType == IR.Values.InstructionIR.InstructionType.SGE) {
            if (x >= y) return 1;
        } else if (instructionType == IR.Values.InstructionIR.InstructionType.SLT) {
            if (x < y) return 1;
        } else if (instructionType == IR.Values.InstructionIR.InstructionType.SLE) {
            if (x <= y) return 1;
        }
        return 0;
    }

    private MIPSInstruction handleIcmpRegNum(IR.Values.InstructionIR.InstructionType instructionType,
                                             VirtualReg targetReg, VirtualReg virtualReg1, int value2Num) {
        if (instructionType == IR.Values.InstructionIR.InstructionType.EQ) {    // * 处理一个寄存器，一个数字的比较指令
            return new MIPSComp(InstructionType.seq, targetReg, virtualReg1, value2Num);
        } else if (instructionType == IR.Values.InstructionIR.InstructionType.NE) {
            return new MIPSComp(InstructionType.sne, targetReg, virtualReg1, value2Num);
        } else if (instructionType == IR.Values.InstructionIR.InstructionType.SGT) {
            return new MIPSComp(InstructionType.sgt, targetReg, virtualReg1, value2Num);
        } else if (instructionType == IR.Values.InstructionIR.InstructionType.SGE) {
            return new MIPSComp(InstructionType.sge, targetReg, virtualReg1, value2Num);
        } else if (instructionType == IR.Values.InstructionIR.InstructionType.SLT) {
            return new MIPSComp(InstructionType.slti, targetReg, virtualReg1, value2Num);
        } else if (instructionType == IR.Values.InstructionIR.InstructionType.SLE) {
            return new MIPSComp(InstructionType.sle, targetReg, virtualReg1, value2Num);
        }
        return null;
    }

    private MIPSInstruction handleIcmpRegReg(IR.Values.InstructionIR.InstructionType instructionType,
                                             VirtualReg targetReg, VirtualReg virtualReg1, VirtualReg virtualReg2) {
        if (instructionType == IR.Values.InstructionIR.InstructionType.EQ) {    // * 处理两个寄存器类型的比较指令
            return new MIPSComp(InstructionType.seq, targetReg, virtualReg1, virtualReg2);
        } else if (instructionType == IR.Values.InstructionIR.InstructionType.NE) {
            return new MIPSComp(InstructionType.sne, targetReg, virtualReg1, virtualReg2);
        } else if (instructionType == IR.Values.InstructionIR.InstructionType.SGT) {
            return new MIPSComp(InstructionType.sgt, targetReg, virtualReg1, virtualReg2);
        } else if (instructionType == IR.Values.InstructionIR.InstructionType.SGE) {
            return new MIPSComp(InstructionType.sge, targetReg, virtualReg1, virtualReg2);
        } else if (instructionType == IR.Values.InstructionIR.InstructionType.SLT) {
            return new MIPSComp(InstructionType.slt, targetReg, virtualReg1, virtualReg2);
        } else if (instructionType == IR.Values.InstructionIR.InstructionType.SLE) {
            return new MIPSComp(InstructionType.sle, targetReg, virtualReg1, virtualReg2);
        }
        return null;
    }

    private void parseLoadInst(LoadInst loadInst) {
        String targetName = loadInst.getName().substring(1);
        Value pointerValue = loadInst.getOperands().get(0);
        VirtualReg targetReg = globalStringVirtualRegHashMap.get(targetName);
        String pointerName = pointerValue.getName().substring(1);
        VirtualReg pointerReg = globalStringVirtualRegHashMap.get(pointerName);
        currentBlock.addInstruction(new MIPSLw(targetReg, pointerReg));
    }

    private void parseStoreInst(StoreInst storeInst) {
        String targetName = storeInst.getOperands().get(1).getName().substring(1);
        Value sourceValue = storeInst.getOperands().get(0);
        String sourceName = sourceValue.getName().substring(1);
        VirtualReg targetReg = globalStringVirtualRegHashMap.get(targetName);
        if (sourceValue instanceof ConstantInteger) {
            currentBlock.addInstruction(new MIPSLi(allPhysicalRegs.get("$t0"),
                    ((ConstantInteger) sourceValue).getValue()));
            currentBlock.addInstruction(new MIPSSw(allPhysicalRegs.get("$t0"), targetReg));
        } else {
            VirtualReg sourceReg = globalStringVirtualRegHashMap.get(sourceName);
            currentBlock.addInstruction(new MIPSSw(sourceReg, targetReg));
        }
    }

    private void parseZextInst(ZextInst zextInst) {
        String targetName = zextInst.getName().substring(1);
        String sourceName = zextInst.getOperands().get(0).getName().substring(1);
        VirtualReg targetReg = globalStringVirtualRegHashMap.get(targetName);
        VirtualReg sourceReg = globalStringVirtualRegHashMap.get(sourceName);
        currentBlock.addInstruction(new MIPSMove(targetReg, sourceReg));
    }

    private void parseMoveInst(MoveInst moveInst) {
        String sourceName = moveInst.getSrc().getName().substring(1);
        String destName = moveInst.getDest().getName().substring(1);
        VirtualReg destReg = globalStringVirtualRegHashMap.get(destName);
        // 考虑Source为常数的情况
        Value sourceValue = moveInst.getSrc();
        if (sourceValue instanceof ConstantInteger) {
            currentBlock.addInstruction(new MIPSLi(allPhysicalRegs.get("$t0"),
                    ((ConstantInteger) sourceValue).getValue()));
            currentBlock.addInstruction(new MIPSMove(destReg, allPhysicalRegs.get("$t0")));
        } else {
            VirtualReg sourceReg = globalStringVirtualRegHashMap.get(sourceName);
            currentBlock.addInstruction(new MIPSMove(destReg, sourceReg));
        }
    }

//    private TreeMap<String, String> parseString() {
//        TreeMap<String, String> strings = new TreeMap<>();
//        for (ConstantString string : llvmModule.getConstantStrings()) {
//            strings.put(string.getName().substring(1), string.getString());
//        }
//        return strings;
//    }

    private VirtualReg getVirtualRegInterface(String name, SymbolTypeForMIPS symbolType) {
        if (name.equals("")) {
            // 如果出现名字为空，则说明有常数没有考虑
            System.out.println("有常数没有考虑");
        }
        // ? 这是一个统一处理所有Virtual reg -> Physical Reg的函数
        // * 如果Virtual之前出现过，则直接返回即可，并设置setEnd，方便后面分配寄存器
        if (globalStringVirtualRegHashMap.containsKey(name)) {
            globalStringVirtualRegHashMap.get(name).setEnd(instrPosition);
            return globalStringVirtualRegHashMap.get(name);
        } else {
            // * 说明这个 Virtual Reg之前没有出现过，需要进行
            VirtualReg virtualReg = new VirtualReg(name, symbolType);
            if (symbolType != SymbolTypeForMIPS.BlockLabel) {
                virtualReg.setStart(instrPosition);
                if (!isGlobal)
                    functionStringVirRegHashMap.put(name, virtualReg);
            }
            globalStringVirtualRegHashMap.put(name, virtualReg);
            return virtualReg;
        }
    }


}
