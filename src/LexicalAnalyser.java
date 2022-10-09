import java.util.ArrayList;
import java.util.HashMap;

// 词法分析

public class LexicalAnalyser {
    private String inputStr;
    private ArrayList<Token> tokens = new ArrayList<>();
    public static HashMap<String, String> separatorStr = new HashMap<String, String>() {
        {
            put("NOT", "!");
            put("!", "NOT");
            put("AND", "&&");
            put("&&", "AND");
            put("OR", "||");
            put("||", "OR");
            put("PLUS", "+");
            put("+", "PLUS");
            put("MINU", "-");
            put("-", "MINU");
            put("MULT", "*");
            put("*", "MULT");
            put("DIV", "/");
            put("/", "DIV");
            put("MOD", "%");
            put("%", "MOD");
            put("<", "LSS");
            put("LSS", "<");
            put("<=", "LEQ");
            put("LEQ", "<=");
            put(">", "GRE");
            put("GRE", ">");
            put(">=", "GEQ");
            put("GEQ", ">=");
            put("==", "EQL");
            put("EQL", "==");
            put("!=", "NEQ");
            put("NEQ", "!=");
            put("=", "ASSIGN");
            put("ASSIGN", "=");
            put(";", "SEMICN");
            put("SEMICN", ";");
            put(",", "COMMA");
            put("COMMA", ",");
            put("(", "LPARENT");
            put("LPARENT", "(");
            put(")", "RPARENT");
            put("RPARENT", ")");
            put("[", "LBRACK");
            put("LBRACK", "[");
            put("]", "RBRACK");
            put("RBRACK", "]");
            put("{", "LBRACE");
            put("LBRACE", "{");
            put("}", "RBRACE");
            put("RBRACE", "}");
            put(" ", "BLANK");
            put("\t", "TABTK");
            put("\n", "ENTERTK");
        }
    };
    public static HashMap<String, String> reserveStr = new HashMap<String, String>() {
        {
            put("MAINTK", "main");
            put("main", "MAINTK");
            put("CONSTTK", "const");
            put("const", "CONSTTK");
            put("INTTK", "int");
            put("int", "INTTK");
            put("BREAKTK", "break");
            put("break", "BREAKTK");
            put("CONTINUETK", "continue");
            put("continue", "CONTINUETK");
            put("IFTK", "if");
            put("if", "IFTK");
            put("ELSETK", "else");
            put("else", "ELSETK");
            put("WHILETK", "while");
            put("while", "WHILETK");
            put("GETINTTK", "getint");
            put("getint", "GETINTTK");
            put("PRINTFTK", "printf");
            put("printf", "PRINTFTK");
            put("RETURNTK", "return");
            put("return", "RETURNTK");
            put("VOIDTK", "void");
            put("void", "VOIDTK");
        }
    };

