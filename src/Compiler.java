import BackEnd.GenerateMIPS;
import BackEnd.GenerateMIPS2;
import ErrorDetect.ErrorDetection;
import FileIO.BasicScanner;
import FileIO.FilePrinter;
import FileIO.LLVMTreePrinter;
import FileIO.TreePrinter;
import IR.GenerateModule;
import Lexical.LexicalAnalyser;
import Syntax.SyntaxAnalyser;
import TokenDefines.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Compiler {
    public static void main(String[] args) {
        // * 输入处理文本
        BasicScanner basicScanner = new BasicScanner();
        String ans = basicScanner.getAns();
        // * 词法分析lexical analyser，接收ans。得到tokes序列
        LexicalAnalyser lexicalAnalyser = new LexicalAnalyser(ans);
        ArrayList<Token> tokens = lexicalAnalyser.getTokens();
        // * 处理所有tokens行号
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
        // * 声明allFalse Map用于存储所有的错误
        HashMap<Token, Character> allFalse = new HashMap<>();
        // * 语法分析 syntax analyser
        SyntaxAnalyser syntaxAnalyser = new SyntaxAnalyser(tokens, allFalse);
        Token root = syntaxAnalyser.getRoot();
        FilePrinter filePrinter = FilePrinter.getFilePrinter();
//        TreePrinter treePrinter = new TreePrinter(root);
        // * 错误处理 error detection，将HashMap转换成有序的TreeMap，用于输出
//        ErrorDetection errorDetection = new ErrorDetection(root, allFalse);
//        TreeMap<Integer, Character> treeMap = new TreeMap<>();
//        for (Token token : allFalse.keySet()) {
//            treeMap.put(token.getLineNumber(), allFalse.get(token));
//        }
//        for (Map.Entry entry : treeMap.entrySet()) {
//            System.out.println(entry.getKey() + " " + entry.getValue());
//        }
        // * 回归测试语法分析的输出，已完成
        TreePrinter treePrinter = new TreePrinter(root);
        GenerateModule generateModule = new GenerateModule();
        generateModule.parseModule(root);
        LLVMTreePrinter llvmTreePrinter = new LLVMTreePrinter();
//        new GenerateMIPS();
        new GenerateMIPS2();
        filePrinter.closeOut();
    }
}
