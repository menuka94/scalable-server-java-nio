package cs455.scaling.server;

public class WorkerThread implements Runnable {
    private static int instances = 0;
    public final int tnum;
    Task task;

    public WorkerThread() {
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
                if (task != null) task.resolve();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //count++;
            //if(count%10 == 0) System.out.println("thread number " + tnum);
        }
    }
}