    public LexicalAnalyser(String inputStr) {
        FilePrinter filePrinter = FilePrinter.getFilePrinter();
        this.inputStr = inputStr;
        int i = 0;
        while (i < this.inputStr.length()) {
            String token = "";
            while (i < this.inputStr.length() && isBlank(this.inputStr.charAt(i))) {
                i++;
            }
            int tokenLineStartNumber;
            if (i < this.inputStr.length()) {
                tokenLineStartNumber = i;
                token += String.valueOf(this.inputStr.charAt(i));
            } else {
                break;
            }
            TokenType tokenType = null;
            if (token.equals("!")) {
                if (i + 1 < this.inputStr.length() && this.inputStr.charAt(i + 1) == '=') {
                    filePrinter.outPrintln("NEQ !=");
                    token = "!=";
                    tokenType = TokenType.NEQ;
                    i += 2;
                } else {
                    i++;
                    filePrinter.outPrintln("NOT !");
                    tokenType = TokenType.NOT;
                }
            } else if (token.equals("&")) {
                if (i + 1 < this.inputStr.length() && this.inputStr.charAt(i + 1) == '&') {
                    i += 2;
                    filePrinter.outPrintln("AND &&");
                    token = "&&";
                    tokenType = TokenType.AND;
                }
            } else if (token.equals("|")) {
                if (i + 1 < this.inputStr.length() && this.inputStr.charAt(i + 1) == '|') {
                    i += 2;
                    filePrinter.outPrintln("OR ||");
                    token = "||";
                    tokenType = TokenType.OR;
                }
            } else if (token.equals("=")) {
                if (i + 1 < this.inputStr.length() && this.inputStr.charAt(i + 1) == '=') {
                    i += 2;
                    filePrinter.outPrintln("EQL ==");
                    token = "==";
                    tokenType = TokenType.EQL;
                } else {
                    i++;
                    filePrinter.outPrintln("ASSIGN =");
                    tokenType = TokenType.ASSIGN;
                }
            } else if (token.equals(">")) {
                if (i + 1 < this.inputStr.length() && this.inputStr.charAt(i + 1) == '=') {
                    i += 2;
                    filePrinter.outPrintln("GEQ >=");
                    token = ">=";
                    tokenType = TokenType.GEQ;
                } else {
                    i++;
                    filePrinter.outPrintln("GRE >");
                    tokenType = TokenType.GRE;
                }
            } else if (token.equals("<")) {
                if (i + 1 < this.inputStr.length() && this.inputStr.charAt(i + 1) == '=') {
                    i += 2;
                    filePrinter.outPrintln("LEQ <=");
                    token = "<=";
                    tokenType = TokenType.LEQ;
                } else {
                    i++;
                    filePrinter.outPrintln("LSS <");
                    tokenType = TokenType.LSS;
                }
            } else if (token.equals("+")) {
                i++;
                filePrinter.outPrintln("PLUS +");
                tokenType = TokenType.PLUS;
            } else if (token.equals("-")) {
                i++;
                filePrinter.outPrintln("MINU -");
                tokenType = TokenType.MINU;
            } else if (token.equals("*")) {
                i++;
                filePrinter.outPrintln("MULT *");
                tokenType = TokenType.MULT;
            } else if (token.equals("/")) {
                i++;
                filePrinter.outPrintln("DIV /");
                tokenType = TokenType.DIV;
            } else if (token.equals("%")) {
                i++;
                filePrinter.outPrintln("MOD %");
                tokenType = TokenType.MOD;
            } else if (token.equals(";")) {
                i++;
                filePrinter.outPrintln("SEMICN ;");
                tokenType = TokenType.SEMICN;
            } else if (token.equals(",")) {
                i++;
                filePrinter.outPrintln("COMMA ,");
                tokenType = TokenType.COMMA;
            } else if (token.equals("(")) {
                i++;
                filePrinter.outPrintln("LPARENT (");
                tokenType = TokenType.LPARENT;
            } else if (token.equals(")")) {
                i++;
                filePrinter.outPrintln("RPARENT )");
                tokenType = TokenType.RPARENT;
            } else if (token.equals("[")) {
                i++;
                filePrinter.outPrintln("LBRACK [");
                tokenType = TokenType.LBRACK;
            } else if (token.equals("]")) {
                i++;
                filePrinter.outPrintln("RBRACK ]");
                tokenType = TokenType.RBRACK;
            } else if (token.equals("{")) {
                i++;
                filePrinter.outPrintln("LBRACE {");
                tokenType = TokenType.LBRACE;
            } else if (token.equals("}")) {
                i++;
                filePrinter.outPrintln("RBRACE }");
                tokenType = TokenType.RBRACE;
            } else if (token.equals("\"")) {
                i++;
                while (i < this.inputStr.length() && this.inputStr.charAt(i) != '\"') {
                    token += this.inputStr.charAt(i);
                    i++;
                }
                if (i < this.inputStr.length() && this.inputStr.charAt(i) == '\"') {
                    i++;
                }
                token += "\"";
                filePrinter.outPrintln("STRCON " + token);
                tokenType = TokenType.STRCON;
            } else if (isNum(token.charAt(0))) {
                i++;
                while (i < this.inputStr.length() && isNum(this.inputStr.charAt(i))) {
                    token += this.inputStr.charAt(i);
                    i++;
                }
                filePrinter.outPrintln("INTCON " + token);
                tokenType = TokenType.INTCON;
            } else {
                i++;
                while (i < this.inputStr.length() && !isSeparate(this.inputStr.charAt(i))) {
                    token += String.valueOf(this.inputStr.charAt(i));
                    i++;
                }
                String type = getReserveStr(token);
                if (type.equals("")) {
                    filePrinter.outPrintln("IDENFR " + token);
                    tokenType = TokenType.IDENFR;
                } else {
                    filePrinter.outPrintln(type + " " + token);
                    tokenType = TokenType.valueOf(type);
                }
            }
//            tokens.add(new Token(0, tokenType, token));
            Token token1 = new Token(tokenType, token);
            token1.setLineStartNumber(tokenLineStartNumber);
            tokens.add(token1);
        }
        //filePrinter.closeOut();
    }
    
    public ArrayList<Token> getTokens() {
        return tokens;
    }

    private boolean isBlank(char c) {
        return c == ' ' || c == '\t' || c == '\n';
    }

    private boolean isSeparate(char c) {
        return separatorStr.containsKey(String.valueOf(c)) || c == '&' || c == '|';
    }

    public static String getReserveStr(String str) {
        if (reserveStr.containsKey(str)) {
            return reserveStr.get(str);
        }
        return "";
    }

    public static boolean isNum(char c) {
        return c >= '0' && c <= '9';
    }
}
