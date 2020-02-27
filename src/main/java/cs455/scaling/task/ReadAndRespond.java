package cs455.scaling.task;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReadAndRespond implements Task {
    private static final Logger log = LogManager.getLogger(ReadAndRespond.class);
    private SelectionKey key;

    public ReadAndRespond(SelectionKey key) {
        this.key = key;
    }

    @Override
    public void execute() throws IOException {
        // Create a buffer to read into
        ByteBuffer buffer = ByteBuffer.allocate(256);

        // Grab the socket from the key
        SocketChannel client = (SocketChannel) key.channel();

        // Read from it
        int bytesRead = client.read(buffer);

        // Handle a closed connection
        if (bytesRead == -1) {
            client.close();
            log.info("\t\tClient disconnected.");
        } else {
            // Return the hash of message back to the client
            log.info("\t\tReceived: " + new String(buffer.array()));

            // Flip the buffer now write
            buffer.flip();
            client.write(buffer);

            // Clear the buffer
            buffer.clear();
        }
    }
}
