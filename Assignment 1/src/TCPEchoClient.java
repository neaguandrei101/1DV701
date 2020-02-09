import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

public class TCPEchoClient extends NetworkLayer {
    public Socket socket;

    public TCPEchoClient(String buffer, String ipAddress, String serverPort, String messageSize) {
        super(buffer, ipAddress, serverPort, messageSize);
    }

    public static void main(String[] args) {
        TCPEchoClient echoClient = new TCPEchoClient(args[0], args[1], args[2], args[3]);

        echoClient.socket = new Socket();
        SocketAddress serverAddress = new InetSocketAddress(echoClient.serverIp, echoClient.serverPort);

        try {
            echoClient.socket.setSoTimeout(2000);
        } catch (SocketException e) {
            System.err.println("Cannot set the timeout.");
        }
        try {
            echoClient.socket.connect(serverAddress);
        } catch (SocketException e) {
            System.err.print("Timeout reached, " + e.getMessage());
            System.exit(1);
        } catch (IOException ex) {
            System.err.println("Cannot start socket.");
        } finally {
            try {
                echoClient.socket.close();
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Cannot close the socket socket.");
            }
        }
        InputStream readStream = null;
        OutputStream writeStream = null;
        try {
            readStream = echoClient.socket.getInputStream();
            writeStream = echoClient.socket.getOutputStream();
        } catch (IOException e) {
            System.err.println("Cannot get the input or output stream.");
        }
        while (true) {
            try {
                writeStream.write(echoClient.myMessage.getBytes(), 0, echoClient.myMessage.length());
            } catch (IOException e) {
                System.err.println("Cannot write!");
                System.exit(1);
            }
            int read = 0;
            try {
                read = readStream.read(echoClient.buf);
            } catch (IOException e) {
                System.err.println("Cannot read!");
                System.exit(1);
            }

            String receivedMsg = new String(echoClient.buf, 0, read);
            if (receivedMsg.compareTo(echoClient.myMessage) == 0) {
                System.out.println("Correct message sent and received.");
            } else {
                System.err.println("Wrong message sent and received.");
            }
        }
    }
}
