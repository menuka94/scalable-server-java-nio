package cs455.scaling.task;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import cs455.scaling.util.Batch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ComputeAndSend implements Task {
    private static final Logger log = LogManager.getLogger(ComputeAndSend.class);

    private final LinkedBlockingQueue<Batch> batches;

    public ComputeAndSend() {
        batches = new LinkedBlockingQueue<>();
    }

    public void addBatch(Batch batch) {
        try {
            batches.put(batch);
        } catch (InterruptedException e) {
            log.error("Adding a new batch of tasks");
            e.printStackTrace();
        }
    }


    @Override
    public void execute() throws IOException {
        synchronized (batches) {
            try {
                Batch batch = batches.take();
                batch.process();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
