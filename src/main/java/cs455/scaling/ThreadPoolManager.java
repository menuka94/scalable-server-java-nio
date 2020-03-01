package cs455.scaling;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import cs455.scaling.task.Task;
import cs455.scaling.util.Batch;
import cs455.scaling.util.TaskQueue;
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
    private final TaskQueue taskQueue;
    private Batch currentBatch;
    private LinkedBlockingQueue<Worker> workers;
    public Semaphore batchSemaphore;
    private long batchSize;
    private long batchTime;

    public ThreadPoolManager(long batchSize, long batchTime) {
        taskQueue = new TaskQueue();
        batchSemaphore = new Semaphore(10);
        workers = new LinkedBlockingQueue<>();
        this.batchSize = batchSize;
        this.batchTime = batchTime;
    }


    private class Worker extends Thread {
        private final Logger log = LogManager.getLogger(Worker.class);

        @Override
        public void run() {

        }
    }

    public Task addWorkerToPool(Worker worker) {
        synchronized (taskQueue) {
            try {
                workers.add(worker);
                worker.wait();
                if (!taskQueue.isEmpty()) {
                    return taskQueue.removeAndGetNext();
                }
                return null;
            } catch (InterruptedException e) {
                log.error("Error adding Worker to Thread Pool");
                e.printStackTrace();
            }
        }
        return null;
    }

    public void setCurrentBatch (Batch batch) {
        currentBatch = batch;
    }

    @Override
    public void run() {
        Long batchStart = System.currentTimeMillis();

    }
}
