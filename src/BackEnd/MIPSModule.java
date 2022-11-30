package BackEnd;

import IR.SymbolTableForIR.SymbolForIR;
import IR.Values.ConstantIR.ConstantString;
import IR.Values.Function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MIPSModule {
    private ArrayList<MIPSFunction> mipsFunctions;
    private ArrayList<SymbolForIR> globalVariables;
    private ArrayList<ConstantString> constantStrings;
    private HashMap<String, VirtualReg> globalStringVirtualRegHashMap;
    private static MIPSModule mipsModule = new MIPSModule();

    private MIPSModule() {
        this.mipsFunctions = new ArrayList<>();
    }

    public static MIPSModule getMipsModule() {
        return mipsModule;
    }

    public void setGlobalStringVirtualRegHashMap(HashMap<String, VirtualReg> globalStringVirtualRegHashMap) {
        this.globalStringVirtualRegHashMap = globalStringVirtualRegHashMap;
    }

    public void setGlobalVariables(ArrayList<SymbolForIR> globalVariables) {
        this.globalVariables = globalVariables;
    }

    public void setConstantStrings(ArrayList<ConstantString> constantStrings) {
        this.constantStrings = constantStrings;
    }

    public void addMIPSFunction(MIPSFunction mipsFunction) {
        this.mipsFunctions.add(mipsFunction);
    }

    public String genMIPSFromMIPSModule() {
        StringBuilder sb = new StringBuilder();
        // ? 输出数据段
        sb.append(".data\n");
        // * 输出全局变量
        for (SymbolForIR symbol : globalVariables) {
            sb.append(parseGlobalVariable(symbol));
        }
        // * 输出字符串
        TreeMap<String, String> stringStringTreeMap = parseString();
        for (Map.Entry<String, String> entry : stringStringTreeMap.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            sb.append(name + ": .asciiz \"" + value + "\"\n");
        }
        // ? 输出代码段
        sb.append(".text\njal main\nli $v0, 10\nsyscall\n");
        sb.append("getint:\n" +
                "li $v0, 5\n" +
                "syscall\n" +
                "jr $ra\n" +
                "\n" +
                "putstr:\n" +
                "lw $a0, 0($sp)\n" +
                "li $v0, 4\n" +
                "syscall\n" +
                "jr $ra\n" +
                "\n" +
                "putint:\n" +
                "lw $a0, 0($sp)\n" +
                "li $v0, 1\n" +
                "syscall\n"+
                "jr $ra\n" +
                "\n");
        // * 函数代码
        for (MIPSFunction function : mipsFunctions) {
            sb.append(function.genMIPSFromMIPSFunction(globalStringVirtualRegHashMap));
        }
        return sb.toString();
    }

    private String parseGlobalVariable(SymbolForIR symbol) {
        StringBuilder sb = new StringBuilder();
        sb.append(symbol.getAftName().substring(1));
        sb.append(": .word ");
        sb.append(symbol.getInitValue().genMIPSGlobalVariableInit());
        sb.append("\n");
        return sb.toString();
    }

    private TreeMap<String, String> parseString() {
        TreeMap<String, String> strings = new TreeMap<>();
        for (ConstantString string : constantStrings) {
            strings.put(string.getName().substring(1), string.getString());
        }
        return strings;
    }
}
