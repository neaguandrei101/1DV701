package requests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RequestParser {
    InputStream inputStream;

    public RequestParser(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    // reads the first line, use one
    public String getHttpGetMethod() throws IOException {
        var in = new BufferedReader(new InputStreamReader(inputStream));
        return in.readLine();
    }

    // reads the first line, use one
    public String getHttpGetFile() throws IOException {
        var in = new BufferedReader(new InputStreamReader(inputStream));
        String str = in.readLine();
        String[] words = str.split("\\s+");
        return words[1];
    }
}
