import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Predicate;

public class ServerThread extends Thread {
    static final Logger logger = LoggerFactory.getLogger(ServerThread.class);
    private Socket socket;
    private String rootPath;

    public ServerThread(Socket socket, String rootPath) {
        this.socket = socket;
        this.rootPath = rootPath;
    }

    @Override
    public void run() {
        try (OutputStream outputStream = socket.getOutputStream()) {
            if (rootPath.endsWith(".htm") || rootPath.endsWith(".html"))
                this.responseHtml(this.rootPath, outputStream);
            else if (rootPath.endsWith(".png") || rootPath.endsWith(".PNG"))
                this.responseImage(rootPath, outputStream);
        } catch (IOException e) {
            logger.error("Cannot send response : ", e);
        }
    }

    //TODO remove hardcode after impl input
    private void responseImage(String rootPath, OutputStream outputStream) throws IOException {
        Path path = Paths.get("/home/paperman/Documents/Repos/1DV701/Assignment 2/src/main/resources/", "banana.png");
        byte[] fileContent = Files.readAllBytes(path);

        String response = "HTTP/1.1 200" + "\r\n" +
                "Content-Length: " + fileContent.length+ "\r\n" +
                "Content-Type: image/png" + "\r\n" + "\r\n";
        outputStream.write(response.getBytes());
        outputStream.write(fileContent);
        logger.info(".png sent.");
    }

    //TODO remove hardcode after impl input
    private void responseHtml(String rootPath, OutputStream outputStream) throws IOException {
        Path path = Paths.get("/home/paperman/Documents/Repos/1DV701/Assignment 2/src/main/resources/", "test.html");
        StringBuilder sb = new StringBuilder();
        try (BufferedReader bufferedReader = Files.newBufferedReader(path)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            logger.error("Cannot read from file : ", e);
        }
        String result = sb.toString();
        Optional<String> opt = Optional.of(result).filter(Predicate.not(String::isEmpty));
        String html = opt.orElseThrow(() -> new RuntimeException("The file read is empty"));
        String response = "HTTP/1.1 200 OK" + "\r\n" +
                "Content-Length: " + html.getBytes().length + "\r\n" +
                "Content-Type: text/html" + "\r\n" +
                "Content-Location: /src/main/resources/" + "\r\n" +
                "\r\n" + "\r\n" +
                html;
        outputStream.write(response.getBytes());
        logger.info(".html sent.");
    }
}
