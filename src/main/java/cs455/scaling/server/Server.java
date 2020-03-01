package cs455.scaling.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;
import cs455.scaling.ThreadPoolManager;
import cs455.scaling.task.ReadAndRespond;
import cs455.scaling.task.Register;
import cs455.scaling.util.Batch;
import javafx.concurrent.Worker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * There is exactly one server node in the system.
 * The server node provides the following functions:
 * A. Accepts incoming network connections from the clients.
 * B. Accepts incoming traffic from these connections
 * C. Groups data from the clients together into batches
 * D. Replies to clients by sending back a hash code for each message received.
 * E. The server performs functions A, B, C, and D by relying on the thread pool.
 */
public class Server {
    private static final Logger log = LogManager.getLogger(Server.class);
    private static ThreadPoolManager threadPoolManager;
    private static Batch batch;

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 4) {
            log.warn("Invalid number of arguments. Provide <port-num> <thread-pool-size> " +
                    "<batch-size> <batch-time>");
            System.exit(1);
        }

        int portNum = 0;
        int threadPoolSize = 0;
        int batchSize = 0;
        int batchTime = 0;

        try {
            portNum = Integer.parseInt(args[0]);
            threadPoolSize = Integer.parseInt(args[1]);
            batchSize = Integer.parseInt(args[2]);
            batchTime = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            log.error(e.getStackTrace());
            log.info("Invalid arguments. Exiting ...");
            System.exit(1);
        }

        ThreadPoolManager threadPoolManager = new ThreadPoolManager(batchSize, batchTime);
        threadPoolManager.setCurrentBatch(new Batch());
        threadPoolManager.start();

        // start workers
        for (int i = 0; i < threadPoolSize; i++) {

        }
    }

}
