package NonTerminals;

public class SAbk {
    /*
    import com.sun.media.sound.DLSSample;
import org.w3c.dom.ls.LSSerializer;

import java.util.ArrayList;
import java.util.logging.LoggingPermission;

// 语法分析

public class Syntax.SyntaxAnalyser {
    private ArrayList<TokenDefines.Token> tokens;
    private TokenDefines.Token root;
    private int cur_pos;
    private static boolean isParsed = false;

    public Syntax.SyntaxAnalyser(ArrayList<TokenDefines.Token> tokens) {
        this.tokens = tokens;
    }

    public void parseSyntax() {
        isParsed = true;
        cur_pos = 0;
        this.root = new TokenDefines.Token(0, Defines.TokenType.CompUnit, "");
        try {
            // Decl
            while (cur_pos < tokens.size()) {
                if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.CONSTTK) {
                    TokenDefines.Token decl = parseDecl();
                    root.addSons(decl);
                } else if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.INTTK) {
                    if (cur_pos + 1 < tokens.size() && tokens.get(cur_pos + 1).getTokenType() == Defines.TokenType.MAINTK) {
                        break;
                    } else if (cur_pos + 2 < tokens.size() && tokens.get(cur_pos + 1).getTokenType() == Defines.TokenType.IDENFR
                            && tokens.get(cur_pos + 2).getTokenType() == Defines.TokenType.LPARENT) {
                        break;
                    } else if (cur_pos + 1 < tokens.size() && tokens.get(cur_pos + 1).getTokenType() == Defines.TokenType.IDENFR) {
                        TokenDefines.Token decl = parseDecl();
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
                if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.VOIDTK) {
                    TokenDefines.Token funcDef = parseFuncDef();
                    root.addSons(funcDef);
                } else if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.INTTK) {
                    if (tokens.get(cur_pos + 1).getTokenType() == Defines.TokenType.MAINTK) {
                        break;
                    } else if (cur_pos + 2 < tokens.size() && tokens.get(cur_pos + 1).getTokenType() == Defines.TokenType.IDENFR &&
                            tokens.get(cur_pos + 2).getTokenType() == Defines.TokenType.LPARENT) {
                        TokenDefines.Token funcDef = parseFuncDef();
                        root.addSons(funcDef);
                    } else {    // Undefined
                        break;
                    }
                } else {    // Undefined
                    break;
                }
            }
            // MainFuncDef
            if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.INTTK &&
                    tokens.get(cur_pos + 1).getTokenType() == Defines.TokenType.MAINTK) {
                TokenDefines.Token mainFuncDef = parseMainFuncDef();
                root.addSons(mainFuncDef);
            }
        } catch (Syntax.SyntaxException e) {
            e.printStackTrace();
        }
    }

    public TokenDefines.Token getRoot() {
        if (!isParsed) parseSyntax();
        return root;
    }

    private TokenDefines.Token parseDecl() throws Syntax.SyntaxException {
        TokenDefines.Token decl = new TokenDefines.Token(0, Defines.TokenType.Decl, "");
        if (cur_pos < tokens.size()) {
            if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.CONSTTK) {
                TokenDefines.Token constDecl = parseConstDecl();
                decl.addSons(constDecl);
            } else if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.INTTK) {
                TokenDefines.Token varDecl = parseVarDecl();
                decl.addSons(varDecl);
            }
        } else {    // Undefined
            throw new Syntax.SyntaxException("parseDecl: position out of maxLength");
            //return null;
        }
        return decl;
    }

    private TokenDefines.Token parseConstDecl() throws Syntax.SyntaxException {
        TokenDefines.Token constDecl = new TokenDefines.Token(0, Defines.TokenType.ConstDecl, "");
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.CONSTTK) {
            //return null;
            throw new Syntax.SyntaxException("ConstDecl: not const");
        }
        constDecl.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept const
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.INTTK) {
            //return null;
            throw new Syntax.SyntaxException("ConstDecl: not int");
        }
        TokenDefines.Token bType = parseBType();
        constDecl.addSons(bType);
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.IDENFR) {
            //return null;
            throw new Syntax.SyntaxException("ConstDecl: not ident");
        }
        TokenDefines.Token constDef = parseConstDef();
        constDecl.addSons(constDef);
        while (cur_pos < tokens.size()) {
            if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.SEMICN) {
                break;
            } else if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.COMMA) {
                constDecl.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept ,
                TokenDefines.Token constDef_loop = parseConstDef();
                constDecl.addSons(constDef_loop);
            }
        }
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.SEMICN) {
//            return null;
            throw new Syntax.SyntaxException("ConstDecl: not ;");
        }
        constDecl.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept ;
        return constDecl;
    }

    private TokenDefines.Token parseBType() throws Syntax.SyntaxException {
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.INTTK) {
            //return null;
            throw new Syntax.SyntaxException("BType: not int");
        }
        TokenDefines.Token bType = tokens.get(cur_pos);
        cur_pos++;
        return bType;
    }

    private TokenDefines.Token parseConstDef() throws Syntax.SyntaxException {
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.IDENFR) {
            //return null;
            throw new Syntax.SyntaxException("ConstDef: not ident");
        }
        TokenDefines.Token constDef = new TokenDefines.Token(0, Defines.TokenType.ConstDef, "");
        constDef.addSons(tokens.get(cur_pos));
        cur_pos++; // accept Ident
        while (cur_pos < tokens.size()) {
            if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.ASSIGN) {
                break;
            } else if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.LBRACK) {
                constDef.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept [
                TokenDefines.Token constExp = parseConstExp();
                constDef.addSons(constExp);
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.RBRACK) {
                    //return null;
                    throw new Syntax.SyntaxException("ConstDef: not ]");
                }
                constDef.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept ]
            } else {    // Undefine
                //return null;
                throw new Syntax.SyntaxException("ConstDef: undefine");
            }
        }
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.ASSIGN) {
            //return null;
            throw new Syntax.SyntaxException("ConstDef: not assign");
        }
        constDef.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept =
        TokenDefines.Token constInitVal = parseConstInitVal();
        constDef.addSons(constInitVal);
        return constDef;
    }

    private TokenDefines.Token parseConstInitVal() throws Syntax.SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("ConstInitVal: out size");
        }
        TokenDefines.Token constInitVal = new TokenDefines.Token(0, Defines.TokenType.ConstInitVal, "");
        Defines.TokenType tokenType = tokens.get(cur_pos).getTokenType();
        if (tokenType == Defines.TokenType.PLUS || tokenType == Defines.TokenType.MINU ||
            tokenType == Defines.TokenType.NOT || tokenType == Defines.TokenType.IDENFR ||
            tokenType == Defines.TokenType.LPARENT || tokenType == Defines.TokenType.INTCON) {
            TokenDefines.Token constExp = parseConstExp();
            constInitVal.addSons(constExp);
        } else if (tokenType == Defines.TokenType.LBRACE) {
            constInitVal.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept {
            if (cur_pos >= tokens.size()) {
                //return null;
                throw new Syntax.SyntaxException("ConstInitVal: out size");
            }
            if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.RBRACE) {
                constInitVal.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept }
            } else {    // [constInitVal {',' ConstInitVal}]
                TokenDefines.Token midConstInitVal = parseConstInitVal();
                constInitVal.addSons(midConstInitVal);
                while (cur_pos < tokens.size()) {
                    if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.RBRACE) {
                        break;
                    } else if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.COMMA) {
                        constInitVal.addSons(tokens.get(cur_pos));
                        cur_pos++;  // accept ,
                        TokenDefines.Token loopConstInitVal = parseConstInitVal();
                        constInitVal.addSons(loopConstInitVal);
                    }
                }
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.RBRACE) {
                    //return null;
                    throw new Syntax.SyntaxException("ConstInitVal: not }");
                }
                constInitVal.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept }
            }
        } else {    // Undefine
            //return null;
            throw new Syntax.SyntaxException("ConstInitVal: undefine");
        }
        return constInitVal;
    }

    private TokenDefines.Token parseVarDecl() throws Syntax.SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("VarDecl: out size");
        }
        TokenDefines.Token varDecl = new TokenDefines.Token(0, Defines.TokenType.VarDecl, "");
        TokenDefines.Token bType = parseBType();
        varDecl.addSons(bType);
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("VarDecl: out size");
        }
        varDecl.addSons(parseVarDef());
        while (cur_pos < tokens.size()) {
            if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.SEMICN) {
                break;
            } else if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.COMMA) {
                varDecl.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept ,
                varDecl.addSons(parseVarDef());
            }
        }
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.SEMICN) {
            //return null;
            throw new Syntax.SyntaxException("VarDecl: not ;");
        }
        varDecl.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept ;
        return varDecl;
    }

    private TokenDefines.Token parseVarDef() throws Syntax.SyntaxException {
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.IDENFR) {
            //return null;
            throw new Syntax.SyntaxException("VarDef: not ident");
        }
        TokenDefines.Token varDef = new TokenDefines.Token(0, Defines.TokenType.VarDef, "");
        varDef.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept Ident
        while (cur_pos < tokens.size()) {
            if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.ASSIGN ||
                tokens.get(cur_pos).getTokenType() == Defines.TokenType.COMMA ||
                tokens.get(cur_pos).getTokenType() == Defines.TokenType.SEMICN) {
                break;
            } else if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.LBRACK) {
                varDef.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept [
                varDef.addSons(parseConstExp());
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.RBRACK) {
                    //return null;
                    throw new Syntax.SyntaxException("VarDef: not ]");
                }
                varDef.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept ]
            } else {
                //return null;
                throw new Syntax.SyntaxException("VarDef: not in first && follow");
            }
        }
        if (cur_pos < tokens.size() && tokens.get(cur_pos).getTokenType() == Defines.TokenType.ASSIGN) {
            varDef.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept =
            varDef.addSons(parseInitVal());
        }
        return varDef;
    }

    private TokenDefines.Token parseInitVal() throws Syntax.SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("InitVal: out size");
        }
        TokenDefines.Token initVal = new TokenDefines.Token(0, Defines.TokenType.InitVal, "");
        Defines.TokenType tokenType = tokens.get(cur_pos).getTokenType();
        if (tokenType == Defines.TokenType.PLUS || tokenType == Defines.TokenType.MINU ||
            tokenType == Defines.TokenType.NOT || tokenType == Defines.TokenType.LPARENT ||
            tokenType == Defines.TokenType.IDENFR || tokenType == Defines.TokenType.INTCON) {
            initVal.addSons(parseExp());
        } else if (tokenType == Defines.TokenType.LBRACE) {
            initVal.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept {
            if (cur_pos >= tokens.size()) {
                //return null;
                throw new Syntax.SyntaxException("InitVal: out size");
            }
            if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.RBRACE) {
                initVal.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept }
            } else {
                initVal.addSons(parseInitVal());
                while (cur_pos < tokens.size()) {
                    if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.RBRACE) {
                        break;
                    } else if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.COMMA) {
                        initVal.addSons(tokens.get(cur_pos));
                        cur_pos++;  // accept ,
                        initVal.addSons(parseInitVal());
                    } else {
                        //return null;
                        throw new Syntax.SyntaxException("InitVal: not in First Follow");
                    }
                }
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.RBRACE) {
                    //return null;
                    throw new Syntax.SyntaxException("InitVal: not }");
                }
                initVal.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept }
            }
        } else {
            //return null;
            throw new Syntax.SyntaxException("InitVal: Undefine");
        }
        return initVal;
    }

    private TokenDefines.Token parseFuncDef() throws Syntax.SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("FuncDef: out size");
        }
        TokenDefines.Token funcDef = new TokenDefines.Token(0, Defines.TokenType.FuncDef, "");
        funcDef.addSons(parseFuncType());
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.IDENFR) {
            //return null;
            throw new Syntax.SyntaxException("FuncDef: not ident");
        }
        funcDef.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept Ident
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.LPARENT) {
            //return null;
            throw new Syntax.SyntaxException("FuncDef: not (");
        }
        funcDef.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept (
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("FuncDef: out size");
        }
        if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.RPARENT) {
            funcDef.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept )
        } else if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.INTTK) {
            funcDef.addSons(parseFuncFParams());
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.RPARENT) {
                //return null;
                throw new Syntax.SyntaxException("FuncDef: not )");
            }
            funcDef.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept )
        } else {
            //return null;
            throw new Syntax.SyntaxException("FuncDef: undefine, maybe messing )");
        }

        funcDef.addSons(parseBlock());
        return funcDef;
    }

    private TokenDefines.Token parseMainFuncDef() throws Syntax.SyntaxException {
        if (cur_pos + 3 >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("MainFuncDef: out size");
        }
        TokenDefines.Token mainFuncDef = new TokenDefines.Token(0, Defines.TokenType.MainFuncDef, "");
        if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.INTTK) {
            mainFuncDef.addSons(tokens.get(cur_pos));
            cur_pos++;
        } else {
            //return null;
            throw new Syntax.SyntaxException("MainFuncDef: missing int");
        }
        if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.MAINTK) {
            mainFuncDef.addSons(tokens.get(cur_pos));
            cur_pos++;
        } else {
            //return null;
            throw new Syntax.SyntaxException("MainFuncDef: missing main");
        }
        if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.LPARENT) {
            mainFuncDef.addSons(tokens.get(cur_pos));
            cur_pos++;
        } else {
            //return null;
            throw new Syntax.SyntaxException("MainFuncDef: missing (");
        }
        if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.RPARENT) {
            mainFuncDef.addSons(tokens.get(cur_pos));
            cur_pos++;
        } else {
            //return null;
            throw new Syntax.SyntaxException("MainFuncDef: missing )");
        }
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("MainFuncDef: out size");
        }
        mainFuncDef.addSons(parseBlock());
        return mainFuncDef;
    }

    private TokenDefines.Token parseFuncType() throws Syntax.SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("FuncType: out size");
        }
        TokenDefines.Token funcType = new TokenDefines.Token(0, Defines.TokenType.FuncType, "");
        if (cur_pos >= tokens.size() ||
                (tokens.get(cur_pos).getTokenType() != Defines.TokenType.VOIDTK &&
                        tokens.get(cur_pos).getTokenType() != Defines.TokenType.INTTK)) {
            //return null;
            throw new Syntax.SyntaxException("FuncType: not void or int");
        }
        funcType.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept type (void or int)
        return funcType;
    }

    private TokenDefines.Token parseFuncFParams() throws Syntax.SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("FuncFParams: out size");
        }
        TokenDefines.Token funcFParams = new TokenDefines.Token(0, Defines.TokenType.FuncFParams, "");
        if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.INTTK) {
            funcFParams.addSons(parseFuncFParam());
            while (cur_pos < tokens.size()) {
                if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.RPARENT) {
                    break;
                } else if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.COMMA) {
                    funcFParams.addSons(tokens.get(cur_pos));
                    cur_pos++;
                    funcFParams.addSons(parseFuncFParam());
                } else {
                    //return null;
                    throw new Syntax.SyntaxException("FuncFParams: not in First or Follow");
                }
            }
        } else {
            //return null;
            throw new Syntax.SyntaxException("FuncFParams: undefine");
        }
        return funcFParams;
    }

    private TokenDefines.Token parseFuncFParam() throws Syntax.SyntaxException {
        if (cur_pos + 1 >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("FuncFParam: out size");
        }
        TokenDefines.Token funcFParam = new TokenDefines.Token(0, Defines.TokenType.FuncFParam, "");
        funcFParam.addSons(parseBType());
        if (tokens.get(cur_pos).getTokenType() != Defines.TokenType.IDENFR) {
            //return null;
            throw new Syntax.SyntaxException("FuncFParam: not int");
        }
        funcFParam.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept Ident
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("FuncFParam: out size");
        }
        if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.RPARENT ||
                tokens.get(cur_pos).getTokenType() == Defines.TokenType.COMMA) {
            return funcFParam;
        } else if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.LBRACK) {
            //if (cur_pos + 1 >= tokens.size()) return null;
            funcFParam.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept [
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.RBRACK) {
                //return null;
                throw new Syntax.SyntaxException("FuncFParam: not ]");
            }
            funcFParam.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept ]
            while (cur_pos < tokens.size()) {
                if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.RPARENT ||
                        tokens.get(cur_pos).getTokenType() == Defines.TokenType.COMMA) {
                    break;
                } else if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.LBRACK) {
                    funcFParam.addSons(tokens.get(cur_pos));
                    cur_pos++;  // accept [
                    funcFParam.addSons(parseConstExp());
                    if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.RBRACK) {
                        //return null;
                        throw new Syntax.SyntaxException("FuncFParam: not ]");
                    }
                    funcFParam.addSons(tokens.get(cur_pos));
                    cur_pos++;  // accept ]
                } else {
                    //return null; // Undefine
                    throw new Syntax.SyntaxException("FuncFParam: undefine, maybe missing ]");
                }
            }
        }
        return funcFParam;
    }

    private TokenDefines.Token parseBlock() throws Syntax.SyntaxException {
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.LBRACE) {
            //return null;
            throw new Syntax.SyntaxException("Block: not {");
        }
        TokenDefines.Token block = new TokenDefines.Token(0, Defines.TokenType.Block, "");
        block.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("Block: out size");
        }
        while (cur_pos < tokens.size()) {
            Defines.TokenType tokenType = tokens.get(cur_pos).getTokenType();
            if (tokenType == Defines.TokenType.RBRACE) {
                break;
            } else if (tokenType == Defines.TokenType.IDENFR || tokenType == Defines.TokenType.PLUS ||
                    tokenType == Defines.TokenType.MINU || tokenType == Defines.TokenType.NOT ||
                    tokenType == Defines.TokenType.LPARENT || tokenType == Defines.TokenType.INTCON ||
                    tokenType == Defines.TokenType.SEMICN || tokenType == Defines.TokenType.LBRACE ||
                    tokenType == Defines.TokenType.IFTK || tokenType == Defines.TokenType.WHILETK ||
                    tokenType == Defines.TokenType.BREAKTK || tokenType == Defines.TokenType.RETURNTK ||
                    tokenType == Defines.TokenType.PRINTFTK || tokenType == Defines.TokenType.INTTK ||
                    tokenType == Defines.TokenType.CONSTTK || tokenType == Defines.TokenType.CONTINUETK) {
                block.addSons(parseBlockItem());
            } else {
                //return null;
                throw new Syntax.SyntaxException("Block: not in First or Follow");
            }
        }
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.RBRACE) {
            //return null;
            throw new Syntax.SyntaxException("Block: not }");
        }
        block.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept }
        return block;
    }

    private TokenDefines.Token parseBlockItem() throws Syntax.SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("BlockItem: out size");
        }
        Defines.TokenType tokenType = tokens.get(cur_pos).getTokenType();
        TokenDefines.Token blockItem = new TokenDefines.Token(0, Defines.TokenType.BlockItem, "");
        if (tokenType == Defines.TokenType.INTTK || tokenType == Defines.TokenType.CONSTTK) {
            blockItem.addSons(parseDecl());
        } else if (tokenType == Defines.TokenType.IDENFR || tokenType == Defines.TokenType.PLUS ||
                tokenType == Defines.TokenType.MINU || tokenType == Defines.TokenType.NOT ||
                tokenType == Defines.TokenType.LPARENT || tokenType == Defines.TokenType.INTCON ||
                tokenType == Defines.TokenType.SEMICN || tokenType == Defines.TokenType.LBRACE ||
                tokenType == Defines.TokenType.IFTK || tokenType == Defines.TokenType.WHILETK ||
                tokenType == Defines.TokenType.BREAKTK || tokenType == Defines.TokenType.RETURNTK ||
                tokenType == Defines.TokenType.PRINTFTK || tokenType == Defines.TokenType.CONTINUETK) {
            blockItem.addSons(parseStmt());
        } else {
            //return null;
            throw new Syntax.SyntaxException("BlockItem: not in First or Follow");
        }
        return blockItem;
    }

    private TokenDefines.Token parseStmt() throws Syntax.SyntaxException {
        //System.out.println(debug + " " + cur_pos);
        //debug++;
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("Stmt: out size");
        }
        TokenDefines.Token stmt = new TokenDefines.Token(0, Defines.TokenType.Stmt, "");
        Defines.TokenType tokenType = tokens.get(cur_pos).getTokenType();
        if (tokenType == Defines.TokenType.IDENFR) {
            int flag = -1;
            for (int i = cur_pos; i < tokens.size(); i++) {
                if (tokens.get(i).getTokenType() == Defines.TokenType.SEMICN) {
                    flag = 2;
                    break;
                } else if (tokens.get(i).getTokenType() == Defines.TokenType.ASSIGN) {
                    flag = 0;
                    break;
                }
            }
            if (flag == 0) {
                stmt.addSons(parseLVal());
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.ASSIGN) {
                    //return null;
                    throw new Syntax.SyntaxException("Stmt: not =");
                }
                stmt.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept =
                if (cur_pos >= tokens.size()) {
                    //return null;
                    throw new Syntax.SyntaxException("Stmt: out size");
                }
                Defines.TokenType tokenTypeExp = tokens.get(cur_pos).getTokenType();
                if (tokenTypeExp == Defines.TokenType.GETINTTK) {
                    stmt.addSons(tokens.get(cur_pos));
                    cur_pos++;  // accept getint
                    if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.LPARENT) {
                        //return null;
                        throw new Syntax.SyntaxException("Stmt: not (");
                    }
                    stmt.addSons(tokens.get(cur_pos));
                    cur_pos++;  // accept (
                    if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.RPARENT) {
                        //return null;
                        throw new Syntax.SyntaxException("Stmt: not )");
                    }
                    stmt.addSons(tokens.get(cur_pos));
                    cur_pos++;  // accept )
                } else if (tokenTypeExp == Defines.TokenType.PLUS || tokenTypeExp == Defines.TokenType.MINU ||
                        tokenTypeExp == Defines.TokenType.NOT || tokenTypeExp == Defines.TokenType.IDENFR ||
                        tokenTypeExp == Defines.TokenType.LPARENT || tokenTypeExp == Defines.TokenType.INTCON) {
                    TokenDefines.Token exp = parseExp(); // Exp branch
                    stmt.addSons(exp);
                }
            } else if (flag == 2) {
                stmt.addSons(parseExp());
            } else {
                //return null;
                throw new Syntax.SyntaxException("Stmt: undefine");
            }

            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.SEMICN) {
                //return null;
                throw new Syntax.SyntaxException("Stmt: not ;");
            }
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept ;
        } else if (tokenType == Defines.TokenType.LBRACE) {
            stmt.addSons(parseBlock());
        } else if (tokenType == Defines.TokenType.IFTK) {
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept if
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.LPARENT) {
                //return null;
                throw new Syntax.SyntaxException("Stmt: not (");
            }
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept (
            try {
                TokenDefines.Token cond = parseCond();
            } catch (Syntax.SyntaxException e) {

            }
            TokenDefines.Token cond = parseCond();
            stmt.addSons(cond);
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.RPARENT) {
                //return null;
                throw new Syntax.SyntaxException("Stmt: not )");
            }
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept )
            stmt.addSons(parseStmt());
            if (cur_pos < tokens.size() && tokens.get(cur_pos).getTokenType() == Defines.TokenType.ELSETK) {
                stmt.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept else
                stmt.addSons(parseStmt());
            }
        } else if (tokenType == Defines.TokenType.WHILETK) {
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept while
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.LPARENT) {
                //return null;
                throw new Syntax.SyntaxException("Stmt: not (");
            }
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept (
            stmt.addSons(parseCond());
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.RPARENT) {
                //return null;
                throw new Syntax.SyntaxException("Stmt: not )");
            }
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept )
            stmt.addSons(parseStmt());
        } else if (tokenType == Defines.TokenType.BREAKTK) {
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept break
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.SEMICN) {
                //return null;
                throw new Syntax.SyntaxException("Stmt: not ;");
            }
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept ;
        } else if (tokenType == Defines.TokenType.CONTINUETK) {
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept continue
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.SEMICN) {
                //return null;
                throw new Syntax.SyntaxException("Stmt: not ;");
            }
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept ;
        } else if (tokenType == Defines.TokenType.RETURNTK) {
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept return
            if (cur_pos >= tokens.size()) {
                //return null;
                throw new Syntax.SyntaxException("Stmt: out size");
            }
            Defines.TokenType tokenTypeExp = tokens.get(cur_pos).getTokenType();
            if (tokenTypeExp == Defines.TokenType.SEMICN) {
                stmt.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept return
            } else if (tokenTypeExp == Defines.TokenType.PLUS || tokenTypeExp == Defines.TokenType.MINU ||
                       tokenTypeExp == Defines.TokenType.NOT || tokenTypeExp == Defines.TokenType.IDENFR ||
                       tokenTypeExp == Defines.TokenType.LPARENT || tokenTypeExp == Defines.TokenType.INTCON) {
                stmt.addSons(parseExp());
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.SEMICN) {
                    //return null;
                    throw new Syntax.SyntaxException("Stmt: not ;");
                }
                stmt.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept ;
            } else {
                //return null;
                throw new Syntax.SyntaxException("Stmt: undefine maybe missing ;");
            }
        } else if (tokenType == Defines.TokenType.PRINTFTK) {
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept printf
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.LPARENT) {
                //return null;
                throw new Syntax.SyntaxException("Stmt: not (");
            }
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept (
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.STRCON) {
                //return null;
                throw new Syntax.SyntaxException("Stmt: not string");
            }
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept FormatString
            while (cur_pos < tokens.size()) {
                if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.RPARENT) {
                    break;
                } else if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.COMMA) {
                    stmt.addSons(tokens.get(cur_pos));
                    cur_pos++;  // accept ,
                    stmt.addSons(parseExp());
                }
            }
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.RPARENT) {
                //return null;
                throw new Syntax.SyntaxException("Stmt: not )");
            }
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept )
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.SEMICN) {
                //return null;
                throw new Syntax.SyntaxException("Stmt: not ;");
            }
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept ;
        } else if (tokenType == Defines.TokenType.SEMICN) {
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept ;
        } else if (tokenType == Defines.TokenType.PLUS || tokenType == Defines.TokenType.MINU ||
                tokenType == Defines.TokenType.NOT ||
                tokenType == Defines.TokenType.LPARENT || tokenType == Defines.TokenType.INTCON) {
            stmt.addSons(parseExp());
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.SEMICN) {
                //return null;
                throw new Syntax.SyntaxException("Stmt: not ;");
            }
            stmt.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept ;
        } else {
            //return null; // Undefine
            throw new Syntax.SyntaxException("Stmt: undefine");
        }
        return stmt;
    }

    private TokenDefines.Token parseExp() throws Syntax.SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("Exp: out size");
        }
        TokenDefines.Token exp = new TokenDefines.Token(0, Defines.TokenType.Exp, "");
        exp.addSons(parseAddExp());
        return exp;
    }

    private TokenDefines.Token parseCond() throws Syntax.SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("Cond: out size");
        }
        TokenDefines.Token cond = new TokenDefines.Token(0, Defines.TokenType.Cond, "");
        cond.addSons(parseLOrExp());
        return cond;
    }

    private TokenDefines.Token parseLVal() throws Syntax.SyntaxException {
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.IDENFR) {
            //return null;
            throw new Syntax.SyntaxException("LVal: not ident");
        }
        TokenDefines.Token lVal = new TokenDefines.Token(0, Defines.TokenType.LVal, "");
        lVal.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept ident
        while (cur_pos < tokens.size()) {
            Defines.TokenType tokenType = tokens.get(cur_pos).getTokenType();
            if (tokenType == Defines.TokenType.ASSIGN || tokenType == Defines.TokenType.RPARENT ||
                tokenType == Defines.TokenType.OR || tokenType == Defines.TokenType.AND ||
                tokenType == Defines.TokenType.EQL || tokenType == Defines.TokenType.NEQ ||
                tokenType == Defines.TokenType.LSS || tokenType == Defines.TokenType.GRE ||
                tokenType == Defines.TokenType.LEQ || tokenType == Defines.TokenType.GEQ ||
                tokenType == Defines.TokenType.COMMA || tokenType == Defines.TokenType.SEMICN ||
                tokenType == Defines.TokenType.RBRACE || tokenType == Defines.TokenType.RBRACK ||
                tokenType == Defines.TokenType.PLUS || tokenType == Defines.TokenType.MINU ||
                tokenType == Defines.TokenType.MULT || tokenType == Defines.TokenType.DIV ||
                tokenType == Defines.TokenType.MOD) {
                break;
            } else if (tokenType == Defines.TokenType.LBRACK) {
                lVal.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept [
                lVal.addSons(parseExp());
                if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.RBRACK) {
                    //return null;
                    throw new Syntax.SyntaxException("LVal: not ]");
                }
                lVal.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept ]
            } else {
                //return null;
                throw new Syntax.SyntaxException("LVal: undefine");
            }
        }
        return lVal;
    }

    private TokenDefines.Token parsePrimaryExp() throws Syntax.SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("PrimaryExp: out size");
        }
        TokenDefines.Token primaryExp = new TokenDefines.Token(0, Defines.TokenType.PrimaryExp, "");
        if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.LPARENT) {
            primaryExp.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept (
            primaryExp.addSons(parseExp());
            if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.RPARENT) {
                //return null;
                throw new Syntax.SyntaxException("PrimaryExp: not )");
            }
            primaryExp.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept )
        } else if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.IDENFR) {
            primaryExp.addSons(parseLVal());
        } else if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.INTCON) {
            primaryExp.addSons(parseNumber());
        }
        return primaryExp;
    }

    private TokenDefines.Token parseNumber() throws Syntax.SyntaxException {
        if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.INTCON) {
            //return null;
            throw new Syntax.SyntaxException("Number: not int");
        }
        TokenDefines.Token number = new TokenDefines.Token(0, Defines.TokenType.Number, "");
        number.addSons(tokens.get(cur_pos));
        cur_pos++;  // accept IntConst
        return number;
    }

    private TokenDefines.Token parseUnaryExp() throws Syntax.SyntaxException {
        // PrimaryExp 和 Ident 分支的FIRST集合产生冲突，向后看第二个token，如果是(则为分支2，否则进入分支1(PrimaryExp)
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("UnaryExp: out size");
        }
        TokenDefines.Token unaryExp = new TokenDefines.Token(0, Defines.TokenType.UnaryExp, "");
        if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.IDENFR) {
            if (cur_pos + 1 >= tokens.size()) {
                //return null;
                throw new Syntax.SyntaxException("UnaryExp: out size");
            }
            if (tokens.get(cur_pos + 1).getTokenType() == Defines.TokenType.LPARENT) {  // branch 2
                unaryExp.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept Ident
                unaryExp.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept (
                if (cur_pos < tokens.size() && tokens.get(cur_pos).getTokenType() == Defines.TokenType.RPARENT) {
                    unaryExp.addSons(tokens.get(cur_pos));
                    cur_pos++;  // accept )
                } else if (cur_pos < tokens.size() && tokens.get(cur_pos).getTokenType() == Defines.TokenType.PLUS ||
                        tokens.get(cur_pos).getTokenType() == Defines.TokenType.MINU ||
                        tokens.get(cur_pos).getTokenType() == Defines.TokenType.NOT ||
                        tokens.get(cur_pos).getTokenType() == Defines.TokenType.IDENFR ||
                        tokens.get(cur_pos).getTokenType() == Defines.TokenType.LPARENT ||
                        tokens.get(cur_pos).getTokenType() == Defines.TokenType.INTCON) {
                    unaryExp.addSons(parseFuncRParams());
                    if (cur_pos >= tokens.size() || tokens.get(cur_pos).getTokenType() != Defines.TokenType.RPARENT) {
                        //return null;
                        throw new Syntax.SyntaxException("UnaryExp: not )");
                    }
                    unaryExp.addSons(tokens.get(cur_pos));
                    cur_pos++;  // accept )

                } else {
                    //return null;
                    throw new Syntax.SyntaxException("UnaryExp: undefine maybe missing )");
                }
            } else {
                unaryExp.addSons(parsePrimaryExp());
            }
        } else if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.LPARENT ||
                tokens.get(cur_pos).getTokenType() == Defines.TokenType.INTCON) {
            unaryExp.addSons(parsePrimaryExp());
        } else if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.PLUS ||
                tokens.get(cur_pos).getTokenType() == Defines.TokenType.MINU ||
                tokens.get(cur_pos).getTokenType() == Defines.TokenType.NOT) {
            unaryExp.addSons(parseUnaryOp());
            if (cur_pos >= tokens.size()) {
                //return null;
                throw new Syntax.SyntaxException("UnaryExp: out size");
            }
            unaryExp.addSons(parseUnaryExp());
        } else {
            //return null;
            throw new Syntax.SyntaxException("UnaryExp: undefine");
        }
        return unaryExp;
    }

    private TokenDefines.Token parseUnaryOp() throws Syntax.SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("UnaryOp: out size");
        }
        TokenDefines.Token unaryOp = new TokenDefines.Token(0, Defines.TokenType.UnaryOp, "");
        if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.PLUS ||
            tokens.get(cur_pos).getTokenType() == Defines.TokenType.MINU ||
            tokens.get(cur_pos).getTokenType() == Defines.TokenType.NOT) {
            unaryOp.addSons(tokens.get(cur_pos));
            cur_pos++;  // accept + or - or !
        } else {
            //return null;
            throw new Syntax.SyntaxException("UnaryOp: not in First or Follow");
        }
        return unaryOp;
    }

    private TokenDefines.Token parseFuncRParams() throws Syntax.SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("FuncRParams: out size");
        }
        TokenDefines.Token funcRParams = new TokenDefines.Token(0, Defines.TokenType.FuncRParams, "");
        funcRParams.addSons(parseExp());
        while (cur_pos < tokens.size()) {
            if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.RPARENT) {
                break;
            } else if (tokens.get(cur_pos).getTokenType() == Defines.TokenType.COMMA) {
                funcRParams.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept ,
                funcRParams.addSons(parseExp());
            } else {
                //return null;
                throw new Syntax.SyntaxException("FuncRParams: not in First or Follow");
            }
        }
        return funcRParams;
    }

    private TokenDefines.Token parseMulExp() throws Syntax.SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("MulExp: out size");
        }
        TokenDefines.Token mulExp = new TokenDefines.Token(0, Defines.TokenType.MulExp, "");
        mulExp.addSons(parseUnaryExp());
        while (cur_pos < tokens.size()) {
            Defines.TokenType tokenType = tokens.get(cur_pos).getTokenType();
            if (tokenType == Defines.TokenType.RPARENT || tokenType == Defines.TokenType.OR || tokenType == Defines.TokenType.AND ||
                    tokenType == Defines.TokenType.EQL || tokenType == Defines.TokenType.NEQ ||
                    tokenType == Defines.TokenType.LSS || tokenType == Defines.TokenType.GRE ||
                    tokenType == Defines.TokenType.LEQ || tokenType == Defines.TokenType.GEQ ||
                    tokenType == Defines.TokenType.COMMA || tokenType == Defines.TokenType.SEMICN ||
                    tokenType == Defines.TokenType.RBRACE || tokenType == Defines.TokenType.RBRACK ||
                    tokenType == Defines.TokenType.PLUS || tokenType == Defines.TokenType.MINU) {
                break;
            } else if (tokenType == Defines.TokenType.MULT || tokenType == Defines.TokenType.DIV || tokenType == Defines.TokenType.MOD) {
                mulExp.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept * / %
                mulExp.addSons(parseUnaryExp());
            } else {
                //return null;
                throw new Syntax.SyntaxException("MulExp: not in First or Follow");
            }
        }
        return mulExp;
    }

    private TokenDefines.Token parseAddExp() throws Syntax.SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("AddExp: out size");
        }
        TokenDefines.Token addExp = new TokenDefines.Token(0, Defines.TokenType.AddExp, "");
        addExp.addSons(parseMulExp());
        while (cur_pos < tokens.size()) {
            Defines.TokenType tokenType = tokens.get(cur_pos).getTokenType();
            if (tokenType == Defines.TokenType.RPARENT || tokenType == Defines.TokenType.OR || tokenType == Defines.TokenType.AND ||
                    tokenType == Defines.TokenType.EQL || tokenType == Defines.TokenType.NEQ ||
                    tokenType == Defines.TokenType.LSS || tokenType == Defines.TokenType.GRE ||
                    tokenType == Defines.TokenType.LEQ || tokenType == Defines.TokenType.GEQ ||
                    tokenType == Defines.TokenType.COMMA || tokenType == Defines.TokenType.SEMICN ||
                    tokenType == Defines.TokenType.RBRACE || tokenType == Defines.TokenType.RBRACK) {
                break;
            } else if (tokenType == Defines.TokenType.PLUS || tokenType == Defines.TokenType.MINU) {
                addExp.addSons(tokens.get(cur_pos));
                cur_pos++;   // accept + or -
                addExp.addSons(parseMulExp());
            } else {
                //return null;
                throw new Syntax.SyntaxException("AddExp: not in First or Follow");
            }
        }
        return addExp;
    }

    private TokenDefines.Token parseRelExp() throws Syntax.SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("RelExp: out size");
        }
        TokenDefines.Token relExp = new TokenDefines.Token(0, Defines.TokenType.RelExp, "");
        relExp.addSons(parseAddExp());
        while (cur_pos < tokens.size()) {
            Defines.TokenType tokenType = tokens.get(cur_pos).getTokenType();
            if (tokenType == Defines.TokenType.RPARENT || tokenType == Defines.TokenType.OR ||
                tokenType == Defines.TokenType.AND || tokenType == Defines.TokenType.EQL ||
                tokenType == Defines.TokenType.NEQ) {
                break;
            } else if (tokenType == Defines.TokenType.LSS || tokenType == Defines.TokenType.GRE ||
                tokenType == Defines.TokenType.LEQ || tokenType == Defines.TokenType.GEQ) {
                relExp.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept < > <= >=
                relExp.addSons(parseAddExp());
            } else {
                //return null;
                throw new Syntax.SyntaxException("RelExp: not in First or Follow");
            }
        }
        return relExp;
    }

    private TokenDefines.Token parseEqExp() throws Syntax.SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("EqExp: out size");
        }
        TokenDefines.Token eqExp = new TokenDefines.Token(0, Defines.TokenType.EqExp, "");
        eqExp.addSons(parseRelExp());
        while (cur_pos < tokens.size()) {
            Defines.TokenType tokenType = tokens.get(cur_pos).getTokenType();
            if (tokenType == Defines.TokenType.RPARENT || tokenType == Defines.TokenType.OR ||
                tokenType == Defines.TokenType.AND) {
                break;
            } else if (tokenType == Defines.TokenType.EQL || tokenType == Defines.TokenType.NEQ) {
                eqExp.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept == !=
                eqExp.addSons(parseRelExp());
            } else {
                //return null;
                throw new Syntax.SyntaxException("EqExp: not in First or Follow");
            }
        }
        return eqExp;
    }

    private TokenDefines.Token parseLAndExp() throws Syntax.SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("LAndExp: out size");
        }
        TokenDefines.Token lAndExp = new TokenDefines.Token(0, Defines.TokenType.LAndExp, "");
        lAndExp.addSons(parseEqExp());
        while (cur_pos < tokens.size()) {
            Defines.TokenType tokenType = tokens.get(cur_pos).getTokenType();
            if (tokenType == Defines.TokenType.RPARENT || tokenType == Defines.TokenType.OR) {
                break;
            } else if (tokenType == Defines.TokenType.AND) {
                lAndExp.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept &&
                lAndExp.addSons(parseEqExp());
            } else {
                //return null;
                throw new Syntax.SyntaxException("LAndExp: not in First or Follow");
            }
        }
        return lAndExp;
    }

    private TokenDefines.Token parseLOrExp() throws Syntax.SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("LOrExp: out size");
        }
        TokenDefines.Token lOrExp = new TokenDefines.Token(0, Defines.TokenType.LOrExp, "");
        lOrExp.addSons(parseLAndExp());
        while (cur_pos < tokens.size()) {
            Defines.TokenType tokenType = tokens.get(cur_pos).getTokenType();
            if (tokenType == Defines.TokenType.RPARENT) {
                break;
            } else if (tokenType == Defines.TokenType.OR) {
                lOrExp.addSons(tokens.get(cur_pos));
                cur_pos++;  // accept ||
                lOrExp.addSons(parseLAndExp());
            } else {
                //return null;
                throw new Syntax.SyntaxException("LOrExp: not in First or Follow");
            }
        }
        return lOrExp;
    }

    private TokenDefines.Token parseConstExp() throws Syntax.SyntaxException {
        if (cur_pos >= tokens.size()) {
            //return null;
            throw new Syntax.SyntaxException("ConstExp: out size");
        }
        TokenDefines.Token constExp = new TokenDefines.Token(0, Defines.TokenType.ConstExp, "");
        constExp.addSons(parseAddExp());
        return constExp;
    }
}

     */
}
