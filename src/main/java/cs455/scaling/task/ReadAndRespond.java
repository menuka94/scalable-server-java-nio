package cs455.scaling.task;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicLong;

import cs455.scaling.util.Constants;
import cs455.scaling.util.HashUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReadAndRespond implements Task {
    private static final Logger log = LogManager.getLogger(ReadAndRespond.class);
    private SelectionKey key;
    private static final AtomicLong numMessagesProcessed = new AtomicLong(0);

    public ReadAndRespond(SelectionKey key) {
        this.key = key;
    }

    @Override
    public void execute() throws IOException {
        log.debug("ReadAndRespond.execute()");
        // Create a buffer to read into
        ByteBuffer buffer = ByteBuffer.allocate(Constants.MESSAGE_SIZE);

        // Grab the socket from the key
        SocketChannel clientSocket = (SocketChannel) key.channel();

        // Read from it
        int bytesRead = 0;

        while (buffer.hasRemaining() && bytesRead != -1) {
            bytesRead = clientSocket.read(buffer);
        }

        // Handle a closed connection
        if (bytesRead == -1) {
            clientSocket.close();
            log.warn("\t\tClient disconnected.");
            Register.decrementNumOfClients();
        } else {
            // Return the hash of message back to the client
            log.debug("\t\tReceived: " + new String(buffer.array()));

            String digest = HashUtil.SHA1FromBytes(buffer.array());

            buffer.clear();

            ByteBuffer respondBuffer = ByteBuffer.wrap(digest.getBytes());
            while (respondBuffer.hasRemaining()) {
                clientSocket.write(respondBuffer);
            }

            // Flip the buffer to write
            buffer.flip();
            clientSocket.write(respondBuffer);

            // Clear the buffer
            respondBuffer.clear();
            numMessagesProcessed.getAndIncrement();
        }
        key.attach(null);
        log.debug("ReadAndRespond.execute() complete");
    }

    public static AtomicLong getNumMessagesProcessed() {
        return numMessagesProcessed;
    }

    public static void resetNumMessagesProcessed() {
        numMessagesProcessed.set(0);
    }
}
