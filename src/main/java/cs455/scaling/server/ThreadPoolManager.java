package cs455.scaling.server;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import cs455.scaling.datastructures.Batch;
import cs455.scaling.datastructures.TaskQueue;

public class ThreadPoolManager implements Runnable {
    //initialize this as a final variable here so only one taskQueue is ever created on
    //a server
    public static final TaskQueue taskQueue = new TaskQueue();
    public static Batch currentBatch;
    public static HashMap<SocketChannel, Long> cMessagesSent = new HashMap<SocketChannel, Long>();
    //should never have more than 1000 threads running at once
    //sem is needed to control problem of having any number able to add
    //but needing to block all of them if creating a new batch
    public static Semaphore batchSem = new Semaphore(1000);
    private static BlockingQueue<WorkerThread> threads = new LinkedBlockingQueue<WorkerThread>();
    private long batchTime;
    private long batchSize;


    public ThreadPoolManager(long batchSize, long batchTime) {
        this.batchSize = batchSize;
        this.batchTime = batchTime;
    }

    /*
    the pool manager should check for different jobs that need to be done.
    it needs to keep track of the batch timer and process the current batch when that is up

    it also needs to allocate threads to accept connections, read data in, and process batches.

     */

    public static Task addToPool(WorkerThread thread) {
        //System.out.println("adding to pool");

        synchronized (thread) {
            try {
                //System.out.println("waiting");
                threads.add(thread);
                thread.wait();
                synchronized (taskQueue) {
                    if (!taskQueue.isEmpty()) {
                        //grab a task out of the front of the queue and resolve it in the thread that is calling this method
                        return taskQueue.getNext();

                    }
                    return null;
                }
            } catch (InterruptedException e) {
                return null;
            }
        }
    }


    /*
    public static void allocateThread() throws Exception {
        //check the task queue and if it has something at the top grab it and give it to a this thread
        //this is a compound action so we must grab the lock for the taskQueue object
        Task nextTask = null;
        synchronized (taskQueue) {
            if (!taskQueue.isEmpty()) {
                //grab a task out of the front of the queue and resolve it in the thread that is calling this method
                nextTask = taskQueue.getNext();

            }
        }
        if (nextTask != null) {
            nextTask.resolve();
        }
    }
     */


    //run method that will keep a thread spinning on checking batch times and
    //creating new batches
    //only 1 instance of ThreadPoolManager should ever exist at one.
    public void run() {
        Long batchStart = System.currentTimeMillis();
        ComputeAndSend sendTask;
        long statTime = System.currentTimeMillis();
        //initialize batch and time

        while (true) {
            Long time = System.currentTimeMillis();
            boolean isTimeUp = time - batchStart > batchTime;
            boolean isBatchFull = (batchSize <= currentBatch.getCount().longValue());
            //System.out.println("Batch size " + batchSize + " is full? " + isBatchFull + " count " + currentBatch.getCount());
            //check if batch is full
            //check if batch time is up
            if (!threads.isEmpty()) {
                //System.out.println("thread ready");
                WorkerThread next = threads.remove();
                synchronized (next) {
                    next.notify();
                }
            }

            if (isTimeUp | isBatchFull) {
                //System.out.println("Batch size " + batchSize + " is full? " + isBatchFull + " count " + currentBatch.getCount());
                try {
                    //aquire all permits so you wait for all adding to
                    //finish then prevent adding until you release them
                    batchSem.acquire(1000);
                    //System.out.println("Processing batch");
                    //remove current batch
                    //create new batch
                    batchStart = System.currentTimeMillis();
                    Batch oldBatch = currentBatch;
                    //System.out.println("here b4 consrt");
                    currentBatch = new Batch();
                    //create a batch process task
                    //System.out.println("here0");
                    sendTask = new ComputeAndSend(oldBatch);
                    //add to task queue
                    taskQueue.add(sendTask);
                    //System.out.println("add to q");
                } catch (Exception e) {
                } finally {
                    //return permits to pool no matter what
                    batchSem.release(1000);
                    //System.out.println("here");
                }


            }

            //compute statistics and print every 20 seconds
            if (time - statTime > 20000) {
                ArrayList<Long> sentCounts = new ArrayList<Long>();
                System.out.println("Stat time");
                for (SocketChannel client : cMessagesSent.keySet()) {
                    synchronized (cMessagesSent) {
                        Long sent = cMessagesSent.get(client);
                        sentCounts.add(sent);
                    }
                }
                synchronized (cMessagesSent) {
                    cMessagesSent = new HashMap<SocketChannel, Long>();
                }

                double throughput = 0;
                for (Long number : sentCounts) {
                    throughput += number;
                }

                double standardOfDev = 0;

                throughput = throughput / 20;
                double numClients = sentCounts.size();
                double mean = throughput / numClients;

                for (Long number : sentCounts) {
                    standardOfDev += Math.pow((double) (number / 20) - mean, 2);
                }
                standardOfDev = Math.sqrt(standardOfDev / numClients);

                System.out.println("Server Throughput: " + throughput + " Active Clients: " + numClients
                        + " Mean Per-Client Throughput: " + mean + " STD of Mean Throughput: " + standardOfDev);


                statTime = System.currentTimeMillis();
            }
        }
    }
}
