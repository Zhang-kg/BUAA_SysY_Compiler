package NonTerminals;

public class SAbk {
    /*
    import com.sun.media.sound.DLSSample;
import org.w3c.dom.ls.LSSerializer;

import java.util.ArrayList;
import java.util.logging.LoggingPermission;

// 语法分析

public class SyntaxAnalyser {
    private ArrayList<Token> tokens;
    private Token root;
    private int cur_pos;
    private static boolean isParsed = false;

    public SyntaxAnalyser(ArrayList<Token> tokens) {
        this.tokens = tokens;
    }

    public void parseSyntax() {
        isParsed = true;
        cur_pos = 0;
        this.root = new Token(0, TokenType.CompUnit, "");
        try {
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
        } catch (SyntaxException e) {
            e.printStackTrace();
        }
    }

    public Token getRoot() {
        if (!isParsed) parseSyntax();
        return root;
    }

    private Token parseDecl() throws SyntaxException {
        Token decl = new Token(0, TokenType.Decl, "");
        if (cur_pos < tokens.size()) {
            if (tokens.get(cur_pos).getTokenType() == TokenType.CONSTTK) {
                Token constDecl = parseConstDecl();
                decl.addSons(constDecl);
            } else if (tokens.get(cur_pos).getTokenType() == TokenType.INTTK) {
                Token varDecl = parseVarDecl();
                decl.addSons(varDecl);
            }
        } else {    // Undefined
            throw new SyntaxException("parseDecl: position out of maxLength");
            //return null;
        }
        return decl;
    }

    private Token parseConstDecl() throws SyntaxException {
        Token constDecl = new Token(0, TokenType.ConstDecl, "");
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.CONSTTK) {
            //return null;
            throw new SyntaxException("ConstDecl: not const");
        }
        constDecl.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept const
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.INTTK) {
            //return null;
            throw new SyntaxException("ConstDecl: not int");
        }
        Token bType = parseBType();
        constDecl.addSons(bType);
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.IDENFR) {
            //return null;
            throw new SyntaxException("ConstDecl: not ident");
        }
        Token constDef = parseConstDef();
        constDecl.addSons(constDef);
        while (cur_pos < tokens.size()) {
            if (tokens.get(cur_pos).getTokenType() == TokenType.SEMICN) {
                break;
            } else if (tokens.get(cur_pos).getTokenType() == TokenType.COMMA) {
                constDecl.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept ,
                Token constDef_loop = parseConstDef();
                constDecl.addSons(constDef_loop);
            }
        }
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.SEMICN) {
//            return null;
            throw new SyntaxException("ConstDecl: not ;");
        }
        constDecl.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept ;
        return constDecl;
    }

    private Token parseBType() throws SyntaxException {
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.INTTK) {
            //return null;
            throw new SyntaxException("BType: not int");
        }
        Token bType = tokens.get(cur_pos);
        cur_pos++;
        return bType;
    }

    private Token parseConstDef() throws SyntaxException {
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.IDENFR) {
            //return null;
            throw new SyntaxException("ConstDef: not ident");
        }
        Token constDef = new Token(0, TokenType.ConstDef, "");
        constDef.addSons(tokens.get(cur_pos));
        cur_pos++; // accept Ident
        while (cur_pos < tokens.size()) {
            if (tokens.get(cur_pos).getTokenType() == TokenType.ASSIGN) {
                break;
            } else if (tokens.get(cur_pos).getTokenType() == TokenType.LBRACK) {
                constDef.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept [
                Token constExp = parseConstExp();
                constDef.addSons(constExp);
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RBRACK) {
                    //return null;
                    throw new SyntaxException("ConstDef: not ]");
                }
                constDef.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept ]
            } else {    // Undefine
                //return null;
                throw new SyntaxException("ConstDef: undefine");
            }
        }
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.ASSIGN) {
            //return null;
            throw new SyntaxException("ConstDef: not assign");
        }
        constDef.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept =
        Token constInitVal = parseConstInitVal();
        constDef.addSons(constInitVal);
        return constDef;
    }

    private Token parseConstInitVal() throws SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("ConstInitVal: out size");
        }
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
            if (cur_pos >= tokens.size()) {
                //return null;
                throw new SyntaxException("ConstInitVal: out size");
            }
            if (tokens.get(cur_pos).getTokenType() == TokenType.RBRACE) {
                constInitVal.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept }
            } else {    // [constInitVal {',' ConstInitVal}]
                Token midConstInitVal = parseConstInitVal();
                constInitVal.addSons(midConstInitVal);
                while (cur_pos < tokens.size()) {
                    if (tokens.get(cur_pos).getTokenType() == TokenType.RBRACE) {
                        break;
                    } else if (tokens.get(cur_pos).getTokenType() == TokenType.COMMA) {
                        constInitVal.addSons(tokens.get(cur_pos));
                        cur_pos++;  // accept ,
                        Token loopConstInitVal = parseConstInitVal();
                        constInitVal.addSons(loopConstInitVal);
                    }
                }
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RBRACE) {
                    //return null;
                    throw new SyntaxException("ConstInitVal: not }");
                }
                constInitVal.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept }
            }
        } else {    // Undefine
            //return null;
            throw new SyntaxException("ConstInitVal: undefine");
        }
        return constInitVal;
    }

    private Token parseVarDecl() throws SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("VarDecl: out size");
        }
        Token varDecl = new Token(0, TokenType.VarDecl, "");
        Token bType = parseBType();
        varDecl.addSons(bType);
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("VarDecl: out size");
        }
        varDecl.addSons(parseVarDef());
        while (cur_pos < tokens.size()) {
            if (tokens.get(cur_pos).getTokenType() == TokenType.SEMICN) {
                break;
            } else if (tokens.get(cur_pos).getTokenType() == TokenType.COMMA) {
                varDecl.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept ,
                varDecl.addSons(parseVarDef());
            }
        }
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.SEMICN) {
            //return null;
            throw new SyntaxException("VarDecl: not ;");
        }
        varDecl.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept ;
        return varDecl;
    }

    private Token parseVarDef() throws SyntaxException {
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.IDENFR) {
            //return null;
            throw new SyntaxException("VarDef: not ident");
        }
        Token varDef = new Token(0, TokenType.VarDef, "");
        varDef.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept Ident
        while (cur_pos < tokens.size()) {
            if (tokens.get(cur_pos).getTokenType() == TokenType.ASSIGN ||
                tokens.get(cur_pos).getTokenType() == TokenType.COMMA ||
                tokens.get(cur_pos).getTokenType() == TokenType.SEMICN) {
                break;
            } else if (tokens.get(cur_pos).getTokenType() == TokenType.LBRACK) {
                varDef.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept [
                varDef.addSons(parseConstExp());
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RBRACK) {
                    //return null;
                    throw new SyntaxException("VarDef: not ]");
                }
                varDef.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept ]
            } else {
                //return null;
                throw new SyntaxException("VarDef: not in first && follow");
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
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("InitVal: out size");
        }
        Token initVal = new Token(0, TokenType.InitVal, "");
        TokenType tokenType = tokens.get(cur_pos).getTokenType();
        if (tokenType == TokenType.PLUS || tokenType == TokenType.MINU ||
            tokenType == TokenType.NOT || tokenType == TokenType.LPARENT ||
            tokenType == TokenType.IDENFR || tokenType == TokenType.INTCON) {
            initVal.addSons(parseExp());
        } else if (tokenType == TokenType.LBRACE) {
            initVal.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept {
            if (cur_pos >= tokens.size()) {
                //return null;
                throw new SyntaxException("InitVal: out size");
            }
            if (tokens.get(cur_pos).getTokenType() == TokenType.RBRACE) {
                initVal.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept }
            } else {
                initVal.addSons(parseInitVal());
                while (cur_pos < tokens.size()) {
                    if (tokens.get(cur_pos).getTokenType() == TokenType.RBRACE) {
                        break;
                    } else if (tokens.get(cur_pos).getTokenType() == TokenType.COMMA) {
                        initVal.addSons(tokens.get(cur_pos));
                        cur_pos++;  // accept ,
                        initVal.addSons(parseInitVal());
                    } else {
                        //return null;
                        throw new SyntaxException("InitVal: not in First Follow");
                    }
                }
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RBRACE) {
                    //return null;
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
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("FuncDef: out size");
        }
        Token funcDef = new Token(0, TokenType.FuncDef, "");
        funcDef.addSons(parseFuncType());
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.IDENFR) {
            //return null;
            throw new SyntaxException("FuncDef: not ident");
        }
        funcDef.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept Ident
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.LPARENT) {
            //return null;
            throw new SyntaxException("FuncDef: not (");
        }
        funcDef.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept (
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("FuncDef: out size");
        }
        if (tokens.get(cur_pos).getTokenType() == TokenType.RPARENT) {
            funcDef.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept )
        } else if (tokens.get(cur_pos).getTokenType() == TokenType.INTTK) {
            funcDef.addSons(parseFuncFParams());
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RPARENT) {
                //return null;
                throw new SyntaxException("FuncDef: not )");
            }
            funcDef.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept )
        } else {
            //return null;
            throw new SyntaxException("FuncDef: undefine, maybe messing )");
        }

        funcDef.addSons(parseBlock());
        return funcDef;
    }

    private Token parseMainFuncDef() throws SyntaxException {
        if (cur_pos + 3 >= tokens.size()) {
            //return null;
            throw new SyntaxException("MainFuncDef: out size");
        }
        Token mainFuncDef = new Token(0, TokenType.MainFuncDef, "");
        if (tokens.get(cur_pos).getTokenType() == TokenType.INTTK) {
            mainFuncDef.addSons(tokens.get(cur_pos));
            cur_pos++;
        } else {
            //return null;
            throw new SyntaxException("MainFuncDef: missing int");
        }
        if (tokens.get(cur_pos).getTokenType() == TokenType.MAINTK) {
            mainFuncDef.addSons(tokens.get(cur_pos));
            cur_pos++;
        } else {
            //return null;
            throw new SyntaxException("MainFuncDef: missing main");
        }
        if (tokens.get(cur_pos).getTokenType() == TokenType.LPARENT) {
            mainFuncDef.addSons(tokens.get(cur_pos));
            cur_pos++;
        } else {
            //return null;
            throw new SyntaxException("MainFuncDef: missing (");
        }
        if (tokens.get(cur_pos).getTokenType() == TokenType.RPARENT) {
            mainFuncDef.addSons(tokens.get(cur_pos));
            cur_pos++;
        } else {
            //return null;
            throw new SyntaxException("MainFuncDef: missing )");
        }
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("MainFuncDef: out size");
        }
        mainFuncDef.addSons(parseBlock());
        return mainFuncDef;
    }

    private Token parseFuncType() throws SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("FuncType: out size");
        }
        Token funcType = new Token(0, TokenType.FuncType, "");
        if (cur_pos >= tokens.size() ||
                (tokens.get(cur_pos).getTokenType() != TokenType.VOIDTK &&
                        tokens.get(cur_pos).getTokenType() != TokenType.INTTK)) {
            //return null;
            throw new SyntaxException("FuncType: not void or int");
        }
        funcType.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept type (void or int)
        return funcType;
    }

    private Token parseFuncFParams() throws SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("FuncFParams: out size");
        }
        Token funcFParams = new Token(0, TokenType.FuncFParams, "");
        if (tokens.get(cur_pos).getTokenType() == TokenType.INTTK) {
            funcFParams.addSons(parseFuncFParam());
            while (cur_pos < tokens.size()) {
                if (tokens.get(cur_pos).getTokenType() == TokenType.RPARENT) {
                    break;
                } else if (tokens.get(cur_pos).getTokenType() == TokenType.COMMA) {
                    funcFParams.addSons(tokens.get(cur_pos));
                    cur_pos++;
                    funcFParams.addSons(parseFuncFParam());
                } else {
                    //return null;
                    throw new SyntaxException("FuncFParams: not in First or Follow");
                }
            }
        } else {
            //return null;
            throw new SyntaxException("FuncFParams: undefine");
        }
        return funcFParams;
    }

    private Token parseFuncFParam() throws SyntaxException {
        if (cur_pos + 1 >= tokens.size()) {
            //return null;
            throw new SyntaxException("FuncFParam: out size");
        }
        Token funcFParam = new Token(0, TokenType.FuncFParam, "");
        funcFParam.addSons(parseBType());
        if (tokens.get(cur_pos).getTokenType() != TokenType.IDENFR) {
            //return null;
            throw new SyntaxException("FuncFParam: not int");
        }
        funcFParam.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept Ident
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("FuncFParam: out size");
        }
        if (tokens.get(cur_pos).getTokenType() == TokenType.RPARENT ||
                tokens.get(cur_pos).getTokenType() == TokenType.COMMA) {
            return funcFParam;
        } else if (tokens.get(cur_pos).getTokenType() == TokenType.LBRACK) {
            //if (cur_pos + 1 >= tokens.size()) return null;
            funcFParam.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept [
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RBRACK) {
                //return null;
                throw new SyntaxException("FuncFParam: not ]");
            }
            funcFParam.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept ]
            while (cur_pos < tokens.size()) {
                if (tokens.get(cur_pos).getTokenType() == TokenType.RPARENT ||
                        tokens.get(cur_pos).getTokenType() == TokenType.COMMA) {
                    break;
                } else if (tokens.get(cur_pos).getTokenType() == TokenType.LBRACK) {
                    funcFParam.addSons(tokens.get(cur_pos));
                    cur_pos++;  // accept [
                    funcFParam.addSons(parseConstExp());
                    if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RBRACK) {
                        //return null;
                        throw new SyntaxException("FuncFParam: not ]");
                    }
                    funcFParam.addSons(tokens.get(cur_pos));
                    cur_pos++;  // accept ]
                } else {
                    //return null; // Undefine
                    throw new SyntaxException("FuncFParam: undefine, maybe missing ]");
                }
            }
        }
        return funcFParam;
    }

    private Token parseBlock() throws SyntaxException {
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.LBRACE) {
            //return null;
            throw new SyntaxException("Block: not {");
        }
        Token block = new Token(0, TokenType.Block, "");
        block.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("Block: out size");
        }
        while (cur_pos < tokens.size()) {
            TokenType tokenType = tokens.get(cur_pos).getTokenType();
            if (tokenType == TokenType.RBRACE) {
                break;
            } else if (tokenType == TokenType.IDENFR || tokenType == TokenType.PLUS ||
                    tokenType == TokenType.MINU || tokenType == TokenType.NOT ||
                    tokenType == TokenType.LPARENT || tokenType == TokenType.INTCON ||
                    tokenType == TokenType.SEMICN || tokenType == TokenType.LBRACE ||
                    tokenType == TokenType.IFTK || tokenType == TokenType.WHILETK ||
                    tokenType == TokenType.BREAKTK || tokenType == TokenType.RETURNTK ||
                    tokenType == TokenType.PRINTFTK || tokenType == TokenType.INTTK ||
                    tokenType == TokenType.CONSTTK || tokenType == TokenType.CONTINUETK) {
                block.addSons(parseBlockItem());
            } else {
                //return null;
                throw new SyntaxException("Block: not in First or Follow");
            }
        }
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RBRACE) {
            //return null;
            throw new SyntaxException("Block: not }");
        }
        block.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept }
        return block;
    }

    private Token parseBlockItem() throws SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("BlockItem: out size");
        }
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
            //return null;
            throw new SyntaxException("BlockItem: not in First or Follow");
        }
        return blockItem;
    }

    private Token parseStmt() throws SyntaxException {
        //System.out.println(debug + " " + cur_pos);
        //debug++;
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("Stmt: out size");
        }
        Token stmt = new Token(0, TokenType.Stmt, "");
        TokenType tokenType = tokens.get(cur_pos).getTokenType();
        if (tokenType == TokenType.IDENFR) {
            int flag = -1;
            for (int i = cur_pos; i < tokens.size(); i++) {
                if (tokens.get(i).getTokenType() == TokenType.SEMICN) {
                    flag = 2;
                    break;
                } else if (tokens.get(i).getTokenType() == TokenType.ASSIGN) {
                    flag = 0;
                    break;
                }
            }
            if (flag == 0) {
                stmt.addSons(parseLVal());
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.ASSIGN) {
                    //return null;
                    throw new SyntaxException("Stmt: not =");
                }
                stmt.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept =
                if (cur_pos >= tokens.size()) {
                    //return null;
                    throw new SyntaxException("Stmt: out size");
                }
                TokenType tokenTypeExp = tokens.get(cur_pos).getTokenType();
                if (tokenTypeExp == TokenType.GETINTTK) {
                    stmt.addSons(tokens.get(cur_pos));
                    cur_pos++;  // accept getint
                    if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.LPARENT) {
                        //return null;
                        throw new SyntaxException("Stmt: not (");
                    }
                    stmt.addSons(tokens.get(cur_pos));
                    cur_pos++;  // accept (
                    if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RPARENT) {
                        //return null;
                        throw new SyntaxException("Stmt: not )");
                    }
                    stmt.addSons(tokens.get(cur_pos));
                    cur_pos++;  // accept )
                } else if (tokenTypeExp == TokenType.PLUS || tokenTypeExp == TokenType.MINU ||
                        tokenTypeExp == TokenType.NOT || tokenTypeExp == TokenType.IDENFR ||
                        tokenTypeExp == TokenType.LPARENT || tokenTypeExp == TokenType.INTCON) {
                    Token exp = parseExp(); // Exp branch
                    stmt.addSons(exp);
                }
            } else if (flag == 2) {
                stmt.addSons(parseExp());
            } else {
                //return null;
                throw new SyntaxException("Stmt: undefine");
            }

            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.SEMICN) {
                //return null;
                throw new SyntaxException("Stmt: not ;");
            }
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept ;
        } else if (tokenType == TokenType.LBRACE) {
            stmt.addSons(parseBlock());
        } else if (tokenType == TokenType.IFTK) {
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept if
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.LPARENT) {
                //return null;
                throw new SyntaxException("Stmt: not (");
            }
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept (
            try {
                Token cond = parseCond();
            } catch (SyntaxException e) {

            }
            Token cond = parseCond();
            stmt.addSons(cond);
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RPARENT) {
                //return null;
                throw new SyntaxException("Stmt: not )");
            }
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept )
            stmt.addSons(parseStmt());
            if (cur_pos < tokens.size() && tokens.get(cur_pos).getTokenType() == TokenType.ELSETK) {
                stmt.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept else
                stmt.addSons(parseStmt());
            }
        } else if (tokenType == TokenType.WHILETK) {
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept while
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.LPARENT) {
                //return null;
                throw new SyntaxException("Stmt: not (");
            }
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept (
            stmt.addSons(parseCond());
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RPARENT) {
                //return null;
                throw new SyntaxException("Stmt: not )");
            }
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept )
            stmt.addSons(parseStmt());
        } else if (tokenType == TokenType.BREAKTK) {
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept break
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.SEMICN) {
                //return null;
                throw new SyntaxException("Stmt: not ;");
            }
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept ;
        } else if (tokenType == TokenType.CONTINUETK) {
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept continue
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.SEMICN) {
                //return null;
                throw new SyntaxException("Stmt: not ;");
            }
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept ;
        } else if (tokenType == TokenType.RETURNTK) {
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept return
            if (cur_pos >= tokens.size()) {
                //return null;
                throw new SyntaxException("Stmt: out size");
            }
            TokenType tokenTypeExp = tokens.get(cur_pos).getTokenType();
            if (tokenTypeExp == TokenType.SEMICN) {
                stmt.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept return
            } else if (tokenTypeExp == TokenType.PLUS || tokenTypeExp == TokenType.MINU ||
                       tokenTypeExp == TokenType.NOT || tokenTypeExp == TokenType.IDENFR ||
                       tokenTypeExp == TokenType.LPARENT || tokenTypeExp == TokenType.INTCON) {
                stmt.addSons(parseExp());
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.SEMICN) {
                    //return null;
                    throw new SyntaxException("Stmt: not ;");
                }
                stmt.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept ;
            } else {
                //return null;
                throw new SyntaxException("Stmt: undefine maybe missing ;");
            }
        } else if (tokenType == TokenType.PRINTFTK) {
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept printf
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.LPARENT) {
                //return null;
                throw new SyntaxException("Stmt: not (");
            }
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept (
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.STRCON) {
                //return null;
                throw new SyntaxException("Stmt: not string");
            }
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept FormatString
            while (cur_pos < tokens.size()) {
                if (tokens.get(cur_pos).getTokenType() == TokenType.RPARENT) {
                    break;
                } else if (tokens.get(cur_pos).getTokenType() == TokenType.COMMA) {
                    stmt.addSons(tokens.get(cur_pos));
                    cur_pos++;  // accept ,
                    stmt.addSons(parseExp());
                }
            }
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RPARENT) {
                //return null;
                throw new SyntaxException("Stmt: not )");
            }
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept )
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.SEMICN) {
                //return null;
                throw new SyntaxException("Stmt: not ;");
            }
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept ;
        } else if (tokenType == TokenType.SEMICN) {
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept ;
        } else if (tokenType == TokenType.PLUS || tokenType == TokenType.MINU ||
                tokenType == TokenType.NOT ||
                tokenType == TokenType.LPARENT || tokenType == TokenType.INTCON) {
            stmt.addSons(parseExp());
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.SEMICN) {
                //return null;
                throw new SyntaxException("Stmt: not ;");
            }
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept ;
        } else {
            //return null; // Undefine
            throw new SyntaxException("Stmt: undefine");
        }
        return stmt;
    }

    private Token parseExp() throws SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("Exp: out size");
        }
        Token exp = new Token(0, TokenType.Exp, "");
        exp.addSons(parseAddExp());
        return exp;
    }

    private Token parseCond() throws SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("Cond: out size");
        }
        Token cond = new Token(0, TokenType.Cond, "");
        cond.addSons(parseLOrExp());
        return cond;
    }

    private Token parseLVal() throws SyntaxException {
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.IDENFR) {
            //return null;
            throw new SyntaxException("LVal: not ident");
        }
        Token lVal = new Token(0, TokenType.LVal, "");
        lVal.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept ident
        while (cur_pos < tokens.size()) {
            TokenType tokenType = tokens.get(cur_pos).getTokenType();
            if (tokenType == TokenType.ASSIGN || tokenType == TokenType.RPARENT ||
                tokenType == TokenType.OR || tokenType == TokenType.AND ||
                tokenType == TokenType.EQL || tokenType == TokenType.NEQ ||
                tokenType == TokenType.LSS || tokenType == TokenType.GRE ||
                tokenType == TokenType.LEQ || tokenType == TokenType.GEQ ||
                tokenType == TokenType.COMMA || tokenType == TokenType.SEMICN ||
                tokenType == TokenType.RBRACE || tokenType == TokenType.RBRACK ||
                tokenType == TokenType.PLUS || tokenType == TokenType.MINU ||
                tokenType == TokenType.MULT || tokenType == TokenType.DIV ||
                tokenType == TokenType.MOD) {
                break;
            } else if (tokenType == TokenType.LBRACK) {
                lVal.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept [
                lVal.addSons(parseExp());
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RBRACK) {
                    //return null;
                    throw new SyntaxException("LVal: not ]");
                }
                lVal.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept ]
            } else {
                //return null;
                throw new SyntaxException("LVal: undefine");
            }
        }
        return lVal;
    }

    private Token parsePrimaryExp() throws SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("PrimaryExp: out size");
        }
        Token primaryExp = new Token(0, TokenType.PrimaryExp, "");
        if (tokens.get(cur_pos).getTokenType() == TokenType.LPARENT) {
            primaryExp.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept (
            primaryExp.addSons(parseExp());
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RPARENT) {
                //return null;
                throw new SyntaxException("PrimaryExp: not )");
            }
            primaryExp.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept )
        } else if (tokens.get(cur_pos).getTokenType() == TokenType.IDENFR) {
            primaryExp.addSons(parseLVal());
        } else if (tokens.get(cur_pos).getTokenType() == TokenType.INTCON) {
            primaryExp.addSons(parseNumber());
        }
        return primaryExp;
    }

    private Token parseNumber() throws SyntaxException {
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.INTCON) {
            //return null;
            throw new SyntaxException("Number: not int");
        }
        Token number = new Token(0, TokenType.Number, "");
        number.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept IntConst
        return number;
    }

    private Token parseUnaryExp() throws SyntaxException {
        // PrimaryExp 和 Ident 分支的FIRST集合产生冲突，向后看第二个token，如果是(则为分支2，否则进入分支1(PrimaryExp)
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("UnaryExp: out size");
        }
        Token unaryExp = new Token(0, TokenType.UnaryExp, "");
        if (tokens.get(cur_pos).getTokenType() == TokenType.IDENFR) {
            if (cur_pos + 1 >= tokens.size()) {
                //return null;
                throw new SyntaxException("UnaryExp: out size");
            }
            if (tokens.get(cur_pos + 1).getTokenType() == TokenType.LPARENT) {  // branch 2
                unaryExp.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept Ident
                unaryExp.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept (
                if (cur_pos < tokens.size() && tokens.get(cur_pos).getTokenType() == TokenType.RPARENT) {
                    unaryExp.addSons(tokens.get(cur_pos));
                    cur_pos++;  // accept )
                } else if (cur_pos < tokens.size() && tokens.get(cur_pos).getTokenType() == TokenType.PLUS ||
                        tokens.get(cur_pos).getTokenType() == TokenType.MINU ||
                        tokens.get(cur_pos).getTokenType() == TokenType.NOT ||
                        tokens.get(cur_pos).getTokenType() == TokenType.IDENFR ||
                        tokens.get(cur_pos).getTokenType() == TokenType.LPARENT ||
                        tokens.get(cur_pos).getTokenType() == TokenType.INTCON) {
                    unaryExp.addSons(parseFuncRParams());
                    if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != TokenType.RPARENT) {
                        //return null;
                        throw new SyntaxException("UnaryExp: not )");
                    }
                    unaryExp.addSons(tokens.get(cur_pos));
                    cur_pos++;  // accept )

                } else {
                    //return null;
                    throw new SyntaxException("UnaryExp: undefine maybe missing )");
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
            if (cur_pos >= tokens.size()) {
                //return null;
                throw new SyntaxException("UnaryExp: out size");
            }
            unaryExp.addSons(parseUnaryExp());
        } else {
            //return null;
            throw new SyntaxException("UnaryExp: undefine");
        }
        return unaryExp;
    }

    private Token parseUnaryOp() throws SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("UnaryOp: out size");
        }
        Token unaryOp = new Token(0, TokenType.UnaryOp, "");
        if (tokens.get(cur_pos).getTokenType() == TokenType.PLUS ||
            tokens.get(cur_pos).getTokenType() == TokenType.MINU ||
            tokens.get(cur_pos).getTokenType() == TokenType.NOT) {
            unaryOp.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept + or - or !
        } else {
            //return null;
            throw new SyntaxException("UnaryOp: not in First or Follow");
        }
        return unaryOp;
    }

    private Token parseFuncRParams() throws SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("FuncRParams: out size");
        }
        Token funcRParams = new Token(0, TokenType.FuncRParams, "");
        funcRParams.addSons(parseExp());
        while (cur_pos < tokens.size()) {
            if (tokens.get(cur_pos).getTokenType() == TokenType.RPARENT) {
                break;
            } else if (tokens.get(cur_pos).getTokenType() == TokenType.COMMA) {
                funcRParams.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept ,
                funcRParams.addSons(parseExp());
            } else {
                //return null;
                throw new SyntaxException("FuncRParams: not in First or Follow");
            }
        }
        return funcRParams;
    }

    private Token parseMulExp() throws SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("MulExp: out size");
        }
        Token mulExp = new Token(0, TokenType.MulExp, "");
        mulExp.addSons(parseUnaryExp());
        while (cur_pos < tokens.size()) {
            TokenType tokenType = tokens.get(cur_pos).getTokenType();
            if (tokenType == TokenType.RPARENT || tokenType == TokenType.OR || tokenType == TokenType.AND ||
                    tokenType == TokenType.EQL || tokenType == TokenType.NEQ ||
                    tokenType == TokenType.LSS || tokenType == TokenType.GRE ||
                    tokenType == TokenType.LEQ || tokenType == TokenType.GEQ ||
                    tokenType == TokenType.COMMA || tokenType == TokenType.SEMICN ||
                    tokenType == TokenType.RBRACE || tokenType == TokenType.RBRACK ||
                    tokenType == TokenType.PLUS || tokenType == TokenType.MINU) {
                break;
            } else if (tokenType == TokenType.MULT || tokenType == TokenType.DIV || tokenType == TokenType.MOD) {
                mulExp.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept * / %
                mulExp.addSons(parseUnaryExp());
            } else {
                //return null;
                throw new SyntaxException("MulExp: not in First or Follow");
            }
        }
        return mulExp;
    }

    private Token parseAddExp() throws SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("AddExp: out size");
        }
        Token addExp = new Token(0, TokenType.AddExp, "");
        addExp.addSons(parseMulExp());
        while (cur_pos < tokens.size()) {
            TokenType tokenType = tokens.get(cur_pos).getTokenType();
            if (tokenType == TokenType.RPARENT || tokenType == TokenType.OR || tokenType == TokenType.AND ||
                    tokenType == TokenType.EQL || tokenType == TokenType.NEQ ||
                    tokenType == TokenType.LSS || tokenType == TokenType.GRE ||
                    tokenType == TokenType.LEQ || tokenType == TokenType.GEQ ||
                    tokenType == TokenType.COMMA || tokenType == TokenType.SEMICN ||
                    tokenType == TokenType.RBRACE || tokenType == TokenType.RBRACK) {
                break;
            } else if (tokenType == TokenType.PLUS || tokenType == TokenType.MINU) {
                addExp.addSons(tokens.get(cur_pos));
                cur_pos++;   // accept + or -
                addExp.addSons(parseMulExp());
            } else {
                //return null;
                throw new SyntaxException("AddExp: not in First or Follow");
            }
        }
        return addExp;
    }

    private Token parseRelExp() throws SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("RelExp: out size");
        }
        Token relExp = new Token(0, TokenType.RelExp, "");
        relExp.addSons(parseAddExp());
        while (cur_pos < tokens.size()) {
            TokenType tokenType = tokens.get(cur_pos).getTokenType();
            if (tokenType == TokenType.RPARENT || tokenType == TokenType.OR ||
                tokenType == TokenType.AND || tokenType == TokenType.EQL ||
                tokenType == TokenType.NEQ) {
                break;
            } else if (tokenType == TokenType.LSS || tokenType == TokenType.GRE ||
                tokenType == TokenType.LEQ || tokenType == TokenType.GEQ) {
                relExp.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept < > <= >=
                relExp.addSons(parseAddExp());
            } else {
                //return null;
                throw new SyntaxException("RelExp: not in First or Follow");
            }
        }
        return relExp;
    }

    private Token parseEqExp() throws SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("EqExp: out size");
        }
        Token eqExp = new Token(0, TokenType.EqExp, "");
        eqExp.addSons(parseRelExp());
        while (cur_pos < tokens.size()) {
            TokenType tokenType = tokens.get(cur_pos).getTokenType();
            if (tokenType == TokenType.RPARENT || tokenType == TokenType.OR ||
                tokenType == TokenType.AND) {
                break;
            } else if (tokenType == TokenType.EQL || tokenType == TokenType.NEQ) {
                eqExp.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept == !=
                eqExp.addSons(parseRelExp());
            } else {
                //return null;
                throw new SyntaxException("EqExp: not in First or Follow");
            }
        }
        return eqExp;
    }

    private Token parseLAndExp() throws SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("LAndExp: out size");
        }
        Token lAndExp = new Token(0, TokenType.LAndExp, "");
        lAndExp.addSons(parseEqExp());
        while (cur_pos < tokens.size()) {
            TokenType tokenType = tokens.get(cur_pos).getTokenType();
            if (tokenType == TokenType.RPARENT || tokenType == TokenType.OR) {
                break;
            } else if (tokenType == TokenType.AND) {
                lAndExp.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept &&
                lAndExp.addSons(parseEqExp());
            } else {
                //return null;
                throw new SyntaxException("LAndExp: not in First or Follow");
            }
        }
        return lAndExp;
    }

    private Token parseLOrExp() throws SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("LOrExp: out size");
        }
        Token lOrExp = new Token(0, TokenType.LOrExp, "");
        lOrExp.addSons(parseLAndExp());
        while (cur_pos < tokens.size()) {
            TokenType tokenType = tokens.get(cur_pos).getTokenType();
            if (tokenType == TokenType.RPARENT) {
                break;
            } else if (tokenType == TokenType.OR) {
                lOrExp.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept ||
                lOrExp.addSons(parseLAndExp());
            } else {
                //return null;
                throw new SyntaxException("LOrExp: not in First or Follow");
            }
        }
        return lOrExp;
    }

    private Token parseConstExp() throws SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new SyntaxException("ConstExp: out size");
        }
        Token constExp = new Token(0, TokenType.ConstExp, "");
        constExp.addSons(parseAddExp());
        return constExp;
    }
}

     */
}
