package net.eduardolira.tap;

import java.io.*;
import java.util.HashMap;
import java.util.function.Consumer;

public class CssParser {
    private static final char CARRIAGE_RETURN = 0x000D;
    private static final char FORM_FEED = 0x000C;
    private static final char LINE_FEED = 0x000A;
    private static final char CHARACTER_TABULATION = 0x0009;
    private static final char SPACE = 0x0020;
    private static final char NULL = 0x0000;
    private static final char REPLACEMENT_CHARACTER = 0xFFFD;
    private static final char QUOTATION_MARK = 0x0022;
    private static final char APOSTROPHE = 0x0027;
    private static final char REVERSE_SOLIDUS = 0x005C;
    private static final char NUMBER_SIGN = 0x0023;
    private static final char DOLLAR_SIGN = 0x0024;
    public static final char LEFT_PARENTHESIS = 0x0028;
    public static final char RIGHT_PARENTHESIS = 0x0029;
    public static final char COMMA = 0x002C;
    public static final char COLON = 0x003A;
    public static final char SEMICOLON = 0x003B;
    public static final char LEFT_SQUARE_BRACKET = 0x005B;
    public static final char RIGHT_SQUARE_BRACKET = 0x005D;
    public static final char LEFT_CURLY_BRACKET = 0x007B;
    public static final char RIGHT_CURLY_BRACKET = 0x007D;
    public static final char LOW_LINE = 0x005F;
    public static final char LATIN_CAPITAL_LETTER_A = 0x0041;
    public static final char LATIN_CAPITAL_LETTER_Z = 0x005A;
    public static final char LATIN_SMALL_LETTER_A = 0x0061;
    public static final char LATIN_SMALL_LETTER_Z = 0x007A;
    public static final char HYPHEN_MINUS = 0x002D;
    private static final char DIGIT_ZERO = 0x0030;
    private static final char DIGIT_NINE = 0x0039;

    private BufferedReader rd;
    private static final HashMap<Character, Integer> charToSimpleTokenType = new HashMap<>();
    static {
        charToSimpleTokenType.put(COMMA, Token.COMMA);
        charToSimpleTokenType.put(COLON, Token.COLON);
        charToSimpleTokenType.put(SEMICOLON, Token.SEMICOLON);
        charToSimpleTokenType.put(LEFT_PARENTHESIS, Token.LEFT_PARENTHESIS);
        charToSimpleTokenType.put(RIGHT_PARENTHESIS, Token.RIGHT_PARENTHESIS);
        charToSimpleTokenType.put(LEFT_SQUARE_BRACKET, Token.LEFT_SQUARE_BRACKET);
        charToSimpleTokenType.put(RIGHT_SQUARE_BRACKET, Token.RIGHT_SQUARE_BRACKET);
        charToSimpleTokenType.put(LEFT_CURLY_BRACKET, Token.LEFT_CURLY_BRACKET);
        charToSimpleTokenType.put(RIGHT_CURLY_BRACKET, Token.RIGHT_CURLY_BRACKET);
    }
    public void setInputStream(InputStream inputStream) {
        this.rd = new BufferedReader(new InputStreamReader(inputStream));
    }

    public void streamTokens(Consumer<Token> tokenConsumer) throws IOException {
        int in = -1;
        StringBuilder token = new StringBuilder();
        while((in = rd.read()) != -1) {
            char codepoint = preprocess((char) in);
            if(isWhiteSpace(codepoint)) tokenConsumer.accept(handleWhitespace(token));
            else if(codepoint == QUOTATION_MARK) tokenConsumer.accept(handleString(token, QUOTATION_MARK));
            //else if(codepoint == NUMBER_SIGN) tokenConsumer.accept(handleNumSign(token));
            else if(codepoint == APOSTROPHE) tokenConsumer.accept(handleString(token, APOSTROPHE));
            else if(charToSimpleTokenType.containsKey(codepoint)){
                tokenConsumer.accept(new Token(charToSimpleTokenType.get(codepoint),Character.toString(codepoint)));
            }
        }
    }

