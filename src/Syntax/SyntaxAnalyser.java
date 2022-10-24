package Syntax;

import Syntax.SyntaxException;
import TokenDefines.Token;
import TokenDefines.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
// 语法分析

public class SyntaxAnalyser {
    private ArrayList<Token> tokens;
    private Token root;
    private int cur_pos;
    private static boolean isParsed = false;
    private HashMap<Token, Character> allFalse;

    public SyntaxAnalyser(ArrayList<Token> tokens, HashMap<Token, Character> allFalse) {
        this.tokens = tokens;
        this.allFalse = allFalse;
    }

    private void checkSize() throws SyntaxException {
        if (cur_pos >= tokens.size()) {
            throw new SyntaxException("out size");
        }
    }
    public void parseSyntax() throws SyntaxException {
        isParsed = true;
        cur_pos = 0;
        this.root = new Token(0, TokenType.CompUnit, "");
            // Decl
        while (cur_pos < tokens.size()) {
            if (tokens.get(cur_pos).getTokenType() == TokenType.CONSTTK) {
                Token decl = parseDecl();
                root.addSons(decl);
            } else if (tokens.get(cur_pos).getTokenType() == TokenType.INTTK) {
                if (cur_pos + 1 < tokens.size() && tokens.get(cur_pos + 1).getTokenType() == TokenType.MAINTK) {
                    break;
                } else if (cur_pos + 2 < tokens.size() && tokens.get(cur_pos + 1).getTokenType() == TokenType.IDENFR
                        && tokens.get(cur_pos + 2).getTokenType() == TokenType.LPARENT) {
                    break;
                } else if (cur_pos + 1 < tokens.size() && tokens.get(cur_pos + 1).getTokenType() == TokenType.IDENFR) {
                    Token decl = parseDecl();
                    root.addSons(decl);
                } else {    // Undefined
                    break;
                }
            } else {
                break;
            }
        }
        // FuncDef
        while (cur_pos < tokens.size()) {
            if (tokens.get(cur_pos).getTokenType() == TokenType.VOIDTK) {
                Token funcDef = parseFuncDef();
                root.addSons(funcDef);
            } else if (tokens.get(cur_pos).getTokenType() == TokenType.INTTK) {
                if (tokens.get(cur_pos + 1).getTokenType() == TokenType.MAINTK) {
                    break;
                } else if (cur_pos + 2 < tokens.size() && tokens.get(cur_pos + 1).getTokenType() == TokenType.IDENFR &&
                        tokens.get(cur_pos + 2).getTokenType() == TokenType.LPARENT) {
                    Token funcDef = parseFuncDef();
                    root.addSons(funcDef);
                } else {    // Undefined
                    break;
                }
            } else {    // Undefined
                break;
            }
        }
        // MainFuncDef
        if (tokens.get(cur_pos).getTokenType() == TokenType.INTTK &&
                tokens.get(cur_pos + 1).getTokenType() == TokenType.MAINTK) {
            Token mainFuncDef = parseMainFuncDef();
            root.addSons(mainFuncDef);
        }
    }
    
    public Token getRoot() {
        if (!isParsed) {
            try {
                parseSyntax();
            } catch (SyntaxException e) {
                e.printStackTrace();
            }
        }
        return root;
    }
    
    private Token parseDecl() throws SyntaxException {
        checkSize();
        Token decl = new Token(0, TokenType.Decl, "");
        if (tokens.get(cur_pos).getTokenType() == TokenType.CONSTTK) {
            Token constDecl = parseConstDecl();
            decl.addSons(constDecl);
        } else if (tokens.get(cur_pos).getTokenType() == TokenType.INTTK) {
            Token varDecl = parseVarDecl();
            decl.addSons(varDecl);
        }
        return decl;
    }
    
    private Token parseConstDecl() throws SyntaxException {
        checkSize();
        Token constDecl = new Token(0, TokenType.ConstDecl, "");
        if (tokens.get(cur_pos).getTokenType() != TokenType.CONSTTK) {
            throw new SyntaxException("ConstDecl: not const");
        }
        constDecl.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept const
        Token bType = parseBType();
        constDecl.addSons(bType);
        Token constDef = parseConstDef();
        constDecl.addSons(constDef);
        Token lastToken = constDef;
        while (cur_pos < tokens.size()) {
            if (tokens.get(cur_pos).getTokenType() == TokenType.COMMA) {
                constDecl.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept ,
                Token constDef_loop = parseConstDef();
                constDecl.addSons(constDef_loop);
                lastToken = constDef_loop;
            } else {
                break;
            }
        }
        checkSize();
        if (tokens.get(cur_pos).getTokenType() != TokenType.SEMICN) {
            allFalse.put(lastToken, 'i');
            constDecl.addSons(new Token(TokenType.SEMICN, ";"));
//            throw new Syntax.SyntaxException("ConstDecl: not ;");
        } else {
            constDecl.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept ;
        }
        return constDecl;
    }
    
