package net.eduardolira.tap;


public class Token {
    public static final int WHITESPACE = 0;
    public static final int STRING = 1;
    public static final int BAD_STRING = 2;
    public static final int HASH = 3;
    public static final int DELIM = 4;
    public static final int LEFT_PARENTHESIS = 5;
    public static final int RIGHT_PARENTHESIS = 6;
    public static final int COMMA = 7;
    public static final int COLON = 8;
    public static final int SEMICOLON = 9;
    public static final int LEFT_SQUARE_BRACKET = 10;
    public static final int RIGHT_SQUARE_BRACKET = 11;
    public static final int LEFT_CURLY_BRACKET = 12;
    public static final int RIGHT_CURLY_BRACKET = 13;
    public static final int NUMBER = 14;
    public static final int PERCENTAGE = 15;
    public static final int DIMENSION = 16;
    public static final int URL = 17;
    public static final int FUNCTION = 18;
    public static final int IDENT = 19;

    public static final int FLAG_NONE = -1;
    public static final int FLAG_ID = 0;
    private final int tokenType;
    private final String content;
    private int flag = -1;

    public Token(int tokenType, String content) {
        this.tokenType = tokenType;
        this.content = content;
    }

    public int getTokenType() {
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
        return tokenType + " " + content + (flag!=-1?" " +flag:"");
    }
}
