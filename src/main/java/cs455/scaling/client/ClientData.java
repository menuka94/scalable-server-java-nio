package cs455.scaling.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import cs455.scaling.util.HashUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientData {
    private static final Logger log = LogManager.getLogger(ClientData.class);

    private LinkedList<byte[]> messages;
    private SocketChannel socketChannel;

    public ClientData(SocketChannel socketChannel) {
        messages = new LinkedList<>();
        this.socketChannel = socketChannel;
    }

    public synchronized void addMessage(byte[] message) {
        messages.add(message);
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public synchronized void sendMessages() {
        for (byte[] message : messages) {
            String messageDigest = HashUtil.SHA1FromBytes(message);
            try {
                socketChannel.write(ByteBuffer.wrap(messageDigest.getBytes()));
            } catch (IOException e) {
                log.error("Error in sending message to client");
                e.printStackTrace();
            }
        }
    }
}
