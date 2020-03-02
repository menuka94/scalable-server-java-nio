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
            log.warn("Invalid arguments. Provide <port-num> <thread-pool-size> " +
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

        threadPoolManager = new ThreadPoolManager(threadPoolSize, batchSize, batchTime);
        threadPoolManager.start();
        threadPoolManager.startWorkers();

        // Open the selector
        Selector selector = Selector.open();

        // Create input channel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress("localhost", portNum));

        // Register channel to the selector
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        // serverSocketChannel.register(selector, serverSocketChannel.validOps());

        // Loop on selector
        while (true) {
            // log.info("Listening for new connections or messages");

            // block until one or more channels have activity
            selector.select();
           // log.info("\tActivity on selector!");

            // get keys that have activity
            Set<SelectionKey> selectedKeys = selector.selectedKeys();

            // loop over keys
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();

                if (!key.isValid()) {
                    log.warn("Invalid key");
                    continue;
                }

                // New connection on serverSocketChannel
                if (key.isAcceptable()) {
                    if (key.attachment() == null) {
                        log.info("\tRegister");
                        // selector.wakeup();
                        key.attach(42); // attach random, not-null object
                        Register register = new Register(selector, serverSocketChannel, key);
                        register.execute(); // need to do the registration at once without adding to the queue
                    } else {
                        log.info("\tAlready registered");
                    }
                }

                // Previous connection has data to read
                if (key.isReadable()) {
                    if (key.attachment() == null) {
                        key.attach(43);
                        log.info("\tReadAndRespond");
                        ReadAndRespond readAndRespond = new ReadAndRespond(key);
                        threadPoolManager.addTask(readAndRespond);
                    } else {
                        // log.info("\tAlreadyReadAndResponded");
                    }
                    // TODO: add to a pool and deregister the read interest
                    // that way, the loop will not be spinning over and over again
                }
                iterator.remove();
            }
        }
    }
}
