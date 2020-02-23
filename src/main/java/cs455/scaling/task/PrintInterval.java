package cs455.scaling.task;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Expresses intention better than thread.sleep()
 * Code readability
 * Timer object runs on one thread, can have one thread execute many different timer tasks
 * Other built-in functionality
 */
public class PrintInterval {
    public static void main(String[] args) {
        Timer timer = new Timer();

        PrintTask printTask = new PrintTask();

        timer.scheduleAtFixedRate(printTask, 0L, 5000L);
    }

    static class PrintTask extends TimerTask {
        @Override
        public void run() {
            System.out.println("Hello World");
        }
    }
}

