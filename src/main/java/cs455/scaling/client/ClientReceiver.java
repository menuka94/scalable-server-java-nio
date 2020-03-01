package cs455.scaling.client;

import cs455.scaling.util.Constants;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class ClientReceiver extends Thread {

    public AtomicLong recCount;
    private SocketChannel socketChannel;
    private LinkedBlockingQueue<byte[]> expectedHashes;

    public ClientReceiver(SocketChannel socketChannel, LinkedBlockingQueue<byte[]> expectedHashes) {
        this.socketChannel = socketChannel;
        this.expectedHashes = expectedHashes;
    }

    @Override
    public void run() {
        recCount = new AtomicLong(0);
        System.out.println("Starting receiver thread");

        byte[] expected;
        while (true) {
            try {
                ByteBuffer messageBuffer = ByteBuffer.allocate(Constants.SHA1_DIGEST_SIZE);

                int bytesRead = 0;
                while (bytesRead != Constants.SHA1_DIGEST_SIZE) {
                    bytesRead += socketChannel.read(messageBuffer);
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
