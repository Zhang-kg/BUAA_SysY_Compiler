import FileIO.BasicScanner;
import FileIO.FilePrinter;
import FileIO.TreePrinter;
import Lexical.LexicalAnalyser;
import Syntax.SyntaxAnalyser;
import TokenDefines.Token;

import java.util.ArrayList;
import java.util.HashMap;

public class Compiler {
    public static void main(String[] args) {
        BasicScanner basicScanner = new BasicScanner();
        String ans = basicScanner.getAns();

        LexicalAnalyser lexicalAnalyser = new LexicalAnalyser(ans);
        ArrayList<Token> tokens = lexicalAnalyser.getTokens();

        HashMap<Integer, Integer> lineStartNumbers = basicScanner.getLineStartNumbers();
        HashMap<Integer, Integer> lineEndNumbers = basicScanner.getLineEndNumbers();
        int currentLineNumber = 1;
        int i = 0;
        while (i < tokens.size()) {
            int lineStartNumber = tokens.get(i).getLineStartNumber();
            if (lineStartNumber >= lineStartNumbers.get(currentLineNumber) &&
                    lineStartNumber < lineEndNumbers.get(currentLineNumber)) {
//                System.out.print(tokens.get(i).getTokenString() + " ");
                tokens.get(i).setLineNumber(currentLineNumber);
            } else if (lineStartNumber < lineStartNumbers.get(currentLineNumber)) {
                System.out.println("wrong");
                break;
            } else if (lineStartNumber >= lineStartNumbers.get(currentLineNumber + 1)) {
                currentLineNumber++;
//                System.out.print("\n");
                continue;
            }
            i++;
        }
//        System.out.println("");
//        System.out.println("------end------");
        HashMap<Token, Character> allFalse = new HashMap<>();
        SyntaxAnalyser syntaxAnalyser = new SyntaxAnalyser(tokens, allFalse);
        Token root = syntaxAnalyser.getRoot();
        FilePrinter filePrinter = FilePrinter.getFilePrinter();
        TreePrinter treePrinter = new TreePrinter(root);


        //for (TokenDefines.Token token : tokens) {
        //    filePrinter.outPrintlnNew(token.getTokenType() + " " + token.getTokenString());
        //    //System.out.println(token.getTokenType() + " " + token.getTokenString());
        //}
        
        filePrinter.closeOut();
    }
}
