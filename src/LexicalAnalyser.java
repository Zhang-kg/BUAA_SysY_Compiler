import java.util.ArrayList;

public class LexicalAnalyser {
    private String inputStr;
    private ArrayList<Token> tokens = new ArrayList<>();
    
    public LexicalAnalyser(String inputStr) {
        FilePrinter filePrinter = FilePrinter.getFilePrinter();
        this.inputStr = inputStr;
        int i = 0;
        while (i < this.inputStr.length()) {
            String token = "";
            while (i < this.inputStr.length() && Defines.isBlank(this.inputStr.charAt(i))) {
                i++;
            }
            if (i < this.inputStr.length()) {
                token += String.valueOf(this.inputStr.charAt(i));
            } else {
                break;
            }
            Defines.TokenType tokenType = null;
            if (token.equals("!")) {
                if (i + 1 < this.inputStr.length() && this.inputStr.charAt(i + 1) == '=') {
                    filePrinter.outPrintln("NEQ !=");
                    token = "!=";
                    tokenType = Defines.TokenType.NEQ;
                    i += 2;
                } else {
                    i++;
                    filePrinter.outPrintln("NOT !");
                    tokenType = Defines.TokenType.NOT;
                }
            } else if (token.equals("&")) {
                if (i + 1 < this.inputStr.length() && this.inputStr.charAt(i + 1) == '&') {
                    i += 2;
                    filePrinter.outPrintln("AND &&");
                    token = "&&";
                    tokenType = Defines.TokenType.AND;
                }
            } else if (token.equals("|")) {
                if (i + 1 < this.inputStr.length() && this.inputStr.charAt(i + 1) == '|') {
                    i += 2;
                    filePrinter.outPrintln("OR ||");
                    token = "||";
                    tokenType = Defines.TokenType.OR;
                }
            } else if (token.equals("=")) {
                if (i + 1 < this.inputStr.length() && this.inputStr.charAt(i + 1) == '=') {
                    i += 2;
                    filePrinter.outPrintln("EQL ==");
                    token = "==";
                    tokenType = Defines.TokenType.EQL;
                } else {
                    i++;
                    filePrinter.outPrintln("ASSIGN =");
                    tokenType = Defines.TokenType.ASSIGN;
                }
            } else if (token.equals(">")) {
                if (i + 1 < this.inputStr.length() && this.inputStr.charAt(i + 1) == '=') {
                    i += 2;
                    filePrinter.outPrintln("GEQ >=");
                    token = ">=";
                    tokenType = Defines.TokenType.GEQ;
                } else {
                    i++;
                    filePrinter.outPrintln("GRE >");
                    tokenType = Defines.TokenType.GRE;
                }
            } else if (token.equals("<")) {
                if (i + 1 < this.inputStr.length() && this.inputStr.charAt(i + 1) == '=') {
                    i += 2;
                    filePrinter.outPrintln("LEQ <=");
                    token = "<=";
                    tokenType = Defines.TokenType.LEQ;
                } else {
                    i++;
                    filePrinter.outPrintln("LSS <");
                    tokenType = Defines.TokenType.LSS;
                }
            } else if (token.equals("+")) {
                i++;
                filePrinter.outPrintln("PLUS +");
                tokenType = Defines.TokenType.PLUS;
            } else if (token.equals("-")) {
                i++;
                filePrinter.outPrintln("MINU -");
                tokenType = Defines.TokenType.MINU;
            } else if (token.equals("*")) {
                i++;
                filePrinter.outPrintln("MULT *");
                tokenType = Defines.TokenType.MULT;
            } else if (token.equals("/")) {
                i++;
                filePrinter.outPrintln("DIV /");
                tokenType = Defines.TokenType.DIV;
            } else if (token.equals("%")) {
                i++;
                filePrinter.outPrintln("MOD %");
                tokenType = Defines.TokenType.MOD;
            } else if (token.equals(";")) {
                i++;
                filePrinter.outPrintln("SEMICN ;");
                tokenType = Defines.TokenType.SEMICN;
            } else if (token.equals(",")) {
                i++;
                filePrinter.outPrintln("COMMA ,");
                tokenType = Defines.TokenType.COMMA;
            } else if (token.equals("(")) {
                i++;
                filePrinter.outPrintln("LPARENT (");
                tokenType = Defines.TokenType.LPARENT;
            } else if (token.equals(")")) {
                i++;
                filePrinter.outPrintln("RPARENT )");
                tokenType = Defines.TokenType.RPARENT;
            } else if (token.equals("[")) {
                i++;
                filePrinter.outPrintln("LBRACK [");
                tokenType = Defines.TokenType.LBRACK;
            } else if (token.equals("]")) {
                i++;
                filePrinter.outPrintln("RBRACK ]");
                tokenType = Defines.TokenType.RBRACK;
            } else if (token.equals("{")) {
                i++;
                filePrinter.outPrintln("LBRACE {");
                tokenType = Defines.TokenType.LBRACE;
            } else if (token.equals("}")) {
                i++;
                filePrinter.outPrintln("RBRACE }");
                tokenType = Defines.TokenType.RBRACE;
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
                tokenType = Defines.TokenType.STRCON;
            } else if (Defines.isNum(token.charAt(0))) {
                i++;
                while (i < this.inputStr.length() && Defines.isNum(this.inputStr.charAt(i))) {
                    token += this.inputStr.charAt(i);
                    i++;
                }
                filePrinter.outPrintln("INTCON " + token);
                tokenType = Defines.TokenType.INTCON;
            } else {
                i++;
                while (i < this.inputStr.length() && !Defines.isSeparate(this.inputStr.charAt(i))) {
                    token += String.valueOf(this.inputStr.charAt(i));
                    i++;
                }
                String type = Defines.getReserveStr(token);
                if (type.equals("")) {
                    filePrinter.outPrintln("IDENFR " + token);
                    tokenType = Defines.TokenType.IDENFR;
                } else {
                    filePrinter.outPrintln(type + " " + token);
                    tokenType = Defines.TokenType.valueOf(type);
                }
            }
            tokens.add(new Token(0, tokenType, token));
        }
        //filePrinter.closeOut();
    }
    
    public ArrayList<Token> getTokens() {
        return tokens;
    }
    
}
