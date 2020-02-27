package cs455.scaling;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import cs455.scaling.task.Task;
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
public class ThreadPool {
    private static final Logger log = LogManager.getLogger(ThreadPool.class);
    private LinkedBlockingQueue<Task> taskQueue;
    private ArrayList<Worker> workers;
    private int threadPoolSize;
    private int batchTime;

    public ThreadPool(int threadPoolSize, int batchTime) {
        this.threadPoolSize = threadPoolSize;
        this.batchTime = batchTime;
        taskQueue = new LinkedBlockingQueue<>();
        workers = new ArrayList<>();
        for (int i = 0; i < threadPoolSize; i++) {
            workers.add(new Worker());
        }
    }

    public void startThreads() {
        for (Worker worker : workers) {
            worker.start();
        }
    }

    private class Worker extends Thread {
        private final Logger log = LogManager.getLogger(Worker.class);

        @Override
        public void run() {
            while(true) {
                Task task = null;
                try {
                    task = taskQueue.take();
                    task.execute();
                } catch (InterruptedException | IOException e) {
                    log.error(e.getStackTrace());
                }
            }
        }
    }

    public void addTask(Task task) throws InterruptedException {
        taskQueue.put(task);
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }
}
