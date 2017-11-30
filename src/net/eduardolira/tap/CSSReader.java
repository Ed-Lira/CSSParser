package net.eduardolira.tap;

import java.io.EOFException;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

import static net.eduardolira.tap.CodePoint.*;

public class CSSReader extends PushbackReader{

    public CSSReader(Reader in) {
        super(in,20);
    }

    @Override @Deprecated
    public int read() throws IOException {
        return super.read();
    }

    public int readCodePoint() throws IOException {
        //todo this is debug!
        int out = readCodePointo();
        //System.out.println("    " + out);
        //if(out==-1) new Exception().printStackTrace();
        return out;
    }

    public int readCodePointo() throws IOException {
        int temp;
        temp = super.read();
        if(temp == -1) return -1;
        char n0 = (char) temp;

        //Replace pairs of U+000D CARRIAGE RETURN (CR) followed by U+000A LINE FEED (LF) with a single U+000A LINE FEED (LF) code point.
        //Replace of U+000D CARRIAGE RETURN (not followed by U+000A LINE FEED (LF)) with U+000A LINE FEED (LF) code
        if(n0 == CARRIAGE_RETURN) {
            temp = super.read();
            char n1 = (char) temp;
            if(temp == -1){
                return n0;
            }
            if (n1 == LINE_FEED){
                return LINE_FEED;
            }else{
                unread(n1);
                return LINE_FEED;
            }
        }

        //Replace U+000C FORM FEED with U+000A LINE FEED
        if(n0 == FORM_FEED) return LINE_FEED;
        //Replace any U+0000 NULL code point with U+FFFD REPLACEMENT CHARACTER
        if(n0 == NULL) return REPLACEMENT_CHARACTER;
        return n0;
    }
}
