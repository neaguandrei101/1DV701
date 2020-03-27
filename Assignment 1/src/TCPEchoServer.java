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
            System.exit(1);
        }

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println(String.format("Accepting connection from %s ", clientSocket.getInetAddress()));
                new EchoServerThread(clientSocket, echoServer.buf).start();
            } catch (IOException e) {
                System.err.println("No connection.");
            }
        }
    }

    // private object that runs the threads
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
                    StringBuilder stringBuilder = new StringBuilder();
                    String receivedMessage;
                    int read;
                    do {
                        read = inputStream.read(buf, 0, buf.length);
                        if (read == -1) {
                            break;
                        }
                        receivedMessage = new String(buf, 0, read);
                        stringBuilder.append(receivedMessage);
                    } while (inputStream.available() > 0);

                    outputStream.write(stringBuilder.toString().getBytes());
                    if (read == -1) {
                        break;
                    }
                    System.out.printf("Message sent: %s\n", stringBuilder.toString());
                } catch (IOException e) {
                    System.err.println("Cannot read or write.");
                    break;
                }
            }
        }
    }

    @Override
    public void checkMessage() {
        if (super.myMessage.isEmpty()) {
            System.err.println("Message is not valid.");
            System.exit(1);
        }
    }
}