    private Token handleNumSign(StringBuilder token) {
        return null;
    }


    private char preprocess(char codepoint) throws IOException {
        //Replace pairs of U+000D CARRIAGE RETURN (CR) followed by U+000A LINE FEED (LF) with a single U+000A LINE FEED (LF) code point.
        //Replace of U+000D CARRIAGE RETURN (not followed by U+000A LINE FEED (LF)) with U+000A LINE FEED (LF) code
        if(codepoint == CARRIAGE_RETURN) {
            rd.mark(1);
            char nextCodepoint = (char) rd.read(); //todo what if end of stream
            if (nextCodepoint == LINE_FEED){
                return LINE_FEED;
            }else{
                rd.reset();
                return LINE_FEED;
            }
        }
        //Replace U+000C FORM FEED with U+000A LINE FEED
        if(codepoint == FORM_FEED) return LINE_FEED;
        //Replace any U+0000 NULL code point with U+FFFD REPLACEMENT CHARACTER
        if(codepoint == NULL) return REPLACEMENT_CHARACTER;
        return codepoint;
    }

    private Token handleWhitespace(StringBuilder stringBuilder) throws IOException {
        stringBuilder.setLength(0);
        int in;
        rd.mark(1);
        while((in = rd.read()) != -1) {
            char character = preprocess((char) in);
            if(isWhiteSpace(character)){
                rd.mark(1);
                stringBuilder.append(character);
            }else{
                break;
            }
        }
        rd.reset();
        return new Token(Token.WHITESPACE, stringBuilder.toString());
    }

    private Token handleString(StringBuilder stringBuilder, char quotes) throws IOException {
        stringBuilder.setLength(0);
        int in;
        rd.mark(1);
        while((in = rd.read()) != -1) {
            //todo REVERSE SOLIDUS;
            char character = (char) in; //todo still preprocess
            if (isNewLine(character)) {
                rd.reset();
                return new Token(Token.BAD_STRING, stringBuilder.toString());
            }else if(character==quotes){
                return new Token(Token.STRING, stringBuilder.toString());
            }else{
                rd.mark(1);
                stringBuilder.append(character);
            }
        }
        return new Token(Token.STRING, stringBuilder.toString());
    }

    private static boolean isNewLine(char codepoint){
        return codepoint == LINE_FEED;
    }

    private static boolean isWhiteSpace(char codepoint){
        if(isNewLine(codepoint)) return true;
        if(codepoint == CHARACTER_TABULATION) return true;
        if(codepoint == SPACE) return true;
        return false;
    }

    private static boolean isNonAsciiCodepoint(char codepoint){
        return codepoint >= 0x080;
    }

    private static boolean isNameStartCodepoint(char codepoint){
        if(isLetter(codepoint)) return true;
        if(isNonAsciiCodepoint(codepoint)) return true;
        if(codepoint==LOW_LINE) return true;
        return false;
    }

    private static boolean isNameCodepoint(char codepoint){
        if(isNameStartCodepoint(codepoint)) return true;
        if(isDigit(codepoint)) return true;
        if(codepoint == HYPHEN_MINUS) return true;
        return false;
    }

    private static boolean isDigit(char codepoint){
        return codepoint>=DIGIT_ZERO&&codepoint<=DIGIT_NINE;
    }
    private static boolean isLetter(char codepoint){
        return isUpperCaseLetter(codepoint) || isLowerCaseLetter(codepoint);
    }

    private static boolean isLowerCaseLetter(char codepoint){
        return (codepoint>=LATIN_SMALL_LETTER_A && codepoint<=LATIN_SMALL_LETTER_Z);
    }

    private static boolean isUpperCaseLetter(char codepoint){
        return (codepoint>=LATIN_CAPITAL_LETTER_A && codepoint<=LATIN_CAPITAL_LETTER_Z);
    }

}
