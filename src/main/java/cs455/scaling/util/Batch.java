package cs455.scaling.util;

import java.util.LinkedList;
import cs455.scaling.task.Task;

public class Batch {
    private LinkedList<Task> tasks;

    public Batch(int batchSize) {
        tasks = new LinkedList<>();
    }

    public LinkedList<Task> getTasks() {
        return tasks;
    }

    public void addBatchTask(Task task) {
        tasks.add(task);
    }
}
