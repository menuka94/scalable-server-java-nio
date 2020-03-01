package cs455.scaling.datastructures;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import cs455.scaling.Hash;

public class ClientMessages {

    private LinkedList<byte[]> messages;
    private SocketChannel clientChannel;

    public ClientMessages(SocketChannel clientChannel) {
        synchronized (this) {
            messages = new LinkedList<byte[]>();
            this.clientChannel = clientChannel;
        }
    }

    public synchronized SocketChannel getClientChannel() {
        return clientChannel;
    }

    public synchronized void add(byte[] message) {
        messages.add(message);
        //System.out.println("add length: " + message.length);
    }

    public synchronized void sendMessages() {
        synchronized (clientChannel) {
            for (byte[] message : messages) {
                try {
                    byte[] hash = Hash.hash(message);

                    clientChannel.write(ByteBuffer.wrap(hash));

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
