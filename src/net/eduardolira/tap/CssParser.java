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
            else if(codePoint == NUMBER_SIGN) tokenConsumer.accept(handleNumSign(token));
            //todo else if(codePoint == DOLLAR_SIGN) tokenConsumer.accept(handleDollarSign(token));
            else if(codePoint == APOSTROPHE) tokenConsumer.accept(handleString(token, APOSTROPHE));
            //else if(codePoint == HYPHEN_MINUS) tokenConsumer.accept(handleHyphenMinus(token));

                //else if(codePoint == FULL_STOP) tokenConsumer.accept(handleFullStop(token));
            //else if(codePoint == SOLIDUS) tokenConsumer.accept(handleSolidus(token));
            //else if(codePoint == LESS_THAN_SIGN) tokenConsumer.accept(handleLessThanSign(token));
            else if(isDigit(codePoint)) tokenConsumer.accept(consumeNumericToken(codePoint, token));
            else if(charToSimpleTokenType.containsKey(codePoint)){
                tokenConsumer.accept(new Token(charToSimpleTokenType.get(codePoint),Character.toString(codePoint)));
            }
        }
    }

    private Token handleHyphenMinus(StringBuilder token) {
        return null;
    }

    private Token consumeNumericToken(char codePoint, StringBuilder token) throws IOException {
        token.setLength(0);
        token.append(codePoint);
        CSSNumber cssnum = consumeNumber(token);
        return new Token(Token.NUMBER, cssnum.repr);
    }

    private CSSNumber consumeNumber(StringBuilder token) throws IOException {
        CSSNumber number = new CSSNumber();
        number.type = CSSNumber.TYPE_INTEGER;
        rd.mark(1);
        char n0 = (char) rd.read();
        if(n0==HYPHEN_MINUS || n0==PLUS_SIGN){
            token.append(n0);
        }else{
            rd.reset();
        }
        while (true){
            rd.mark(1);
            char np = (char) rd.read();
            if(!isDigit(np)){
                rd.reset();
                break;
            }
            token.append(np);
        }
        rd.mark(2);
        char n2 = (char) rd.read();
        char n3 = (char) rd.read();
        if(n2==FULL_STOP && isDigit(n3)){
            token.append(n2);
            token.append(n3);
            number.type = CSSNumber.TYPE_NUMBER;
            while (true){
                rd.mark(1);
                char np = (char) rd.read();
                if(!isDigit(np)){
                    rd.reset();
                    break;
                }
                token.append(np);
            }
        }
        if(n2==LATIN_CAPITAL_LETTER_E || n2==LATIN_SMALL_LETTER_E){
            if(n3==HYPHEN_MINUS || n3==PLUS_SIGN){
                char n4 = (char) rd.read();
                if(isDigit(n4)){
                    token.append(n2);
                    token.append(n3);
                    token.append(n4);
                    while (true){
                        rd.mark(1);
                        char np = (char) rd.read();
                        if(!isDigit(np)){
                            rd.reset();
                            break;
                        }
                        token.append(np);
                    }
                }
            }else if(isDigit(n3)){
                token.append(n2);
                token.append(n3);
                while (true){
                    rd.mark(1);
                    char np = (char) rd.read();
                    if(!isDigit(np)){
                        rd.reset();
                        break;
                    }
                    token.append(np);
                }
            }
        }
        //Convert repr to a number, and set the value to the returned value.
        number.repr = token.toString();
        return number;
    }

    private Token handleNumSign(StringBuilder token) throws IOException {
        rd.mark(3);
        char n1 = (char) rd.read();
        char n2 = (char) rd.read();
        char n3 = (char) rd.read();
        if(isNameCodePoint(n1)||isValidEscape(n1,n2)){
            rd.reset();
            boolean wouldStartValidId = wouldStartValidIdentifier(n1, n2, n3);
            consumeAName(token);
            Token hashToken = new Token(Token.HASH,token.toString());
            if(wouldStartValidId) hashToken.setFlag(Token.FLAG_ID);
            return hashToken;
        }else{
            //Otherwise, return a <delim-token> with its value set to the current input code point.
            rd.reset();
            return new Token(Token.DELIM, Character.toString(n1));
        }
    }

    private boolean wouldStartValidIdentifier(char n0, char n1, char n2){
        if(n0==HYPHEN_MINUS){
            if(isNameStartCodePoint(n1)) return true;
            return isValidEscape(n1,n2);
        }else if(isNameStartCodePoint(n0)){
            return true;
        }else if(n0==REVERSE_SOLIDUS){
            return isValidEscape(n0,n1);
        }
        return false;
    }

    private boolean isValidEscape(char n0, char n1){
        if(n0!=REVERSE_SOLIDUS) return false;
        if(isNewLine(n1)) return false;
        return true;
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

    private String consumeAName(StringBuilder stringBuilder) throws IOException {
        stringBuilder.setLength(0);
        int in;
        boolean procede = true;
        while(procede) {
            procede = (in = rd.read()) != -1;
            rd.mark(1);
            char n0 = (char) in; //todo still preprocess
            if(isNameCodePoint(n0)){
                stringBuilder.append(n0);
                continue;
            }
            rd.reset();
            rd.mark(2);
            rd.skip(1);
            char n1 = (char) rd.read(); //todo still preprocess
            if(isValidEscape(n0,n1)){
                stringBuilder.append(n1);
            }else{
                rd.reset();
                return stringBuilder.toString();
            }
        }
        return stringBuilder.toString();
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
