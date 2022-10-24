package ErrorDetect;

import TokenDefines.Token;
import TokenDefines.TokenType;

import java.util.ArrayList;
import java.util.HashMap;


public class ErrorDetection {
    private final Token root;
    private HashMap<Token, Character> allFalse;
    private SymbolTable rootTable;

    public ErrorDetection(Token root, HashMap<Token, Character> allFalse) {
        this.root = root;
        this.allFalse = allFalse;
        detectCompUnit(root);
    }

    private void detectCompUnit(Token compUnit) {
        rootTable = new SymbolTable(null, 0);
        ArrayList<Token> sons = compUnit.getSons();
        for (Token token : sons) {
            TokenType tokenType = token.getTokenType();
            if (tokenType == TokenType.Decl) {
                detectDecl(token, rootTable);
            } else if (tokenType == TokenType.FuncDef) {
                detectFuncDef(token, rootTable);
            } else if (tokenType == TokenType.MainFuncDef) {
                detectMainFuncDef(token, rootTable);
            }
        }
    }

    private void detectDecl(Token decl, SymbolTable currentTable) {
        ArrayList<Token> sons = decl.getSons();
        for (Token token : sons) {
            TokenType tokenType = token.getTokenType();
            if (tokenType == TokenType.ConstDecl) {
                detectConstDecl(token, currentTable);
            } else if (tokenType == TokenType.VarDecl) {
                detectVarDecl(token, currentTable);
            }
        }
    }

    private void detectConstDecl(Token constDecl, SymbolTable currentTable) {
        ArrayList<Token> sons = constDecl.getSons();
        SymbolTableItem constDeclAttributes = new SymbolTableItem();
        SymbolTableItem typeAttributes = null;
        for (Token son : sons) {
            TokenType tokenType = son.getTokenType();
            if (tokenType == TokenType.BType) {
                typeAttributes = detectBType(son);
            } else if (tokenType == TokenType.ConstDef) {
                detectConstDef(son, currentTable, typeAttributes);
            }
        }
    }

    private SymbolTableItem detectBType(Token BType) {
        SymbolTableItem bTypeAttributes = new SymbolTableItem();
        ArrayList<Token> sons = BType.getSons();
        for (Token son : sons) {
            TokenType tokenType = son.getTokenType();
            if (tokenType == TokenType.INTTK) {
                bTypeAttributes.setType("int");
            }
        }
        return bTypeAttributes;
    }

    private void detectConstDef(Token constDef, SymbolTable currentTable, SymbolTableItem typeAttributes) {
        String name = "";
        int dimensions = 0;
        ArrayList<Token> sons = constDef.getSons();
        SymbolTableItem attributes = new SymbolTableItem();
        attributes.setSymbolType(SymbolType.VARIABLE);
        attributes.setConst(true);
        for (Token son : sons) {
            TokenType tokenType = son.getTokenType();
            if (tokenType == TokenType.IDENFR) {
                name = son.getTokenString();
                if (currentTable.findIdentInCurrentTable(name)) {
                    allFalse.put(son, 'b');
                }
            } else if (tokenType == TokenType.ConstExp) {
                SymbolTableItem dimensionAttributes = detectConstExp(son, currentTable);
                attributes.setSymbolType(SymbolType.ARRAY);
                dimensions++;
            } else if (tokenType == TokenType.ConstInitVal) {
                SymbolTableItem initValAttributes = detectConstInitVal(son, currentTable);
                // 进行初始化，不清楚要传递什么东西（感觉维度信息可以传递进去），但是估计不会出这样的数据点，所以不考虑了
                //  维度信息不需要传递进去，如果为空则默认为0.
                //  如果超过原数组长度则展开进行赋值，const int a[6] = {1, 2, 3, 4, 5, 6} ok;
                //  const int a[6] = {{1, 2, 3, 4}, {5, 6}} 不行
//                attributes.addArrayInitVal(initValAttributes);
            }
        }
        // 添加信息: name, dimensionAttributes, initValAttributes
        attributes.setType(typeAttributes.getType());
        attributes.setName(name);
        if (attributes.getSymbolType() == SymbolType.ARRAY)
            attributes.setDimensions(dimensions);
        currentTable.addItem(attributes);       //  插入符号表
    }

