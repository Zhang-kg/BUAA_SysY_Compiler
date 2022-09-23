public class Compiler {
    public static void main(String[] args) {
        BasicScanner basicScanner = new BasicScanner();
        String ans = basicScanner.getAns();
        SyntaxParse syntaxParse = new SyntaxParse(ans);
    }
}
