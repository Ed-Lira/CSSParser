package net.eduardolira.tap;

public class CodePoint {
    public static final char CARRIAGE_RETURN = 0x000D;
    public static final char FORM_FEED = 0x000C;
    public static final char LINE_FEED = 0x000A;
    public static final char CHARACTER_TABULATION = 0x0009;
    public static final char SPACE = 0x0020;
    public static final char NULL = 0x0000;
    public static final char REPLACEMENT_CHARACTER = 0xFFFD;
    public static final char QUOTATION_MARK = 0x0022;
    public static final char APOSTROPHE = 0x0027;
    public static final char SOLIDUS = 0x002F;
    public static final char REVERSE_SOLIDUS = 0x005C;
    public static final char NUMBER_SIGN = 0x0023;
    public static final char DOLLAR_SIGN = 0x0024;
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
    public static final char PLUS_SIGN = 0x002B;
    public static final char DIGIT_ZERO = 0x0030;
    public static final char DIGIT_NINE = 0x0039;
    public static final char FULL_STOP = 0x002E;
    public static final char LESS_THAN_SIGN = 0x003C;
    public static final char COMMERCIAL_AT = 0x0040;
    public static final char CIRCUMFLEX_ACCENT = 0x005E;
    public static final char LATIN_CAPITAL_LETTER_E = 0x0045;
    public static final char LATIN_SMALL_LETTER_E = 0x0065;

    public static boolean isNewLine(char codePoint) {
        return codePoint == LINE_FEED;
    }

    public static boolean isWhiteSpace(char codePoint) {
        if (isNewLine(codePoint)) return true;
        if (codePoint == CHARACTER_TABULATION) return true;
        return codePoint == SPACE;
    }

    public static boolean isNonAsciiCodePoint(char codePoint) {
        return codePoint >= 0x080;
    }

    public static boolean isNameStartCodePoint(char codePoint) {
        if (isLetter(codePoint)) return true;
        if (isNonAsciiCodePoint(codePoint)) return true;
        return codePoint == LOW_LINE;
    }

    public static boolean isNameCodePoint(char codePoint) {
        if (isNameStartCodePoint(codePoint)) return true;
        if (isDigit(codePoint)) return true;
        return codePoint == HYPHEN_MINUS;
    }

    public static boolean isDigit(char codePoint) {
        return codePoint >= DIGIT_ZERO && codePoint <= DIGIT_NINE;
    }

    public static boolean isLetter(char codePoint) {
        return isUpperCaseLetter(codePoint) || isLowerCaseLetter(codePoint);
    }

    public static boolean isLowerCaseLetter(char codePoint) {
        return (codePoint >= LATIN_SMALL_LETTER_A && codePoint <= LATIN_SMALL_LETTER_Z);
    }

    public static boolean isUpperCaseLetter(char codePoint) {
        return (codePoint >= LATIN_CAPITAL_LETTER_A && codePoint <= LATIN_CAPITAL_LETTER_Z);
    }
}
