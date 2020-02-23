import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import requests.RequestParser;
import responses.ResponseFactory;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class ServerThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ServerThread.class);
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
            Triplet<String, Optional<String>, String> header = new RequestParser(inputStream).http200Parse();
            if(!header.getValue2().contains("GET")) {
                this.response500(outputStream);
                inputStream.close();
            }
            // only english allowed
            // replace the if statement argument with true to test with morgan's tests
            if (header.getValue1().isPresent() && header.getValue1().get().contains("en-US")) {
                String http200RequestDirectory = header.getValue0();
                logger.info("Request for file from client: " + http200RequestDirectory);
                if (http200RequestDirectory.startsWith("/") && !http200RequestDirectory.endsWith("/") && !http200RequestDirectory.contains(".")) {
                    this.response200Html(this.rootPath, http200RequestDirectory + "/index.html", outputStream);
                    inputStream.close();
                    socket.close();
                } else if (http200RequestDirectory.startsWith("/") && !http200RequestDirectory.contains(".")) {
                    this.response200Html(this.rootPath, http200RequestDirectory + "index.html", outputStream);
                    inputStream.close();
                    socket.close();
                } else if (http200RequestDirectory.endsWith(".htm") || http200RequestDirectory.endsWith(".html")) {
                    if (http200RequestDirectory.endsWith(".htm")) {
                        this.response302(http200RequestDirectory, outputStream, ".html");
                    } else {
                        this.response200Html(this.rootPath, http200RequestDirectory, outputStream);
                        inputStream.close();
                        socket.close();
                    }
                } else if (http200RequestDirectory.endsWith(".png") || http200RequestDirectory.endsWith(".PNG")) {
                    if (http200RequestDirectory.endsWith(".PNG")) {
                        this.response302(http200RequestDirectory, outputStream, ".png");
                    } else {
                        this.response200Image(rootPath, http200RequestDirectory, outputStream);
                        inputStream.close();
                        socket.close();
                    }
                }
            } else if (header.getValue1().isEmpty() || !header.getValue1().get().contains("en-US")){
            this.response403(outputStream);
            } else {
                this.response500(outputStream);
            }
        } catch (IOException e) {
            logger.error("Cannot get the streams: ", e);
        }

    }

    private void response200Image(String rootPath, String relativePath, OutputStream outputStream) throws IOException {
        BufferedOutputStream dataOut = new BufferedOutputStream(outputStream);
        Path path = Paths.get(rootPath, relativePath);
        if (!Files.exists(path)) {
            dataOut.write(ResponseFactory.get404HtmlHeaderBytes());
            dataOut.flush();
            dataOut.write(ResponseFactory.get404HtmlMsgBytes());
            logger.info("404 Error sent");
            dataOut.close();
        } else {
            byte[] messageBytes = Files.readAllBytes(path);
            byte[] headerBytes = ResponseFactory.get200HtmlHeaderBytes(String.valueOf(messageBytes.length), "image/png");
            dataOut.write(headerBytes);
            dataOut.flush();
            dataOut.write(messageBytes);
            outputStream.close();
            logger.info(relativePath + " sent");
        }
    }

    private void response200Html(String rootPath, String relativePath, OutputStream outputStream) throws IOException {
        BufferedOutputStream dataOut = new BufferedOutputStream(outputStream);
        Path path = Paths.get(rootPath, relativePath);
        if (!Files.exists(path)) {
            dataOut.write(ResponseFactory.get404HtmlHeaderBytes());
            dataOut.flush();
            dataOut.write(ResponseFactory.get404HtmlMsgBytes());
            dataOut.close();
        } else {
            byte[] messageBytes = Files.readAllBytes(path);
            byte[] headerBytes = ResponseFactory.get200HtmlHeaderBytes(String.valueOf(messageBytes.length), "text/html");
            outputStream.write(headerBytes);
            outputStream.write(messageBytes);
            outputStream.close();
            logger.info(relativePath + " sent");
        }
    }

    private void response302(String httpRequestDirectory, OutputStream outputStream, String redirectNewExtension) throws IOException {
        String extensionRemoved = httpRequestDirectory.split("\\.")[0];
        String redirect = extensionRemoved + redirectNewExtension;
        outputStream.write(ResponseFactory.get302HtmlHeaderBytes(redirect));
        outputStream.flush();
        outputStream.write(ResponseFactory.get302HtmlMsgBytes());
        outputStream.close();
    }

    private void response403(OutputStream outputStream) throws IOException{
        outputStream.write(ResponseFactory.get403HtmlHeaderBytes());
        outputStream.flush();
        outputStream.write(ResponseFactory.get403HtmlMsgBytes());
        outputStream.close();
        logger.info("403 response sent");
    }

    private void response500(OutputStream outputStream) throws IOException{
        outputStream.write(ResponseFactory.get500HtmlHeaderBytes());
        outputStream.flush();
        outputStream.write(ResponseFactory.get500HtmlMsgBytes());
        outputStream.close();
        logger.error("500 response sent, crash");
    }

}
