package cs455.scaling.util;

import java.util.Vector;

import cs455.scaling.task.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Batch {
    private static final Logger log = LogManager.getLogger(Batch.class);

    private volatile Vector<Task> tasks;

    public Batch() {
        tasks = new Vector<>();
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public Vector<Task> getTasks() {
        return tasks;
    }

    public int getSize() {
        return tasks.size();
    }
}
