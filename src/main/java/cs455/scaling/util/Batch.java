package cs455.scaling.util;

import java.util.Vector;
import cs455.scaling.ThreadPool;
import cs455.scaling.task.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Batch {
    private static final Logger log = LogManager.getLogger(Batch.class);

    private Vector<Task> tasks;
    private ThreadPool threadPool;
    private int batchSize;

    public Batch(int batchSize, ThreadPool threadPool) {
        this.batchSize = batchSize;
        tasks = new Vector<>();
        this.threadPool = threadPool;
    }

    public Vector<Task> getTasks() {
        return tasks;
    }

    public synchronized void addBatchTask(Task task) throws InterruptedException {
        if (tasks.size() == batchSize) {
            log.info("Adding a new batch of tasks to the ThreadPool");
            threadPool.addBatch(this);
            // reinitialize tasks list
            tasks = new Vector<>();
        }
        tasks.add(task);
    }
}
