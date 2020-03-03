package cs455.scaling.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

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
    private static AtomicLong messagesSent;
    private static AtomicLong messagesReceived;
    private static LinkedBlockingQueue<String> hashes;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException,
            InterruptedException {
        hashes = new LinkedBlockingQueue<>();
        ClientProcessor clientProcessor = new ClientProcessor(hashes);
        clientProcessor.start();

        if (args.length != 3) {
            log.warn("Invalid arguments. Provide <server-host> <server-port> " +
                    "<message-rate>");
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

        messagesSent = new AtomicLong();
        messagesReceived = new AtomicLong();

        // Connect to the server
        socketChannel = SocketChannel.open(new InetSocketAddress(serverHost, serverPort));

        // Create buffer
        // buffer = ByteBuffer.allocate(256);

        // buffer = ByteBuffer.wrap("Please send this back to me.".getBytes());
        // socketChannel.write(buffer);
        // buffer.clear();
        // socketChannel.read(buffer);
        // String response = new String(buffer.array()).trim();
        // log.info("Server responded with: " + response);
        // buffer.clear();

        Random random = new Random();
        ByteBuffer byteBuffer = ByteBuffer.allocate(8192);
        while (true) {
            // an 8KB message
            byte[] message = new byte[8192];
            random.nextBytes(message);

            // prepare to message to send
            byteBuffer = ByteBuffer.wrap(message);
            socketChannel.write(byteBuffer);
            // log.info("Messages Sent: " + messagesSent.get());

            byteBuffer.clear();
            socketChannel.read(byteBuffer);
            String response = new String(byteBuffer.array()).trim();
            log.debug("Server responded with: " + response);
            messagesReceived.getAndIncrement();
            // log.info("Messages Received: " + messagesReceived.get());

            String hashedMessage = HashUtil.SHA1FromBytes(message);
            hashes.put(hashedMessage);
            clientProcessor.addReceivedHash(response);

            byteBuffer.clear();
            messagesSent.getAndIncrement();
            Thread.sleep(sleepTime);
        }
    }
}
