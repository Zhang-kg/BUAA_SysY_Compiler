package ErrorDetect;

import TokenDefines.Token;
import TokenDefines.TokenType;
import com.sun.org.apache.bcel.internal.generic.LOR;
import com.sun.xml.internal.bind.v2.model.core.ID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.jar.Attributes;


public class ErrorDetection {
    private Token root;
    private HashMap<Token, Character> allFalse;
    private SymbolTable rootTable;

    public ErrorDetection(Token root, HashMap<Token, Character> allFalse) {
        this.root = root;
        this.allFalse = allFalse;
    }

    public Token errorDetection(Token root) {
        detectCompUnit(root);
        return root;
    }

    private Token detectCompUnit(Token compUnit) {
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
        return compUnit;
    }

    private SymbolTableItem detectDecl(Token decl, SymbolTable currentTable) {
        ArrayList<Token> sons = decl.getSons();
        for (Token token : sons) {
            TokenType tokenType = token.getTokenType();
            if (tokenType == TokenType.ConstDecl) {
                detectConstDecl(token, currentTable);
            } else if (tokenType == TokenType.VarDecl) {
                detectVarDecl(token, currentTable);
            }
        }
        return null;
    }

    private SymbolTableItem detectConstDecl(Token constDecl, SymbolTable currentTable) {
        ArrayList<Token> sons = constDecl.getSons();
        SymbolTableItem constDeclAttributes = new SymbolTableItem();
        SymbolTableItem typeAttributes = null;
        for (Token son : sons) {
            TokenType tokenType = son.getTokenType();
            if (tokenType == TokenType.CONSTTK) continue;
            else if (tokenType == TokenType.BType) {
                typeAttributes = detectBType(son);
            } else if (tokenType == TokenType.ConstDef) {
                detectConstDef(son, currentTable, typeAttributes);
            }
        }
        return constDeclAttributes;
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

    private SymbolTableItem detectConstDef(Token constDef, SymbolTable currentTable, SymbolTableItem attributes) {
        String type = attributes.getType();
        String name;
        SymbolTableItem symbolTableItem = new SymbolTableItem();
        ArrayList<Integer> dimensions = new ArrayList<>();
        ArrayList<Token> sons = constDef.getSons();
        for (Token son : sons) {
            TokenType tokenType = son.getTokenType();
            if (tokenType == TokenType.IDENFR) {
                name = constDef.getTokenString();
                if (currentTable.findIdentInCurrentTable(name)) {
                    allFalse.put(son, 'b');
                }
            } else if (tokenType == TokenType.ConstExp) {
                SymbolTableItem dimensionAttributes = detectConstExp(son, currentTable);
                // TODO: 添加维度信息，不清楚要传什么内容
            } else if (tokenType == TokenType.ConstInitVal) {
                SymbolTableItem initValAttributes = detectConstInitVal(son, currentTable);
                // TODO：进行初始化，不清楚要传递什么东西（感觉维度信息可以传递进去），但是估计不会出这样的数据点，所以不考虑了
                //  维度信息不需要传递进去，如果为空则默认为0.
                //  如果超过原数组长度则展开进行赋值，const int a[6] = {1, 2, 3, 4, 5, 6} ok;
                //  const int a[6] = {{1, 2, 3, 4}, {5, 6}} 不行
            }
        }
        // TODO: 添加信息: name, dimensionAttributes, initValAttributes
        //  插入符号表
        return symbolTableItem;
    }

    private SymbolTableItem detectConstInitVal(Token constInitVal, SymbolTable currentTable) {
        ArrayList<Token> sons = constInitVal.getSons();
        SymbolTableItem attributes = new SymbolTableItem();
        for (Token son : sons) {
            TokenType tokenType = son.getTokenType();
            if (tokenType == TokenType.ConstExp) {
                // variable
                SymbolTableItem variableAttributes = detectConstExp(son, currentTable);
                // TODO: 将解析获得的变量的值复制到当前节点Attribute中，并且返回
            } else if (tokenType == TokenType.ConstInitVal) {
                // array
                SymbolTableItem arrayAttributes = detectConstInitVal(son, currentTable);
                // TODO: 如果解析获得了值则有初始值，否则默认初始值为0.
            }
        }
        return attributes;
    }

    private SymbolTableItem detectVarDecl(Token varDecl, SymbolTable currentTable) {
        ArrayList<Token> sons = varDecl.getSons();
        SymbolTableItem attributes = new SymbolTableItem();
        SymbolTableItem bTypeAttributes = null;
        for (Token son : sons) {
            TokenType tokenType = son.getTokenType();
            if (tokenType == TokenType.BType) {
                bTypeAttributes = detectBType(son);
                // TODO：填写BType信息
            } else if (tokenType == TokenType.VarDef) {
                detectVarDef(son, currentTable, bTypeAttributes);
            }
        }
        return attributes;
    }

    private SymbolTableItem detectVarDef(Token varDef, SymbolTable currentTable, SymbolTableItem typeAttributes) {
        ArrayList<Token> sons = varDef.getSons();
        SymbolTableItem attributes = new SymbolTableItem();
        int arrayDimensions = 0;
        for (Token son : sons) {
            TokenType tokenType = son.getTokenType();
            if (tokenType == TokenType.IDENFR) {
                if (currentTable.findIdentInCurrentTable(son.getTokenString())) {
                    allFalse.put(son, 'b');
                }
            } else if (tokenType == TokenType.ConstExp) {
                arrayDimensions++;
                SymbolTableItem constExpAttributes = detectConstExp(son, currentTable);
                // TODO：获得当前维度大小。将结果保存至attributes
            } else if (tokenType == TokenType.InitVal) {
                SymbolTableItem initValAttributes = detectInitVal(son, currentTable);
                // TODO: 获得初始值，若为空则默认赋值0。支持展开。需要将值填写到Attributes中
            }
        }
        // TODO：插入符号表
        return attributes;
    }

    private SymbolTableItem detectInitVal(Token initVal, SymbolTable currentTable) {
        ArrayList<Token> sons = initVal.getSons();
        SymbolTableItem attributes = new SymbolTableItem();
        for (Token son : sons) {
            TokenType tokenType = son.getTokenType();
            if (tokenType == TokenType.Exp) {
                SymbolTableItem expAttributes = detectExp(son, currentTable);
                // TODO：汇总得到的值
            } else if (tokenType == TokenType.InitVal) {
                SymbolTableItem initValAttributes = detectInitVal(son, currentTable);
                // TODO: 汇总得到的值，使用数组ArrayList模仿树形结构
            }
        }
        return attributes;
    }

    private SymbolTableItem detectFuncDef(Token funcDef, SymbolTable currentTable) {
        ArrayList<Token> sons = funcDef.getSons();
        SymbolTableItem attributes = new SymbolTableItem();
        SymbolTableItem typeAttributes = null;
        SymbolTableItem paramsAttributes = null;
        SymbolTable sonTable = null;
        for (Token son : sons) {
            TokenType tokenType = son.getTokenType();
            if (tokenType == TokenType.FuncType) {
                typeAttributes = detectFuncType(son, currentTable);
            } else if (tokenType == TokenType.IDENFR) {
                if (currentTable.findIdentInCurrentTable(son.getTokenString())) {
                    allFalse.put(son, 'b');
                }
            } else if (tokenType == TokenType.FuncFParams) {
                sonTable = new SymbolTable(currentTable, currentTable.getIndex());
                paramsAttributes = detectFuncFParams(son, sonTable);
                // TODO: 当前符号表中插入函数名+参数列表
            } else if (tokenType == TokenType.Block) {
                detectBlock(son, sonTable, typeAttributes, Boolean.FALSE);
                // TODO: 这里的BLOCK应该根据类型检验其中的return语句
            }
        }
        return attributes;
    }

    private SymbolTableItem detectMainFuncDef(Token mainFuncDef, SymbolTable currentTable) {
        ArrayList<Token> sons = mainFuncDef.getSons();
        SymbolTable sonTable = null;
        SymbolTableItem typeAttributes = new SymbolTableItem();
        typeAttributes.setType("int");
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.Block) {
                sonTable = new SymbolTable(currentTable, currentTable.getIndex());
                detectBlock(son, sonTable, typeAttributes, Boolean.FALSE);
            }
        }
        return null;
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
                // TODO: 把这个参数的信息增加到总参数列表的信息中。
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
        for (Token son : sons) {
            TokenType tokenType = son.getTokenType();
            if (tokenType == TokenType.BType) {
                typeAttributes = detectBType(son);
            } else if (tokenType == TokenType.IDENFR) {
                if (currentTable.findIdentInCurrentTable(son.getTokenString())) {
                    allFalse.put(son, 'b');
                }
            } else if (tokenType == TokenType.LBRACK) {
                dimensions++;
            } else if (tokenType == TokenType.ConstExp) {
                constExpAttributes = detectConstExp(son, currentTable);
                // TODO: 可以据此确定数组各个维度的大小
            }
        }
        // TODO：增加维度大小dimensions
        //  在符号表中增加表项
        return attributes;
    }

    private SymbolTableItem detectBlock(Token block, SymbolTable currentTable,
                                        SymbolTableItem typeAttributes, Boolean inLoop) {
        ArrayList<Token> sons = block.getSons();
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.BlockItem) {
                detectBlockItem(son, currentTable, typeAttributes, inLoop);
            }
        }
        return null;
    }

    private SymbolTableItem detectBlockItem(Token blockItem, SymbolTable currentTable,
                                            SymbolTableItem typeAttributes, boolean inLoop) {
        ArrayList<Token> sons = blockItem.getSons();
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.Decl) {
                detectDecl(son, currentTable);
            } else if (son.getTokenType() == TokenType.Stmt) {
                detectStmt(son, currentTable, typeAttributes, inLoop);
            }
        }
        return null;
    }

    private SymbolTableItem detectStmt(Token stmt, SymbolTable currentTable,
                                       SymbolTableItem typeAttributes, boolean inLoop) {
        ArrayList<Token> sons = stmt.getSons();
        Token firstToken = sons.get(0);
        TokenType firstTokenType = firstToken.getTokenType();
        if (firstTokenType == TokenType.LVal) {
            for (Token son : sons) {
                if (son.getTokenType() == TokenType.LVal) {
                    detectLVal(son, currentTable);
                    // TODO： 这里得到解析LVal的Attributes后，进行解析判断，如果是常量则报错。
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
                    detectStmt(son, currentTable, typeAttributes, Boolean.TRUE);
                }
            }
        } else if (firstTokenType == TokenType.BREAKTK || firstTokenType == TokenType.CONTINUETK) {
            if (inLoop == Boolean.FALSE) {
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
                allFalse.put(firstToken, 'i');
            }
        } else if (firstTokenType == TokenType.Exp) {
            detectExp(firstToken, currentTable);
        }
        return null;
    }

    private int checkStrCon(Token formatStr) {
        String formatString = formatStr.getTokenString();
        int strExpNum = 0;
        for (int i = 0; i < formatString.length(); i++) {
            if (formatString.charAt(i) == '\\' && i + 1 < formatString.length() &&
                    formatString.charAt(i + 1) != 'n') {
                allFalse.put(formatStr, 'a');
            }
            if (formatString.charAt(i) == '%' && i + 1 < formatString.length() &&
                    formatString.charAt(i + 1) == 'd') {
                strExpNum++;
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
            }
        }
        return attributes;
    }

    private SymbolTableItem detectCond(Token cond, SymbolTable currentTable) {
        ArrayList<Token> sons = cond.getSons();
        SymbolTableItem attributes = new SymbolTableItem();
        SymbolTableItem lOrExpAttributes = null;
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.LOrExp) {
                lOrExpAttributes = detectLOrExp(son, currentTable);
                // TODO: combine together
            }
        }
        return attributes;
    }

    private SymbolTableItem detectLVal(Token lVal, SymbolTable currentTable) {
        // TODO: 需要查一下LVal是不是常量
        SymbolTableItem attributes = new SymbolTableItem();
        SymbolTableItem lValAttributes = null;
        ArrayList<Token> sons = lVal.getSons();
        int arrayDimensions = 0;
        for (Token son : sons) {
            if (son.getTokenType() == TokenType.IDENFR) {
                lValAttributes = currentTable.findIdentInAllTable(lVal.getTokenString());
                // TODO: 判断解析LVal的返回值，得到所有属性，判断是否是常量
                //  在这里不判断是否是常量，因为PrimaryExp中可能调用LVal，所以在这类就检查这些值，是否之前定义过
                //  定义过则整合Attributes，为定义过则返回Null
            } else if (son.getTokenType() == TokenType.LBRACK) {
                arrayDimensions++;
                // TODO：维度定西增加；
            } else if (son.getTokenType() == TokenType.Exp) {
                SymbolTableItem expAttributes = detectExp(son, currentTable);
                // TODO：得到的结果整合
            }
        }
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
                }
            }
        } else if (firstToken.getTokenType() == TokenType.LVal) {
            SymbolTableItem lValAttributes = detectLVal(firstToken, currentTable);
            // TODO: combine
        } else if (firstToken.getTokenType() == TokenType.Number) {
            // TODO: combine
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
        } else if (firstToken.getTokenType() == TokenType.IDENFR) {
            SymbolTableItem functionAttributes = currentTable.findIdentInAllTable(firstToken.getTokenString());
            for (Token son : sons) {
                if (son.getTokenType() == TokenType.FuncRParams) {
                    SymbolTableItem funcRParamsAttributes = detectFuncRParams(son, currentTable);
                    // TODO: Check FuncRParams with the functionAttributes
                }
            }
        } else if (firstToken.getTokenType() == TokenType.UnaryOp) {
            SymbolTableItem unaryOpAttributes = detectUnaryOp(firstToken, currentTable);
            for (Token son : sons) {
                if (son.getTokenType() == TokenType.UnaryExp) {
                    SymbolTableItem unaryExpAttributes = detectUnaryExp(son, currentTable);
                    // TODO: combine the unaryExp attributes with unaryOp attributes;
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
            }
        }
        return attributes;
    }
}