    private SymbolTableItem detectConstInitVal(Token constInitVal, SymbolTable currentTable) {
        ArrayList<Token> sons = constInitVal.getSons();
        SymbolTableItem attributes = new SymbolTableItem();
        for (Token son : sons) {
            TokenType tokenType = son.getTokenType();
            if (tokenType == TokenType.ConstExp) {
                // variable
                SymbolTableItem variableAttributes = detectConstExp(son, currentTable);
                // TODO将解析获得的变量的值复制到当前节点Attribute中，并且返回
//                attributes.addArrayInitVal(variableAttributes);
                attributes.combineAttributes(variableAttributes);
            } else if (tokenType == TokenType.ConstInitVal) {
                // array
                SymbolTableItem arrayAttributes = detectConstInitVal(son, currentTable);
                // TODO如果解析获得了值则有初始值，否则默认初始值为0.
//                attributes.addArrayInitVal(arrayAttributes);
            }
        }
        return attributes;
    }

    private void detectVarDecl(Token varDecl, SymbolTable currentTable) {
        ArrayList<Token> sons = varDecl.getSons();
        SymbolTableItem attributes = new SymbolTableItem();
        SymbolTableItem bTypeAttributes = null;
        for (Token son : sons) {
            TokenType tokenType = son.getTokenType();
            if (tokenType == TokenType.BType) {
                bTypeAttributes = detectBType(son);
                // TODO填写BType信息
            } else if (tokenType == TokenType.VarDef) {
                detectVarDef(son, currentTable, bTypeAttributes);
            }
        }
    }

    private void detectVarDef(Token varDef, SymbolTable currentTable, SymbolTableItem typeAttributes) {
        ArrayList<Token> sons = varDef.getSons();
        SymbolTableItem attributes = new SymbolTableItem();
        int arrayDimensions = 0;
        for (Token son : sons) {
            TokenType tokenType = son.getTokenType();
            if (tokenType == TokenType.IDENFR) {
                if (currentTable.findIdentInCurrentTable(son.getTokenString())) {
                    allFalse.put(son, 'b');
                }
                attributes.setName(son.getTokenString());
            } else if (tokenType == TokenType.ConstExp) {
                arrayDimensions++;
                SymbolTableItem constExpAttributes = detectConstExp(son, currentTable);
                // TODO获得当前维度大小。将结果保存至attributes
            } else if (tokenType == TokenType.InitVal) {
                SymbolTableItem initValAttributes = detectInitVal(son, currentTable);
                // TODO获得初始值，若为空则默认赋值0。支持展开。需要将值填写到Attributes中
//                attributes.addArrayInitVal(initValAttributes);
            }
        }
        // TODO插入符号表
        attributes.setType(typeAttributes.getType());
        attributes.setDimensions(arrayDimensions);
        currentTable.addItem(attributes);
    }

    private SymbolTableItem detectInitVal(Token initVal, SymbolTable currentTable) {
        ArrayList<Token> sons = initVal.getSons();
        SymbolTableItem attributes = new SymbolTableItem();
        for (Token son : sons) {
            TokenType tokenType = son.getTokenType();
            if (tokenType == TokenType.Exp) {
                SymbolTableItem expAttributes = detectExp(son, currentTable);
//                attributes.addArrayInitVal(expAttributes); // TODO汇总得到的值
            } else if (tokenType == TokenType.InitVal) {
                SymbolTableItem initValAttributes = detectInitVal(son, currentTable);
                // TODO汇总得到的值，使用数组ArrayList模仿树形结构
//                attributes.addArrayInitVal(initValAttributes);
            }
        }
        return attributes;
    }

