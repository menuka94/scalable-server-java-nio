package cs455.scaling.task;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * once every 20 seconds after starting up, every client should print the number of messages it has sent and received
 * during the last 20 seconds. This log message should look similar to the following.
 * [timestamp] Total Sent Count: x, Total Received Count: y
 */
public class ClientStats extends TimerTask {
    private static final Logger log = LogManager.getLogger(ClientStats.class);
    private final AtomicLong numMessagesSent;
    private final AtomicLong numMessagesReceived;

    public ClientStats(AtomicLong numMessagesSent, AtomicLong numMessagesReceived) {
        this.numMessagesSent = numMessagesSent;
        this.numMessagesReceived = numMessagesReceived;
    }

    @Override
    public void run() {
        log.info("Total Sent Count: " + numMessagesSent + ", Total Received Count: " + numMessagesReceived);
        numMessagesSent.set(0);
        numMessagesReceived.set(0);
    }
}
