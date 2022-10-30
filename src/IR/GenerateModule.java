package IR;

import IR.SymbolTableForIR.SymbolForIR;
import IR.SymbolTableForIR.SymbolTableForIR;
import IR.Values.BasicBlock;
import IR.Values.ConstantIR.ConstantInteger;
import IR.Values.ConstantIR.ConstantString;
import IR.Values.Function;
import IR.Values.InitValue;
import IR.Values.InstructionIR.*;
import IR.Values.InstructionIR.TerminatorIR.BrInst;
import IR.Values.InstructionIR.TerminatorIR.CallInst;
import IR.Values.InstructionIR.TerminatorIR.RetInst;
import IR.Values.Value;
import IR.types.*;
import TokenDefines.Token;
import TokenDefines.TokenType;

import java.util.*;

public class GenerateModule {
    private SymbolTableForIR rootTable;
//    private LLVMSymbolTable llvmSymbolTable;
    private ArrayList<Use> useArrayList;
    private BasicBlock currentBasicBlock = null;
    private Function currentFunction = null;
    private HashMap<String, Function> declareFunctions = null;
    private ArrayList<SymbolForIR> globalVariables = new ArrayList<>();
    private ArrayList<ConstantString> globalString = new ArrayList<>();
    private TreeMap<String, Integer> globalIdentChangeTable  = new TreeMap<>();
    private HashSet<String> globalIdentSet = new HashSet<>();
    private boolean isGlobal = true;

    public String addIdent(String befName) {
        if (!isGlobal) {
            if (befName.charAt(0) != '%')
                befName = "%" + befName;
        } else {
            if (befName.charAt(0) != '@')
                befName = "@" + befName;
        }
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

    public GenerateModule() {
        rootTable = new SymbolTableForIR(null, 0);
//        llvmSymbolTable = new LLVMSymbolTable();
        useArrayList = new ArrayList<>();
        declareFunctions = new HashMap<>();
        declareFunctions.put("@getint", new Function("@getint", new FunctionType(null, IntType.i32)));
        declareFunctions.put("@putstr", new Function("@putstr", new FunctionType(
                new ArrayList<>(Arrays.asList(new PointerType(IntType.i8))), VoidType.voidType
        )));
        declareFunctions.put("@putint", new Function("@putint", new FunctionType(
                new ArrayList<>(Arrays.asList(IntType.i32)), VoidType.voidType
        )));
        declareFunctions.put("@putch", new Function("@putch", new FunctionType(
                new ArrayList<>(Arrays.asList(IntType.i32)), VoidType.voidType
        )));
    }

    public void parseModule(Token root) {
        ArrayList<Token> sons = root.getSons();
        for (Token son : sons) {
            isGlobal = true;
            switch (son.getTokenType()) {
                case Decl:
//                    System.out.println("parse Decl");
                    parseDeclForIR(son, rootTable);
                    break;
                case FuncDef:
//                    System.out.println("parse funcDef");
                    parseFuncDefForIR(son, rootTable);
                    break;
                case MainFuncDef: {
//                    System.out.println("parse mainFuncDef");
//                    Function mainFunction = new Function("MAIN_FUNC", new FunctionType(null, null));
//                    currentFunction = mainFunction;
//                    Module.getMyModule().addFunction(mainFunction);
//                    currentBasicBlock = mainFunction.getBasicBlocks().get(0);
                    parseMainFuncDefForIR(son, rootTable);
//                    currentFunction = null;
//                    currentBasicBlock = null;
                    // set main func return type;
                    break;
                }
            }
        }
        Module.getMyModule().setConstantStrings(globalString);
        Module.getMyModule().setGlobalVariables(globalVariables);
    }

    private void checkBasicBlockTerminate(Function function, Type type) {
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            if (!basicBlock.isTerminated()) {
                if (type == VoidType.voidType) {
                    new RetInst(basicBlock);
                } else {
                    new RetInst(basicBlock, ConstantInteger.zero);
                }
            }
        }
    }

    private void parseDeclForIR(Token decl, SymbolTableForIR currentTable) {
        ArrayList<Token> sons = decl.getSons();
        for (Token son : sons) {
            switch (son.getTokenType()) {
                case ConstDecl: {
                    parseConstDeclForIR(son, currentTable);
                    break;
                }
                case VarDecl: {
                    parseVarDeclForIR(son, currentTable);
                    break;
                }
            }
        }
    }

    private void parseConstDeclForIR(Token constDecl, SymbolTableForIR currentTable) {
        ArrayList<Token> sons = constDecl.getSons();
        Type type = IntType.i32;
        for (Token son : sons) {
            switch (son.getTokenType()) {
                case BType: {
                    type = parseBTypeForIR(son);
                    break;
                }
                case ConstDef: {
                    parseConstDefForIR(son, currentTable, type);
                    break;
                }
            }
        }
    }

