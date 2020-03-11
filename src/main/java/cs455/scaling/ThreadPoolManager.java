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
    private final double batchTime;

    private final Object lock;
    private volatile long startTime;

    public ThreadPoolManager(int threadPoolSize, int batchSize, double batchTime) {
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
        while (true) {
            try {
                int sleepTime = (int) (batchTime * 1000);
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                log.error("Error in pausing thread");
                e.printStackTrace();
            }
            synchronized (lock) {
                if (currentBatch.getSize() > 0) {
                    log.debug("Batch time (" + batchTime + ") exceeded. \n" +
                            "No. of tasks in the current batch: " + currentBatch.getSize());
                    // process tasks in the current batch
                    resetBatch();
                }
            }
        }
    }

    private void resetBatch() {
        log.debug("Resetting current batch");
        batchQueue.add(currentBatch);
        currentBatch = new Batch();
    }

    // both worker threads and the ThreadPoolManager thread access the currentBatch object
    public void addTask(Task task) {
        synchronized (lock) {
            log.debug("currentBatch.getSize(): " + currentBatch.getSize());
            if (currentBatch.getSize() < batchSize - 1) {
                log.debug("Adding new task to batch");
                currentBatch.addTask(task);
            } else {
                log.debug("Batch is full. Creating a new batch");
                currentBatch.addTask(task);
                resetBatch();
            }
            log.debug("currentBatch.getCurrentSize: " + currentBatch.getSize());
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
                    log.debug("Worker taking one batch to process");
                    Vector<Task> tasks = batch.getTasks();
                    Iterator<Task> iterator = tasks.iterator();
                    log.debug("tasks.size(): " + tasks.size());
                    int i = 0;
                    while (iterator.hasNext()) {
                        log.debug("Executing task " + ++i);
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
