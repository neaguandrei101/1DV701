import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import requests.RequestParser;
import responses.ResponseFactory;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
            String httpRequestDirectory = new RequestParser(inputStream).getHttpDirectory();
            logger.info("Request for file from client: " + httpRequestDirectory);
            if (httpRequestDirectory.startsWith("/") && !httpRequestDirectory.endsWith("/") && !httpRequestDirectory.contains(".")) {
                this.responseHtml(this.rootPath, httpRequestDirectory + "/index.html", outputStream);
                inputStream.close();
                socket.close();
            } else if (httpRequestDirectory.startsWith("/") && !httpRequestDirectory.contains(".")) {
                this.responseHtml(this.rootPath, httpRequestDirectory + "index.html", outputStream);
                inputStream.close();
                socket.close();
            } else if (httpRequestDirectory.endsWith(".htm") || httpRequestDirectory.endsWith(".html")) {
                if (httpRequestDirectory.endsWith(".htm")) {
                    String extensionRemoved = httpRequestDirectory.split("\\.")[0];
                    String redirect = extensionRemoved + ".html";
                    outputStream.write(ResponseFactory.get302HtmlHeaderBytes(redirect));
                    outputStream.flush();
                    outputStream.write(ResponseFactory.get302HtmlMsgBytes());
                    outputStream.close();
                } else {
                    this.responseHtml(this.rootPath, httpRequestDirectory, outputStream);
                    inputStream.close();
                    socket.close();
                }
            } else if (httpRequestDirectory.endsWith(".png") || httpRequestDirectory.endsWith(".PNG")) {
                if (httpRequestDirectory.endsWith(".PNG")) {
                    String extensionRemoved = httpRequestDirectory.split("\\.")[0];
                    String redirect = extensionRemoved + ".png";
                    outputStream.write(ResponseFactory.get302HtmlHeaderBytes(redirect));
                    outputStream.flush();
                    outputStream.write(ResponseFactory.get302HtmlMsgBytes());
                    outputStream.close();
                } else {
                    this.responseImage(rootPath, httpRequestDirectory, outputStream);
                    inputStream.close();
                    socket.close();
                }
            }
        } catch (IOException e) {
            logger.error("Cannot get the output streams: ", e);
        }
    }

    private void responseImage(String rootPath, String relativePath, OutputStream outputStream) throws IOException {
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

    private void responseHtml(String rootPath, String relativePath, OutputStream outputStream) throws IOException {
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

}
