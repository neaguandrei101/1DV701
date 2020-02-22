package responses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResponseFactory {
    private static final Logger logger = LoggerFactory.getLogger(ResponseFactory.class);

    public static byte[] get404HtmlHeaderBytes() {
        String response = "HTTP/1.1 404 File Not Found" + "\r\n" + "\r\n";
        return response.getBytes();
    }

    public static byte[] get404HtmlMsgBytes() {
        byte[] bytes = new byte[0];
        try {
            Path path = Paths.get("src/main/java/responses/folder/404.html");
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            logger.error("Cant read 404.html");
        }
        return bytes;
    }

    public static byte[] get200HtmlHeaderBytes(String contentLength, String contentType) {
        String response = "HTTP/1.1 200 OK" + "\r\n" +
                "Content-Length: " + contentLength + "\r\n" +
                "Content-Type: "+contentType + "\r\n" + "\r\n";
        return response.getBytes();
    }

    public static byte[] get302HtmlHeaderBytes(String location) {
        String response = "HTTP/1.1 302 Found" + "\r\n" +
                "Location: " + location + "\r\n" + "\r\n";
        return response.getBytes();
    }

    public static byte[] get302HtmlMsgBytes() {
        byte[] bytes = new byte[0];
        try {
            Path path = Paths.get("src/main/java/responses/folder/302.html");
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            logger.error("Cant read 302.html");
        }
        return bytes;
    }

    public static byte[] get403HtmlHeaderBytes() {
        String response = "HTTP/1.1 403 Forbidden" + "\r\n" + "\r\n";
        return response.getBytes();
    }

    public static byte[] get403HtmlMsgBytes() {
        byte[] bytes = new byte[0];
        try {
            Path path = Paths.get("src/main/java/responses/folder/403.html");
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            logger.error("Cant read 403.html");
        }
        return bytes;
    }

    public static byte[] get500HtmlHeaderBytes() {
        String response = "HTTP/1.1 500 Internal Server Error" + "\r\n" + "\r\n";
        return response.getBytes();
    }

    public static byte[] get500HtmlMsgBytes() {
        byte[] bytes = new byte[0];
        try {
            Path path = Paths.get("src/main/java/responses/folder/500.html");
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            logger.error("Cant read 500.html");
        }
        return bytes;
    }


}
