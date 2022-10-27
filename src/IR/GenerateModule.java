package IR;

import ErrorDetect.SymbolTable;
import ErrorDetect.SymbolTableItem;
import ErrorDetect.SymbolType;
import IR.Values.BasicBlock;
import IR.Values.Function;
import IR.Values.InitValue;
import IR.Values.InstructionIR.AllocaInst;
import IR.Values.InstructionIR.StoreInst;
import IR.Values.Value;
import IR.types.IntType;
import IR.types.PointerType;
import IR.types.Type;
import TokenDefines.Token;
import TokenDefines.TokenType;

import java.util.ArrayList;

public class GenerateModule {
    private SymbolTable rootTable;
    private LLVMSymbolTable llvmSymbolTable;

    public GenerateModule() {
        rootTable = new SymbolTable(null, 0);
        llvmSymbolTable = new LLVMSymbolTable();
    }

    public void parseModule(Token root) {
        ArrayList<Token> sons = root.getSons();
        for (Token son : sons) {
            switch (son.getTokenType()) {
                case Decl -> {
                    System.out.println("parse Decl");
                    parseDeclForIR(son, rootTable, llvmSymbolTable,
                            Module.getMyModule().getGlobalBasicBlock());
                }
                case FuncDef -> System.out.println("parse funcDef");
                case MainFuncDef -> {
                    System.out.println("parse mainFuncDef");
                    Function mainFunction = new Function();
                    parseMainFuncDefForIR(son, rootTable, llvmSymbolTable,
                            mainFunction.getBasicBlocks().get(0));
                }
            }
        }
    }

    private void parseDeclForIR(Token decl, SymbolTable currentTable,
                                LLVMSymbolTable tableForIR, BasicBlock currentBasicBlock) {
        ArrayList<Token> sons = decl.getSons();
        for (Token son : sons) {
            switch (son.getTokenType()) {
                case ConstDecl -> {
                    parseConstDeclForIR(son, currentTable, tableForIR, currentBasicBlock);
                }
                case VarDecl -> {
                    parseVarDeclForIR(son, currentTable, tableForIR, currentBasicBlock);
                }
            }
        }
    }

    private void parseConstDeclForIR(Token constDecl, SymbolTable currentTable,
                                     LLVMSymbolTable tableForIR, BasicBlock currentBasicBlock) {
        ArrayList<Token> sons = constDecl.getSons();
        SymbolTableItem constDeclAttributes = new SymbolTableItem();
        Type type = IntType.i32;
        for (Token son : sons) {
            switch (son.getTokenType()) {
                case BType -> {
                    type = parseBTypeForIR(son);
                }
                case ConstDef -> {
                    parseConstDefForIR(son, currentTable, type, tableForIR, currentBasicBlock);
                }
            }
        }
    }