    private Type parseBTypeForIR(Token bType) {
        ArrayList<Token> sons = bType.getSons();
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.INTTK) {
                return IntType.i32;
            }
        }
        return null;
    }

    private void parseConstDefForIR(Token constDef, SymbolTableForIR currentTable, Type type) {
        int dimensions = 0;
        String befName, aftName = null;
        ArrayList<Token> sons = constDef.getSons();
        SymbolForIR constDefSymbol = new SymbolForIR();
        boolean isVariable = true;
        boolean isInitial = false;
        ArrayList<ConstantInteger> dims = new ArrayList<>();
        constDefSymbol.setConstant(true);
        Value pointer;
        InitValue initValue = null;
        for (Token son : sons) {
            switch (son.getTokenType()) {
                case IDENFR: {
                    befName = son.getTokenString();
                    constDefSymbol.setName(befName);
                    aftName = addIdent(son.getTokenString());
                    constDefSymbol.setAftName(aftName);
                    break;
                }
                case ConstExp: {
                    isVariable = false;
                    dimensions++;
                    ConstantInteger dimValue = parseConstExpForIR(son, currentTable);
                    dims.add(dimValue);
                    break;
                }
                case ConstInitVal: {
                    isInitial = true;
                    initValue = parseConstInitValForIR(son, currentTable);
                    break;
                }
            }
        }
        assert aftName != null;
        if (isVariable) {   // 变量
            constDefSymbol.setType(type);
            constDefSymbol.setValue(new Value(new PointerType(type), aftName)); // this is pointer
            constDefSymbol.setConstValue(initValue);
            if (!isGlobal) {
                new AllocaInst(currentBasicBlock, aftName, true, type);
                new StoreInst(currentBasicBlock, constDefSymbol.getValue(), initValue.getValue());
            } else {
                globalVariables.add(constDefSymbol);
            }
        } else {    // 数组
            Type finalArrayType = IntType.i32;
            for (int i = dims.size() - 1; i >= 0; i--) {
                ConstantInteger dimValue = dims.get(i);
                int dimValuenum = dimValue.getValue();
                finalArrayType = new ArrayType(finalArrayType, dimValuenum);
            }
            constDefSymbol.setType(finalArrayType);
            constDefSymbol.setValue(new Value(new PointerType(finalArrayType), aftName));
            new AllocaInst(currentBasicBlock, aftName, true, finalArrayType);
            if (isInitial) {    // 赋初值，常量一定有
                // ! 熟悉之后再做一下
            }
        }
        currentTable.addItem(constDefSymbol);   // * add this to symbol table
    }

    private InitValue parseConstInitValForIR(Token constInitVal, SymbolTableForIR currentTable) {
        ArrayList<Token> sons = constInitVal.getSons();
        InitValue constInitValue = null;
        boolean isVariable = true;
        ConstantInteger constantInteger;
        ArrayList<InitValue> arrayList = new ArrayList<>();
        for (Token son : sons) {
            switch (son.getTokenType()) {
                case ConstExp: {
                    constantInteger = parseConstExpForIR(son, currentTable);
//                    assert constantInteger != null;
                    constInitValue = new InitValue(constantInteger);
                    break;
                }
                case ConstInitVal: {
                    isVariable = false;
                    InitValue arrayInitVal = parseConstInitValForIR(son, currentTable);
                    arrayList.add(arrayInitVal);
                    break;
                }
            }
        }
        if (!isVariable) {
            constInitValue = new InitValue(arrayList);
        }
        assert constInitValue != null;
        return constInitValue;
    }

    private void parseVarDeclForIR(Token varDecl, SymbolTableForIR currentTable) {
        ArrayList<Token> sons = varDecl.getSons();
        Type type = null;
        for (Token son : sons) {
            switch (son.getTokenType()) {
                case BType: {
                    type = parseBTypeForIR(son);
                    break;
                }
                case VarDef: {
                    parseVarDefForIR(son, currentTable, type);
                    break;
                }
            }
        }
    }

    private void parseVarDefForIR(Token varDef, SymbolTableForIR currentTable, Type type) {
        String befName = null, aftName = null;
        boolean isVariable = true;
        boolean isInitial = false;
        int dimensions = 0;
        ArrayList<ConstantInteger> dims = new ArrayList<>();
        ArrayList<Token> sons = varDef.getSons();
        SymbolForIR varDefSymbol = new SymbolForIR();
        varDefSymbol.setConstant(false);
        Value pointer;
        InitValue initValue = null;
        for (Token son : sons) {
            switch (son.getTokenType()) {
                case IDENFR: {
                    befName = son.getTokenString();
                    varDefSymbol.setName(befName);
                    aftName = addIdent(befName);
                    varDefSymbol.setAftName(aftName);
                    break;
                }
                case ConstExp: {
                    isVariable = false;
                    dimensions++;
                    ConstantInteger dimValue = parseConstExpForIR(son, currentTable);
                    dims.add(dimValue);
                    break;
                }
                case InitVal: {
                    isInitial = true;
                    initValue = parseInitValForIR(son, currentTable);
                    break;
                }
            }
        }
        assert aftName != null;
        if (isVariable) {
            varDefSymbol.setType(type);
            varDefSymbol.setValue(new Value(new PointerType(type), aftName));
            if (!isGlobal) {
                new AllocaInst(currentBasicBlock, aftName, false, type);
                if (isInitial) {
                    new StoreInst(currentBasicBlock, varDefSymbol.getValue(), initValue.getValue());
                }
            } else {    // 如果是全局的变量
                globalVariables.add(varDefSymbol);
                if (!isInitial) {   // 如果没赋初始值
                    // 默认赋成0
                    initValue = new InitValue(ConstantInteger.zero);
                }
                varDefSymbol.setConstValue(initValue);
            }
            currentTable.addItem(varDefSymbol);
        } else {
            Type finalArrayType = IntType.i32;
            for (int i = dims.size() - 1; i >= 0; i--) {
                ConstantInteger dimValue = dims.get(i);
                int dimValueNum = dimValue.getValue();
                finalArrayType = new ArrayType(finalArrayType, dimValueNum);
            }
            varDefSymbol.setType(finalArrayType);
            varDefSymbol.setValue(new Value(new PointerType(finalArrayType), aftName));
            new AllocaInst(currentBasicBlock, aftName, false, finalArrayType);
            if (isInitial) {
                // ! 熟悉之后做一下
            }
            currentTable.addItem(varDefSymbol);
        }
    }

    private InitValue parseInitValForIR(Token initVal, SymbolTableForIR currentTable) {
        ArrayList<Token> sons = initVal.getSons();
        InitValue initValue = null;
        boolean isVariable = true;
        Value variableInitValue;
        ArrayList<InitValue> arrayList = new ArrayList<>();
        for (Token son : sons) {
            switch (son.getTokenType()) {
                case Exp: {
                    variableInitValue = parseExpForIR(son, currentTable);
//                    assert constantInteger != null;
                    initValue = new InitValue(variableInitValue);
                    break;
                }
                case InitVal: {
                    isVariable = false;
                    InitValue arrayInitVal = parseInitValForIR(son, currentTable);
                    arrayList.add(arrayInitVal);
                    break;
                }
            }
        }
        if (!isVariable) {
            initValue = new InitValue(arrayList);
        }
        assert initValue != null;
        return initValue;
    }

    private Type parseFuncDefForIR(Token funcDef, SymbolTableForIR currentTable) {
        ArrayList<Token> sons = funcDef.getSons();
        Type retType = null;
        String befFuncName = null, aftFuncName = null;
        SymbolTableForIR sonTable = null;
        ArrayList<Value> arguments = null;
        SymbolForIR functionSymbol = new SymbolForIR();

        Function function = new Function(null, null);
        // * 将全局变量currentFunction改变
        Function befFunction = currentFunction;
        currentFunction = function;
        BasicBlock befBasicBlock = null;
        FunctionType functionType = new FunctionType(null, null);
        for (Token son : sons) {
            switch (son.getTokenType()) {
                case FuncType: {
                    retType = parseFuncTypeForIR(son);
                    functionType.setReturnType(retType);
                    break;
                }
                case IDENFR: {
                    befFuncName = son.getTokenString();
                    aftFuncName = addIdent(befFuncName);
                    function.setName(aftFuncName);
                    functionSymbol.setName(befFuncName);
                    functionSymbol.setAftName(aftFuncName);
                    break;
                }
                case FuncFParams: {
                    isGlobal = false;
                    sonTable = new SymbolTableForIR(currentTable, currentTable.getIndex());
                    befBasicBlock = currentBasicBlock;
                    currentBasicBlock = function.getBasicBlocks().get(0);
                    arguments = parseFuncFParamsForIR(son, sonTable);
                    // ! 计算一下arguments Types
                    currentFunction.setArguments(arguments);
                    ArrayList<Type> argumentTypes = currentFunction.getArgumentsType();
                    functionType.setArgumentsType(argumentTypes);
                    currentBasicBlock = befBasicBlock;
                    isGlobal = true;
                    break;
                }
                case Block: {
                    isGlobal = false;
                    if (sonTable == null) {
                        sonTable = new SymbolTableForIR(currentTable, currentTable.getIndex());
                    }
                    befBasicBlock = currentBasicBlock;
                    currentBasicBlock = function.getBasicBlocks().get(0);
                    parseBlockForIR(son, sonTable);
                    currentBasicBlock = befBasicBlock;
                    isGlobal = true;
                    break;
                }
            }
        }
        function.setType(functionType);
        functionSymbol.setType(functionType);
        functionSymbol.setValue(function);
        functionSymbol.setConstant(false);
        Module.getMyModule().addFunction(function); // add Function to current Module
        currentTable.addItem(functionSymbol);
        // * 将currentFunction改回原来的Function
        checkBasicBlockTerminate(currentFunction, functionType.getReturnType());
        currentFunction = befFunction;
        return null;
    }

    private void parseMainFuncDefForIR(Token mainFuncDef, SymbolTableForIR currentTable) {
        /*
         * currentTable: 代表层次结构的Table
         * tableForIR: 并非是层次结构的。对于MainSymbolTable，内部没有子表，只有表项
         */
        SymbolTableForIR sonTable;
        ArrayList<Token> sons = mainFuncDef.getSons();
//        Function mainFunction = new Function(null, null);
        Function mainFunction = new Function("@main", new FunctionType(new ArrayList<>(), IntType.i32));
        Module.getMyModule().addFunction(mainFunction);
        Function befFunction = currentFunction;
        currentFunction = mainFunction;
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.Block) {
                isGlobal = false;
                BasicBlock befBasicBlock = currentBasicBlock;
                currentBasicBlock = mainFunction.getBasicBlocks().get(0);
                sonTable = new SymbolTableForIR(currentTable, currentTable.getIndex());
                parseBlockForIR(son, sonTable);
                currentBasicBlock = befBasicBlock;
                isGlobal = true;
            }
        }
        checkBasicBlockTerminate(currentFunction, IntType.i32);
        currentFunction = befFunction;
    }

    private Type parseFuncTypeForIR(Token funcType) {
        ArrayList<Token> sons = funcType.getSons();
        for (Token son : sons) {
            switch (son.getTokenType()) {
                case INTTK: {
                    return IntType.i32;
                }
                case VOIDTK: {
                    return VoidType.voidType;
                }
            }
        }
        return IntType.i32;
    }

    private ArrayList<Value> parseFuncFParamsForIR(Token funcFParams, SymbolTableForIR currentTable) {
        // * 内部需要将这些值保存到局部变量中
        ArrayList<Token> sons = funcFParams.getSons();
        ArrayList<Value> arguments = new ArrayList<>();
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.FuncFParam) {
                arguments.add(parseFuncFParamForIR(son, currentTable));
            }
        }
        return arguments;
    }

    private Value parseFuncFParamForIR(Token funcFParam, SymbolTableForIR currentTable) {
        // * 内部需要将这些值保存到局部变量中
        ArrayList<Token> sons = funcFParam.getSons();
        int dimensions = 0;
        String befName = null, aftName = null;
        SymbolForIR paramSymbol = new SymbolForIR();
        boolean isVariable = true;
        ArrayList<ConstantInteger> dims = new ArrayList<>();
        paramSymbol.setConstant(false);
        Value pointer;
        Type type = null;
        for (Token son : sons) {
            switch (son.getTokenType()) {
                case BType: {
                    type = parseBTypeForIR(son);
                    break;
                }
                case IDENFR: {
                    befName = son.getTokenString();
                    aftName = addIdent(befName);
                    paramSymbol.setName(befName);
                    break;
//                    paramSymbol.setAftName(aftName);
                }
                case ConstExp: {
                    isVariable = false;
                    ConstantInteger dimValue = parseConstExpForIR(son, currentTable);
                    dims.add(dimValue);
                    break;
                }
            }
        }
        assert aftName != null && type != null;
        String aftAftName = addIdent(aftName);
        // * 这个argument是返回的argument，其name应该是aftName，用于函数
        // * 定义是括号中的声明
        Value argument = new Value(null, aftName);
        // * paramSymbol是插入符号表中的symbol，当后面的变量使用参数是，找到的应该是它
        // * 这个Symbol setValue 时应该是一个PointerType，这是后来使用alloc Inst分配的空间
        paramSymbol.setAftName(aftAftName);
        if (isVariable) {   // * 变量
            new AllocaInst(currentBasicBlock, aftAftName, false, type);
            paramSymbol.setType(type);
            argument.setType(type);
            paramSymbol.setValue(new Value(new PointerType(type), aftAftName));
            new StoreInst(currentBasicBlock, paramSymbol.getValue(), new Value(type, aftName));
        } else {            // * 数组
            Type finalArrayType = IntType.i32;
            for (int i = dims.size() - 1; i >= 0; i--) {
                ConstantInteger dimValue = dims.get(i);
                int dimValueNum = dimValue.getValue();
                finalArrayType = new ArrayType(finalArrayType, dimValueNum);
            }
            finalArrayType = new PointerType(finalArrayType);
            paramSymbol.setType(finalArrayType);
            argument.setType(finalArrayType);
            paramSymbol.setValue(new Value(new PointerType(finalArrayType), aftAftName));
            new AllocaInst(currentBasicBlock, aftAftName, false, finalArrayType);
            new StoreInst(currentBasicBlock, paramSymbol.getValue(), argument);
        }
        currentTable.addItem(paramSymbol);
        return argument;
    }

    private void parseBlockForIR(Token block, SymbolTableForIR currentTable) {
        /*
         * Block可能在函数、Main函数、或者任何嵌套内部中出现，不论出现什么，由于LLVM中符号表不变，所以tableForIR不变
         */
        ArrayList<Token> sons = block.getSons();
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.BlockItem) {
                parseBlockItemForIR(son, currentTable);
            }
        }
    }

    private void parseBlockItemForIR(Token blockItem, SymbolTableForIR currentTable) {
        ArrayList<Token> sons = blockItem.getSons();
        for (Token son : sons) {
            switch (son.getTokenType()) {
                case Decl: {
//                    System.out.println("Decl");
                    parseDeclForIR(son, currentTable);
                    break;
                }
                case Stmt: {
//                    System.out.println("Stmt");
                    parseStmtForIR(son, currentTable);
                    break;
                }
            }
        }
    }

    private void parseGetIntForIR() {

    }

    private void parseStmtForIR(Token stmt, SymbolTableForIR currentTable) {
        ArrayList<Token> sons = stmt.getSons();
        switch (sons.get(0).getTokenType()) {
            case Exp: {
                parseExpForIR(sons.get(0), currentTable);
                break;
            }
            case LVal: {
                Value lVal = null;
                Value valueFrom = null;
                for (Token son : sons) {
                    switch (son.getTokenType()) {
                        case LVal: {
                            lVal = parseLValForIR(son, currentTable, true);
                            break;
                        }
                        case GETINTTK: {
                            valueFrom = new CallInst(currentBasicBlock, declareFunctions.get("@getint"), null);
                            break;
                        }
                        case Exp: {
                            valueFrom = parseExpForIR(son, currentTable);
                            break;
                        }
                    }
                }
                new StoreInst(currentBasicBlock, lVal, valueFrom);
                break;
            }
            case Block: {
                SymbolTableForIR sonSymbolTableForIR = new SymbolTableForIR(currentTable, currentTable.getIndex());
                parseBlockForIR(sons.get(0), sonSymbolTableForIR);
                break;
            }
            case IFTK: {
                boolean hasElse = false;
                BasicBlock trueBasicBlock = new BasicBlock();   // * True Stmt
                BasicBlock elseBasicBlock = new BasicBlock();   // * Else Stmt
                BasicBlock postIfBasicBlock = new BasicBlock(); // * Aft if Block
                BasicBlock befBasicBlock = currentBasicBlock;
                Value cond = null;
                for (Token son : sons) {
                    switch (son.getTokenType()) {
                        case Cond: {
                            cond = parseCondForIR(son, currentTable);
                            break;
                        }
                        case ELSETK: {
                            hasElse = true;
                            break;
                        }
                        case Stmt: {
                            if (!hasElse) {
                                currentBasicBlock = trueBasicBlock;
                                parseStmtForIR(son, currentTable);
                            } else {
                                currentBasicBlock = postIfBasicBlock;
                                parseStmtForIR(son, currentTable);
                            }
                            break;
                        }
                    }
                }
                if (hasElse) {
                    // * add Basic && set Label
                    currentFunction.addBasicBlock(trueBasicBlock);
                    currentFunction.addBasicBlock(elseBasicBlock);
                    currentFunction.addBasicBlock(postIfBasicBlock);
                    currentBasicBlock = postIfBasicBlock;
//                    String labelName = LabelType.getNewLabelName();
//                    trueBasicBlock.setLabel(new Value(LabelType.getLabelType(), labelName));
//                    labelName = LabelType.getNewLabelName();
//                    elseBasicBlock.setLabel(new Value(LabelType.getLabelType(), labelName));
//                    labelName = LabelType.getNewLabelName();
//                    postIfBasicBlock.setLabel(new Value(LabelType.getLabelType(), labelName));
                    new BrInst(befBasicBlock, cond, trueBasicBlock.getLabel(), elseBasicBlock.getLabel());
                    new BrInst(trueBasicBlock, postIfBasicBlock.getLabel());
                    new BrInst(elseBasicBlock, postIfBasicBlock.getLabel());
                    currentBasicBlock = postIfBasicBlock;
                } else {
                    currentFunction.addBasicBlock(trueBasicBlock);
                    currentFunction.addBasicBlock(postIfBasicBlock);
//                    String labelName = LabelType.getNewLabelName();
//                    trueBasicBlock.setLabel(new Value(LabelType.getLabelType(), labelName));
//                    labelName = LabelType.getNewLabelName();
//                    postIfBasicBlock.setLabel(new Value(LabelType.getLabelType(), labelName));
                    new BrInst(befBasicBlock, cond, trueBasicBlock.getLabel(), postIfBasicBlock.getLabel());
                    new BrInst(trueBasicBlock, postIfBasicBlock.getLabel());
                    currentBasicBlock = postIfBasicBlock;
                }
                break;
            }
            case WHILETK: {
                BasicBlock condBasicBlock = new BasicBlock();
                BasicBlock trueBasicBlock = new BasicBlock();
                BasicBlock postWhileBasicBlock = new BasicBlock();
                BasicBlock befBasicBlock = currentBasicBlock;
                Value cond = null;
                for (Token son : sons) {
                    switch (son.getTokenType()) {
                        case Cond: {
                            currentBasicBlock = condBasicBlock;
                            cond = parseCondForIR(son, currentTable);
                            break;
                        }
                        case Stmt: {
                            currentBasicBlock = trueBasicBlock;
                            parseStmtForIR(son, currentTable);
                            break;
                        }
                    }
                }
                currentBasicBlock = postWhileBasicBlock;
                currentFunction.addBasicBlock(condBasicBlock);
                currentFunction.addBasicBlock(trueBasicBlock);
                currentFunction.addBasicBlock(postWhileBasicBlock);
//                String labelName = LabelType.getNewLabelName();
//                condBasicBlock.setLabel(new Value(LabelType.getLabelType(), labelName));
//                labelName = LabelType.getNewLabelName();
//                trueBasicBlock.setLabel(new Value(LabelType.getLabelType(), labelName));
//                labelName = LabelType.getNewLabelName();
//                postWhileBasicBlock.setLabel(new Value(LabelType.getLabelType(), labelName));
                new BrInst(befBasicBlock, condBasicBlock.getLabel());
                new BrInst(condBasicBlock, cond, trueBasicBlock.getLabel(), postWhileBasicBlock.getLabel());
                new BrInst(trueBasicBlock, condBasicBlock.getLabel());
                break;
            }
            case BREAKTK: {
//                System.out.println("didn't parse Break");
                break;
            }
            case CONTINUETK: {
//                System.out.println("didn't parse continue");
                break;
            }
            case RETURNTK: {
                Value exp = null;
                for (Token son : sons) {
                    if (son.getTokenType() == TokenType.Exp) {
                        exp = parseExpForIR(son, currentTable);
                    }
                }
                // ! 这里Basic Block如果套着if或者while如何解决，与continue和break一起想想
                if (exp != null) {
                    new RetInst(currentBasicBlock, exp);
                } else {
                    new RetInst(currentBasicBlock);
                }
                break;
            }
            case PRINTFTK: {
                ArrayList<String> strings = new ArrayList<>();
                ArrayList<Value> stringValues = new ArrayList<>();
                String strstr = "";
                ArrayList<Value> exps = new ArrayList<>();
                for (Token son : sons) {
                    switch (son.getTokenType()) {
                        case STRCON: {
                            strstr = parseString(son.getTokenString());
                            strings = parseStrForIR(strstr);
                            strings.removeIf(e -> e.length() == 0);
                            break;
                        }
                        case Exp: {
                            exps.add(parseExpForIR(son, currentTable));
                            break;
                        }
                    }
                }
                for (String string : strings) {
                    int len = string.length() + 1;
                    for (int i = 0; i < string.length(); i++) {
                        if (string.charAt(i) == '\\')
                            len--;
                    }
//                    ConstantString constantString = new ConstantString(new PointerType(IntType.i8), string);
                    ConstantString constantString = new ConstantString(new PointerType(new ArrayType(IntType.i8, len)), string);
                    stringValues.add(constantString);
                    globalString.add(constantString);   // ? 全局信息中添加符号串信息
                }
                int expIndex = 0;
                int strIndex = 0;
                int i = 0;
                while (i < strstr.length()) {
                    if (i + 1 < strstr.length() && strstr.charAt(i) == '%' && strstr.charAt(i + 1) == 'd') {
                        new CallInst(currentBasicBlock, declareFunctions.get("@putint"),
                                new ArrayList<>(Arrays.asList(exps.get(expIndex))));
                        expIndex++;
                        i += 2;
                    } else {
//                        Value value = new LoadInst(currentBasicBlock, stringValues.get(strIndex));
//                        Value value1 = new GEPInst()// TODO：转化成GEP指令
                        Value value1 = new GEPInst(currentBasicBlock, stringValues.get(strIndex));
                        ArrayList<Value> arguments = new ArrayList<>();
                        arguments.add(value1);
                        new CallInst(currentBasicBlock, declareFunctions.get("@putstr"),
                                arguments);
                        i += strings.get(strIndex).length();
                        strIndex++;
                    }
                }
                break;
            }
        }
    }

    private String parseString(String str) {
        if (str.charAt(0) == '\"')
            str = str.substring(1);
        if (str.charAt(str.length() - 1) == '\"')
            str = str.substring(0, str.length() - 1);
        return str;
    }

    private ArrayList<String> parseStrForIR(String str) {
        return new ArrayList<>(Arrays.asList(str.split("%d")));
    }

    private void generateBranch(Value cond) {

    }

    private Value parseExpForIR(Token exp, SymbolTableForIR currentTable) {
        ArrayList<Token> sons = exp.getSons();
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.AddExp) {
                return parseAddExpForIR(son, currentTable);
            }
        }
        System.out.println("Wrong parse Exp for IR");
        return null;
    }

    private Value parseCondForIR(Token cond, SymbolTableForIR currentTable) {
        ArrayList<Token> sons = cond.getSons();
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.LOrExp) {
                return parseLOrExpForIR(son, currentTable);
            }
        }
        System.out.println("Wrong parse LOrExp for IR");
        return null;
    }

    private Value parseLValForIR(Token lVal, SymbolTableForIR currentTable, boolean left) {
        ArrayList<Token> sons = lVal.getSons();
        ArrayList<Value> expValues = new ArrayList<>();
        Value identValue = null;
        boolean isVariable = true;
        boolean isConstant = false;
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.IDENFR) {
                String ident = son.getTokenString();
                SymbolForIR identSymbol = currentTable.findIdentInAllTable(ident);
                if (identSymbol.isConstant()) { // ? 常数
                    isConstant = true;
                    identValue = identSymbol.getConstValue();
                } else {
                    identValue = identSymbol.getValue();
                }
            } else if (son.getTokenType() == TokenType.Exp) {
                isVariable = false;
                expValues.add(parseExpForIR(son, currentTable));
            }
        }
        if (!isVariable) {
            Value gepInst = identValue;
            for (Value expValue : expValues) {
                assert identValue != null;
                gepInst = new GEPInst(currentBasicBlock, gepInst, expValue);
            }
            if (left) {
                return gepInst;
            } else {
                assert gepInst != null;
                return new LoadInst(currentBasicBlock, gepInst);
            }
        } else {    // * 是变量
            if (left) {
                return identValue;
            } else {
                assert identValue != null;
                if (isConstant) {
                    assert ((InitValue)identValue).getValue() instanceof ConstantInteger;
                    return ((InitValue)identValue).getValue();
                }
                return new LoadInst(currentBasicBlock, identValue);
            }
        }
    }

    private Value parsePrimaryExpForIR(Token primaryExp, SymbolTableForIR currentTable) {
        ArrayList<Token> sons = primaryExp.getSons();
        Value sonValue = null;
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.Exp) {
                sonValue = parseExpForIR(son, currentTable);
            } else if (son.getTokenType() == TokenType.LVal) {
                sonValue = parseLValForIR(son, currentTable, false);
            } else if (son.getTokenType() == TokenType.Number) {
                sonValue = parseNumberForIR(son);
            }
        }
        return sonValue;
    }

    private Value parseNumberForIR(Token number) {
        ArrayList<Token> sons = number.getSons();
        return new ConstantInteger(IntType.i32,
                sons.get(0).getTokenString(),
                Integer.parseInt(sons.get(0).getTokenString()));
    }

    private Value parseUnaryExpForIR(Token unaryExp, SymbolTableForIR currentTable) {
        ArrayList<Token> sons = unaryExp.getSons();
        switch (sons.get(0).getTokenType()) {
            case PrimaryExp: {
                return parsePrimaryExpForIR(sons.get(0), currentTable);
            }
            case IDENFR: {
                SymbolForIR function = currentTable.findIdentInAllTable(sons.get(0).getTokenString());
                ArrayList<Value> params = new ArrayList<>();
                for (Token son : sons) {
                    if (son.getTokenType() == TokenType.FuncRParams) {
                        params = parseFuncRParamsForIR(son, currentTable);
                    }
                }
                return new CallInst(currentBasicBlock, (Function) function.getValue(), params);
            }
            case UnaryOp: {
                InstructionType instructionType = parseUnaryOpForIR(sons.get(0));
                Value unaryExpValue = null;
                for (Token son : sons) {
                    if (son.getTokenType() == TokenType.UnaryExp) {
                        unaryExpValue = parseUnaryExpForIR(son, currentTable);
                    }
                }
                if (instructionType == InstructionType.ADD) {
                    return unaryExpValue;
                } else if (instructionType == InstructionType.NOT) {
                    return new IcmpInst(currentBasicBlock, InstructionType.EQ, ConstantInteger.zero, unaryExpValue);
                }
                if (unaryExpValue instanceof ConstantInteger) {
                    if (instructionType == InstructionType.SUB) {
                        return new ConstantInteger(unaryExpValue.getType(),
                                String.valueOf(-((ConstantInteger) unaryExpValue).getValue()),
                                -((ConstantInteger) unaryExpValue).getValue());
                    }
                } else {
                    if (instructionType == InstructionType.SUB) {
                        return new BinaryOperator(currentBasicBlock, InstructionType.SUB, ConstantInteger.zero, unaryExpValue);
                    }
                }
                break;
            }
        }
        return null;
    }

    private InstructionType parseUnaryOpForIR(Token unaryOp) {
        ArrayList<Token> sons = unaryOp.getSons();
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.PLUS) {
                return InstructionType.ADD;
            } else if (son.getTokenType() == TokenType.MINU) {
                return InstructionType.SUB;
            } else if (son.getTokenType() == TokenType.NOT) {
                return InstructionType.NOT;
            }
        }
        return null;
    }

    private ArrayList<Value> parseFuncRParamsForIR(Token funcRParams, SymbolTableForIR currentTable) {
        ArrayList<Token> sons = funcRParams.getSons();
        ArrayList<Value> exps = new ArrayList<>();
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.Exp) {
                exps.add(parseExpForIR(son, currentTable));
            }
        }
        return exps;
    }

    private Value binaryHelper(BasicBlock basicBlock,
                               InstructionType instructionType,
                               Value value1, Value value2) {
        if (value1.getType() != IntType.i32) {
            value1 = new ZextInst(basicBlock, value1, IntType.i32);
        }
        if (value2.getType() != IntType.i32) {
            value2 = new ZextInst(basicBlock, value2, IntType.i32);
        }
        if (value1 instanceof ConstantInteger && value2 instanceof ConstantInteger) {
            int ans = 0;
            switch (instructionType) {
                case ADD: {
                    ans = ((ConstantInteger) value1).getValue() + ((ConstantInteger) value2).getValue();
                    break;
                }
                case SUB: {
                    ans = ((ConstantInteger) value1).getValue() - ((ConstantInteger) value2).getValue();
                    break;
                }
                case MUL: {
                    ans = ((ConstantInteger) value1).getValue() * ((ConstantInteger) value2).getValue();
                    break;
                }
                case DIV: {
                    ans = ((ConstantInteger) value1).getValue() / ((ConstantInteger) value2).getValue();
                    break;
                }
                case SREM: {
                    ans = ((ConstantInteger) value1).getValue() % ((ConstantInteger) value2).getValue();
                    break;
                }
            }
            return new ConstantInteger(IntType.i32, String.valueOf(ans), ans);
        } else {
            return new BinaryOperator(basicBlock, instructionType, value1, value2);
        }
    }

    private Value parseMulExpForIR(Token mulExp, SymbolTableForIR currentTable) {
        ArrayList<Token> sons = mulExp.getSons();
        Value lastValue = null;
        String op = "";
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.UnaryExp) {
                if (lastValue == null) {
                    lastValue = parseUnaryExpForIR(son, currentTable);
                    continue;
                }
                Value value = parseUnaryExpForIR(son, currentTable);
                if (op.equals("*")) {
//                    lastValue = new BinaryOperator(currentBasicBlock, InstructionType.MUL, lastValue, value);
                    lastValue = binaryHelper(currentBasicBlock, InstructionType.MUL, lastValue, value);
                } else if (op.equals("/")) {
//                    lastValue = new BinaryOperator(currentBasicBlock, InstructionType.DIV, lastValue, value);
                    lastValue = binaryHelper(currentBasicBlock, InstructionType.DIV, lastValue, value);
                } else if (op.equals("%")) {
//                    lastValue = new BinaryOperator(currentBasicBlock, InstructionType.SREM, lastValue, value);
                    lastValue = binaryHelper(currentBasicBlock, InstructionType.SREM, lastValue, value);
                }
            } else if (son.getTokenType() == TokenType.MULT) {
                op = "*";
            } else if (son.getTokenType() == TokenType.DIV) {
                op = "/";
            } else if (son.getTokenType() == TokenType.MOD) {
                op = "%";
            }
        }
        return lastValue;
    }

    private Value parseAddExpForIR(Token addExp, SymbolTableForIR currentTable) {
        ArrayList<Token> sons = addExp.getSons();
        Value lastValue = null;
        String op = "";
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.MulExp) {
                if (lastValue == null) {
                    lastValue = parseMulExpForIR(son, currentTable);
                    continue;
                }
                Value value = parseMulExpForIR(son, currentTable);
                if (op.equals("+")) {
//                    lastValue = new BinaryOperator(currentBasicBlock, InstructionType.ADD, lastValue, value);
                    lastValue = binaryHelper(currentBasicBlock, InstructionType.ADD, lastValue, value);
                } else if (op.equals("-")) {
//                    lastValue = new BinaryOperator(currentBasicBlock, InstructionType.SUB, lastValue, value);
                    lastValue = binaryHelper(currentBasicBlock, InstructionType.SUB, lastValue, value);
                }
            } else if (son.getTokenType() == TokenType.PLUS) {
                op = "+";
            } else if (son.getTokenType() == TokenType.MINU) {
                op = "-";
            }
        }
        return lastValue;
    }

    private Value parseRelExpForIR(Token relExp, SymbolTableForIR currentTable) {
        ArrayList<Token> sons = relExp.getSons();
        Value lastValue = null;
        String op = "";
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.AddExp) {
                if (lastValue == null) {
                    lastValue = parseAddExpForIR(son, currentTable);
                    continue;
                }
                Value value = parseAddExpForIR(son, currentTable);
                if (op.equals("<")) {
                    lastValue = new IcmpInst(currentBasicBlock, InstructionType.SLT, lastValue, value);
                } else if (op.equals(">")) {
                    lastValue = new IcmpInst(currentBasicBlock, InstructionType.SGT, lastValue, value);
                } else if (op.equals("<=")) {
                    lastValue = new IcmpInst(currentBasicBlock, InstructionType.SLE, lastValue, value);
                } else if (op.equals(">=")) {
                    lastValue = new IcmpInst(currentBasicBlock, InstructionType.SGE, lastValue, value);
                }
            } else if (son.getTokenType() == TokenType.LSS) {
                op = "<";
            } else if (son.getTokenType() == TokenType.GRE) {
                op = ">";
            } else if (son.getTokenType() == TokenType.LEQ) {
                op = "<=";
            } else if (son.getTokenType() == TokenType.GEQ) {
                op = ">=";
            }
        }
        return lastValue;
    }

    private Value parseEqExpForIR(Token eqExp, SymbolTableForIR currentTable) {
        ArrayList<Token> sons = eqExp.getSons();
        Value lastValue = null;
        String op = "";
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.RelExp) {
                if (lastValue == null) {
                    lastValue = parseRelExpForIR(son, currentTable);
                    continue;
                }
                Value value = parseRelExpForIR(son, currentTable);
                if (op.equals("==")) {
                    lastValue = new IcmpInst(currentBasicBlock, InstructionType.EQ, lastValue, value);
                } else if (op.equals("!=")) {
                    lastValue = new IcmpInst(currentBasicBlock, InstructionType.NE, lastValue, value);
                }
            } else if (son.getTokenType() == TokenType.EQL) {
                op = "==";
            } else if (son.getTokenType() == TokenType.NEQ) {
                op = "!=";
            }
        }
        return lastValue;
    }

    private Value parseLAndExpForIR(Token lAndExp, SymbolTableForIR currentTable) {
        ArrayList<Token> sons = lAndExp.getSons();
        Value lastValue = null;
        String op = "";
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.EqExp) {
                if (lastValue == null) {
                    lastValue = parseEqExpForIR(son, currentTable);
                    continue;
                }
                Value value = parseEqExpForIR(son, currentTable);
                if (op.equals("&&")) {
                    lastValue = new BinaryOperator(currentBasicBlock, InstructionType.AND, lastValue, value);
                }
            } else if (son.getTokenType() == TokenType.AND) {
                op = "&&";
            }
        }
        return lastValue;
    }

    private Value parseLOrExpForIR(Token lOrExp, SymbolTableForIR currentTable) {
        ArrayList<Token> sons = lOrExp.getSons();
        Value lastValue = null;
        String op = "";
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.LAndExp) {
                if (lastValue == null) {
                    lastValue = parseLAndExpForIR(son, currentTable);
                    continue;
                }
                Value value = parseLAndExpForIR(son, currentTable);
                if (op.equals("||")) {
                    lastValue = new BinaryOperator(currentBasicBlock, InstructionType.OR, lastValue, value);
                }
            } else if (son.getTokenType() == TokenType.OR) {
                op = "||";
            }
        }
        return lastValue;
    }

    private ConstantInteger parseConstExpForIR(Token constExp, SymbolTableForIR currentTable) {
        ArrayList<Token> sons = constExp.getSons();
        Value value = null;
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.AddExp) {
                value = parseAddExpForIR(son, currentTable);
            }
        }
        if (value instanceof ConstantInteger)
            return (ConstantInteger) value;
        return null;
    }
}
