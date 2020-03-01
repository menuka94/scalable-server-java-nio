package cs455.scaling.server;

import java.util.concurrent.LinkedBlockingQueue;
import cs455.scaling.datastructures.Batch;

public class ReadAndRespond extends Task {
    private static LinkedBlockingQueue<Batch> batchQueue = new LinkedBlockingQueue<>();

    //give the task a batch that it will process and send out
    public ReadAndRespond(Batch batch) {
        batchQueue.add(batch);
    }

    //use the batch
    //compute the hashes and send them back to the client
    public void execute() {
        synchronized (batchQueue) {
            try {
                Batch work = batchQueue.take();
                work.processBatch();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
