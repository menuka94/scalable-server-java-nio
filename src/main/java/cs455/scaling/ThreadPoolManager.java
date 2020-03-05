package cs455.scaling;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import cs455.scaling.task.Register;
import cs455.scaling.task.Task;
import cs455.scaling.util.Batch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * You will be developing a server to handle network traffic by designing and building your own thread pool.
 * This thread pool will have a configurable number of threads that will be used to perform tasks relating
 * to network communications. Specifically, you will use this thread pool to manage all tasks relating
 * to network communications. This includes:
 * 1. Managing incoming network connections
 * 2. Receiving data over these network connections
 * 3. Organizing data into batches to improve performance
 * 4. Sending data over any of these links
 */
public class ThreadPoolManager extends Thread {
    private static final Logger log = LogManager.getLogger(ThreadPoolManager.class);
    private volatile LinkedBlockingQueue<Batch> batchQueue;
    private volatile LinkedBlockingQueue<Register> registerTaskQueue;
    private Batch currentBatch;
    private volatile Vector<Worker> workers;
    private final int threadPoolSize;
    private final int batchSize;
    private final int batchTime;

    private final Object lock;

    public ThreadPoolManager(int threadPoolSize, int batchSize, int batchTime) {
        this.threadPoolSize = threadPoolSize;
        this.batchSize = batchSize;
        this.batchTime = batchTime;
        batchQueue = new LinkedBlockingQueue<>();
        currentBatch = new Batch();
        lock = new Object();
        workers = new Vector<>();
        for (int i = 0; i < threadPoolSize; i++) {
            workers.add(new Worker("Worker " + (i + 1)));
        }
    }

    @Override
    public void run() {
        // long startTime = System.currentTimeMillis();
        //
        // long currentTime;
        // while (true) {
        //     currentTime = System.currentTimeMillis();
        //     long timeDifference = currentTime - startTime;
        //         if (timeDifference > batchTime * 1000) {
        //             if (currentBatch.getSize() > 0) {
        //                 log.info("Batch time (" + batchTime + ") exceeded.");
        //                 log.info("No. of tasks in the current batch: " + currentBatch.getSize());
        //                 // process tasks in the current batch
        //                 resetBatch();
        //             }
        //             startTime =  System.currentTimeMillis();
        //         }
        // }
    }

    private void resetBatch () {
        // synchronized (lock) {
            log.info("Resetting current batch");
            batchQueue.add(currentBatch);
            currentBatch = new Batch();
        // }
    }

    public void addTask(Task task) {
        log.info("BatchSize: " + batchSize);
        log.info("currentBatch.getSize(): " + currentBatch.getSize());
        if (currentBatch.getSize() < batchSize - 1) {
            log.info("Adding new task to batch");
            currentBatch.addTask(task);
        } else {
            log.info("Batch is full. Creating a new batch");
            // TODO: Properly pause ThreadPoolManager while creating a new batch
            // this.interrupt();
            currentBatch.addTask(task);
            resetBatch();
        }
        log.info("currentBatch.getCurrentSize: " + currentBatch.getSize());
    }

    public void startWorkers() {
        for (Worker worker : workers) {
            worker.start();
        }
    }

    private class Worker extends Thread {
        private final Logger log = LogManager.getLogger(Worker.class);

        public Worker(String name) {
            super(name);
        }

        @Override
        public void run() {
            log.info("Worker starting ...");
            while (true) {
                Batch batch = null;
                try {
                    batch = batchQueue.take();
                    log.info("Worker taking one batch to process");
                    Vector<Task> tasks = batch.getTasks();
                    Iterator<Task> iterator = tasks.iterator();
                    log.info("tasks.size(): " + tasks.size());
                    int i = 0;
                    while (iterator.hasNext()) {
                        log.info("Executing task " + ++i);
                        Task task = iterator.next();
                        if (task == null) {
                            log.warn("Task is null");
                        } else {
                            task.execute();
                        }
                    }
                } catch (InterruptedException | IOException e) {
                    log.error("Error in Worker");
                    e.printStackTrace();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }
}
