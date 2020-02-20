import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import requests.RequestParser;
// TODO remove logger after done
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
            String httpDirectory = new RequestParser(inputStream).getHttpDirectory();
            logger.info("Request for file from client: " + httpDirectory);
            if (httpDirectory.startsWith("/") && !httpDirectory.endsWith("/") && !httpDirectory.contains(".")) {
                this.responseHtml(this.rootPath, httpDirectory + "/index.html", outputStream);
                inputStream.close();
                socket.close();
            } else if (httpDirectory.startsWith("/") && !httpDirectory.contains(".")) {
                this.responseHtml(this.rootPath, httpDirectory + "index.html", outputStream);
                inputStream.close();
                socket.close();
            }else if (httpDirectory.endsWith(".htm") || httpDirectory.endsWith(".html")) {
                this.responseHtml(this.rootPath, httpDirectory, outputStream);
                inputStream.close();
                socket.close();
            } else if (httpDirectory.endsWith(".png") || httpDirectory.endsWith(".PNG")) {
                this.responseImage(rootPath, httpDirectory, outputStream);
                inputStream.close();
                socket.close();
            }
        } catch (IOException e) {
            logger.error("Cannot get the output streams: ", e);
        }
    }

    //TODO remove hardcode after impl GET
    private void responseImage(String rootPath, String fileName, OutputStream outputStream) throws IOException {
        BufferedOutputStream dataOut = new BufferedOutputStream(outputStream);
        Path path = Paths.get(rootPath, fileName);
        if (!Files.exists(path))
            logger.error("Cannot get the file does not exist");

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

    //TODO remove hardcode after impl GET
    private void responseHtml(String rootPath, String relativePath, OutputStream outputStream) throws IOException {
        Path path = Paths.get(rootPath, relativePath);
        if (!Files.exists(path))
            logger.error("Cannot get the file does not exist");
        String response =
                "HTTP/1.1 200 OK" + "\r\n" +
                        "Content-Length: " + Files.readAllBytes(path).length + "\r\n" +
                        "Content-Type: text/html" + "\r\n" +
                        "Content-Location: /src/main/resources/" + "\r\n" +
                        "\r\n";
        outputStream.write(response.getBytes());
        outputStream.write(Files.readAllBytes(path));
        outputStream.close();
        logger.info(relativePath + " sent.");
    }
}
