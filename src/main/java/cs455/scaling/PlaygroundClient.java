package cs455.scaling;

import java.util.concurrent.Semaphore;

public class PlaygroundClient {

    public static Semaphore sem = new Semaphore(0);
    /*
    Random rand = new Random();
        System.out.println("enter");
        byte[] af = new byte[16];
        rand.nextBytes(af);
        String result = new String(af, "UTF-8");
        byte[]  better = result.getBytes("UTF-8");
        System.out.println("here");
        for(int i = 0; i<af.length;i++){
            //System.out.println("in loop");
            if(better[i] != af[i]) System.out.println(better[i] + ":" + af[i]);
        }
     */

    public static void main(String[] argv) throws Exception {
        Nothing n = new Nothing();
        Thread t = new Thread(n);
        t.start();
        sem.acquire();
        synchronized (n) {
            System.out.println("attempting notify");
            n.notify();
        }

    }

    private static class Nothing implements Runnable {


        public void run() {
            System.out.println("entering run");
            try {
                synchronized (this) {
                    sem.release();
                    System.out.println("starting wait");
                    wait();
                    System.out.println("finished waiting");
                }

            } catch (Exception e) {
            }

        }

    }
}
