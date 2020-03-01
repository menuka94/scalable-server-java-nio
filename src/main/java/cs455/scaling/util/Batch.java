package cs455.scaling.util;

import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import cs455.scaling.client.ClientData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Batch {
    private static final Logger log = LogManager.getLogger(Batch.class);

    private LinkedList<ClientData> clientDataList;
    private HashSet<SocketChannel> socketChannels;
    private AtomicLong count;
    private Semaphore addingSemaphore;

    public Batch() {
        clientDataList = new LinkedList<>();
        socketChannels = new HashSet<>();
        count = new AtomicLong(0);
        addingSemaphore = new Semaphore(1000);
    }

    public long getCount() {
        return count.get();
    }

    public void addMessage(byte[] message, SocketChannel clientSocketChannel) {
        if (!socketChannels.contains(clientSocketChannel)) {
            try {
                addingSemaphore.acquire(1000);
                if (!socketChannels.contains(clientSocketChannel)) {
                    ClientData clientData = new ClientData(clientSocketChannel);
                    socketChannels.add(clientSocketChannel);
                    clientDataList.add(clientData);
                }
            } catch (InterruptedException e) {
                log.error("Error in adding new message.");
                e.printStackTrace();
            } finally {
                addingSemaphore.release(1000);
            }
        }

        boolean found = true;

        try {
            addingSemaphore.acquire();
            for (ClientData clientData : clientDataList) {
                if (clientData.getSocketChannel().equals(clientSocketChannel)) {
                    clientData.addMessage(message);
                    count.getAndIncrement();
                }
                found = false;
                break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            addingSemaphore.release();
        }

        if (!found) {
            log.info("Did not find clientSocketChannel");
        }
    }

    public void process() {
        // compute hashes and send back to each client
        for (ClientData clientData : clientDataList) {
            clientData.sendMessages();
        }
    }
}
