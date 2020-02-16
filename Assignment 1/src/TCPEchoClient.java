import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class TCPEchoClient extends NetworkLayer {
    public Socket socket;

    public TCPEchoClient(String buffer, String ipAddress, String serverPort, String messageSize) {
        super(buffer, ipAddress, serverPort, messageSize);
    }

    public static void main(String[] args) {
        TCPEchoClient echoClient = new TCPEchoClient(args[0], args[1], args[2], args[3]);
        echoClient.socket = new Socket();
        SocketAddress address = new InetSocketAddress(echoClient.serverIp, echoClient.serverPort);
        try {
            echoClient.socket.connect(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream readStream = null;
        OutputStream writeStream = null;
        try {
            readStream = echoClient.socket.getInputStream();
            writeStream = echoClient.socket.getOutputStream();
        } catch (IOException e) {
            System.err.println("Cannot connect to the server.");
            System.exit(1);
        }
        while (true) {
            int read = 0;
            try {
                writeStream.write(echoClient.myMessage.getBytes(), 0, echoClient.myMessage.length());
                read = readStream.read(echoClient.buf);
            } catch (IOException e) {
                System.err.println("Cannot read or write.");
            }
            if (!(read >= 0)) {
                break;
            }
            String receivedMsg = new String(echoClient.buf, 0, read);

            if (receivedMsg.compareTo(echoClient.myMessage) == 0) {
                System.out.println("Correct message sent and received.");
            } else {
                System.err.println("Wrong message");

            }
        }
    }
}