package cs455.scaling.task;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PrintClientStatsTask extends TimerTask {
    private static final Logger log = LogManager.getLogger(PrintClientStatsTask.class);
    private AtomicLong noOfMessagesSent;

    public PrintClientStatsTask(AtomicLong noOfMessagesSent) {
        this.noOfMessagesSent = noOfMessagesSent;
    }

    @Override
    public void run() {
        double throughput = noOfMessagesSent.get() / 20.0;
    }
}
