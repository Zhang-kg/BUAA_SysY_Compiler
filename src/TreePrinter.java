import java.util.ArrayList;

public class TreePrinter {
    private Token root;
    
    public TreePrinter(Token root) {
        this.root = root;
        dfs(root);
    }
    
    public void dfs(Token node) {
        FilePrinter filePrinter = FilePrinter.getFilePrinter();
        String type = node.getTokenType().toString();
        ArrayList<Token> sons = node.getSons();
        if (type.equals("MulExp") || type.equals("AddExp") || type.equals("RelExp") || type.equals("EqExp") ||
                type.equals("LAndExp") || type.equals("LOrExp")) {
            for (Token son : sons) {
                dfs(son);
                if (!isTerminal(son.getTokenType())) {
                    //System.out.println("<" + type + ">");
                    filePrinter.outPrintlnNew("<" + type + ">");
                }
            }
        } else {
            for (Token son : sons) {
                dfs(son);
            }
            if (isTerminal(node.getTokenType())) {
                //System.out.println(node.getTokenType().toString() + " " + node.getTokenString());
                filePrinter.outPrintlnNew(node.getTokenType().toString() + " " + node.getTokenString());
            } else {
                if (!(node.getTokenType().toString().equals("BlockItem") ||
                        node.getTokenType().toString().equals("Decl") ||
                        node.getTokenType().toString().equals("BType"))){
                    //System.out.println("<" + node.getTokenType().toString() + ">");
                    filePrinter.outPrintlnNew("<" + node.getTokenType().toString() + ">");
                }
            }
        }
    }

    public static boolean isTerminal(TokenType tokenType) {
        return tokenType.ordinal() < 38;
    }
}
