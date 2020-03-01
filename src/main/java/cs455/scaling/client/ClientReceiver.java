package cs455.scaling.client;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class ClientReceiver implements Runnable {

    public AtomicLong recCount;
    private SocketChannel myChannel;
    private BlockingQueue<byte[]> expectedHashes;

    public ClientReceiver(SocketChannel myChannel, BlockingQueue<byte[]> expectedHashes) {
        this.myChannel = myChannel;
        this.expectedHashes = expectedHashes;
    }

    public void run() {
        recCount = new AtomicLong(0);
        System.out.println("Starting receiver thread");

        byte[] expected;
        while (true) {
            try {
                //sha1 hashes are 160 bits long so we only need to allocate 20 bytes
                ByteBuffer messageBuffer = ByteBuffer.allocate(20);

                int bytesRead = 0;
                while (bytesRead != 20) {
                    bytesRead += myChannel.read(messageBuffer);
                }
                byte[] hash = messageBuffer.array();

                expected = expectedHashes.take();

                String hashString = new BigInteger(1, hash).toString();
                String expectedString = new BigInteger(1, expected).toString();

                if (expectedString.equals(hashString)) {
                    recCount.getAndIncrement();
                } else {
                    System.out.println("Mismatched hash: " + expectedString + " : " + hashString);

                    byte[] checkMessageDrop = expectedHashes.peek();
                    String checkDrop = new BigInteger(1, checkMessageDrop).toString();
                    if (checkDrop.equals(hashString)) {
                        System.out.println("Message was dropped somewhere and did not return: " + expectedString);
                        expectedHashes.take();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
