import java.util.ArrayList;

public class Token {
    private int lineNumber;
    private ArrayList<Token> sons;
    private Defines.TokenType tokenType;
    private String tokenString;
    
    public Token(int lineNumber, Defines.TokenType tokenType, String tokenString) {
        this.lineNumber = lineNumber;
        this.tokenType = tokenType;
        this.sons = new ArrayList<>();
        this.tokenString = tokenString;
    }
    
    public Defines.TokenType getTokenType() {
        return tokenType;
    }
    
    public String getTokenString() {
        return tokenString;
    }
}
