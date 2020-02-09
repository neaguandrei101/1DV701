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

    public static void main(String[] args) throws IOException{
        TCPEchoClient echoClient = new TCPEchoClient(args[0], args[1], args[2], args[3]);
        echoClient.socket = new Socket();
        SocketAddress address = new InetSocketAddress(echoClient.serverIp, echoClient.serverPort);
        try {
            echoClient.socket.connect(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream readStream = echoClient.socket.getInputStream();
        OutputStream writeStream = echoClient.socket.getOutputStream();
        while(true) {
            writeStream.write(echoClient.myMessage.getBytes(), 0, echoClient.myMessage.length());

            int read = readStream.read(echoClient.buf);
            String receivedMsg = new String(echoClient.buf, 0, read);

            if (receivedMsg.compareTo(echoClient.myMessage) == 0) {
                System.out.println("correct message sent and received.");
            }
        }
    }
}
