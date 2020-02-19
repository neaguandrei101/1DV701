import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
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
            logger.info("Response sent.");
        } catch (IOException e) {
            logger.error("Cannot send response : ", e);
        }
    }

    private void responseImage(String rootPath, OutputStream outputStream) throws IOException {
        File file = new File(rootPath);
        byte[] fileContent = new byte[0];
        try {
            fileContent = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            logger.error("Cannot read from file : ", e);
        }
        String response = "HTTP/1.1 200 OK" + "\r\n" +                //Status line
                "Content-Length: " + fileContent.length + "\r\n" +    //HTTP headers
                "Content-Type: image/png" +
                "Content-Location: /src/main/resources/" + "\r\n" +
                "\r\n" + "\r\n";
        outputStream.write(response.getBytes());
        outputStream.write(fileContent);                              //Data block

    }

    private void responseHtml(String rootPath, OutputStream outputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (FileReader fileReader = new FileReader(rootPath);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
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
    }
}