    private Token parseBType() throws SyntaxException {
        Token bType = new Token(0, TokenType.BType, "");
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.INTTK) {
            throw new SyntaxException("BType: not int");
        }
        bType.addSons(tokens.get(cur_pos));
        cur_pos++;
        return bType;
    }
    
    private Token parseConstDef() throws SyntaxException {
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.IDENFR) {
            throw new SyntaxException("ConstDef: not ident");
        }
        Token constDef = new Token(0, TokenType.ConstDef, "");
        constDef.addSons(tokens.get(cur_pos));
        cur_pos++; // accept Ident
        while (cur_pos < tokens.size()) {
            if (tokens.get(cur_pos).getTokenType() == TokenType.LBRACK) {
                constDef.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept [
                Token constExp = parseConstExp();
                constDef.addSons(constExp);
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RBRACK) {
                    allFalse.put(constExp, 'k');
                    constDef.addSons(new Token(TokenType.RBRACK, "]"));
//                    throw new Syntax.SyntaxException("ConstDef: not ]");
                } else {
                    constDef.addSons(tokens.get(cur_pos));
                    cur_pos++;  // accept ]
                }
            } else {
                break;
            }
        }
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.ASSIGN) {
            throw new SyntaxException("ConstDef: not assign");
        }
        constDef.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept =
        Token constInitVal = parseConstInitVal();
        constDef.addSons(constInitVal);
        return constDef;
    }
    
    private Token parseConstInitVal() throws SyntaxException {
        checkSize();
        Token constInitVal = new Token(0, TokenType.ConstInitVal, "");
        TokenType tokenType = tokens.get(cur_pos).getTokenType();
        if (tokenType == TokenType.PLUS || tokenType == TokenType.MINU ||
            tokenType == TokenType.NOT || tokenType == TokenType.IDENFR ||
            tokenType == TokenType.LPARENT || tokenType == TokenType.INTCON) {
            Token constExp = parseConstExp();
            constInitVal.addSons(constExp);
        } else if (tokenType == TokenType.LBRACE) {
            constInitVal.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept {
            checkSize();
            if (tokens.get(cur_pos).getTokenType() == TokenType.RBRACE) {
                constInitVal.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept }
            } else {    // [constInitVal {',' ConstInitVal}]
                Token midConstInitVal = parseConstInitVal();
                constInitVal.addSons(midConstInitVal);
                while (cur_pos < tokens.size()) {
                    if (tokens.get(cur_pos).getTokenType() == TokenType.COMMA) {
                        constInitVal.addSons(tokens.get(cur_pos));
                        cur_pos++;  // accept ,
                        Token loopConstInitVal = parseConstInitVal();
                        constInitVal.addSons(loopConstInitVal);
                    } else {
                        break;
                    }
                }
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RBRACE) {
                    throw new SyntaxException("ConstInitVal: not }");
                }
                constInitVal.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept }
            }
        } else {    // Undefine
            throw new SyntaxException("ConstInitVal: undefine");
        }
        return constInitVal;
    }
    
    private Token parseVarDecl() throws SyntaxException {
        checkSize();
        Token varDecl = new Token(0, TokenType.VarDecl, "");
        Token bType = parseBType();
        varDecl.addSons(bType);
        checkSize();
        Token lastToken;
        varDecl.addSons(lastToken = parseVarDef());
        while (cur_pos < tokens.size()) {
            if (tokens.get(cur_pos).getTokenType() == TokenType.COMMA) {
                varDecl.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept ,
                varDecl.addSons(lastToken = parseVarDef());
            } else {
                break;
            }
        }
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.SEMICN) {
            allFalse.put(lastToken, 'i');
            varDecl.addSons(new Token(TokenType.SEMICN, ";"));
//            throw new Syntax.SyntaxException("VarDecl: not ;");
        } else {
            varDecl.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept ;
        }
        return varDecl;
    }
    
    private Token parseVarDef() throws SyntaxException {
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.IDENFR) {
            throw new SyntaxException("VarDef: not ident");
        }
        Token varDef = new Token(0, TokenType.VarDef, "");
        varDef.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept Ident
        Token lastToken;
        while (cur_pos < tokens.size()) {
            if (tokens.get(cur_pos).getTokenType() == TokenType.LBRACK) {
                varDef.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept [
                varDef.addSons(lastToken = parseConstExp());
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RBRACK) {
                    allFalse.put(lastToken, 'k');
                    varDef.addSons(new Token(TokenType.RBRACK, "]"));
//                    throw new Syntax.SyntaxException("VarDef: not ]");
                } else {
                    varDef.addSons(tokens.get(cur_pos));
                    cur_pos++;  // accept ]
                }
            } else {
                break;
            }
        }
        if (cur_pos < tokens.size() && tokens.get(cur_pos).getTokenType() == TokenType.ASSIGN) {
            varDef.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept =
            varDef.addSons(parseInitVal());
        }
        return varDef;
    }
    
    private Token parseInitVal() throws SyntaxException {
        checkSize();
        Token initVal = new Token(0, TokenType.InitVal, "");
        TokenType tokenType = tokens.get(cur_pos).getTokenType();
        if (tokenType == TokenType.PLUS || tokenType == TokenType.MINU ||
            tokenType == TokenType.NOT || tokenType == TokenType.LPARENT ||
            tokenType == TokenType.IDENFR || tokenType == TokenType.INTCON) {
            initVal.addSons(parseExp());
        } else if (tokenType == TokenType.LBRACE) {
            initVal.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept {
            checkSize();
            if (tokens.get(cur_pos).getTokenType() == TokenType.RBRACE) {
                initVal.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept }
            } else {
                initVal.addSons(parseInitVal());
                while (cur_pos < tokens.size()) {
                    if (tokens.get(cur_pos).getTokenType() == TokenType.COMMA) {
                        initVal.addSons(tokens.get(cur_pos));
                        cur_pos++;  // accept ,
                        initVal.addSons(parseInitVal());
                    } else {
                        break;
                    }
                }
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RBRACE) {
                    throw new SyntaxException("InitVal: not }");
                }
                initVal.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept }
            }
        } else {
            //return null;
            throw new SyntaxException("InitVal: Undefine");
        }
        return initVal;
    }
    
    private Token parseFuncDef() throws SyntaxException {
        checkSize();
        Token funcDef = new Token(0, TokenType.FuncDef, "");
        funcDef.addSons(parseFuncType());
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.IDENFR) {
            throw new SyntaxException("FuncDef: not ident");
        }
        funcDef.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept Ident
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.LPARENT) {
            throw new SyntaxException("FuncDef: not (");
        }
        Token lastToken;
        funcDef.addSons(lastToken = tokens.get(cur_pos));
        cur_pos++;  // accept (
        checkSize();
        if (tokens.get(cur_pos).getTokenType() == TokenType.INTTK) {
            funcDef.addSons(lastToken = parseFuncFParams());
        }
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RPARENT) {
            allFalse.put(lastToken, 'j');
            funcDef.addSons(new Token(TokenType.RPARENT, ")"));
//            throw new Syntax.SyntaxException("FuncDef: not )");
        } else {
            funcDef.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept )
        }
        funcDef.addSons(parseBlock());
        return funcDef;
    }
    
    private Token parseMainFuncDef() throws SyntaxException {
        checkSize();
        Token mainFuncDef = new Token(0, TokenType.MainFuncDef, "");
        if (tokens.get(cur_pos).getTokenType() == TokenType.INTTK) {
            mainFuncDef.addSons(tokens.get(cur_pos));
            cur_pos++;
        } else {
            throw new SyntaxException("MainFuncDef: missing int");
        }
        checkSize();
        if (tokens.get(cur_pos).getTokenType() == TokenType.MAINTK) {
            mainFuncDef.addSons(tokens.get(cur_pos));
            cur_pos++;
        } else {
            throw new SyntaxException("MainFuncDef: missing main");
        }
        checkSize();
        if (tokens.get(cur_pos).getTokenType() == TokenType.LPARENT) {
            mainFuncDef.addSons(tokens.get(cur_pos));
            cur_pos++;
        } else {
            throw new SyntaxException("MainFuncDef: missing (");
        }
        if (tokens.get(cur_pos).getTokenType() == TokenType.RPARENT) {
            mainFuncDef.addSons(tokens.get(cur_pos));
            cur_pos++;
        } else {
            allFalse.put(tokens.get(cur_pos - 1), 'j');
            mainFuncDef.addSons(new Token(TokenType.RPARENT, ")"));
//            throw new Syntax.SyntaxException("MainFuncDef: missing )");
        }
        checkSize();
        mainFuncDef.addSons(parseBlock());
        return mainFuncDef;
    }
    
    private Token parseFuncType() throws SyntaxException {
        checkSize();
        Token funcType = new Token(0, TokenType.FuncType, "");
        if (tokens.get(cur_pos).getTokenType() != TokenType.VOIDTK &&
                        tokens.get(cur_pos).getTokenType() != TokenType.INTTK) {
            throw new SyntaxException("FuncType: not void or int");
        }
        funcType.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept type (void or int)
        return funcType;
    }
    
    private Token parseFuncFParams() throws SyntaxException {
        checkSize();
        Token funcFParams = new Token(0, TokenType.FuncFParams, "");
        if (tokens.get(cur_pos).getTokenType() == TokenType.INTTK) {
            funcFParams.addSons(parseFuncFParam());
            while (cur_pos < tokens.size()) {
                if (tokens.get(cur_pos).getTokenType() == TokenType.COMMA) {
                    funcFParams.addSons(tokens.get(cur_pos));
                    cur_pos++;
                    funcFParams.addSons(parseFuncFParam());
                } else {
                    break;
                }
            }
        } else {
            throw new SyntaxException("FuncFParams: undefine");
        }
        return funcFParams;
    }
    
    private Token parseFuncFParam() throws SyntaxException {
        checkSize();
        Token funcFParam = new Token(0, TokenType.FuncFParam, "");
        funcFParam.addSons(parseBType());
        checkSize();
        if (tokens.get(cur_pos).getTokenType() != TokenType.IDENFR) {
            throw new SyntaxException("FuncFParam: not int");
        }
        funcFParam.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept Ident
        checkSize();
        if (tokens.get(cur_pos).getTokenType() == TokenType.LBRACK) {
            funcFParam.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept [
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RBRACK) {
                allFalse.put(tokens.get(cur_pos - 1), 'k');
                funcFParam.addSons(new Token(TokenType.RBRACK, "]"));
//                throw new Syntax.SyntaxException("FuncFParam: not ]");
            } else {
                funcFParam.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept ]
            }
            while (cur_pos < tokens.size()) {
                if (tokens.get(cur_pos).getTokenType() == TokenType.LBRACK) {
                    funcFParam.addSons(tokens.get(cur_pos));
                    cur_pos++;  // accept [
                    Token lastToken;
                    funcFParam.addSons(lastToken = parseConstExp());
                    if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RBRACK) {
                        allFalse.put(lastToken, 'k');
                        funcFParam.addSons(new Token(TokenType.RBRACK, "]"));
//                        throw new Syntax.SyntaxException("FuncFParam: not ]");
                    } else {
                        funcFParam.addSons(tokens.get(cur_pos));
                        cur_pos++;  // accept ]
                    }
                } else {
                    break;
                }
            }
        }
        return funcFParam;
    }
    
    private Token parseBlock() throws SyntaxException {
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.LBRACE) {
            throw new SyntaxException("Block: not {");
        }
        Token block = new Token(0, TokenType.Block, "");
        block.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept {
        checkSize();
        while (cur_pos < tokens.size()) {
            TokenType tokenType = tokens.get(cur_pos).getTokenType();
            if (tokenType == TokenType.IDENFR || tokenType == TokenType.PLUS ||
                    tokenType == TokenType.MINU || tokenType == TokenType.NOT ||
                    tokenType == TokenType.LPARENT || tokenType == TokenType.INTCON ||
                    tokenType == TokenType.SEMICN || tokenType == TokenType.LBRACE ||
                    tokenType == TokenType.IFTK || tokenType == TokenType.WHILETK ||
                    tokenType == TokenType.BREAKTK || tokenType == TokenType.RETURNTK ||
                    tokenType == TokenType.PRINTFTK || tokenType == TokenType.INTTK ||
                    tokenType == TokenType.CONSTTK || tokenType == TokenType.CONTINUETK) {
                block.addSons(parseBlockItem());
            } else {
                break;
            }
        }
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RBRACE) {
            throw new SyntaxException("Block: not }");
        }
        block.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept }
        return block;
    }
    
    private Token parseBlockItem() throws SyntaxException {
        checkSize();
        TokenType tokenType = tokens.get(cur_pos).getTokenType();
        Token blockItem = new Token(0, TokenType.BlockItem, "");
        if (tokenType == TokenType.INTTK || tokenType == TokenType.CONSTTK) {
            blockItem.addSons(parseDecl());
        } else if (tokenType == TokenType.IDENFR || tokenType == TokenType.PLUS ||
                tokenType == TokenType.MINU || tokenType == TokenType.NOT ||
                tokenType == TokenType.LPARENT || tokenType == TokenType.INTCON ||
                tokenType == TokenType.SEMICN || tokenType == TokenType.LBRACE ||
                tokenType == TokenType.IFTK || tokenType == TokenType.WHILETK ||
                tokenType == TokenType.BREAKTK || tokenType == TokenType.RETURNTK ||
                tokenType == TokenType.PRINTFTK || tokenType == TokenType.CONTINUETK) {
            blockItem.addSons(parseStmt());
        } else {
            throw new SyntaxException("BlockItem: undefine");
        }
        return blockItem;
    }
    
    private Token parseStmt() throws SyntaxException { // TODO:把分号统一提出来
        checkSize();
        Token stmt = new Token(0, TokenType.Stmt, "");
        TokenType tokenType = tokens.get(cur_pos).getTokenType();
        if (tokenType == TokenType.LBRACE || tokenType == TokenType.WHILETK || tokenType == TokenType.IFTK) {
            if (tokenType == TokenType.LBRACE) {
                stmt.addSons(parseBlock());
            } else if (tokenType == TokenType.IFTK) {
                stmt.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept if
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.LPARENT) {
                    throw new SyntaxException("Stmt: not (");
                }
                stmt.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept (
                Token cond = parseCond();
                stmt.addSons(cond);
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RPARENT) {
                    allFalse.put(cond, 'j');
                    stmt.addSons(new Token(TokenType.RPARENT, ")"));
//                    throw new Syntax.SyntaxException("Stmt: not )");
                } else {
                    stmt.addSons(tokens.get(cur_pos));
                    cur_pos++;  // accept )
                }
                stmt.addSons(parseStmt());
                if (cur_pos < tokens.size() && tokens.get(cur_pos).getTokenType() == TokenType.ELSETK) {
                    stmt.addSons(tokens.get(cur_pos));
                    cur_pos++;  // accept else
                    stmt.addSons(parseStmt());
                }
            } else {
                stmt.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept while
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.LPARENT) {
                    throw new SyntaxException("Stmt: not (");
                }
                stmt.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept (
                Token cond = parseCond();
                stmt.addSons(cond);
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RPARENT) {
                    allFalse.put(cond, 'j');
                    stmt.addSons(new Token(TokenType.RPARENT, ")"));
//                    throw new Syntax.SyntaxException("Stmt: not )");
                } else {
                    stmt.addSons(tokens.get(cur_pos));
                    cur_pos++;  // accept )
                }
                stmt.addSons(parseStmt());
            }
        } else if (tokenType == TokenType.IDENFR || tokenType == TokenType.BREAKTK ||
                tokenType == TokenType.CONTINUETK || tokenType == TokenType.RETURNTK ||
                tokenType == TokenType.PRINTFTK || tokenType == TokenType.PLUS ||
                tokenType == TokenType.MINU || tokenType == TokenType.NOT ||
                tokenType == TokenType.LPARENT || tokenType == TokenType.INTCON ||
                tokenType == TokenType.SEMICN) {
            if (tokenType == TokenType.IDENFR) {
                int flag = -1;
                for (int i = cur_pos; i < tokens.size(); i++) {
                    if (tokens.get(i).getTokenType() == TokenType.ASSIGN) {
                        flag = 0;
                        break;
                    }
                }
                if (flag == 0) {
                    int traceBackPoint = cur_pos;
                    Token lVal = parseLVal();
//                    stmt.addSons(parseLVal());
                    if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.ASSIGN) {
                        cur_pos = traceBackPoint;
                        stmt.addSons(parseExp());
                    } else {
                        stmt.addSons(lVal);
                        stmt.addSons(tokens.get(cur_pos));
                        cur_pos++;  // accept =
                        checkSize();
                        TokenType tokenTypeExp = tokens.get(cur_pos).getTokenType();
                        if (tokenTypeExp == TokenType.GETINTTK) {
                            stmt.addSons(tokens.get(cur_pos));
                            cur_pos++;  // accept getint
                            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.LPARENT) {
                                throw new SyntaxException("Stmt: not (");
                            }
                            stmt.addSons(tokens.get(cur_pos));
                            cur_pos++;  // accept (
                            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RPARENT) {
                                allFalse.put(tokens.get(cur_pos - 1), 'j');
                                stmt.addSons(new Token(TokenType.RPARENT, ")"));
//                            throw new Syntax.SyntaxException("Stmt: not )");
                            } else {
                                stmt.addSons(tokens.get(cur_pos));
                                cur_pos++;  // accept )
                            }
                        } else if (tokenTypeExp == TokenType.PLUS || tokenTypeExp == TokenType.MINU ||
                                tokenTypeExp == TokenType.NOT || tokenTypeExp == TokenType.IDENFR ||
                                tokenTypeExp == TokenType.LPARENT || tokenTypeExp == TokenType.INTCON) {
                            Token exp = parseExp(); // Exp branch
                            stmt.addSons(exp);
                        }
                    }
                } else {
                    stmt.addSons(parseExp());
                }
            } else if (tokenType == TokenType.BREAKTK) {
                stmt.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept break
            } else if (tokenType == TokenType.CONTINUETK) {
                stmt.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept continue
            } else if (tokenType == TokenType.RETURNTK) {
                stmt.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept return
                checkSize();
                TokenType tokenTypeExp = tokens.get(cur_pos).getTokenType();
                if (tokenTypeExp == TokenType.PLUS || tokenTypeExp == TokenType.MINU ||
                        tokenTypeExp == TokenType.NOT || tokenTypeExp == TokenType.IDENFR ||
                        tokenTypeExp == TokenType.LPARENT || tokenTypeExp == TokenType.INTCON) {
                    stmt.addSons(parseExp());
                }
            } else if (tokenType == TokenType.PRINTFTK) {
                stmt.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept printf
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.LPARENT) {
                    throw new SyntaxException("Stmt: not (");
                }
                stmt.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept (
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.STRCON) {
                    throw new SyntaxException("Stmt: not string");
                }
                Token lastToken;
                stmt.addSons(lastToken = tokens.get(cur_pos));
                cur_pos++;  // accept FormatString
                while (cur_pos < tokens.size()) {
                    if (tokens.get(cur_pos).getTokenType() == TokenType.COMMA) {
                        stmt.addSons(tokens.get(cur_pos));
                        cur_pos++;  // accept ,
                        stmt.addSons(lastToken = parseExp());
                    } else {
                        break;
                    }
                }
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RPARENT) {
                    allFalse.put(lastToken, 'j');
                    stmt.addSons(new Token(TokenType.RPARENT, ")"));
//                    throw new Syntax.SyntaxException("Stmt: not )");
                } else {
                    stmt.addSons(tokens.get(cur_pos));
                    cur_pos++;  // accept )
                }
            } else if (tokenType == TokenType.PLUS || tokenType == TokenType.MINU ||
                    tokenType == TokenType.NOT ||
                    tokenType == TokenType.LPARENT || tokenType == TokenType.INTCON) {
                stmt.addSons(parseExp());
            }
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.SEMICN) {
                allFalse.put(stmt.getLastSon(), 'i');
                stmt.addSons(new Token(TokenType.SEMICN, ";"));
//                throw new Syntax.SyntaxException("Stmt: not ;");
            } else {
                stmt.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept ;
            }
        } else { // Undefine
            throw new SyntaxException("Stmt: 1undefine");
        }
        return stmt;
    }
    
    private Token parseExp() throws SyntaxException {
        checkSize();
        Token exp = new Token(0, TokenType.Exp, "");
        exp.addSons(parseAddExp());
        return exp;
    }
    
    private Token parseCond() throws SyntaxException {
        checkSize();
        Token cond = new Token(0, TokenType.Cond, "");
        cond.addSons(parseLOrExp());
        return cond;
    }
    
    private Token parseLVal() throws SyntaxException {
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.IDENFR) {
            throw new SyntaxException("LVal: not ident");
        }
        Token lVal = new Token(0, TokenType.LVal, "");
        lVal.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept ident
        while (cur_pos < tokens.size()) {
            TokenType tokenType = tokens.get(cur_pos).getTokenType();
            if (tokenType == TokenType.LBRACK) {
                lVal.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept [
                Token lastToken;
                lVal.addSons(lastToken = parseExp());
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RBRACK) {
                    allFalse.put(lastToken, 'k');
                    lVal.addSons(new Token(TokenType.RBRACK, "]"));
//                    throw new Syntax.SyntaxException("LVal: not ]");
                } else {
                    lVal.addSons(tokens.get(cur_pos));
                    cur_pos++;  // accept ]
                }
            } else {
                break;
            }
        }
        return lVal;
    }
    
    private Token parsePrimaryExp() throws SyntaxException {
        checkSize();
        Token primaryExp = new Token(0, TokenType.PrimaryExp, "");
        if (tokens.get(cur_pos).getTokenType() == TokenType.LPARENT) {
            primaryExp.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept (
            Token lastToken;
            primaryExp.addSons(lastToken = parseExp());
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RPARENT) {
                allFalse.put(lastToken, 'j');
                primaryExp.addSons(new Token(TokenType.RPARENT, ")"));
//                throw new Syntax.SyntaxException("PrimaryExp: not )");
            } else {
                primaryExp.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept )
            }
        } else if (tokens.get(cur_pos).getTokenType() == TokenType.IDENFR) {
            primaryExp.addSons(parseLVal());
        } else if (tokens.get(cur_pos).getTokenType() == TokenType.INTCON) {
            primaryExp.addSons(parseNumber());
        }
        return primaryExp;
    }
    
    private Token parseNumber() throws SyntaxException {
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.INTCON) {
            throw new SyntaxException("Number: not int");
        }
        Token number = new Token(0, TokenType.Number, "");
        number.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept IntConst
        return number;
    }
    
    private Token parseUnaryExp() throws SyntaxException {
        // PrimaryExp 和 Ident 分支的FIRST集合产生冲突，向后看第二个token，如果是(则为分支2，否则进入分支1(PrimaryExp)
        checkSize();
        Token unaryExp = new Token(0, TokenType.UnaryExp, "");
        if (tokens.get(cur_pos).getTokenType() == TokenType.IDENFR) {
            if (cur_pos + 1 >= tokens.size()) {
                throw new SyntaxException("UnaryExp: out size");
            }
            if (tokens.get(cur_pos + 1).getTokenType() == TokenType.LPARENT) {  // branch 2 Ident '(' [FuncRParams] ')'
                unaryExp.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept Ident
                Token lastToken;
                unaryExp.addSons(lastToken = tokens.get(cur_pos));
                cur_pos++;  // accept (
                if (cur_pos < tokens.size() && tokens.get(cur_pos).getTokenType() == TokenType.PLUS ||
                        tokens.get(cur_pos).getTokenType() == TokenType.MINU ||
                        tokens.get(cur_pos).getTokenType() == TokenType.NOT ||
                        tokens.get(cur_pos).getTokenType() == TokenType.IDENFR ||
                        tokens.get(cur_pos).getTokenType() == TokenType.LPARENT ||
                        tokens.get(cur_pos).getTokenType() == TokenType.INTCON) {
                    unaryExp.addSons(lastToken = parseFuncRParams());
                }
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RPARENT) {
                    allFalse.put(lastToken, 'j');
                    unaryExp.addSons(new Token(TokenType.RPARENT, ")"));
//                    throw new Syntax.SyntaxException("UnaryExp: not )");
                } else {
                    unaryExp.addSons(tokens.get(cur_pos));
                    cur_pos++;  // accept )
                }
            } else {
                unaryExp.addSons(parsePrimaryExp());
            }
        } else if (tokens.get(cur_pos).getTokenType() == TokenType.LPARENT ||
                tokens.get(cur_pos).getTokenType() == TokenType.INTCON) {
            unaryExp.addSons(parsePrimaryExp());
        } else if (tokens.get(cur_pos).getTokenType() == TokenType.PLUS ||
                tokens.get(cur_pos).getTokenType() == TokenType.MINU ||
                tokens.get(cur_pos).getTokenType() == TokenType.NOT) {
            unaryExp.addSons(parseUnaryOp());
            unaryExp.addSons(parseUnaryExp());
        } else {
            throw new SyntaxException("UnaryExp: undefine");
        }
        return unaryExp;
    }
    
    private Token parseUnaryOp() throws SyntaxException {
        checkSize();
        Token unaryOp = new Token(0, TokenType.UnaryOp, "");
        if (tokens.get(cur_pos).getTokenType() == TokenType.PLUS ||
            tokens.get(cur_pos).getTokenType() == TokenType.MINU ||
            tokens.get(cur_pos).getTokenType() == TokenType.NOT) {
            unaryOp.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept + or - or !
        } else {
            throw new SyntaxException("UnaryOp: undefine");
        }
        return unaryOp;
    }
    
    private Token parseFuncRParams() throws SyntaxException {
        checkSize();
        Token funcRParams = new Token(0, TokenType.FuncRParams, "");
        funcRParams.addSons(parseExp());
        while (cur_pos < tokens.size()) {
            if (tokens.get(cur_pos).getTokenType() == TokenType.COMMA) {
                funcRParams.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept ,
                funcRParams.addSons(parseExp());
            } else {
                break;
            }
        }
        return funcRParams;
    }
    
    private Token parseMulExp() throws SyntaxException {
        checkSize();
        Token mulExp = new Token(0, TokenType.MulExp, "");
        mulExp.addSons(parseUnaryExp());
        while (cur_pos < tokens.size()) {
            TokenType tokenType = tokens.get(cur_pos).getTokenType();
            if (tokenType == TokenType.MULT || tokenType == TokenType.DIV || tokenType == TokenType.MOD) {
                mulExp.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept * / %
                mulExp.addSons(parseUnaryExp());
            } else {
                break;
            }
        }
        return mulExp;
    }
    
    private Token parseAddExp() throws SyntaxException {
        checkSize();
        Token addExp = new Token(0, TokenType.AddExp, "");
        addExp.addSons(parseMulExp());
        while (cur_pos < tokens.size()) {
            TokenType tokenType = tokens.get(cur_pos).getTokenType();
            if (tokenType == TokenType.PLUS || tokenType == TokenType.MINU) {
                addExp.addSons(tokens.get(cur_pos));
                cur_pos++;   // accept + or -
                addExp.addSons(parseMulExp());
            } else {
                break;
            }
        }
        return addExp;
    }
    
    private Token parseRelExp() throws SyntaxException {
        checkSize();
        Token relExp = new Token(0, TokenType.RelExp, "");
        relExp.addSons(parseAddExp());
        while (cur_pos < tokens.size()) {
            TokenType tokenType = tokens.get(cur_pos).getTokenType();
            if (tokenType == TokenType.LSS || tokenType == TokenType.GRE ||
                tokenType == TokenType.LEQ || tokenType == TokenType.GEQ) {
                relExp.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept < > <= >=
                relExp.addSons(parseAddExp());
            } else {
                break;
            }
        }
        return relExp;
    }
    
    private Token parseEqExp() throws SyntaxException {
        checkSize();
        Token eqExp = new Token(0, TokenType.EqExp, "");
        eqExp.addSons(parseRelExp());
        while (cur_pos < tokens.size()) {
            TokenType tokenType = tokens.get(cur_pos).getTokenType();
            if (tokenType == TokenType.EQL || tokenType == TokenType.NEQ) {
                eqExp.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept == !=
                eqExp.addSons(parseRelExp());
            } else {
                break;
            }
        }
        return eqExp;
    }
    
    private Token parseLAndExp() throws SyntaxException {
        checkSize();
        Token lAndExp = new Token(0, TokenType.LAndExp, "");
        lAndExp.addSons(parseEqExp());
        while (cur_pos < tokens.size()) {
            TokenType tokenType = tokens.get(cur_pos).getTokenType();
            if (tokenType == TokenType.AND) {
                lAndExp.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept &&
                lAndExp.addSons(parseEqExp());
            } else {
                break;
            }
        }
        return lAndExp;
    }
    
    private Token parseLOrExp() throws SyntaxException {
        checkSize();
        Token lOrExp = new Token(0, TokenType.LOrExp, "");
        lOrExp.addSons(parseLAndExp());
        while (cur_pos < tokens.size()) {
            TokenType tokenType = tokens.get(cur_pos).getTokenType();
            if (tokenType == TokenType.OR) {
                lOrExp.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept ||
                lOrExp.addSons(parseLAndExp());
            } else {
                break;
            }
        }
        return lOrExp;
    }
    
    private Token parseConstExp() throws SyntaxException {
        checkSize();
        Token constExp = new Token(0, TokenType.ConstExp, "");
        constExp.addSons(parseAddExp());
        return constExp;
    }
}