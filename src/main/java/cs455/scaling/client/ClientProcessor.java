package cs455.scaling.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.LinkedBlockingQueue;

public class ClientProcessor extends Thread {
    private static final Logger log = LogManager.getLogger(ClientProcessor.class);

    private LinkedBlockingQueue<String> hashes;
    private LinkedBlockingQueue<String> receivedHashes;

    public ClientProcessor(LinkedBlockingQueue<String> hashes) {
        this.hashes = hashes;
        receivedHashes = new LinkedBlockingQueue<>();
    }

    public void addReceivedHash(String receivedHash) {
        receivedHashes.add(receivedHash);
    }

    @Override
    public void run() {
        log.info("Starting ClientProcessor");
        while (true) {
            try {
                String receivedHash = receivedHashes.take();
                if (hashes.contains(receivedHash)) {
                    log.info("Hash matched. Removing it...");
                    hashes.remove(receivedHash);
                } else {
                    log.warn("Received hash not found in sent hashes list");
                }
            } catch (InterruptedException e) {
                log.error("Error in checking received hashes");
                e.printStackTrace();
            }
        }
    }
}
