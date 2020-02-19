import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import requests.RequestParser;
// TODO remove logger after done
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
        try (OutputStream outputStream = socket.getOutputStream();
             InputStream inputStream = socket.getInputStream()) {
            String fileNameFromRequest = new RequestParser(inputStream).getHttpGetFile();
            logger.info("Request for file from client: " + fileNameFromRequest);
            if (fileNameFromRequest.endsWith(".htm") || fileNameFromRequest.endsWith(".html")) {
                this.responseHtml(this.rootPath, fileNameFromRequest, outputStream);
                inputStream.close();
                socket.close();
            } else if (fileNameFromRequest.endsWith(".png") || fileNameFromRequest.endsWith(".PNG")) {
                this.responseImage(rootPath, fileNameFromRequest, outputStream);
                inputStream.close();
                socket.close();
            }
        } catch (IOException e) {
            logger.error("Cannot get the output streams: ", e);
        }
    }

    //TODO add support for 1 file in directory
    private void responseImage(String rootPath, String fileName, OutputStream outputStream) throws IOException {
        BufferedOutputStream dataOut = new BufferedOutputStream(outputStream);
        Path path = Paths.get(rootPath, fileName);

        byte[] fileContent = Files.readAllBytes(path);
        String response = "HTTP/1.1 200" + "\r\n" +
                "Content-Length: " + fileContent.length + "\r\n" +
                "Content-Type: image/png" + "\r\n" + "\r\n";

        dataOut.write(response.getBytes(), 0, response.length());
        dataOut.flush();
        dataOut.write(fileContent, 0, fileContent.length);
        dataOut.flush();
        outputStream.close();
        logger.info(".png sent.");
    }

    //TODO add support for 1 file in directory
    private void responseHtml(String rootPath, String fileName, OutputStream outputStream) throws IOException {
        Path path = Paths.get(rootPath, fileName);

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
        outputStream.close();
        logger.info(".html sent.");
    }
}
