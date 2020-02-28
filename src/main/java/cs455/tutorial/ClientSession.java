package cs455.tutorial;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ClientSession {
    private SelectionKey selectionKey;
    private SocketChannel socketChannel;
    ByteBuffer byteBuffer;

    public ClientSession(SelectionKey selectionKey, SocketChannel socketChannel) throws IOException {
        this.selectionKey = selectionKey;
        this.socketChannel = socketChannel;

        // configure channel as non-blocking
        this.socketChannel.configureBlocking(false);
        byteBuffer = ByteBuffer.allocate(64);
    }

    // cancel the SelectionKey and close the Channel
    public void disconnect() {
        try {
            if (selectionKey != null) {
                selectionKey.cancel();
            }
            if (socketChannel == null) {
                return;
            }
            System.out.println("Disconnecting " + socketChannel.getRemoteAddress());
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void read() {
        try {
            int amountRead = -1;
            amountRead = socketChannel.read((ByteBuffer) byteBuffer.clear());

            if (amountRead == -1) {
                disconnect();
            } else if (amountRead < 1) {
                return;
            }

            System.out.println("Sending back " + byteBuffer.position() + " bytes");

            byteBuffer.flip();
            socketChannel.write(byteBuffer);
        } catch (IOException e) {
            disconnect();
            e.printStackTrace();
        }
    }

}
