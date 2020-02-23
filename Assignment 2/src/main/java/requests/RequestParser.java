package requests;

import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

public class RequestParser {
    InputStream inputStream;

    public RequestParser(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /* returns a tuple with the first element containing the 200 OK directory
       and the second in the tuple an Optional containing the "Accept-Language:"
       optional header value, used for 403 Forbidden
     */
    public Triplet<String, Optional<String>, String> http200Parse() throws IOException {
        var in = new BufferedReader(new InputStreamReader(inputStream));
        String str = in.readLine();
        String[] words = str.split("\\s+");
        String acceptLanguage = null;
        for (String line = in.readLine(); !line.equals(""); line = in.readLine()) {
            if (line.startsWith("Accept-Language:")) {
                acceptLanguage = line;
                break;
            }
        }
        Optional<String> acceptLanguageOptional = Optional.ofNullable(acceptLanguage);
        String[] splitStr = null;
        String acceptLanguageValue = null;
        if (acceptLanguageOptional.isPresent()) {
            splitStr =acceptLanguageOptional.get().split("\\s+");
            acceptLanguageValue = splitStr[1];
        }
        acceptLanguageOptional = Optional.ofNullable(acceptLanguageValue);
        return new Triplet<>(words[1], acceptLanguageOptional, str);
    }
}
