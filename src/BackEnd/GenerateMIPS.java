package BackEnd;

import FileIO.FilePrinter;
import IR.Module;
import IR.Values.BasicBlock;
import IR.Values.ConstantIR.ConstantInteger;
import IR.Values.ConstantIR.ConstantString;
import IR.Values.Function;
import IR.Values.InstructionIR.*;
import IR.Values.InstructionIR.TerminatorIR.CallInst;
import IR.Values.InstructionIR.TerminatorIR.RetInst;
import IR.Values.Value;
import IR.types.VoidType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class GenerateMIPS {
    private Module module = Module.getMyModule();
    private FilePrinter filePrinter = FilePrinter.getFilePrinter();
    private static int instNum = 0;
    private HashMap<String, RegAllocation> functionRegAllocation = new HashMap<>();

    public GenerateMIPS() {
        // ? 输出数据段
        filePrinter.outPrintlnMIPS(".data");
        // * 输出alloca变量
        ArrayList<String> allocNames = parseAlloca();
        for (String s : allocNames) {
            filePrinter.outPrintlnMIPS(s + ": .space 4");
        }
        // * 输出字符串
        TreeMap<String, String> stringStringTreeMap = parseString();
        for (Map.Entry entry : stringStringTreeMap.entrySet()) {
            String name = (String) entry.getKey();
            String value = (String) entry.getValue();
            filePrinter.outPrintlnMIPS(name + ": .asciiz \"" + value + "\"");
        }
        // ? 输出代码段
        filePrinter.outPrintlnMIPS(".text");
        filePrinter.outPrintlnMIPS("jal main");
        filePrinter.outPrintlnMIPS("li $v0, 10");
        filePrinter.outPrintlnMIPS("syscall");
        filePrinter.outPrintlnMIPS("getint:\n" +
                "li $v0, 5\n" +
                "syscall\n" +
                "jr $ra\n" +
                "\n" +
                "putstr:\n" +
                "lw $a0, 0($sp)\n" +
                "addi $sp, $sp, 4\n" +
                "li $v0, 4\n" +
                "syscall\n" +
                "jr $ra\n" +
                "\n" +
                "putint:\n" +
                "lw $a0, 0($sp)\n" +
                "addi $sp, $sp, 4\n" +
                "li $v0, 1\n" +
                "syscall\n"+
                "jr $ra\n" +
                "\n");
        assignNum();

    }

    private void assignNum() {
        // * Function name -> RegAllocation
        for (Function function : module.getFunctions()) {
            RegAllocation regAllocation = new RegAllocation();
            HashMap<String, VirtualReg> stringVirtualRegHashMap = new HashMap<>();
            getVirtualRegFromFuncParams(stringVirtualRegHashMap, function);
            for (BasicBlock basicBlock : function.getBasicBlocks()) {
                for (Instruction i : basicBlock.getInstructions()) {
                    if (i instanceof CallInst) {
                        getVirtualRegFromCallInst(stringVirtualRegHashMap, (CallInst) i);
                    } else if (i instanceof RetInst) {
                        getVirtualRegFromRetInst(stringVirtualRegHashMap, (RetInst) i);
                    } else if (i instanceof BinaryOperator) {
                        getVirtualRegFromBinaryOperator(stringVirtualRegHashMap, (BinaryOperator) i);
                    } else if (i instanceof GEPInst) {
                        getVirtualRegFromGEP(stringVirtualRegHashMap, (GEPInst) i);
                    } else if (i instanceof LoadInst) {
                        getVirtualRegFromLoadInst(stringVirtualRegHashMap, (LoadInst) i);
                    } else if (i instanceof StoreInst) {
                        getVirtualRegFromStoreInst(stringVirtualRegHashMap, (StoreInst) i);
                    }
                }
            }
            regAllocation.setNameToRegMap(stringVirtualRegHashMap);
            regAllocation.allocateReg();
//            if (function.getName().substring(1).equals("main")) {
//                filePrinter.outPrintlnMIPS("123123");
//            }
            functionRegAllocation.put(function.getName().substring(1), regAllocation);
        }
        genMipsFromFunctions();
    }

    private TreeMap<String, String> parseString() {
        TreeMap<String, String> strings = new TreeMap<>();
        for (ConstantString string : module.getConstantStrings()) {
            strings.put(string.getName().substring(1), string.getString());
        }
        return strings;
    }

    private ArrayList<String> parseAlloca() {
        ArrayList<String> ans = new ArrayList<>();
        for (Function function : module.getFunctions()) {
            for (BasicBlock basicBlock : function.getBasicBlocks()) {
                for (Instruction i : basicBlock.getInstructions()) {
                    if (i instanceof AllocaInst) {
                        ans.add(i.getName().substring(1));
                    }
                }
            }
        }
        return ans;
    }

    private void getVirtualRegFromFuncParams(HashMap<String, VirtualReg> stringVirtualRegHashMap, Function function) {
        if (function.getArguments() == null) {
            return;
        }
        instNum++;
        int paramSize = function.getArguments().size();
        for (int i = 0; i < paramSize; i++) {
            String paramName = function.getArguments().get(i).getName().substring(1);
            stringVirtualRegHashMap.put(paramName, new VirtualReg(instNum, paramName));
        }
    }

    private void getVirtualRegFromCallInst(HashMap<String, VirtualReg> stringVirtualRegHashMap, CallInst callInst) {
        instNum++;
        if (callInst.getArguments() != null) {
            for (Value value : callInst.getArguments()) {
                if (value instanceof ConstantInteger) {
                    continue;
                }
                stringVirtualRegHashMap.get(value.getName().substring(1)).setEnd(instNum);
            }
        }
        if (callInst.getType() != VoidType.voidType) {
            String name = callInst.getName().substring(1);
            stringVirtualRegHashMap.put(name, new VirtualReg(instNum, name));
        }
    }

    private void getVirtualRegFromRetInst(HashMap<String, VirtualReg> stringVirtualRegHashMap, RetInst retInst) {
        instNum++;
        if (retInst.isExpReturn()) {
            Value expValue = retInst.getOperands().get(0);
            if (!(expValue instanceof ConstantInteger)) {
                String name = expValue.getName().substring(1);
                stringVirtualRegHashMap.get(name).setEnd(instNum);
            }
        }
    }

    private void getVirtualRegFromBinaryOperator(HashMap<String, VirtualReg> stringVirtualRegHashMap,
                                                 BinaryOperator binaryOperator) {
        instNum++;
        String newName = binaryOperator.getName().substring(1);
        stringVirtualRegHashMap.put(newName, new VirtualReg(instNum, newName));
        Value value1 = binaryOperator.getOperands().get(0);
        Value value2 = binaryOperator.getOperands().get(1);
        if (!(value1 instanceof ConstantInteger)) {
            String valueName = value1.getName().substring(1);
            stringVirtualRegHashMap.get(valueName).setEnd(instNum);
        }
        if (!(value2 instanceof ConstantInteger)) {
            String valueName = value2.getName().substring(1);
            stringVirtualRegHashMap.get(valueName).setEnd(instNum);
        }
    }

    private void getVirtualRegFromGEP(HashMap<String, VirtualReg> stringVirtualRegHashMap, GEPInst gepInst) {
        instNum++;
        String name = gepInst.getName().substring(1);
        stringVirtualRegHashMap.put(name, new VirtualReg(instNum, name));
    }

    private void getVirtualRegFromLoadInst(HashMap<String, VirtualReg> stringVirtualRegHashMap, LoadInst loadInst) {
        instNum++;
        String name = loadInst.getName().substring(1);
        stringVirtualRegHashMap.put(name, new VirtualReg(instNum, name));
    }

    private void getVirtualRegFromStoreInst(HashMap<String, VirtualReg> stringVirtualRegHashMap, StoreInst storeInst) {
        instNum++;
        if (!(storeInst.getOperands().get(0) instanceof ConstantInteger)) {
            String name = storeInst.getOperands().get(0).getName().substring(1);
            stringVirtualRegHashMap.get(name).setEnd(instNum);
        }
    }

    private void genMipsFromFunctions() {
        for (Function function : module.getFunctions()) {
            String functionName = function.getName().substring(1);
            filePrinter.outPrintlnMIPS(functionName + ":");
            RegAllocation regAllocation = functionRegAllocation.get(functionName);
            // * load the parameters from $sp && add the $sp
            loadParams(regAllocation, function);
            for (BasicBlock basicBlock : function.getBasicBlocks()) {
                filePrinter.outPrintlnMIPS(basicBlock.getLabel().getName().substring(1) + ":");
                for (Instruction i : basicBlock.getInstructions()) {
                    if (i instanceof CallInst) {
                        genMipsFromCallInst(regAllocation, (CallInst) i);
                    } else if (i instanceof RetInst) {
                        genMipsFromRetInst(regAllocation, (RetInst) i);
                    } else if (i instanceof BinaryOperator) {
                        genMipsFromBinaryOperator(regAllocation, (BinaryOperator) i);
                    } else if (i instanceof GEPInst) {
                        genMipsFromGEP(regAllocation, (GEPInst) i);
                    } else if (i instanceof LoadInst) {
                        genMipsFromLoad(regAllocation, (LoadInst) i);
                    } else if (i instanceof StoreInst) {
                        genMipsFromStore(regAllocation, (StoreInst) i);
                    }
                }
            }
        }
    }

    private void saveEnv(RegAllocation regAllocation) {
        // int size = regAllocation.getAvailableRegs().size() + 1;
        int size = regAllocation.getTotalReg();
        filePrinter.outPrintlnMIPS("addi $sp, $sp, " + (size + 1) * (-4));
        for (int i = 0; i < size; i++) {
            filePrinter.outPrintlnMIPS("sw " + regAllocation.getAvailableRegs().get(i) + ", " + (4 * i) + "($sp)");
        }
        filePrinter.outPrintlnMIPS("sw $ra, " + (4 * size) + "($sp)");
    }

    private void loadEnv(RegAllocation regAllocation) {
        // int size = regAllocation.getAvailableRegs().size();
        int size = regAllocation.getTotalReg();
        for (int i = 0; i < size; i++) {
            filePrinter.outPrintlnMIPS("lw " + regAllocation.getAvailableRegs().get(i) + ", " + (4 * i) + "($sp)");
        }
        filePrinter.outPrintlnMIPS("lw $ra, " + (4 * size) + "($sp)");
        filePrinter.outPrintlnMIPS("addi $sp, $sp, " + (size + 1) * 4);
    }

    private void loadParams(RegAllocation regAllocation, Function function) {
        if (function.getArguments() == null) {
            return;
        }
        int paramSize = function.getArguments().size();
        for (int i = 0; i < paramSize; i++) {
            String paramName = function.getArguments().get(i).getName().substring(1);
            String regName = regAllocation.getVirToPhi().get(paramName);
            filePrinter.outPrintlnMIPS("lw " + regName + ", " + (4 * i) + "($sp)");
        }
        filePrinter.outPrintlnMIPS("addi $sp, $sp, " + paramSize * 4);
    }

    private void genMipsFromCallInst(RegAllocation allocation, CallInst callInst) {
        saveEnv(allocation);  // * save the environment
        // * save the parameters
        if (callInst.getArguments() != null) {
            int paramSize = callInst.getArguments().size();
            filePrinter.outPrintlnMIPS("addi $sp, $sp, " + paramSize * (-4));
            for (int i = 0; i < paramSize; i++) {
                if (!(callInst.getArguments().get(i) instanceof ConstantInteger)) {
                    String name = callInst.getArguments().get(i).getName().substring(1);
                    String regName = allocation.getVirToPhi().get(name);
                    filePrinter.outPrintlnMIPS("sw " + regName + ", " + (4 * i) + "($sp)");
                } else {
                    filePrinter.outPrintlnMIPS("li $t0, " + callInst.getArguments().get(i).getName());
                    filePrinter.outPrintlnMIPS("sw $t0, " + (4 * i) + "($sp)");
                }
            }
        }
        filePrinter.outPrintlnMIPS("jal " + callInst.getFunction().getName().substring(1));
        loadEnv(allocation);  // * load the environment
        if (callInst.getType() != VoidType.voidType) {
            String name = callInst.getName().substring(1);
            String regName = allocation.getVirToPhi().get(name);
            filePrinter.outPrintlnMIPS("move " + regName + ", $v0");
        }
    }

    private void genMipsFromRetInst(RegAllocation allocation, RetInst retInst) {
        if (retInst.isExpReturn()) {    // * 有返回值
            Value expValue = retInst.getOperands().get(0);
            if (!(expValue instanceof ConstantInteger)) {   // * 不是常数
                String name = expValue.getName().substring(1);
                String phyRegName = allocation.getVirToPhi().get(name);
                filePrinter.outPrintlnMIPS("move $v0, " + phyRegName);
            } else {
                filePrinter.outPrintlnMIPS("li $v0, " + expValue.getName());
            }
        }
        filePrinter.outPrintlnMIPS("jr $ra");
    }

    private void genMipsFromBinaryOperator(RegAllocation allocation, BinaryOperator binaryOperator) {
        String newName = binaryOperator.getName().substring(1);
        String newNameReg = allocation.getVirToPhi().get(newName);
        Value value1 = binaryOperator.getOperands().get(0);
        Value value2 = binaryOperator.getOperands().get(1);
        String value1Reg = "";
        String value2Reg = "";
        if (!(value1 instanceof ConstantInteger)) {
            String valueName = value1.getName().substring(1);
            value1Reg = allocation.getVirToPhi().get(valueName);
        } else {
            filePrinter.outPrintlnMIPS("li $t0, " + value1.getName());
            value1Reg = "$t0";
        }
        if (!(value2 instanceof ConstantInteger)) {
            String valueName = value2.getName().substring(1);
            value2Reg = allocation.getVirToPhi().get(valueName);
        } else {
            filePrinter.outPrintlnMIPS("li $t0, " + value2.getName());
            value2Reg = "$t0";
        }
        switch (binaryOperator.getInstructionType()) {
            case ADD: {
                filePrinter.outPrintlnMIPS("add " + newNameReg + " " + value1Reg + " " + value2Reg);
                break;
            }
            case SUB: {
                filePrinter.outPrintlnMIPS("sub " + newNameReg + " " + value1Reg + " " + value2Reg);
                break;
            }
            case MUL: {
                filePrinter.outPrintlnMIPS("mul " + newNameReg + " " + value1Reg + " " + value2Reg);
                break;
            }
            case SDIV: {
                filePrinter.outPrintlnMIPS("div " + newNameReg + " " + value1Reg + " " + value2Reg);
                break;
            }
            case SREM: {
                filePrinter.outPrintlnMIPS("div " + value1Reg + " " + value2Reg);
                filePrinter.outPrintlnMIPS("mfhi " + newNameReg);
                break;
            }
        }
    }

    private void genMipsFromGEP(RegAllocation regAllocation, GEPInst gepInst) {
        String name = gepInst.getName().substring(1);
        String regName = regAllocation.getVirToPhi().get(name);
        String strName = gepInst.getOperands().get(0).getName().substring(1);
        filePrinter.outPrintlnMIPS("la " + regName + ", " + strName);
    }

    private void genMipsFromLoad(RegAllocation regAllocation, LoadInst loadInst) {
        String name = loadInst.getName().substring(1);
        String regName = regAllocation.getVirToPhi().get(name);
        String pointerName = loadInst.getOperands().get(0).getName().substring(1);
        filePrinter.outPrintlnMIPS("lw " + regName + ", " + pointerName);
    }

    private void genMipsFromStore(RegAllocation regAllocation, StoreInst storeInst) {
        String pointerName = storeInst.getOperands().get(1).getName().substring(1);
        if (!(storeInst.getOperands().get(0) instanceof ConstantInteger)) {
            String name = storeInst.getOperands().get(0).getName().substring(1);
            String regName = regAllocation.getVirToPhi().get(name);
            filePrinter.outPrintlnMIPS("sw " + regName + ", " + pointerName);
        } else {
            filePrinter.outPrintlnMIPS("li $t0, " + storeInst.getOperands().get(0).getName());
            filePrinter.outPrintlnMIPS("sw $t0, " + pointerName);
        }
    }
}
