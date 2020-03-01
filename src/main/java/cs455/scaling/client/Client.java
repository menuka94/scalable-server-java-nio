package cs455.scaling.client;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import cs455.scaling.Hash;

public class Client {
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("wrong arguments. Requires: serverHostname serverPort sendingRate");
            System.exit(1);
        }

        //know the server port
        String hostName = args[0];
        int port = Integer.parseInt(args[1]);
        //determine the rate of message sending and set a corresponding time for a thread to sleep to achieve that.
        int rate = Integer.parseInt(args[2]);
        int sleepTime = 1000 / rate;

        //use a queue to keep track of which hash needs to come from server back next
        BlockingQueue<byte[]> hashes = new LinkedBlockingQueue<>();

        //initialize the socketChannel that will talk to the server
        InetSocketAddress datAddr = new InetSocketAddress(hostName, port);
        SocketChannel datClient = SocketChannel.open(datAddr);

        //System.out.println("Creating and starting receiver thread");
        ClientReceiver receiver = new ClientReceiver(datClient, hashes);
        Thread recThread = new Thread(receiver);
        recThread.start();
        long statStartTime = System.currentTimeMillis();
        long sentCount = 0;

        Random rand = new Random();
        while (true) {
            //create random 8kb message
            byte[] message = new byte[8192];
            rand.nextBytes(message);
            ByteBuffer buffer = ByteBuffer.wrap(message);
            //send the message
            datClient.write(buffer);
            sentCount++;

            //add the hash to the hash checking queue
            byte[] hash = Hash.hash(message);
            //hashes.add(new BigInteger(1,hash).toString());
            //synchronized (hashes) {
            hashes.add(hash);
            //}
            //sleep for the right amount of time so that we achieve the rate we want
            Long currentTime = System.currentTimeMillis();
            if (currentTime - statStartTime > 20000) {
                System.out.println(java.time.LocalTime.now() + " Total Sent: " + sentCount + " Total Recieved: " + receiver.recCount);
                statStartTime = System.currentTimeMillis();
                sentCount = 0;
                receiver.recCount.set(0);
            }

            Thread.sleep(sleepTime);
        }
    }
}
