package cs455.scaling.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class RecieveIncomingMessages extends Task {

    SelectionKey key;

    public RecieveIncomingMessages(SelectionKey k) {
        key = k;
        k.attach(new Integer(0));

    }


    //read all the messages possible out of a channel with incomming messages
    public void execute() throws IOException {
        //grab the channel we already made for it
        SocketChannel clientChannel = (SocketChannel) key.channel();

        synchronized (ThreadPoolManager.cMessagesSent) {
            Long sentNum = ThreadPoolManager.cMessagesSent.get(clientChannel);
            if (sentNum == null) sentNum = new Long(0);
            ThreadPoolManager.cMessagesSent.put(clientChannel, sentNum + 1);
        }

        SocketAddress clientaddress = clientChannel.getRemoteAddress();
        //make a buffer to read the message
        ByteBuffer messageBuffer = ByteBuffer.allocate(8192);
        //read the message
        int bytesRead = clientChannel.read(messageBuffer);
        if (bytesRead == 0) {
            key.attach(null);
            //System.out.println("duplicate read task, nothing to read");
            return;
        }
        while (bytesRead != 8192) {
            bytesRead += clientChannel.read(messageBuffer);
        }
        //turn message and print it out for testing
        byte[] result = messageBuffer.array();


        //System.out.println(messageBuffer.array()[8000]);
        // System.out.println("bytes length: " + messageBuffer.array().length + " string: " + result.length());

        //System.out.println(bytesRead);
        //System.out.println("reading message");

        //were going to need to add to the pool managers current batch,
        //so grab the lock so the pool manager doesnt try to create a new batch while we do that
        try {
            //make sure pool is not currently creating a new batch
            ThreadPoolManager.batchSem.acquire();
            //System.out.println("getting permit");
            //add message to the correct data structure, and correct place
            ThreadPoolManager.currentBatch.addMessage(result, clientChannel);
        } catch (Exception e) {

        } finally {
            //return permits to pool no matter what
            ThreadPoolManager.batchSem.release();
        }
        //mark this the key as resolved and able to take out of the queue
        key.attach(null);
    }
}
