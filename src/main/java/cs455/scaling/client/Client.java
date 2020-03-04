package cs455.scaling.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import cs455.scaling.util.Constants;
import cs455.scaling.util.HashUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Unlike the server node, there are multiple Clients (minimum of 100) in the system.
 * A client provides the following functionalities:
 * (1) Connect and maintain an active connection to the server.
 * (2) Regularly send data packets to the server. The payloads for these data packets are 8 KB and
 * the values for these bytes are randomly generated. The rate at which each connection will
 * generate packets is R per-second; include a Thread.sleep(1000/R) in the client which ensures
 * that you achieve the targeted production rate. The typical value of R is between 2-4.
 * (3) The client should track hashcodes of the data packets that it has sent to the server.
 * A server will acknowledge every packet that it has received by sending the computed hash code
 * back to the client.
 */
public class Client {
    private static final Logger log = LogManager.getLogger(Client.class);

    private static SocketChannel socketChannel;
    // private static ByteBuffer buffer;
    private static AtomicLong noOfMessagesSent;
    private static LinkedBlockingQueue<String> hashes;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException,
            InterruptedException {
        hashes = new LinkedBlockingQueue<>();

        if (args.length != 3) {
            log.warn("Invalid arguments. Provide <server-host> <server-port> <message-rate>");
            System.exit(1);
        }

        String serverHost = args[0];
        log.info("ServerHost: " + serverHost);
        int serverPort = 0;
        int messageRate = 0;
        try {
            serverPort = Integer.parseInt(args[1]);
            log.info("ServerPort: " + serverPort);
            messageRate = Integer.parseInt(args[2]);
            log.info("messageRate: " + messageRate);
        } catch (NumberFormatException e) {
            log.error(e.getStackTrace());
            log.info("Invalid arguments. Exiting ...");
            System.exit(1);
        }

        long sleepTime = 1000 / messageRate;

        noOfMessagesSent = new AtomicLong();

        // Connect to the server
        socketChannel = SocketChannel.open(new InetSocketAddress(serverHost, serverPort));

        ClientProcessor clientProcessor = new ClientProcessor(socketChannel, hashes);
        clientProcessor.start();

        Random random = new Random();
        ByteBuffer byteBuffer = ByteBuffer.allocate(Constants.MESSAGE_SIZE);
        while (true) {
            // an 8KB message
            byte[] message = new byte[Constants.MESSAGE_SIZE];
            random.nextBytes(message);

            // prepare message to send
            byteBuffer = ByteBuffer.wrap(message);
            socketChannel.write(byteBuffer);
            // log.info("Messages Sent: " + messagesSent.get());

            String hashedMessage = HashUtil.SHA1FromBytes(message);
            // log.info("Sent: " + hashedMessage);
            hashes.put(hashedMessage);

            noOfMessagesSent.getAndIncrement();
            if (noOfMessagesSent.get() % 100 == 0) {
                log.info("No. of Messages Sent:" + noOfMessagesSent.get());
            }

            Thread.sleep(sleepTime);
        }
    }

    public static long getNoOfSentMessages() {
        return noOfMessagesSent.get();
    }

    public static void resetNoOfMessagesSent() {
        noOfMessagesSent.set(0);
    }
}
