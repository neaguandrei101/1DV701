
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    private static int port ;
    private static String rootPath;

    public HttpServer(int port, String rootPath) {
        HttpServer.port = port;
        HttpServer.rootPath = rootPath;
    }

    public static void main(String[] args) {
        HttpServer httpServer = new HttpServer(Integer.parseInt(args[0]), args[1]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server socket started.");
            while (serverSocket.isBound() && !serverSocket.isClosed()) {
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    logger.error("Cannot accept : ", e);
                }
                logger.info("Connection started.");
                ServerThread serverThread = new ServerThread(socket, rootPath);
                serverThread.start();
            }
        } catch (IOException e) {
            logger.error("Port is taken: ", e);
        }
    }
}
