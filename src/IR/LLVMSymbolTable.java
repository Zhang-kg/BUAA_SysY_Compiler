package IR;

import IR.SymbolTableForIR.SymbolTableForIR;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

public class LLVMSymbolTable extends SymbolTableForIR {
//    private ArrayList<SymbolTableItem> globalVariablesLLVM;
//    private ArrayList<SymbolTable> functionsLLVM;
//    private SymbolTable mainFuncTableLLVM;
    private TreeMap<String, Integer> globalIdentChangeTable;
    private HashSet<String> globalIdentSet;
//    private ArrayList<SymbolTableItem> variables;

    public LLVMSymbolTable() {
        super(null, 0);
//        globalVariablesLLVM = new ArrayList<>();
//        functionsLLVM = new ArrayList<>();
//        mainFuncTableLLVM = new SymbolTable(this, 0);
        globalIdentChangeTable = new TreeMap<>();
        globalIdentSet = new HashSet<>();
//        variables = new ArrayList<>();
    }

//    public SymbolTable getMainFuncTableLLVM() {
//        /*
//        * 现在需要在MainFuncTable中干什么吗？
//        * 经过我的变量处理，感觉所有的作用域都已经打通了
//         */
//        return this.mainFuncTableLLVM;
//    }

    public String addIdent(String befName) {
        if (befName.charAt(0) != '%')
            befName = "%" + befName;
        String aftName = befName;
        if (globalIdentChangeTable.containsKey(befName) || globalIdentSet.contains(befName)) {
            int i = 1;
            while (globalIdentSet.contains(aftName) || globalIdentChangeTable.containsKey(aftName)) {
                aftName = befName + i;
                i++;
            }
            globalIdentChangeTable.put(befName, i);
            globalIdentSet.add(aftName);
        } else {
            globalIdentSet.add(befName);
        }
        return aftName;
    }
}
