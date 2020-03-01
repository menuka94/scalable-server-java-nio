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
import cs455.scaling.util.Worker;

public class Server {

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Wrong args: port numWorkers batchSize batchTime");
            System.exit(1);
        }
        final int port = Integer.parseInt(args[0]);
        final int numWorkers = Integer.parseInt(args[1]);
        final int batchSize = Integer.parseInt(args[2]);
        final int batchTime = Integer.parseInt(args[3]);

        System.out.println("Starting server on port " + port);

        LinkedBlockingQueue<Task> taskQueue = ThreadPoolManager.getTaskQueue();
        //initialize the current batch for the first time here so that we know it
        //is created before we spawn any threads
        ThreadPoolManager.setCurrentBatch(new Batch());

        //start the thread pool manager and give it the batch time
        ThreadPoolManager threadPoolManager = new ThreadPoolManager(batchSize, batchTime);
        threadPoolManager.start();

        //spawn as many workers as the args call for
        for (int i = 0; i < numWorkers; i++) {
            Worker worker = new Worker();
            worker.start();
        }

        try {
            //open selector
            Selector selector = Selector.open();

            //open server socket on the port we specified
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            String hostname = InetAddress.getLocalHost().getHostName();
            InetSocketAddress address = new InetSocketAddress(hostname, port);

            serverSocketChannel.bind(address);

            //make sure that we are doing non blocking io
            serverSocketChannel.configureBlocking(false);

            //idk why I have to do this but it makes nio work
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
                    SelectionKey currentKey = iterator.next();

                    //accept the key correctly
                    synchronized (selector) {
                        if (currentKey.isAcceptable()) {
                            //create a task to be allocated to a worker thread
                            Register acceptTask = new Register(serverSocketChannel, selector);
                            //add task to some sort of task queue
                            taskQueue.add(acceptTask);
                        }

                        //read the key if it needs to be read
                        else if (currentKey.isReadable()) {
                            //mark the key as in the queue, and should not be added again
                            if (currentKey.attachment() == null) {
                                //create a task for receiving the messages
                                ReceiveIncomingMessages recTask = new ReceiveIncomingMessages(currentKey);
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