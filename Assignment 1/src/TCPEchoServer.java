import java.io.*;
import java.net.*;

public class TCPEchoServer extends NetworkLayer {

    public TCPEchoServer(String buffer, String serverPort) {
        super(buffer, serverPort);
    }

    public static void main(String[] args) {
        TCPEchoServer echoServer = new TCPEchoServer(args[0], args[1]);

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket();
            SocketAddress localBindPoint = new InetSocketAddress(echoServer.serverPort);
            serverSocket.bind(localBindPoint);
        } catch (IOException e) {
            System.err.println("Cannot start socket.");
        } finally {
            try {
                serverSocket.close();
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Cannot close the socket socket.");
            }
        }


        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println(String.format("Accepting connection from %s ", clientSocket.getInetAddress()));
                new EchoServerThread(clientSocket, echoServer.buf).start();
            } catch (IOException e) {
                System.err.println("Cannot accept connection.");
            } finally {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.err.println("Cannot close the socket socket.");
                }
            }
        }
    }

    private static class EchoServerThread extends Thread {
        private Socket clientSocket;
        private byte[] buf;

        public EchoServerThread(Socket clientSocket, byte[] buf) {
            this.clientSocket = clientSocket;
            this.buf = buf;
        }

        public void run() {
            while (true) {
                try {
                    InputStream inputStream = new DataInputStream(clientSocket.getInputStream());
                    OutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
                    int read = inputStream.read(buf);
                    if (read == -1) {
                        break;
                    }
                    String receivedMessage = new String(buf, 0, read);
                    System.out.println(receivedMessage);
                    outputStream.write(receivedMessage.getBytes());
                } catch (IOException e) {
                    System.err.println("Cannot read or write.");
                }
            }
        }
    }
}
