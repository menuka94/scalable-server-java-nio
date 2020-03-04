package cs455.scaling.client;

import cs455.scaling.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class ClientProcessor extends Thread {
    private static final Logger log = LogManager.getLogger(ClientProcessor.class);

    private final Selector selector;
    private AtomicLong noOfMessagesReceived;
    private SocketChannel socketChannel;
    private LinkedBlockingQueue<String> messagesSent;

    public ClientProcessor(Selector selector, SocketChannel socketChannel, LinkedBlockingQueue<String> messagesSent) {
        this.selector = selector;
        this.socketChannel = socketChannel;
        this.messagesSent = messagesSent;
    }

    @Override
    public void run() {
        noOfMessagesReceived = new AtomicLong(0);
        log.info("Starting ClientProcessor");
        int matched = 0;
        int mismatched = 0;

        while (true) {
            try {
                selector.select();
            } catch (IOException e) {
                log.error("Error in selector.select()");
                e.printStackTrace();
            }
            try {
                ByteBuffer byteBuffer = ByteBuffer.allocate(Constants.SHA1_DIGEST_SIZE);
                socketChannel.read(byteBuffer);
                String response = new String(byteBuffer.array());

                log.info("Response: " + response);
                if (messagesSent.contains(response)) {
                    messagesSent.remove(response);
                    // log.info("Hash matched. Removing ...");
                    matched++;
                } else {
                    log.warn("Hash not found in sent messages");
                    mismatched++;
                }
                noOfMessagesReceived.getAndIncrement();
            } catch (Exception e) {
                e.printStackTrace();
            }

            log.info("Matched: " + matched);
            log.info("Mismatched: " + mismatched);
        }
    }
}
