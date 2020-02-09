import java.util.Arrays;

public abstract class NetworkLayer {
    protected int bufferSize;
    protected int serverPort;
    protected byte[] buf;
    protected int transmissionRate;
    protected String serverIp;
    protected String myMessage;

    //used by UDPEchoClient
    public NetworkLayer(String buffer, String ipAddress, String serverPort, String transmissionRate, String messageSize) {
        if (!NetworkLayer.checkBufferSize(buffer))
            throw new IllegalArgumentException(
                    String.format("Provide the correct buffer size in the java arguments. Your input %s", buffer));
        this.bufferSize = Integer.parseInt(buffer);
        this.buf = new byte[this.bufferSize];

        // Check the server IP and set it
        if (!NetworkLayer.checkIpAddress(ipAddress)) {
            throw new IllegalArgumentException(
                    String.format("Provide the correct ip address in the java arguments. Your input %s", ipAddress));
        }
        this.serverIp = ipAddress;

        // Check the server port and set it
        if (!NetworkLayer.checkPort(serverPort)) {
            throw new IllegalArgumentException(
                    String.format("Provide the correct port in the java arguments. Your input %s", serverPort)
            );
        }
        this.serverPort = Integer.parseInt(serverPort);

        if (!NetworkLayer.checkTransmissionRate(transmissionRate)) {
            throw new IllegalArgumentException(
                    String.format("Provide the correct transmission rate in the java arguments. Your input %s", transmissionRate)
            );
        }
        this.transmissionRate = Integer.parseInt(transmissionRate);

        this.myMessage = "a".repeat(Integer.parseInt(messageSize));
        if (this.myMessage.length() > this.bufferSize) {
            System.err.println("The message is too long for the current buffer, it will cause problems");
        }
    }
        //Used by TCPEchoClient
    public NetworkLayer(String buffer, String ipAddress, String serverPort, String messageSize) {
        //check buffer
        if (!NetworkLayer.checkBufferSize(buffer))
            throw new IllegalArgumentException(
                    String.format("Provide the correct buffer size in the java arguments. Your input %s", buffer));
        this.bufferSize = Integer.parseInt(buffer);
        this.buf = new byte[this.bufferSize];

        // Check the server IP and set it
        if (!NetworkLayer.checkIpAddress(ipAddress)) {
            throw new IllegalArgumentException(
                    String.format("Provide the correct ip address in the java arguments. Your input %s", ipAddress));
        }
        this.serverIp = ipAddress;

        // Check the server port and set it
        if (!NetworkLayer.checkPort(serverPort)) {
            throw new IllegalArgumentException(
                    String.format("Provide the correct port in the java arguments. Your input %s", serverPort)
            );
        }
        this.serverPort = Integer.parseInt(serverPort);

        this.myMessage = "a".repeat(Integer.parseInt(messageSize));
        if (this.myMessage.length() > this.bufferSize) {
            System.err.println("The message is too long for the current buffer, it will cause problems");
        }
    }

    /* Check buffer size and create a byte array using the buffer
       Used by TCPEchoServer
    */
    public NetworkLayer(String buffer, String serverPort) {
        if (!NetworkLayer.checkBufferSize(buffer))
            throw new IllegalArgumentException(
                    String.format("Provide the correct buffer size in the java arguments. Your input %s", buffer));
        this.bufferSize = Integer.parseInt(buffer);
        this.buf = new byte[this.bufferSize];

        // Check the server port and set it
        if (!NetworkLayer.checkPort(serverPort)) {
            throw new IllegalArgumentException(
                    String.format("Provide the correct port in the java arguments. Your input %s", serverPort)
            );
        }
        this.serverPort = Integer.parseInt(serverPort);
    }


    //The turn rate must be a positive integer and be parsable to an int
    public static boolean checkTransmissionRate(String rateArgument) {
        int turnRate;
        try {
            turnRate = Integer.parseInt(rateArgument);

        } catch (NumberFormatException e) {
            return false;
        }
        return turnRate >= 0;
    }

    /*Port is port < 65535 && port >= 0
      If the string is not an integer it is invalid
     */
    public static boolean checkPort(String portArgument) {
        int port;
        try {
            port = Integer.parseInt(portArgument);
        } catch (NumberFormatException e) {
            return false;
        }
        return port < 65535 && port >= 0;
    }

    /*This method validates if the IP4 address is valid
      It also accepts localhost as an IP, used for development
     */
    public static boolean checkIpAddress(String ip) {
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

    /*Maximum size for IPv4 data gram  is 65535 bytes
     20 bytes IPv4 header, which is the maximum
     therefore my buffer size would be 65515
    */
    public static boolean checkBufferSize(String bufferSize) {
        int buffer;
        try {
            buffer = Integer.parseInt(bufferSize);
        } catch (NumberFormatException e) {
            return false;
        }
        return buffer > 0 && buffer <= 65515;
    }
}