    private void detectFuncDef(Token funcDef, SymbolTable currentTable) {
        ArrayList<Token> sons = funcDef.getSons();
        SymbolTableItem attributes = new SymbolTableItem();
        attributes.setSymbolType(SymbolType.FUNCTION);
        SymbolTableItem typeAttributes = null;
        SymbolTableItem paramsAttributes = null;
        SymbolTable sonTable = null;
        int insert = 1;
        for (Token son : sons) {
            TokenType tokenType = son.getTokenType();
            if (tokenType == TokenType.FuncType) {
                typeAttributes = detectFuncType(son, currentTable);
                attributes.setType(typeAttributes.getType());
            } else if (tokenType == TokenType.IDENFR) {
                if (currentTable.findIdentInCurrentTable(son.getTokenString())) {
                    allFalse.put(son, 'b');
                    insert = 0;
                }
                attributes.setName(son.getTokenString());
            } else if (tokenType == TokenType.FuncFParams) {
                sonTable = new SymbolTable(currentTable, currentTable.getIndex());
                paramsAttributes = detectFuncFParams(son, sonTable);
                attributes.setFuncParams(paramsAttributes.getFuncParams());
                // TODO当前符号表中插入函数名+参数列表
            } else if (tokenType == TokenType.Block) {
                if (sonTable == null) {
                    sonTable = new SymbolTable(currentTable, currentTable.getIndex());
                }
                if (insert == 1) currentTable.addItem(attributes);
                detectBlock(son, sonTable, typeAttributes, false);
                // TODO: 这里的BLOCK应该根据类型检验其中的return语句
                checkReturn(son, typeAttributes.getType());

            }
        }
//        currentTable.addItem(attributes);   // add this function into table
    }

