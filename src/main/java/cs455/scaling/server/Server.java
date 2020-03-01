package cs455.scaling.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;
import cs455.scaling.datastructures.Batch;
import cs455.scaling.datastructures.TaskQueue;

public class Server {

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Wrong args: Server port numWorkers batchSize batchTime");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);

        System.out.println("starting server on port " + port);
        //grab the final single instance of the taskqueue that the pool manager uses
        TaskQueue taskQueue = ThreadPoolManager.taskQueue;
        //initialize the current batch for the first time here so that we know it
        //is created before we spawn any threads
        ThreadPoolManager.currentBatch = new Batch();

        //start the thread pool manager and give it the batch time
        ThreadPoolManager threadPoolManager = new ThreadPoolManager(Integer.parseInt(args[2]),
                Integer.parseInt(args[3]));
        Thread poolManagerThread = new Thread(threadPoolManager);
        poolManagerThread.start();

        //spawn as many workers as the args call for
        int numWorkers = Integer.parseInt(args[1]);
        for (int i = 0; i < numWorkers; i++) {
            WorkerThread w = new WorkerThread();
            Thread thread = new Thread(w);
            thread.start();
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
            //register the operations?
            int ops = serverSocketChannel.validOps();
            SelectionKey selectionKey = serverSocketChannel.register(selector, ops);


            while (true) {
                //System.out.println("selecting");
                //selects the keys, but doesnt give them to you yet
                //use selectNow because it does not block unexpectedly
                selector.selectNow();
                //actually get the keys so that we can use them
                Set<SelectionKey> keys = selector.selectedKeys();
                //get an iterator from the keyset that will feed us keys
                Iterator<SelectionKey> keyIterator = keys.iterator();

                //go over all the keys and handle them
                while (keyIterator.hasNext()) {
                    //System.out.println("inside iterator loop: ");
                    SelectionKey currentKey = keyIterator.next();

                    //System.out.println(currentKey.channel());

                    //System.out.println(currentKey.attachment());
                    //check if we have already processed that key

                    //accept the key correctly
                    synchronized (selector) {
                        if (currentKey.isAcceptable()) {
                            //System.out.println("Accepting");
                            //create a task to be allocated to a worker thread
                            AcceptConnection acceptTask = new AcceptConnection(serverSocketChannel, selector);
                            //add task to some sort of task queue
                            taskQueue.add(acceptTask);
                        }

                        //read the key if it needs to be read
                        else if (currentKey.isReadable()) {
                            //mark the key as in the queue, and should not be added again
                            if (currentKey.attachment() == null) {
                                //System.out.println("reading: ");
                                //create a task for receiving the messages
                                RecieveIncomingMessages recTask = new RecieveIncomingMessages(currentKey);
                                //add to the task queue
                                taskQueue.add(recTask);
                            }
                        }
                    }
                    //attach an object to the key so the server know it has already seen it

                    //whatever happens make sure we take the key out of the set so we
                    //can move on to the next one
                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}