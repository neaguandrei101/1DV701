/*
  UDPEchoClient.java
  A simple echo client with no error handling
*/

import java.io.IOException;
import java.net.*;

public class UDPEchoClient extends NetworkLayer {

    public UDPEchoClient(String buffer, String ipAddress, String serverPort, String transmissionRate, String message) {
        super(buffer, ipAddress, serverPort, transmissionRate, message);
    }

    public static void main(String[] args) {
        UDPEchoClient echoClient = new UDPEchoClient(args[0], args[1], args[2], args[3], args[4]);

        /* Create socket */
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(null);
        } catch (SocketException e) {
            System.err.println("Cannot create the socket");
            System.exit(1);
        }

        try {
            socket.setSoTimeout(2000);
        } catch (SocketException e) {
            System.err.println("Cannot set the timeout");
            System.exit(1);
        }

        /* Create remote endpoint */
        SocketAddress remoteBindPoint = new InetSocketAddress(echoClient.serverIp, echoClient.serverPort);

        /* Create datagram packet for sending message */
        DatagramPacket sendPacket =
                new DatagramPacket(echoClient.myMessage.getBytes(),
                        echoClient.myMessage.length(),
                        remoteBindPoint);

        /* Create datagram packet for receiving echoed message */
        DatagramPacket receivePacket = new DatagramPacket(echoClient.buf, echoClient.buf.length);

        while (true) {

            UDPEchoClient.sendAndReceiveOneSec(socket, sendPacket, receivePacket,
                    echoClient.transmissionRate, echoClient.myMessage);

        }
    }

    public static void sendAndReceiveOneSec(DatagramSocket datagramSocket, DatagramPacket sendPacket,
                                            DatagramPacket receivePacket, int transmissionRate, String message) {
        final long RUNTIME = 1000;
        int packets = 0;
        long currentTime = System.currentTimeMillis();
        if (transmissionRate == 0 || transmissionRate == 1) {
            UDPEchoClient.sendAndReceive(datagramSocket, sendPacket, receivePacket, message);
            packets++;
        } else {
            while ((System.currentTimeMillis() < currentTime + RUNTIME) && (packets < transmissionRate)) {
                UDPEchoClient.sendAndReceive(datagramSocket, sendPacket, receivePacket, message);
                packets++;
                if (packets == transmissionRate)
                    break;
            }
        }
        System.out.printf("Number of messages sent in a second: %d out of %d || ", packets, transmissionRate);
        System.out.printf("Number of messages left: %d\n", transmissionRate - packets);
    }

    private static void sendAndReceive(DatagramSocket datagramSocket, DatagramPacket sendPacket,
                                       DatagramPacket receivePacket, String message) {
        try {
            datagramSocket.send(sendPacket);
        } catch (IOException e) {
            System.err.println("Cannot write!");
            System.exit(1);
        }
        try {
            datagramSocket.receive(receivePacket);
        } catch (IOException e) {
            System.err.println("Cannot read!");
            System.exit(1);
        }

        String receivedString = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
        if (!(receivedString.compareTo(message) == 0)) {
            System.err.print("Sent and received msg not equal!\n");
        }
    }
}