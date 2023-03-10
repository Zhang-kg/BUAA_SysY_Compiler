package FileIO;

import IR.Module;
import IR.SymbolTableForIR.SymbolForIR;
import IR.Values.BasicBlock;
import IR.Values.ConstantIR.ConstantInteger;
import IR.Values.ConstantIR.ConstantString;
import IR.Values.Function;
import IR.Values.InitValue;
import IR.Values.InstructionIR.Instruction;
import IR.Values.InstructionIR.PhiInst;
import IR.Values.Value;
import IR.types.ArrayType;
import IR.types.FunctionType;
import IR.types.PointerType;

import java.util.ArrayList;

public class LLVMTreePrinter {
    private Module module;
    private FilePrinter filePrinter = FilePrinter.getFilePrinter();

    public LLVMTreePrinter() {
        this.module = Module.getMyModule();
        dfs(this.module);
    }

    private void dfs(Module module) {
        filePrinter.outPrintlnLLVM("declare i32 @getint()");
        filePrinter.outPrintlnLLVM("declare void @putint(i32)");
        filePrinter.outPrintlnLLVM("declare void @putch(i32)");
        filePrinter.outPrintlnLLVM("declare void @putstr(i8*)");
        printGlobalStrings(module.getConstantStrings());
        printGlobalVariables(module.getGlobalVariables());
        for (Function function : module.getFunctions()) {
            printFunctions(function);
        }
    }

    private void printFunctions(Function function) {
        FunctionType functionType = (FunctionType) function.getType();
        filePrinter.outPrintlnLLVM("define " +
                functionType.getReturnType().toString() + " " + function.getName() +
                "(" + getFuncParams(function.getArguments()) + ") {");
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            printBasicBlock(basicBlock);
        }
        filePrinter.outPrintlnLLVM("}");
    }

    private String getFuncParams(ArrayList<Value> arguments) {
        if (arguments == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arguments.size(); i++) {
            Value argument = arguments.get(i);
            if (i == arguments.size() - 1) {
                sb.append(argument.getType()).append(" ").append(argument.getName());
                continue;
            }
            sb.append(argument.getType()).append(" ").append(argument.getName()).append(", ");
        }
        return sb.toString();
    }

    private void printBasicBlock(BasicBlock basicBlock) {
        if (basicBlock.getLabel() != null) {
            filePrinter.outPrintlnLLVM("\t" + basicBlock.getLabel().getName().substring(1) + ":");
        }
//        if (basicBlock.getLabel().getName().equals("%Label_11")) {
//            System.out.println(1);
//        }
        for (PhiInst phiInst : basicBlock.getPhiInstructions()) {
            filePrinter.outPrintlnLLVM("\t\t" + phiInst.toString());
        }
        for (Instruction instruction : basicBlock.getInstructions()) {
            filePrinter.outPrintlnLLVM("\t\t" + instruction.toString());
        }
    }

    private void printGlobalStrings(ArrayList<ConstantString> globalStrings) {
        for (ConstantString string : globalStrings) {
            int len = string.getString().length() + 1;
            for (int i = 0; i < string.getString().length(); i++) {
                if (string.getString().charAt(i) == '\\')
                    len--;
            }
            filePrinter.outPrintlnLLVM(
                    string.getName() + " = constant [" +
                            len + " x i8] c\"" +
                    string.getString().replace("\\n", "\\0a") + "\\00\""
            );
        }
    }

    private void printGlobalVariables(ArrayList<SymbolForIR> globalVariables) {
        for (SymbolForIR gb : globalVariables) {
            if (gb.getType() instanceof ArrayType) {
//                System.out.println(1);
                Value arrayValue = gb.getValue();
                if (gb.isConstant()) {
                    filePrinter.outPrintlnLLVM(arrayValue.getName() + " = constant " +
                            gb.getInitValue().toString());
                } else {
                    filePrinter.outPrintlnLLVM(arrayValue.getName() + " = global " +
                            gb.getInitValue().toString());
                }
            } else {
                if (gb.isConstant()) {
                    filePrinter.outPrintlnLLVM(
                            gb.getAftName() + " = constant " +
                                    gb.getInitValue().getType() + " " +
                                    ((ConstantInteger)gb.getInitValue().getValue()).getValue()
                    );
                } else {
                    filePrinter.outPrintlnLLVM(
                            gb.getAftName() + " = global " +
                                    gb.getInitValue().getType() + " " +
                                    ((ConstantInteger)gb.getInitValue().getValue()).getValue()
                    );
                }
            }

        }
    }


}
