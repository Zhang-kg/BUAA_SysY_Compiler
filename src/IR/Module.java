package IR;

import ErrorDetect.SymbolTable;
import IR.Values.BasicBlock;
import IR.Values.ConstantIR.ConstantString;
import IR.Values.Function;
import IR.Values.GlobalVariable;
import SysYTokens.MainFuncDef;
import TokenDefines.Token;
import TokenDefines.TokenType;

import java.util.ArrayList;

public class Module {
    private ArrayList<GlobalVariable> globalVariables;
    private ArrayList<ConstantString> constantStrings;
    private ArrayList<Function> functions;
    private BasicBlock globalBasicBlock;


    private static Module myModule = new Module();

    private Module() {
        globalVariables = new ArrayList<>();
        functions = new ArrayList<>();
        globalBasicBlock = new BasicBlock();
        constantStrings = new ArrayList<>();
//        llvmSymbolTable = new LLVMSymbolTable();
    }

    public static Module getMyModule() {
        return myModule;
    }

    public BasicBlock getGlobalBasicBlock() {
        return globalBasicBlock;
    }

    public void addFunction(Function function) {
        this.functions.add(function);
    }

    public ArrayList<Function> getFunctions() {
        return functions;
    }

    public void setConstantStrings(ArrayList<ConstantString> constantStrings) {
        this.constantStrings = constantStrings;
    }

    public ArrayList<ConstantString> getConstantStrings() {
        return constantStrings;
    }
}
