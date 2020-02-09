import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

public class TCPEchoServer extends Thread {
    public static final int BUFSIZE = 1024;
    public static final int MYPORT = 4950;
    private byte[] buf;
    private Socket clientSocket;

    public TCPEchoServer(Socket clientSocket, byte[] buf) {
        this.buf = buf;
        this.clientSocket = clientSocket;
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
                String receivedMessage = new String(buf, 0, read).trim();
                System.out.println(receivedMessage);
                outputStream.write(receivedMessage.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        SocketAddress localBindPoint = new InetSocketAddress(MYPORT);
        serverSocket.bind(localBindPoint);
        byte[] buf = new byte[BUFSIZE];


        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println(String.format("Accepting connection from %s ", clientSocket.getInetAddress()));
            new TCPEchoServer(clientSocket, buf).start();
        }
    }
}
