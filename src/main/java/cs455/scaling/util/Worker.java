package cs455.scaling.util;

import cs455.scaling.task.Task;

public class Worker extends Thread {
    private static int instances = 0;
    public final int tnum;
    Task task;

    public Worker() {
        task = null;
        instances++;
        tnum = instances;
    }

    //allocate work according to thread pool in an infinite loop
    public void run() {
        int count = 0;
        while (true) {
            try {
                task = ThreadPoolManager.addToPool(this);
                if (task != null) task.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
