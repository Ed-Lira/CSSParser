package net.eduardolira.tap;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.function.Consumer;

import static net.eduardolira.tap.CodePoint.*;

public class CssParser {
    private static final HashMap<Character, Integer> charToSimpleTokenType = new HashMap<>();


    private CSSReader rd;
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
        this.rd = new CSSReader(new InputStreamReader(inputStream));
    }

    public void streamTokens(Consumer<Token> tokenConsumer) throws IOException {
        char codePoint;
        StringBuilder token = new StringBuilder();
        int temp;
        while ((temp = rd.readCodePoint())!=-1 && temp!=65535) {
            codePoint = (char) temp;
            if (isWhiteSpace(codePoint)) tokenConsumer.accept(handleWhitespace(token));
            else if (codePoint == QUOTATION_MARK) tokenConsumer.accept(handleString(token, QUOTATION_MARK));
            else if (codePoint == NUMBER_SIGN) tokenConsumer.accept(handleNumSign(token));
                //todo else if(codePoint == DOLLAR_SIGN) tokenConsumer.accept(handleDollarSign(token));
            else if (codePoint == APOSTROPHE) tokenConsumer.accept(handleString(token, APOSTROPHE));
                //else if(codePoint == HYPHEN_MINUS) tokenConsumer.accept(handleHyphenMinus(token));

                //else if(codePoint == FULL_STOP) tokenConsumer.accept(handleFullStop(token));
                //else if(codePoint == SOLIDUS) tokenConsumer.accept(handleSolidus(token));
                //else if(codePoint == LESS_THAN_SIGN) tokenConsumer.accept(handleLessThanSign(token));
            else if (isDigit(codePoint)) tokenConsumer.accept(consumeNumericToken(codePoint, token));
            else if (isNameStartCodePoint(codePoint)) tokenConsumer.accept(consumeAnIdentLike(codePoint, token));
            else if (charToSimpleTokenType.containsKey(codePoint)) {
                tokenConsumer.accept(new Token(charToSimpleTokenType.get(codePoint), Character.toString(codePoint)));
            }
        }
    }

    private Token handleHyphenMinus(StringBuilder token) {
        return null;
    }

    private Token consumeNumericToken(char codePoint, StringBuilder token) throws IOException {
        token.setLength(0);
        rd.unread(codePoint);
        CSSNumber cssnum = consumeNumber(token);
        return new Token(Token.NUMBER, cssnum.repr);
    }

    private CSSNumber consumeNumber(StringBuilder token) throws IOException {
        CSSNumber number = new CSSNumber();
        number.type = CSSNumber.TYPE_INTEGER;
        char n0 = (char) rd.readCodePoint();
        if (n0 == HYPHEN_MINUS || n0 == PLUS_SIGN) {
            token.append(n0);
        } else {
            rd.unread(n0);
        }

        while (true) {
            char np = (char) rd.readCodePoint();
            if (!isDigit(np)) {
                rd.unread(np);
                break;
            }
            token.append(np);
        }

        char n2 = (char) rd.readCodePoint();
        char n3 = (char) rd.readCodePoint();
        if (n2 == FULL_STOP && isDigit(n3)) {
            token.append(n2);
            token.append(n3);
            number.type = CSSNumber.TYPE_NUMBER;
            while (true) {
                char np = (char) rd.readCodePoint();
                if (!isDigit(np)) {
                    rd.unread(np);
                    break;
                }
                token.append(np);
            }
        }else if (n2 == LATIN_CAPITAL_LETTER_E || n2 == LATIN_SMALL_LETTER_E) {
            if (n3 == HYPHEN_MINUS || n3 == PLUS_SIGN) {
                char n4 = (char) rd.readCodePoint();
                if (isDigit(n4)) {
                    token.append(n2);
                    token.append(n3);
                    token.append(n4);
                    while (true) {
                        char np = (char) rd.readCodePoint();
                        if (!isDigit(np)) {
                            rd.unread(np);
                            break;
                        }
                        token.append(np);
                    }
                }
            } else if (isDigit(n3)) {
                token.append(n2);
                token.append(n3);
                while (true) {
                    char np = (char) rd.readCodePoint();
                    if (!isDigit(np)) {
                        rd.unread(np);
                        break;
                    }
                    token.append(np);
                }
            }
        }else{
            rd.unread(n3);
            rd.unread(n2);
        }
        //Convert repr to a number, and set the value to the returned value.
        number.repr = token.toString();
        return number;
    }

    private Token handleNumSign(StringBuilder token) throws IOException {
        char n1 = (char) rd.readCodePoint();
        char n2 = (char) rd.readCodePoint();
        char n3 = (char) rd.readCodePoint();
        if (isNameCodePoint(n1) || isValidEscape(n1, n2)) {
            rd.unread(n3);
            rd.unread(n2);
            rd.unread(n1);
            boolean wouldStartValidId = wouldStartValidIdentifier(n1, n2, n3);
            consumeAName(token);
            Token hashToken = new Token(Token.HASH, token.toString());
            if (wouldStartValidId) hashToken.setFlag(Token.FLAG_ID);
            return hashToken;
        } else {
            //Otherwise, return a <delim-token> with its value set to the current input code point.
            rd.reset();
            return new Token(Token.DELIM, Character.toString(n1));
        }
    }

    private boolean wouldStartValidIdentifier(char n0, char n1, char n2) {
        if (n0 == HYPHEN_MINUS) {
            if (isNameStartCodePoint(n1)) return true;
            return isValidEscape(n1, n2);
        } else if (isNameStartCodePoint(n0)) {
            return true;
        } else if (n0 == REVERSE_SOLIDUS) {
            return isValidEscape(n0, n1);
        }
        return false;
    }

    private boolean isValidEscape(char n0, char n1) {
        if (n0 != REVERSE_SOLIDUS) return false;
        return !isNewLine(n1);
    }

    private Token handleWhitespace(StringBuilder stringBuilder) throws IOException {
        stringBuilder.setLength(0);
        int in;
        while ((in = rd.readCodePoint()) != -1) {
            char character = (char) in;
            if (isWhiteSpace(character)) {
                stringBuilder.append(character);
            } else {
                rd.unread(character);
                break;
            }
        }
        return new Token(Token.WHITESPACE, stringBuilder.toString());
    }

    private Token handleString(StringBuilder stringBuilder, char quotes) throws IOException {
        stringBuilder.setLength(0);
        int in;
        while ((in = rd.readCodePoint()) != -1) {
            //todo REVERSE SOLIDUS;
            char character = (char) in;
            if (isNewLine(character)) {
                rd.unread(character);
                return new Token(Token.BAD_STRING, stringBuilder.toString());
            } else if (character == quotes) {
                return new Token(Token.STRING, stringBuilder.toString());
            } else {
                stringBuilder.append(character);
            }
        }
        return new Token(Token.STRING, stringBuilder.toString());
    }

    private Token consumeAnIdentLike(char n0, StringBuilder stringBuilder) throws IOException {
        rd.unread(n0);
        String name = consumeAName(stringBuilder);
        int temp = rd.readCodePoint();
        char n1 = (char) temp;
        if(name.equalsIgnoreCase("/--/URL TOKEN\\--\\") && n1 == LEFT_PARENTHESIS){
            return new Token(Token.URL, name);
            //todo consume a URL TOKEN;
        }else if(n0 == LEFT_PARENTHESIS){
            if(temp!=-1) rd.unread(n1);
            return new Token(Token.FUNCTION, name);
        }else{
            rd.unread(n1);
            return new Token(Token.IDENT, name);
        }
    }

    private String consumeAName(StringBuilder stringBuilder) throws IOException {
        stringBuilder.setLength(0);
        int in;
        boolean procede = true;
        while (procede) {
            procede = (in = rd.readCodePoint()) != -1;
            char n0 = (char) in;
            if (isNameCodePoint(n0)) {
                stringBuilder.append(n0);
                continue;
            }
            int in1 = rd.readCodePoint();
            char n1 = (char) in;
            if (isValidEscape(n0, n1)) {
                stringBuilder.append(n1);
            } else {
                rd.unread(in1);
                rd.unread(n0);
                return stringBuilder.toString();
            }
        }
        return stringBuilder.toString();
    }

}
