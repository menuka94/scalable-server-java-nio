package cs455.scaling.datastructures;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import cs455.scaling.util.HashUtil;

public class ClientData {

    private LinkedList<byte[]> messages;
    private final SocketChannel clientSocketChannel;

    public ClientData(SocketChannel clientSocketChannel) {
        synchronized (this) {
            messages = new LinkedList<byte[]>();
            this.clientSocketChannel = clientSocketChannel;
        }
    }

    public synchronized SocketChannel getClientSocketChannel() {
        return clientSocketChannel;
    }

    public synchronized void add(byte[] message) {
        messages.add(message);
    }

    public void sendMessages() {
        synchronized (clientSocketChannel) {
            for (byte[] message : messages) {
                try {
                    byte[] hash = HashUtil.hash(message);
                    clientSocketChannel.write(ByteBuffer.wrap(hash));

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
