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

    /* this function is used for VG task 1
    I used Thread.sleep here because I fixed a couple bugs that I have uncovered in Problem 5
    Complete accuracy is not guaranteed to the second
    */
    private static void sendAndReceiveOneSec(DatagramSocket datagramSocket, DatagramPacket sendPacket,
                                            DatagramPacket receivePacket, int transmissionRate, String message) {
        final long RUNTIME = 1000;
        int packets = 0;
        long start = System.currentTimeMillis();
        if (transmissionRate == 0 || transmissionRate == 1) {
            UDPEchoClient.sendAndReceive(datagramSocket, sendPacket, receivePacket, message);
            packets++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.exit(1);
            }
        } else {
            while (packets < transmissionRate) {
                UDPEchoClient.sendAndReceive(datagramSocket, sendPacket, receivePacket, message);
                packets++;
                if(System.currentTimeMillis() - start < RUNTIME){
                    try {
                        Thread.sleep(RUNTIME / transmissionRate);
                    } catch (InterruptedException e) {
                        System.exit(1);
                    }
                }
                if(System.currentTimeMillis() > start + RUNTIME)
                    break;
                if (packets == transmissionRate) {
                    try {
                        Thread.sleep(start+ RUNTIME - System.currentTimeMillis());
                    } catch (InterruptedException e) {
                        System.exit(1);
                    }
                    break;
                }

            }
        }
        System.out.printf("Number of messages sent in a second: %d out of %d || ", packets, transmissionRate);
        System.out.printf("Number of messages left: %d\n", transmissionRate - packets);
    }

    //sends and receives a message one time
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
            System.err.println("Cannot read! The server might not be running");
            System.exit(1);
        }

        String receivedString = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
        if (!(receivedString.compareTo(message) == 0)) {
            System.err.print("Sent and received msg not equal!\n");
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