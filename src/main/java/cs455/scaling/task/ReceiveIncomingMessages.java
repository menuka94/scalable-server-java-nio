package cs455.scaling.task;

import cs455.scaling.util.Constants;
import cs455.scaling.util.ThreadPoolManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ReceiveIncomingMessages implements Task {

    SelectionKey key;

    public ReceiveIncomingMessages(SelectionKey key) {
        this.key = key;
        key.attach(0);
    }

    //read all the messages possible out of a channel with incoming messages
    @Override
    public void execute() throws IOException {
        //grab the channel we already made for it
        SocketChannel clientChannel = (SocketChannel) key.channel();

        synchronized (ThreadPoolManager.clientMessagesSent) {
            Long sentNum = ThreadPoolManager.clientMessagesSent.get(clientChannel);
            if (sentNum == null) {
                sentNum = 0L;
            }
            ThreadPoolManager.clientMessagesSent.put(clientChannel, sentNum + 1);
        }

        //make a buffer to read the message
        ByteBuffer messageBuffer = ByteBuffer.allocate(Constants.MESSAGE_SIZE);
        //read the message
        int bytesRead = clientChannel.read(messageBuffer);
        if (bytesRead == 0) {
            key.attach(null);
            return;
        }
        while (bytesRead != Constants.MESSAGE_SIZE) {
            bytesRead += clientChannel.read(messageBuffer);
        }
        //turn message and print it out for testing
        byte[] result = messageBuffer.array();

        //were going to need to add to the pool managers current batch,
        //so grab the lock so the pool manager doesnt try to create a new batch while we do that
        try {
            //make sure pool is not currently creating a new batch
            ThreadPoolManager.batchSem.acquire();
            //add message to the correct data structure, and correct place
            ThreadPoolManager.getCurrentBatch().addMessage(result, clientChannel);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //return permits to pool no matter what
            ThreadPoolManager.batchSem.release();
        }
        //mark this the key as resolved and able to take out of the queue
        key.attach(null);
    }
}
