import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class SyntaxParse {
    private String inputStr;
    
    public SyntaxParse(String inputStr) {
        //System.out.println(inputStr);
        File outFile = new File("./output.txt");
        PrintWriter out = null;
        
        try {
            out = new PrintWriter(outFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
            if (token.equals("!")) {
                if (i + 1 < this.inputStr.length() && this.inputStr.charAt(i + 1) == '=') {
                    out.println("NEQ !=");
                    i+=2;
                } else {
                    i++;
                    out.println("NOT !");
                }
            } else if (token.equals("&")) {
                if (i + 1 < this.inputStr.length() && this.inputStr.charAt(i + 1) == '&') {
                    i+=2;
                    out.println("AND &&");
                }
            } else if (token.equals("|")) {
                if (i + 1 < this.inputStr.length() && this.inputStr.charAt(i + 1) == '|') {
                    i+=2;
                    out.println("OR ||");
                }
            } else if (token.equals("=")) {
                if (i + 1 < this.inputStr.length() && this.inputStr.charAt(i + 1) == '=') {
                    i+=2;
                    out.println("EQL ==");
                } else {
                    i++;
                    out.println("ASSIGN =");
                }
            } else if (token.equals(">")) {
                if (i + 1 < this.inputStr.length() && this.inputStr.charAt(i + 1) == '=') {
                    i+=2;
                    out.println("GEQ >=");
                } else {
                    i++;
                    out.println("GRE >");
                }
            } else if (token.equals("<")) {
                if (i + 1 < this.inputStr.length() && this.inputStr.charAt(i + 1) == '=') {
                    i+=2;
                    out.println("LEQ <=");
                } else {
                    i++;
                    out.println("LSS <");
                }
            } else if (token.equals("+")) {
                i++;
                out.println("PLUS +");
            } else if (token.equals("-")) {
                i++;
                out.println("MINU -");
            } else if (token.equals("*")) {
                i++;
                out.println("MULT *");
            } else if (token.equals("/")) {
                i++;
                out.println("DIV /");
            } else if (token.equals("%")) {
                i++;
                out.println("MOD %");
            } else if (token.equals(";")) {
                i++;
                out.println("SEMICN ;");
            } else if (token.equals(",")) {
                i++;
                out.println("COMMA ,");
            } else if (token.equals("(")) {
                if (out == null) System.out.println("SDFsadsf");
                i++;
                out.println("LPARENT (");
            } else if (token.equals(")")) {
                i++;
                out.println("RPARENT )");
            } else if (token.equals("[")) {
                i++;
                out.println("LBRACK [");
            } else if (token.equals("]")) {
                i++;
                out.println("RBRACK ]");
            } else if (token.equals("{")) {
                i++;
                out.println("LBRACE {");
            } else if (token.equals("}")) {
                i++;
                out.println("RBRACE }");
            } else if (token.equals("\"")) {
                i++;
                while (i < this.inputStr.length() && this.inputStr.charAt(i) != '\"') {
                    token += this.inputStr.charAt(i);
                    i++;
                }
                if (i < this.inputStr.length() && this.inputStr.charAt(i) == '\"') i++;
                token += "\"";
                out.println("STRCON " + token);
            } else if (Defines.isNum(token.charAt(0))) {
                i++;
                while (i < this.inputStr.length() && Defines.isNum(this.inputStr.charAt(i))) {
                    token += this.inputStr.charAt(i);
                    i++;
                }
                out.println("INTCON " + token);
            }
            else {
                i++;
                while (i < this.inputStr.length() && !Defines.isSeparate(this.inputStr.charAt(i))) {
                    token += String.valueOf(this.inputStr.charAt(i));
                    i++;
                }
                String type = Defines.getReserveStr(token);
                if (type.equals("")) {
                    out.println("IDENFR " + token);
                } else {
                    out.println(type + " " + token);
                }
            }
        }
        out.close();
    }
}
