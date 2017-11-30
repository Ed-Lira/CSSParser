package net.eduardolira.tap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static String urlToRead = "https://www.reddit.com/r/chicago/stylesheet";
    public static void main(String[] args) throws IOException {

        CssParser parser = new CssParser();
        parser.setInputStream(fromFile("res/stylesheet.css"));
        parser.streamTokens((a)->{if (a.getTokenType()!=TokenType.WHITESPACE) System.out.println(a);});
    }

    public static InputStream fromWeb(String urlString) throws IOException {
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        return conn.getInputStream();
    }

    public static InputStream fromFile(String path) throws IOException {
        return Files.newInputStream(Paths.get(path));
    }
}
