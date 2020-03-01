package cs455.scaling.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import cs455.scaling.util.Batch;
import cs455.scaling.task.ReceiveIncomingMessages;
import cs455.scaling.task.Register;
import cs455.scaling.task.Task;
import cs455.scaling.util.ThreadPoolManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server {
    private static final Logger log = LogManager.getLogger(Server.class);

    public static void main(String[] args) {
        if (args.length != 4) {
            log.warn("Invalid arguments. Provide <port-num> <thread-pool-size> " +
                    "<batch-size> <batch-time>");
            System.exit(1);
        }
        final int port = Integer.parseInt(args[0]);
        final int threadPoolSize = Integer.parseInt(args[1]);
        final int batchSize = Integer.parseInt(args[2]);
        final int batchTime = Integer.parseInt(args[3]);

        System.out.println("Starting server on port " + port);

        LinkedBlockingQueue<Task> taskQueue = ThreadPoolManager.getTaskQueue();
        ThreadPoolManager.setCurrentBatch(new Batch());

        ThreadPoolManager threadPoolManager = new ThreadPoolManager(batchSize, batchTime);
        threadPoolManager.start();

        threadPoolManager.startWorkers(threadPoolSize);

        try {
            Selector selector = Selector.open();

            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostName(), port);

            serverSocketChannel.bind(socketAddress);

            serverSocketChannel.configureBlocking(false);

            // make nio work
            serverSocketChannel.register(selector, serverSocketChannel.validOps());

            while (true) {
                //selects the keys, but doesnt give them to you yet
                //use selectNow because it does not block unexpectedly
                selector.selectNow();
                //actually get the keys so that we can use them
                Set<SelectionKey> keys = selector.selectedKeys();
                //get an iterator from the keyset that will feed us keys
                Iterator<SelectionKey> iterator = keys.iterator();

                //go over all the keys and handle them
                while (iterator.hasNext()) {
                    //System.out.println("inside iterator loop: ");
                    SelectionKey key = iterator.next();

                    //accept the key correctly
                    synchronized (selector) {
                        if (key.isAcceptable()) {
                            //create a task to be allocated to a worker thread
                            Register acceptTask = new Register(serverSocketChannel, selector);
                            //add task to some sort of task queue
                            taskQueue.add(acceptTask);
                        }

                        //read the key if it needs to be read
                        else if (key.isReadable()) {
                            //mark the key as in the queue, and should not be added again
                            if (key.attachment() == null) {
                                //create a task for receiving the messages
                                ReceiveIncomingMessages recTask = new ReceiveIncomingMessages(key);
                                //add to the task queue
                                taskQueue.add(recTask);
                            }
                        }
                    }
                    //attach an object to the key so the server know it has already seen it
                    //whatever happens make sure we take the key out of the set so we
                    //can move on to the next one
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}