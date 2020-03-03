package cs455.scaling.util;

import java.util.concurrent.LinkedBlockingQueue;

import cs455.scaling.task.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Batch {
    private static final Logger log = LogManager.getLogger(Batch.class);

    private volatile LinkedBlockingQueue<Task> tasks;

    public Batch() {
        tasks = new LinkedBlockingQueue<>();
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public LinkedBlockingQueue<Task> getTasks() {
        return tasks;
    }

    public int getSize() {
        return tasks.size();
    }
}
