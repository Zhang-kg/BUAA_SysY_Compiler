package ErrorDetect;

import TokenDefines.Token;

import java.util.HashMap;


public class ErrorDetection {
    private Token root;
    private HashMap<Token, Character> allFalse;

    public ErrorDetection(Token root) {
        this.root = root;
    }

    public Token errorDetection(Token root) {
        // detectCompUnit();
        return root;
    }
}
