package cs455.scaling.client;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import cs455.scaling.util.Constants;
import cs455.scaling.util.HashUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Client {
    private static final Logger log = LogManager.getLogger(Client.class);

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            log.warn("Invalid arguments. Provide <server-host> <server-port> " +
                    "<message-rate>");
            System.exit(1);
        }

        String serverHost = args[0];
        int serverPort = Integer.parseInt(args[1]);
        int messageRate = Integer.parseInt(args[2]);
        int sleepTime = 1000 / messageRate;

        //use a queue to keep track of which hash needs to come from server back next
        LinkedBlockingQueue<byte[]> hashes = new LinkedBlockingQueue<>();

        //initialize the socketChannel that will talk to the server
        InetSocketAddress datAddr = new InetSocketAddress(serverHost, serverPort);
        SocketChannel datClient = SocketChannel.open(datAddr);

        ClientReceiver receiver = new ClientReceiver(datClient, hashes);
        receiver.start();
        long statStartTime = System.currentTimeMillis();
        long sentCount = 0;

        Random rand = new Random();
        while (true) {
            //create random 8kb message
            byte[] message = new byte[Constants.MESSAGE_SIZE];
            rand.nextBytes(message);
            ByteBuffer buffer = ByteBuffer.wrap(message);
            //send the message
            datClient.write(buffer);
            sentCount++;

            byte[] hash = HashUtil.hash(message);
            hashes.add(hash);
            long currentTime = System.currentTimeMillis();
            if (currentTime - statStartTime > 20000) {
                System.out.println(java.time.LocalTime.now() + " Total Sent: " + sentCount + " Total Received: " + receiver.recCount);
                statStartTime = System.currentTimeMillis();
                sentCount = 0;
                receiver.recCount.set(0);
            }
            Thread.sleep(sleepTime);
        }
    }
}
