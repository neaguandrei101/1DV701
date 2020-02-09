/*
  UDPEchoClient.java
  A simple echo client with no error handling
*/

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class UDPEchoClient {
    public static final int BUFSIZE = 1024;
    public static final int MYPORT = 0;
    public static final String MSG = "a".repeat(1000);

    public static final int TRNRATE = 5;

    public static void main(String[] args) {
        byte[] buf = new byte[BUFSIZE];
        String serverIP = args[0];
        int serverPort = Integer.parseInt(args[1]);

        if (!isValidInet4Address(serverIP)) {
            throw new RuntimeException("Invalid IP address");
        }

        if (buf.length < MSG.length()) {
            throw new RuntimeException("The message is too long for the buffer.");
        }

        /* Create socket */
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(null);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        /* Create local endpoint using bind() */
        SocketAddress localBindPoint = new InetSocketAddress(MYPORT);
        try {
            socket.bind(localBindPoint);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        /* Create remote endpoint */
        SocketAddress remoteBindPoint = new InetSocketAddress(serverIP, serverPort);

        /* Create datagram packet for sending message */
        DatagramPacket sendPacket =
                new DatagramPacket(MSG.getBytes(),
                        MSG.length(),
                        remoteBindPoint);

        /* Create datagram packet for receiving echoed message */
        DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);

        while (true) {
            UDPEchoClient.sendAndReceiveOneSec(socket, sendPacket, receivePacket);
        }
//        while (true) {
//            for (int i = 0; i <= TRNRATE; i++) {
//                UDPEchoClient.sendAndReceive(socket, sendPacket, receivePacket);
//            }
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
        //socket.close();
    }

    public static void sendAndReceive(DatagramSocket datagramSocket, DatagramPacket sendPacket, DatagramPacket receivePacket) {
        try {
            datagramSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            datagramSocket.receive(receivePacket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String receivedString = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
//        if (receivedString.compareTo(MSG) == 0)
//            System.out.printf("%d bytes sent and received\n", receivePacket.getLength());
//        else
//            System.out.printf("Sent and received msg not equal!\n");
    }

    public static void sendAndReceiveOneSec(DatagramSocket datagramSocket, DatagramPacket sendPacket, DatagramPacket receivePacket) {
        final long RUNTIME = 1000;
        int messages = 0;
        long time = System.currentTimeMillis();
        while (System.currentTimeMillis() < time + RUNTIME) {
            UDPEchoClient.sendAndReceive(datagramSocket, sendPacket, receivePacket);
            messages++;
        }
        System.out.printf("Number of messages sent and received %d\n", messages);
    }

    //fixed valid implementation
    public static boolean isValidInet4Address(String ip) {
        if (ip.compareTo("localhost") == 0) {
            return true;
        }
        String[] groups = ip.split("\\.");
        try {
            return Arrays.stream(groups)
                    .filter(s -> s.length() > 1 && s.length() < 4)
                    .map(Integer::parseInt)
                    .filter(i -> (i >= 0 && i <= 255))
                    .count() == 4;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}