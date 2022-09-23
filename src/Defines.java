import java.util.ArrayList;
import java.util.HashMap;

public class Defines {
    public static ArrayList<String> reserve = new ArrayList<String>() {
        {
            add("main");
            add("const");
            add("int");
            add("break");
            add("continue");
            add("if");
            add("else");
            add("while");
            add("getint");
            add("printf");
            add("return");
            add("void");
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
    
    public static String getReserveStr(String str) {
        if (reserveStr.containsKey(str)) {
            return reserveStr.get(str);
        }
        return "";
    }
    
    public static boolean isBlank(char c) {
        return c == ' ' || c == '\t' || c == '\n';
    }
    
    public static boolean isSeparate(char c) {
        return separatorStr.containsKey(String.valueOf(c)) || c == '&' || c == '|';
    }
    
    public static boolean isNum(char c) {
        return c >= '0' && c <= '9';
    }
}
