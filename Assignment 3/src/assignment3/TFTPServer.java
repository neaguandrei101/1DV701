package assignment3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TFTPServer {
    public static final int TFTPPORT = 4970;
    public static final int BUFSIZE = 516;
    
//    /*Ruslan Location*/
//    public static final String READDIR = "/srv/tftp/"; //custom address at your PC
//    public static final String WRITEDIR = "/srv/tftp/"; //custom address at your PC

    public static final String READDIR = "/home/paperman/Desktop/TFPT/"; //custom address at your PC
    public static final String WRITEDIR = "/home/paperman/Desktop/TFPT/"; //custom address at your PC
    /* OP codes */
    public static final int OP_RRQ = 1;
    public static final int OP_WRQ = 2;
    public static final int OP_DAT = 3;
    public static final int OP_ACK = 4;
    public static final int OP_ERR = 5;

    public static void main(String[] args) {
        if (args.length > 0) {
            System.err.printf("usage: java %s\n", TFTPServer.class.getCanonicalName());
            System.exit(1);
        }
        //Starting the server
        try {
            TFTPServer server = new TFTPServer();
            server.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void start() throws SocketException {
        byte[] buf = new byte[BUFSIZE];

        // Create socket
        DatagramSocket socket = new DatagramSocket(null);

        // Create local bind point
        SocketAddress localBindPoint = new InetSocketAddress(TFTPPORT);
        socket.bind(localBindPoint);

        System.out.printf("Listening at port %d for new requests\n", TFTPPORT);

        // Loop to handle client requests
        while (true) {
            final InetSocketAddress clientAddress = receiveFrom(socket, buf);

            // If clientAddress is null, an error occurred in receiveFrom()
            if (clientAddress == null)
                continue;

            final StringBuffer requestedFile = new StringBuffer();
            final int reqtype = ParseRQ(buf, requestedFile);

            new Thread(() -> {
                try {
                    DatagramSocket sendSocket = new DatagramSocket(0);

                    // Connect to client
                    sendSocket.connect(clientAddress);

                    // Read request
                    if (reqtype == OP_RRQ) {
                        requestedFile.insert(0, READDIR);
                        HandleRQ(sendSocket, requestedFile.toString(), OP_RRQ);
                        System.out.println("REQUESTED FILE " + requestedFile);
                    }
                    // Write request
                    else if (reqtype == OP_WRQ) {
                        requestedFile.insert(0, WRITEDIR);
                        HandleRQ(sendSocket, requestedFile.toString(), OP_WRQ);
                    }
                    else {
                        System.err.println("Invalid request. Sending an error packet.");
                        send_ERROR(sendSocket, 0, "Invalid request.");
                    }
                    sendSocket.close();
                }  catch (SocketException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private InetSocketAddress receiveFrom(DatagramSocket socket, byte[] buf) {
    	/**
         * Reads the first block of data, i.e., the request for an action (read or write).
         *
         * @param socket (socket to read from)
         * @param buf    (where to store the read data)
         * @return socketAddress (the socket address of the client)
         */
    	// Create datagram packet
        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
        // Receive packet
        try {
            socket.receive(datagramPacket);
        } catch (IOException e) {
            System.err.println("Datagram receive error");
        }

        // Get client address and port from the packet
        return new InetSocketAddress(datagramPacket.getAddress(), datagramPacket.getPort());
    }

    
    private int ParseRQ(byte[] buf, StringBuffer requestedFile) {
    	/**
         * Parses the request in buf to retrieve the type of request and requestedFile
         *
         * @param buf           (received request)
         * @param requestedFile (name of file to read/write)
         * @return opcode (request type: RRQ or WRQ)
         */
    	
    	ByteBuffer byteBuffer = ByteBuffer.wrap(buf);
    	
    	final int opcodeSize = 2;
        int opcode = 0;

        // See "TFTP Formats" in TFTP specification for the RRQ/WRQ request contents
        if (buf.length > opcodeSize) {
            int lOpcode = byteBuffer.getShort();
            /*Convert first two bytes into an opcode integer*/
            if (lOpcode<5 && lOpcode >1) {
                opcode = lOpcode;
            }
            System.out.println("Received opCode = " + opcode);
            
            int endOfFileName = opcodeSize; // set endOfFileName to the begining of filename
          
            for (int i = endOfFileName; i < buf.length; i++)
                if (buf[i] == 0) {
                    endOfFileName = i;  // find closest 0 and set the end of the file there
                    break;
                }
            
            /*Insert file name into RequestedFile ByteBuffer*/
            requestedFile.insert(0, new String(buf, opcodeSize, endOfFileName - opcodeSize));
        }
        return opcode;
    }

    private void HandleRQ(DatagramSocket sendSocket, String requestedFile, int opcode) {
    	/**
         * Handles RRQ and WRQ requests
         *
         * @param sendSocket    (socket used to send/receive packets)
         * @param requestedFile (name of file to read/write)
         * @param opcode        (RRQ or WRQ)
         */
    	if (opcode == OP_RRQ) {
            // See "TFTP Formats" in TFTP specification for the DATA and ACK packet contents
//			ByteBuffer data = ByteBuffer.wrap(buf);
        	
        	/* IF file exists */
        	if (new File(requestedFile).exists()) {
        	
            	boolean result = send_DATA_receive_ACK(sendSocket, requestedFile);
            	System.out.printf("Result of handling RRQ: %b\n", result);
        	}
        	else {
        		/*File not found error*/
        		boolean result = send_ERROR(sendSocket, 1, ""); 
        		System.out.printf("Result of handling RRQ: %b\n", result);
        	}
            
        } else if (opcode == OP_WRQ) {
        	
        	if (!new File(requestedFile).exists()) {
        		boolean result = receive_DATA_send_ACK(sendSocket, requestedFile);
        		System.out.printf("Result of handling WRQ: %b\n", result);
        	}
        	else {
        		boolean result = send_ERROR(sendSocket, 6, ""); 
        		System.out.printf("Result of sending ERROR 6: %b\n", result);

        	}
        }
    }

    private boolean receive_DATA_send_ACK(DatagramSocket socket, String requestedFile)  {
    	/* Prepare buffer and array to store a file*/
    	byte [] file = new byte[0];
    	byte [] buf = new byte[BUFSIZE];
    	// Create datagram packet
    	DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);

    	/* the size of the block receved */
    	int blockSize = 0;
    	int blockCounter = 1;
    	/* SEND ACK FOR 0 BLOCK */
    	ackBlock(socket, 0);
    	
    	do {
        /*Receive packet */
        try {
        	socket.receive(datagramPacket);
        } catch (IOException e) {
            System.err.println("Datagram receive error");
        }
        	
    	blockSize = datagramPacket.getLength()-4;
    	ByteBuffer byteBuffer = ByteBuffer.wrap(buf);
    	
    	int opcode = byteBuffer.getShort();
    	int blockNumber = byteBuffer.getShort();

    	if (opcode == OP_DAT && blockCounter == blockNumber) {
	    	/* EXTEND THE FILE BY THE LENGTH OF THE BLOCK RECIEVED */
	    	int pointer = file.length;  // pointer to mark where to write next
	    	byte [] newFile = new byte [file.length + blockSize]; // current array + block recieved
	    	
	    	/* REWRITE THE CURRENT ARRAY */
	    	for (int i = 0; i<file.length; i++) 
	    		newFile[i] = file[i];
	    	file = newFile;
	    	
	    	/* ADD RECEIVED BLOCK TO THE CURRENT FILE ARRAY*/
	    	byteBuffer.get(file, pointer, blockSize);
	    	
	    	/* Send ack for the receved block*/
			if(ackBlock(socket, blockNumber)) {
                blockCounter++;
            }
    	}
    }
    	/* IF received data > 511 bytes - continue receiving */
    	while(blockSize > 511);
    	
    	/* WHEN DONE WRITE A FILE */
    	FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(requestedFile);
			
		} catch (FileNotFoundException e) {
				System.err.println("File " + requestedFile + " NOT AVAILABLE");
		}
		
	    try {
			outputStream.write(file);
			outputStream.close();
		} catch (IOException e) {
			System.err.println("ACCESS VIOLATION to FILE: " + requestedFile);
			send_ERROR(socket, 2, "");
			socket.close();
		}
		return true;
	}

	private boolean ackBlock(DatagramSocket socket, int blockNumber) {
    	/*
    	 * Send ACK for a blockNumber
    	 */
    	byte [] ack = new byte [4];
    	    	
		ByteBuffer byteBufferAck = ByteBuffer.wrap(ack); 
    	byteBufferAck.putShort((short) OP_ACK);
    	byteBufferAck.putShort((short) blockNumber);
    	
    	try {
    		socket.send(new DatagramPacket(ack, ack.length));
    		return true;
        } catch (IOException e) {
            System.err.printf("Cannot send ACK, exception: %s\n", e.getMessage());
        }
    	return false;
	}

	private boolean send_ERROR(DatagramSocket sendSocket, int errorCode, String errorMsg) {
    	String ErrorMsg = errorMsg;

    	if (errorCode == 1)
    		ErrorMsg = "File not found.";
    	
    	if (errorCode == 2)
    		ErrorMsg = "Access violation.";
    	
    	if (errorCode == 6)
    		ErrorMsg = "File already exists.";
    	
    	/* Preparing data error to send */
    	byte[] errorData = new byte[ErrorMsg.length() + 5];
    	ByteBuffer byteBuffer = ByteBuffer.wrap(errorData);
    	
    	// fill array with data
    	byteBuffer.putShort((short) OP_ERR);
    	byteBuffer.putShort((short) errorCode);
    	byteBuffer.put(ErrorMsg.getBytes());
    	byteBuffer.put((byte) 0);
    	
    	
    	/* Sending Error */
        try {
            sendSocket.send(new DatagramPacket(errorData, errorData.length));
        } catch (IOException e) {
            System.err.printf("Cannot send ERROR, exception: %s\n", e.getMessage());
            return false;
        }
    	
		return true;
	}

	private boolean send_DATA_receive_ACK(DatagramSocket sendSocket, String requestedFile) {
        /* take data from a file */
        Path path = Paths.get(requestedFile);
        byte[] fileData = null;
        
        try {
            fileData = Files.readAllBytes(path);
        } catch (IOException e) {
            System.err.printf("File not found, exception: %s\n", e.getMessage());
            return false;
        }

        //data chunks if the file is bigger than 512 bytes
        List<byte[]> chunks = splitToByteArrList(fileData);

        for (int i = 0; i < chunks.size(); i++) {
            byte[] internalByteArr = new byte[chunks.get(i).length + 4];
            ByteBuffer byteBuffer = ByteBuffer.wrap(internalByteArr);
            /* DATA TFTP */
            
            // opcode
            byteBuffer.putShort((short) OP_DAT);
            // block number
            byteBuffer.putShort((short) (i + 1));
            //data
            byteBuffer.put(chunks.get(i));

            int timeOutDuration = 1000;
            boolean ackReceived = false; 
            
            for (int j=0; j<6; j++) { // try to send 6 times
            	ackReceived = false; // by default no ack
                try {
                    sendSocket.send(new DatagramPacket(internalByteArr, internalByteArr.length));
                } catch (IOException e) {
                    System.err.printf("Cannot send DATA, exception: %s\n", e.getMessage());
                    return false;
                }
                
                //receive ACK from client
                byte[] buf = new byte[BUFSIZE];

                DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                // set timeout to to receive ACK from client
                try {
					sendSocket.setSoTimeout(timeOutDuration);
				} catch (SocketException e) {
					System.err.printf("Cannot set the timeout, exception: %s\n", e);
				}
                
                try {
					sendSocket.receive(datagramPacket);
				} catch (IOException e) {
                    System.err.printf("Cannot receive packet, exception: %s\n", e);
                    continue;
                }
            	
                /*check if ack is valid*/
                ackReceived = isAckValid(buf, i+1); // the proper ack is received. We can go to the next block
                if (ackReceived) /* ack is valid don't send it again - break out of J-loop*/  
                	break; // stop resending
                else{
                	/*Something is wrong with ack*/
                	/* IF timeout then continue sending trying but increase timeout length*/
                	timeOutDuration*=2;
                	continue;
                }
            }
            
            /*if byte is send and proper ack is received then send the next block*/
            if (ackReceived)
            	continue;
            else
            	/*ERROR!*/
            	return false;
        }
        /* wait for ack */
        return true;
    }

    //splits  into a list of byte[] if the source is bugger than 512 bytes
    private static List<byte[]> splitToByteArrList(byte[] source) {
        List<byte[]> result = new ArrayList<>();
        int start = 0;
        while (start < source.length) {
            int end = Math.min(source.length, start + 512);
            result.add(Arrays.copyOfRange(source, start, end));
            start += 512;
        }
        return result;
    }

    /*compares an ACK, returns true it they match*/
    private boolean isAckValid(byte[] buf, int blockNum) {
    	ByteBuffer byteBuffer = ByteBuffer.wrap(buf);
    	return (byteBuffer.getShort()==4 && byteBuffer.getShort()==blockNum);
    }
}