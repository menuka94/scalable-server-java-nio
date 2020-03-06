package cs455.scaling.client;

import cs455.scaling.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class ClientProcessor extends Thread {
    private static final Logger log = LogManager.getLogger(ClientProcessor.class);

    public AtomicLong numMessagesReceived;
    private SocketChannel socketChannel;
    private Selector selector;
    private LinkedBlockingQueue<String> messagesSent;

    public ClientProcessor(SocketChannel socketChannel, LinkedBlockingQueue<String> messagesSent,
                           AtomicLong numMessagesReceived) throws IOException {
        this.socketChannel = socketChannel;
        this.messagesSent = messagesSent;
        this.numMessagesReceived = numMessagesReceived;
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    @Override
    public void run() {
        log.info("Starting ClientProcessor");
        int matched = 0;
        int mismatched = 0;

        while (true) {
            try {
                selector.select();
            } catch (Throwable e)  {
                e.printStackTrace();
            }
            try {
                ByteBuffer byteBuffer = ByteBuffer.allocate(Constants.SHA1_DIGEST_SIZE);
                socketChannel.read(byteBuffer);
                String response = new String(byteBuffer.array());

                log.debug("Response: " + response);
                if (messagesSent.contains(response)) {
                    messagesSent.remove(response);
                    log.debug("Hash matched. Removing ...");
                    matched++;
                    numMessagesReceived.getAndIncrement();
                } else {
                    log.warn("Hash not found in sent messages");
                    mismatched++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            log.debug("Matched: " + matched);
            log.debug("Mismatched: " + mismatched);
        }
    }
}
