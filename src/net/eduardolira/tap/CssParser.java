package net.eduardolira.tap;

import java.io.*;
import java.util.HashMap;
import java.util.function.Consumer;
import static net.eduardolira.tap.CodePoint.*;

public class CssParser {
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
            char codePoint = preprocess((char) in);
            if(isWhiteSpace(codePoint)) tokenConsumer.accept(handleWhitespace(token));
            else if(codePoint == QUOTATION_MARK) tokenConsumer.accept(handleString(token, QUOTATION_MARK));
            //else if(codePoint == NUMBER_SIGN) tokenConsumer.accept(handleNumSign(token));
            else if(codePoint == APOSTROPHE) tokenConsumer.accept(handleString(token, APOSTROPHE));
            else if(charToSimpleTokenType.containsKey(codePoint)){
                tokenConsumer.accept(new Token(charToSimpleTokenType.get(codePoint),Character.toString(codePoint)));
            }
        }
    }

    private Token handleNumSign(StringBuilder token) {
        return null;
    }


    private char preprocess(char codePoint) throws IOException {
        //Replace pairs of U+000D CARRIAGE RETURN (CR) followed by U+000A LINE FEED (LF) with a single U+000A LINE FEED (LF) code point.
        //Replace of U+000D CARRIAGE RETURN (not followed by U+000A LINE FEED (LF)) with U+000A LINE FEED (LF) code
        if(codePoint == CARRIAGE_RETURN) {
            rd.mark(1);
            char nextCodePoint = (char) rd.read(); //todo what if end of stream
            if (nextCodePoint == LINE_FEED){
                return LINE_FEED;
            }else{
                rd.reset();
                return LINE_FEED;
            }
        }
        //Replace U+000C FORM FEED with U+000A LINE FEED
        if(codePoint == FORM_FEED) return LINE_FEED;
        //Replace any U+0000 NULL code point with U+FFFD REPLACEMENT CHARACTER
        if(codePoint == NULL) return REPLACEMENT_CHARACTER;
        return codePoint;
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

    private static boolean isNewLine(char codePoint){
        return codePoint == LINE_FEED;
    }

    private static boolean isWhiteSpace(char codePoint){
        if(isNewLine(codePoint)) return true;
        if(codePoint == CHARACTER_TABULATION) return true;
        if(codePoint == SPACE) return true;
        return false;
    }

    private static boolean isNonAsciiCodePoint(char codePoint){
        return codePoint >= 0x080;
    }

    private static boolean isNameStartCodePoint(char codePoint){
        if(isLetter(codePoint)) return true;
        if(isNonAsciiCodePoint(codePoint)) return true;
        if(codePoint==LOW_LINE) return true;
        return false;
    }

    private static boolean isNameCodePoint(char codePoint){
        if(isNameStartCodePoint(codePoint)) return true;
        if(isDigit(codePoint)) return true;
        if(codePoint == HYPHEN_MINUS) return true;
        return false;
    }

    private static boolean isDigit(char codePoint){
        return codePoint>=DIGIT_ZERO&&codePoint<=DIGIT_NINE;
    }
    private static boolean isLetter(char codePoint){
        return isUpperCaseLetter(codePoint) || isLowerCaseLetter(codePoint);
    }

    private static boolean isLowerCaseLetter(char codePoint){
        return (codePoint>=LATIN_SMALL_LETTER_A && codePoint<=LATIN_SMALL_LETTER_Z);
    }

    private static boolean isUpperCaseLetter(char codePoint){
        return (codePoint>=LATIN_CAPITAL_LETTER_A && codePoint<=LATIN_CAPITAL_LETTER_Z);
    }

}
