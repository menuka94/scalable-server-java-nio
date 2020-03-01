package cs455.scaling.util;

import java.util.concurrent.LinkedBlockingQueue;
import cs455.scaling.task.Task;

public class TaskQueue {
    private LinkedBlockingQueue<Task> taskQueue;

    public TaskQueue() {
        synchronized (this) {
            taskQueue = new LinkedBlockingQueue<>();
        }
    }

    public void addTask(Task task) {
        taskQueue.add(task);
    }

    public Task removeAndGetNext() {
        return taskQueue.remove();
    }

    public boolean isEmpty() {
        return taskQueue.isEmpty();
    }
}
