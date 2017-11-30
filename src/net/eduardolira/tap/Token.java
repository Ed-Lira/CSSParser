package net.eduardolira.tap;


public class Token {


    public static final int FLAG_NONE = -1;
    public static final int FLAG_ID = 0;
    private final TokenType tokenType;
    private final String content;
    private int flag = FLAG_NONE;

    public Token(TokenType tokenType, String content) {
        this.tokenType = tokenType;
        this.content = content;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    @Override
    public String toString() {
        return String.format("%1$-9.9s | ", tokenType) + content + (flag!=-1?" " +"flag:ID":"");
    }
}
