package cs455.scaling;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ThreadPool {
    private static final Logger log = LogManager.getLogger(ThreadPool.class);
    private int size;

    public ThreadPool(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}
