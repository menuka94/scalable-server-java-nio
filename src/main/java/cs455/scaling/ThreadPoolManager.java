package cs455.scaling;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
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
    private Batch currentBatch;
    private volatile Vector<Worker> workers;
    private final int threadPoolSize;
    private final int batchSize;
    private final int batchTime;

    public ThreadPoolManager(int threadPoolSize, int batchSize, int batchTime) {
        this.threadPoolSize = threadPoolSize;
        this.batchSize = batchSize;
        this.batchTime = batchTime;
        batchQueue = new LinkedBlockingQueue<>();
        currentBatch = new Batch();
        workers = new Vector<>();
        for (int i = 0; i < threadPoolSize; i++) {
            workers.add(new Worker("Worker " + (i+1)));
        }
    }

    @Override
    public void run() {
        Long batchStart = System.currentTimeMillis();

    }

    public synchronized void addTask(Task task) {
        log.info("currentBatch.getCurrentSize: " + currentBatch.getCurrentSize());
        if (currentBatch.getCurrentSize() < batchSize) {
            log.info("Adding new task to batch");
            currentBatch.addTask(task);
        } else {
            log.info("Batch is full. Creating a new batch");
            batchQueue.add(currentBatch);
            currentBatch = new Batch();
            currentBatch.addTask(task);
        }
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
                        if(task == null) {
                            log.warn("Task is null");
                        } else {
                            task.execute();
                        }
                    }
                } catch (InterruptedException | IOException e) {
                    log.error("Error in Worker");
                    e.printStackTrace();
                }
            }
        }
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }
}
