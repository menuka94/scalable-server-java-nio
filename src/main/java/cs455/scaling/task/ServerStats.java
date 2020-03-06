package cs455.scaling.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Every 20 seconds, the server should print its current throughput (number of messages processed per second during last 20 seconds),
 * the number of active client connections, and mean and standard deviation of per-client throughput to the console.
 * In order to calculate the per-client throughput statistics (mean and standard deviation), you need to maintain the
 * throughputs for individual clients for last 20 seconds (number of messages processed per second sent by a particular
 * client during last 20 seconds) and calculate the mean and the standard deviation of those throughput values.
 * This message should look like the following.
 * <p>
 * [timestamp] Server Throughput: x messages/s, Active Client Connections: y, Mean Per- client Throughput: p messages/s,
 * Std. Dev. Of Per-client Throughput: q messages/s
 */
public class ServerStats extends TimerTask {
    private static final Logger log = LogManager.getLogger(ServerStats.class);
    private ArrayList<Double> throughputs;

    @Override
    public void run() {
        AtomicInteger numClients = Register.getNumClients();
        AtomicLong numMessagesProcessed = ReadAndRespond.getNumMessagesProcessed();
        double serverThroughput = numMessagesProcessed.get()/20.0;
        throughputs = new ArrayList<>();

        log.info("Server Throughput: " + serverThroughput + " messages/s, " +
                "Active Client Connections: " + numClients.get() + ", " +
                "Mean Per-client Throughput: " + " messages/s, " +
                "Std. Dev. Of Per-client Throughput: " + " messages/s");

        ReadAndRespond.resetNumMessagesProcessed();
    }

}
