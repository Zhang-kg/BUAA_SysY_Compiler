import java.util.ArrayList;

public class Compiler {
    public static void main(String[] args) {
        BasicScanner basicScanner = new BasicScanner();
        String ans = basicScanner.getAns();
        LexicalAnalyser lexicalAnalyser = new LexicalAnalyser(ans);
        ArrayList<Token> tokens = lexicalAnalyser.getTokens();
        SyntaxAnalyser syntaxAnalyser = new SyntaxAnalyser(tokens);
        Token root = syntaxAnalyser.getRoot();
        FilePrinter filePrinter = FilePrinter.getFilePrinter();
        TreePrinter treePrinter = new TreePrinter(root);
        //for (Token token : tokens) {
        //    filePrinter.outPrintlnNew(token.getTokenType() + " " + token.getTokenString());
        //    //System.out.println(token.getTokenType() + " " + token.getTokenString());
        //}
        
        filePrinter.closeOut();
    }
}