    private Type parseBTypeForIR(Token bType) {
        ArrayList<Token> sons = bType.getSons();
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.InitVal) {
                return IntType.i32;
            }
        }
        return null;
    }

    /**
     *
     * @param constDef 当前token
     * @param currentTable 当前table
     * @param type 几乎就是 i32
     * @param tableForIR LLVM table
     * @param currentBasicBlock 当前基本块
     */
    private void parseConstDefForIR(Token constDef, SymbolTable currentTable, Type type,
                                    LLVMSymbolTable tableForIR, BasicBlock currentBasicBlock) {
        int dimensions = 0;
        String befName, aftName;
        ArrayList<Token> sons = constDef.getSons();
        SymbolTableItem constDefAttributes = new SymbolTableItem();
        constDefAttributes.setSymbolType(SymbolType.VARIABLE);
        constDefAttributes.setConst(true);
        Value pointer = null;
        for (Token son : sons) {
            switch (son.getTokenType()) {
                case IDENFR -> {
                    befName = son.getTokenString();
                    constDefAttributes.setName(befName);
                    aftName = tableForIR.addIdent(son.getTokenString());
                    constDefAttributes.setAftName(aftName);
                    /*
                     * 这里需要一条alloca指令
                     * 首先，aftName就是最终声明栈式内存地址的名称
                     * 添加 %aftName = alloca type
                     */
                    AllocaInst allocaInst = new AllocaInst(currentBasicBlock, aftName, true, type);
                    currentBasicBlock.addInstruction(allocaInst);   // * 添加指令进去
                    pointer = new Value(new PointerType(type), aftName);
                    // ! 这里用不用User？
                }
                case ConstExp -> {
                    constDefAttributes.setSymbolType(SymbolType.ARRAY);
                    dimensions++;
                    // TODO: 处理多维数组
                }
                case ConstInitVal -> {
                    InitValue initValue = parseConstInitValForIR(son, currentTable, tableForIR);
                    if (constDefAttributes.getSymbolType() == SymbolType.VARIABLE) {
                        // TODO: initValue get value
                        Value constInitValValue = initValue
                    } else {

                    }

                    StoreInst storeInst = new StoreInst(currentBasicBlock, pointer, constInitValValue);
                    currentBasicBlock.addInstruction(storeInst);
                }
            }
        }
    }

    private InitValue parseConstInitValForIR(Token constInitVal, SymbolTable currentTable,
                                             LLVMSymbolTable tableForIR) {
        ArrayList<Token> sons = constInitVal.getSons();
        SymbolTableItem constInitValAttribute = new SymbolTableItem();
        for (Token son : sons) {
            switch (son.getTokenType()) {
                case ConstExp -> {
                    SymbolTableItem constExpAttributes = parseConstExpForIR(son, currentTable, tableForIR);
                }
                case ConstInitVal -> {
                    continue;
                }
            }
        }
    }

    private void parseVarDeclForIR(Token varDecl, SymbolTable currentTable,
                              LLVMSymbolTable tableForIR, BasicBlock currentBasicBlock) {

    }

    private void parseVarDefForIR(Token varDef, SymbolTable currentTable, SymbolTableItem typeAttribute,
                                  LLVMSymbolTable tableForIR) {

    }

    private SymbolTableItem parseInitValForIR(Token initVal, SymbolTable currentTable, LLVMSymbolTable tableForIR) {

    }

    private SymbolTableItem parseFuncDefForIR(Token funcDef, SymbolTable currentTable) {

    }

    private void parseMainFuncDefForIR(Token mainFuncDef, SymbolTable currentTable,
                                             LLVMSymbolTable tableForIR, BasicBlock currentBasicBlock) {
        /*
         * currentTable: 代表层次结构的Table
         * tableForIR: 并非是层次结构的。对于MainSymbolTable，内部没有子表，只有表项
         */
        SymbolTable sonTable = null;
        ArrayList<Token> sons = mainFuncDef.getSons();
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.Block) {
                sonTable = new SymbolTable(currentTable, currentTable.getIndex());
                parseBlockForIR(son, sonTable, tableForIR, currentBasicBlock);
            }
        }
    }

    private SymbolTableItem parseFuncTypeForIR(Token funcType) {

    }

    private SymbolTableItem parseFuncFParamsForIR(Token funcFParams, SymbolTable currentTable) {

    }

    private SymbolTableItem parseFuncFParamForIR(Token funcFParam, SymbolTable currentTable) {

    }

    private void parseBlockForIR(Token block, SymbolTable currentTable,
                                       LLVMSymbolTable tableForIR, BasicBlock currentBasicBlock) {
        /*
         * Block可能在函数、Main函数、或者任何嵌套内部中出现，不论出现什么，由于LLVM中符号表不变，所以tableForIR不变
         */
        ArrayList<Token> sons = block.getSons();
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.BlockItem) {
                parseBlockItemForIR(son, currentTable, tableForIR, currentBasicBlock);
            }
        }
    }

    private void parseBlockItemForIR(Token blockItem, SymbolTable currentTable,
                                     LLVMSymbolTable tableForIR, BasicBlock currentBasicBlock) {
        ArrayList<Token> sons = blockItem.getSons();
        for (Token son : sons) {
            switch (son.getTokenType()) {
                case Decl -> {
                    System.out.println("Decl");
                    parseDeclForIR(son, currentTable, tableForIR, currentBasicBlock);
                }
                case Stmt -> {
                    System.out.println("Stmt");
                    parseStmtForIR(son, currentTable, tableForIR);
                }
            }
        }
    }

    private void parseStmtForIR(Token stmt, SymbolTable currentTable, LLVMSymbolTable tableForIR) {

    }

    private SymbolTableItem parseExpForIR(Token exp, SymbolTable currentTable) {

    }

    private SymbolTableItem parseCondForIR(Token cond, SymbolTable currentTable) {

    }

    private SymbolTableItem parseLValForIR(Token lVal, SymbolTable currentTable) {

    }

    private SymbolTableItem parsePrimaryExpForIR(Token primaryExp, SymbolTable currentTable) {

    }

    private SymbolTableItem parseUnaryExpForIR(Token unaryExp, SymbolTable currentTable) {

    }

    private SymbolTableItem parseUnaryOpForIR(Token unaryOp, SymbolTable currentTable) {

    }

    private SymbolTableItem parseFuncRParamsForIR(Token funcRParams, SymbolTable currentTable) {

    }

    private SymbolTableItem parseMulExpForIR(Token mulExp, SymbolTable currentTable) {

    }

    private SymbolTableItem parseAddExpForIR(Token addExp, SymbolTable currentTable) {

    }

    private SymbolTableItem parseRelExpForIR(Token relExp, SymbolTable currentTable) {

    }

    private SymbolTableItem parseEqExpForIR(Token eqExp, SymbolTable currentTable) {

    }

    private SymbolTableItem parseLAndExpForIR(Token lAndExp, SymbolTable currentTable) {

    }

    private SymbolTableItem parseLOrExpForIR(Token lOrExp, SymbolTable currentTable) {

    }

    private SymbolTableItem parseConstExpForIR(Token constExp, SymbolTable currentTable,
                                               LLVMSymbolTable tableForIR) {

    }
}
