import java.util.ArrayList;

public class Token {
    private int lineNumber;
    private int lineStartNumber;
    private ArrayList<Token> sons;
    private TokenType tokenType;
    private String tokenString;

    public Token(TokenType tokenType, String tokenString) {
        this.tokenType = tokenType;
        this.sons = new ArrayList<>();
        this.tokenString = tokenString;
    }
    public Token(int lineNumber, TokenType tokenType, String tokenString) {
        this.lineNumber = lineNumber;
        this.tokenType = tokenType;
        this.sons = new ArrayList<>();
        this.tokenString = tokenString;
    }
    
    public TokenType getTokenType() {
        return tokenType;
    }
    
    public String getTokenString() {
        return tokenString;
    }
    
    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }
    
    public void addSons(Token son) {
        this.sons.add(son);
    }
    
    public ArrayList<Token> getSons() {
        return sons;
    }

    public int getLineStartNumber() {
        return lineStartNumber;
    }

    public void setLineStartNumber(int lineStartNumber) {
        this.lineStartNumber = lineStartNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
}
