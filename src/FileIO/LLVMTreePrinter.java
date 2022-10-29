package FileIO;

import IR.Module;
import IR.Values.BasicBlock;
import IR.Values.Function;
import IR.Values.InstructionIR.Instruction;
import IR.Values.Value;
import IR.types.FunctionType;

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
        for (Function function : module.getFunctions()) {
            printFunctions(function);
        }
    }

    private void printFunctions(Function function) {
        FunctionType functionType = (FunctionType) function.getType();
        filePrinter.outPrintlnLLVM("declare " +
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
            filePrinter.outPrintlnLLVM(basicBlock.getLabel().getName() + ":");
        }
        for (Instruction instruction : basicBlock.getInstructions()) {
            filePrinter.outPrintlnLLVM(instruction.toString());
        }
    }
}