    private void detectMainFuncDef(Token mainFuncDef, SymbolTable currentTable) {
        ArrayList<Token> sons = mainFuncDef.getSons();
        SymbolTable sonTable = null;
        SymbolTableItem typeAttributes = new SymbolTableItem();
        typeAttributes.setType("int");
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.Block) {
                sonTable = new SymbolTable(currentTable, currentTable.getIndex());
                detectBlock(son, sonTable, typeAttributes, false);
                checkReturn(son, "int");
            }
        }
    }

    private SymbolTableItem detectFuncType(Token funcType, SymbolTable currentTable) {
        SymbolTableItem attributes = new SymbolTableItem();
        ArrayList<Token> sons = funcType.getSons();
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.VOIDTK) {
                attributes.setType("void");
            } else if (son.getTokenType() == TokenType.INTTK) {
                attributes.setType("int");
            }
        }
        return attributes;
    }

    private SymbolTableItem detectFuncFParams(Token funcFParams, SymbolTable currentTable) {
        SymbolTableItem attributes = new SymbolTableItem();
        ArrayList<Token> sons = funcFParams.getSons();
        SymbolTableItem paramAttributes = null;
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.FuncFParam) {
                paramAttributes = detectFuncFParam(son, currentTable);
                // TODO把这个参数的信息增加到总参数列表的信息中。
                attributes.addFuncParam(paramAttributes);   // add funcFParam
            }
        }
        return attributes;
    }

    private SymbolTableItem detectFuncFParam(Token funcFParam, SymbolTable currentTable) {
        SymbolTableItem attributes = new SymbolTableItem();
        ArrayList<Token> sons = funcFParam.getSons();
        SymbolTableItem typeAttributes = null;
        SymbolTableItem constExpAttributes = null;
        int dimensions = 0;
        attributes.setSymbolType(SymbolType.VARIABLE);  // assume it is a variable
        for (Token son : sons) {
            TokenType tokenType = son.getTokenType();
            if (tokenType == TokenType.BType) {
                typeAttributes = detectBType(son);
                attributes.setType(typeAttributes.getType());   // set type
            } else if (tokenType == TokenType.IDENFR) {
                if (currentTable.findIdentInCurrentTable(son.getTokenString())) {
                    allFalse.put(son, 'b');
                }
                attributes.setName(son.getTokenString());   // set name
            } else if (tokenType == TokenType.LBRACK) {
                dimensions++;
            } else if (tokenType == TokenType.ConstExp) {
                constExpAttributes = detectConstExp(son, currentTable);
                // TODO: 可以据此确定数组各个维度的大小
            }
        }
        // TODO增加维度大小dimensions
        //  在符号表中增加表项
        attributes.setDimensions(dimensions);
        currentTable.addItem(attributes);
        return attributes;
    }

    private void detectBlock(Token block, SymbolTable currentTable,
                             SymbolTableItem typeAttributes, Boolean inLoop) {
        ArrayList<Token> sons = block.getSons();
        Token finalToken = null;
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.BlockItem) {
                detectBlockItem(son, currentTable, typeAttributes, inLoop);
                finalToken = son;
            }
        }
    }

    private void checkReturn(Token token, String type) {
        // TODO: checkReturn
        if (!type.equals("int")) return;
        ArrayList<Token> sons = token.getSons();
        boolean noReturnFlag = true;
        for (int i = sons.size() - 1; i >= 0; i--) {
            if (sons.get(i).getTokenType() == TokenType.BlockItem) {
                Token blockItemToken = sons.get(i);
                if (blockItemToken.getSons().get(0).getTokenType() == TokenType.Stmt) {
                    Token stmtToken = blockItemToken.getSons().get(0);
                    if (stmtToken.getSons().get(0).getTokenType() == TokenType.RETURNTK) {
                        noReturnFlag = false;
                    }
                }
                break;
            }
        }
        if (noReturnFlag) {
            for (int i = sons.size() - 1; i >= 0; i--) {
                if (sons.get(i).getTokenType() == TokenType.RBRACE) {
                    allFalse.put(sons.get(i), 'g');
                }
            }
        }
    }
    private void detectBlockItem(Token blockItem, SymbolTable currentTable,
                                 SymbolTableItem typeAttributes, boolean inLoop) {
        ArrayList<Token> sons = blockItem.getSons();
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.Decl) {
                detectDecl(son, currentTable);
            } else if (son.getTokenType() == TokenType.Stmt) {
                detectStmt(son, currentTable, typeAttributes, inLoop);
            }
        }
    }

    private void detectStmt(Token stmt, SymbolTable currentTable,
                            SymbolTableItem typeAttributes, boolean inLoop) {
        ArrayList<Token> sons = stmt.getSons();
        Token firstToken = sons.get(0);
        TokenType firstTokenType = firstToken.getTokenType();
        if (firstTokenType == TokenType.LVal) {
            for (Token son : sons) {
                if (son.getTokenType() == TokenType.LVal) {
                    SymbolTableItem lValAttributes = detectLVal(son, currentTable);
                    // TOD这里得到解析LVal的Attributes后，进行解析判断，如果是常量则报错。
                    if (lValAttributes.isConst()) {
                        allFalse.put(son, 'h');
                    }
                } else if (son.getTokenType() == TokenType.Exp) {
                    detectExp(son, currentTable);
                }
            }
        } else if (firstTokenType == TokenType.Block) {
            SymbolTable sonTable = new SymbolTable(currentTable, currentTable.getIndex());
            detectBlock(firstToken, sonTable, typeAttributes, inLoop);
        } else if (firstTokenType == TokenType.IFTK) {
            for (Token son : sons) {
                if (son.getTokenType() == TokenType.Cond) {
                    detectCond(son, currentTable);
                } else if (son.getTokenType() == TokenType.Stmt) {
                    detectStmt(son, currentTable, typeAttributes, inLoop);
                }
            }
        } else if (firstTokenType == TokenType.WHILETK) {
            for (Token son : sons) {
                if (son.getTokenType() == TokenType.Cond) {
                    detectCond(son, currentTable);
                } else if (son.getTokenType() == TokenType.Stmt) {
                    detectStmt(son, currentTable, typeAttributes, true);
                }
            }
        } else if (firstTokenType == TokenType.BREAKTK || firstTokenType == TokenType.CONTINUETK) {
            if (!inLoop) {
                allFalse.put(firstToken, 'm');
            }
        } else if (firstTokenType == TokenType.PRINTFTK) {
            int expNum = 0;
            int strExpNum = 0;
            for (Token son : sons) {
                if (son.getTokenType() == TokenType.STRCON) {
                    strExpNum = checkStrCon(son);
                } else if (son.getTokenType() == TokenType.Exp) {
                    detectExp(son, currentTable);
                    expNum++;
                }
            }
            if (expNum != strExpNum) {
                allFalse.put(firstToken, 'l');
            }
        } else if (firstTokenType == TokenType.Exp) {
            detectExp(firstToken, currentTable);
        } else if (firstTokenType == TokenType.RETURNTK) {
            boolean hasExp = false;
            for (Token son : sons) {
                if (son.getTokenType() == TokenType.Exp) {
                    detectExp(son, currentTable);
                    hasExp = true;
                }
            }
            if (typeAttributes.getType().equals("void") && hasExp) {
                allFalse.put(firstToken, 'f');
            }
        }
    }

    private int checkStrCon(Token formatStr) {
        String formatString = formatStr.getTokenString();
        int strExpNum = 0;
        for (int i = 1; i < formatString.length() - 1; i++) {
            if (formatString.charAt(i) == '\\' && i + 1 < formatString.length() &&
                    formatString.charAt(i + 1) != 'n') {
                allFalse.put(formatStr, 'a');
                break;
            }
            if (formatString.charAt(i) == '%' && i + 1 < formatString.length() &&
                    formatString.charAt(i + 1) == 'd') {
                strExpNum++;
            }
            if (formatString.charAt(i) == '%' && i + 1 < formatString.length() &&
                    formatString.charAt(i + 1) != 'd') {
                allFalse.put(formatStr, 'a');
                break;
            }
            char c = formatString.charAt(i);
            if (c != 32 && c != 33 && c != 37 && (c < 40 || c > 126)) {
                allFalse.put(formatStr, 'a');
                break;
            }
        }
        return strExpNum;
    }

    private SymbolTableItem detectExp(Token exp, SymbolTable currentTable) {
        ArrayList<Token> sons = exp.getSons();
        SymbolTableItem attributes = new SymbolTableItem();
        SymbolTableItem addExpAttributes = null;
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.AddExp) {
                addExpAttributes = detectAddExp(son, currentTable);
                // TODO: 整合addExpAttributes和attributes
                attributes.combineAttributes(addExpAttributes);
            }
        }
        return attributes;
    }

    private void detectCond(Token cond, SymbolTable currentTable) {
        ArrayList<Token> sons = cond.getSons();
        SymbolTableItem attributes = new SymbolTableItem();
        SymbolTableItem lOrExpAttributes = null;
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.LOrExp) {
                lOrExpAttributes = detectLOrExp(son, currentTable);
                // TODO: combine together
            }
        }
    }

    private SymbolTableItem detectLVal(Token lVal, SymbolTable currentTable) {
        // TODO: 需要查一下LVal是不是常量
        SymbolTableItem attributes = new SymbolTableItem();
        SymbolTableItem lValAttributes = null;
        ArrayList<Token> sons = lVal.getSons();
        attributes.setSymbolType(SymbolType.VARIABLE);
        int arrayDimensions = 0;
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.IDENFR) {
                attributes.setName(son.getTokenString());
                lValAttributes = currentTable.findIdentInAllTable(son.getTokenString());
                // TODO判断解析LVal的返回值，得到所有属性，判断是否是常量
                //  在这里不判断是否是常量，因为PrimaryExp中可能调用LVal，所以在这类就检查这些值，是否之前定义过
                //  定义过则整合Attributes，未定义过则返回Null
                if (lValAttributes == null) {
                    allFalse.put(lVal, 'c');
                    attributes.setConst(false);
                } else {
                    attributes.setConst(lValAttributes.isConst());
                    attributes.setType(lValAttributes.getType());
                }
//                else {
//                    attributes.cloneAttributes(lValAttributes);
//                }
            } else if (son.getTokenType() == TokenType.LBRACK) {
                arrayDimensions++;
                attributes.setSymbolType(SymbolType.ARRAY);
                // TODO：维度定西增加；
            } else if (son.getTokenType() == TokenType.Exp) {
                SymbolTableItem expAttributes = detectExp(son, currentTable);
                // TODO：得到的结果整合

            }
        }
        int currentDimensions = 0;
        if (lValAttributes != null) {
            currentDimensions = Math.max(0, lValAttributes.getDimensions() - arrayDimensions);
        }
        attributes.setDimensions(currentDimensions);
        return attributes;
    }

    private SymbolTableItem detectPrimaryExp(Token primaryExp, SymbolTable currentTable) {
        ArrayList<Token> sons = primaryExp.getSons();
        Token firstToken = sons.get(0);
        SymbolTableItem attributes = new SymbolTableItem();
        if (firstToken.getTokenType() == TokenType.LPARENT) {
            for (Token son : sons) {
                if (son.getTokenType() == TokenType.Exp) {
                    SymbolTableItem expAttributes = detectExp(son, currentTable);
                    // TODO: combine attributes;
                    attributes.combineAttributes(expAttributes);
                }
            }
        } else if (firstToken.getTokenType() == TokenType.LVal) {
            SymbolTableItem lValAttributes = detectLVal(firstToken, currentTable);
            // TODO: combine
            attributes.combineAttributes(lValAttributes);
        } else if (firstToken.getTokenType() == TokenType.Number) {
            // TODO: combine
            attributes.setType("int");
            attributes.setDimensions(0);
        }
        return attributes;
    }

    private SymbolTableItem detectUnaryExp(Token unaryExp, SymbolTable currentTable) {
        ArrayList<Token> sons = unaryExp.getSons();
        Token firstToken = sons.get(0);
        SymbolTableItem attributes = new SymbolTableItem();
        if (firstToken.getTokenType() == TokenType.PrimaryExp) {
            SymbolTableItem primaryAttributes = detectPrimaryExp(firstToken, currentTable);
            // TODO: combine
            attributes.combineAttributes(primaryAttributes);
        } else if (firstToken.getTokenType() == TokenType.IDENFR) {
            SymbolTableItem functionAttributes = currentTable.findIdentInAllTable(firstToken.getTokenString());
            SymbolTableItem funcRParamsAttributes = new SymbolTableItem();
            for (Token son : sons) {
                if (son.getTokenType() == TokenType.FuncRParams) {
                    funcRParamsAttributes = detectFuncRParams(son, currentTable);
                    // TODO: Check FuncRParams with the functionAttributes

                }
            }
            if (functionAttributes == null) {
                allFalse.put(firstToken, 'c');
            } else {
                int errorType = funcRParamsAttributes.checkFuncParams(functionAttributes);
                if (errorType == 1) {
                    allFalse.put(firstToken, 'd');
                } else if (errorType == 2) {
                    allFalse.put(firstToken, 'e');
                }
            }

        } else if (firstToken.getTokenType() == TokenType.UnaryOp) {
            SymbolTableItem unaryOpAttributes = detectUnaryOp(firstToken, currentTable);
            for (Token son : sons) {
                if (son.getTokenType() == TokenType.UnaryExp) {
                    SymbolTableItem unaryExpAttributes = detectUnaryExp(son, currentTable);
                    // TODO: combine the unaryExp attributes with unaryOp attributes;
                    attributes.combineAttributes(unaryOpAttributes);
                }
            }
        }
        return attributes;
    }

    private SymbolTableItem detectUnaryOp(Token unaryOp, SymbolTable currentTable) {
        ArrayList<Token> sons = unaryOp.getSons();
        Token firstToken = sons.get(0);
        SymbolTableItem attributes = new SymbolTableItem();
        if (firstToken.getTokenType() == TokenType.PLUS) {
            // TODO: combine plue
        } else if (firstToken.getTokenType() == TokenType.MINU) {
            // TODO: combine minus
        } else if (firstToken.getTokenType() == TokenType.NOT) {
            // TODO: combine not
        }
        return attributes;
    }

    private SymbolTableItem detectFuncRParams(Token funcRParams, SymbolTable currentTable) {
        int expNumber = 0;
        SymbolTableItem attributes = new SymbolTableItem();
        ArrayList<Token> sons = funcRParams.getSons();
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.Exp) {
                expNumber++;
                SymbolTableItem expAttributes = detectExp(son, currentTable);
                // TODO: combine
                attributes.addFuncParam(expAttributes);
            }
        }
        // TODO: combine expNumber
        return attributes;
    }

    private SymbolTableItem detectMulExp(Token mulExp, SymbolTable currentTable) {
        ArrayList<Token> sons = mulExp.getSons();
        SymbolTableItem attributes = new SymbolTableItem();
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.UnaryExp) {
                SymbolTableItem unaryExpAttributes = detectUnaryExp(son, currentTable);
                // TODO: 整合结果
                attributes.combineAttributes(unaryExpAttributes);
            }
        }
        return attributes;
    }

    private SymbolTableItem detectAddExp(Token addExp, SymbolTable currentTable) {
        ArrayList<Token> sons = addExp.getSons();
        SymbolTableItem attributes = new SymbolTableItem();
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.MulExp) {
                SymbolTableItem mulExpAttributes = detectMulExp(son, currentTable);
                // TODO: 整合结果
                attributes.combineAttributes(mulExpAttributes);
            }
        }
        return attributes;
    }

    private SymbolTableItem detectRelExp(Token relExp, SymbolTable currentTable) {
        ArrayList<Token> sons = relExp.getSons();
        SymbolTableItem attributes = new SymbolTableItem();
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.AddExp) {
                SymbolTableItem addExpAttributes = detectAddExp(son, currentTable);
                // TODO: 整合结果
                attributes.combineAttributes(addExpAttributes);
            }
        }
        return attributes;
    }

    private SymbolTableItem detectEqExp(Token eqExp, SymbolTable currentTable) {
        ArrayList<Token> sons = eqExp.getSons();
        SymbolTableItem attributes = new SymbolTableItem();
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.RelExp) {
                SymbolTableItem relExpAttributes = detectRelExp(son, currentTable);
                // TODO: 整合结果
                attributes.combineAttributes(relExpAttributes);
            }
        }
        return attributes;
    }

    private SymbolTableItem detectLAndExp(Token lAndExp, SymbolTable currentTable) {
        ArrayList<Token> sons = lAndExp.getSons();
        SymbolTableItem attributes = new SymbolTableItem();
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.EqExp) {
                SymbolTableItem EqExpAttributes = detectEqExp(son, currentTable);
                // TODO: 整合结果
                attributes.combineAttributes(EqExpAttributes);
            }
        }
        return attributes;
    }

    private SymbolTableItem detectLOrExp(Token lOrExp, SymbolTable currentTable) {
        ArrayList<Token> sons = lOrExp.getSons();
        SymbolTableItem attributes = new SymbolTableItem();
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.LAndExp) {
                SymbolTableItem lAndExpAttributes = detectLAndExp(son, currentTable);
                // TODO: 整合结果
                attributes.combineAttributes(lAndExpAttributes);
            }
        }
        return attributes;
    }

    private SymbolTableItem detectConstExp(Token constExp, SymbolTable currentTable) {
        ArrayList<Token> sons = constExp.getSons();
        SymbolTableItem attributes = new SymbolTableItem();
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.AddExp) {
                SymbolTableItem addExpAttributes = detectAddExp(son, currentTable);
                // TODO: combine
                attributes.combineAttributes(addExpAttributes);
            }
        }
        return attributes;
    }
}
