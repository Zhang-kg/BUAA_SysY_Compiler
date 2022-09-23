import java.util.ArrayList;

public class Compiler {
    public static void main(String[] args) {
        BasicScanner basicScanner = new BasicScanner();
        String ans = basicScanner.getAns();
        LexicalAnalyser lexicalAnalyser = new LexicalAnalyser(ans);
        ArrayList<Token> tokens = lexicalAnalyser.getTokens();
        FilePrinter filePrinter = FilePrinter.getFilePrinter();
        for (Token token : tokens) {
            filePrinter.outPrintlnNew(token.getTokenType() + " " + token.getTokenString());
            //System.out.println(token.getTokenType() + " " + token.getTokenString());
        }
        
        filePrinter.closeOut();
    }
}
