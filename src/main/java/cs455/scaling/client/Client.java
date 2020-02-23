package cs455.scaling.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Client {
    private static final Logger LOGGER = LogManager.getLogger(Client.class);

    private static SocketChannel client;
    private static ByteBuffer buffer;

    public static void main(String[] args) throws IOException {
        // Connect to the server
        client = SocketChannel.open(new InetSocketAddress("localhost", 5600));

        // Create buffer
        buffer = ByteBuffer.allocate(256);

        buffer = ByteBuffer.wrap("Please send this back to me.".getBytes());
        String response = null;
        client.write(buffer);
        buffer.clear();
        client.read(buffer);
        response = new String(buffer.array()).trim();
        LOGGER.info("Server responded with: " + response);
        buffer.clear();
    }
}
