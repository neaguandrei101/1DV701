package responses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResponseFactory {
    private static final Logger logger = LoggerFactory.getLogger(ResponseFactory.class);

    public static byte[] get404Bytes() {
        byte[] bytes = new byte[0];
        try {
            Path path = Paths.get("src/main/java/responses/folder/404.html");
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            logger.error("Cant read 404.html");
        }
        return bytes;
    }
}
