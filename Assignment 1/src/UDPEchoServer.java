/*
  UDPEchoServer.java
  A simple echo server with no error handling
*/

import java.io.IOException;
import java.net.*;

public class UDPEchoServer extends NetworkLayer {

    public UDPEchoServer(String buffer, String serverPort) {
        super(buffer, serverPort);
    }

    public static void main(String[] args) {
        UDPEchoServer echoServer = new UDPEchoServer(args[0], args[1]);
        DatagramSocket socket = null;

        try {
            /* Create socket */
            socket = new DatagramSocket(null);
        } catch (SocketException e) {
            System.err.println("Cannot create socket.");
            System.exit(1);
        }

        /* Create local bind point */
        SocketAddress localBindPoint = new InetSocketAddress(echoServer.serverPort);
        try {
            socket.bind(localBindPoint);
        } catch (SocketException e) {
            System.err.println("Cannot bind to the port, the port is used. " + e.getMessage());
            System.exit(1);
        }
        while (true) {
            /* Create datagram packet for receiving message */
            DatagramPacket receivePacket = new DatagramPacket(echoServer.buf, echoServer.buf.length);

            /* Receiving message */
            try {
                socket.receive(receivePacket);
            } catch (IOException e) {
                System.err.println("Cannot receive the message.");
            }

            /* Create datagram packet for sending message */
            DatagramPacket sendPacket =
                    new DatagramPacket(receivePacket.getData(),
                            receivePacket.getLength(),
                            receivePacket.getAddress(),
                            receivePacket.getPort());

            /* Send message*/
            try {
                socket.send(sendPacket);
                System.out.printf("UDP echo request from %s", receivePacket.getAddress().getHostAddress());
                System.out.printf(" using port %d\n", receivePacket.getPort());
            } catch (IOException e) {
                System.err.println("Cannot send the message.");
            }
        }
    }
}