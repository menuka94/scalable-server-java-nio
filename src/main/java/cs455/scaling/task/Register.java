package cs455.scaling.task;

import cs455.scaling.util.ThreadPoolManager;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Register implements Task {
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    public Register(ServerSocketChannel serverSocketChannel, Selector selector) {
        this.serverSocketChannel = serverSocketChannel;
        this.selector = selector;
    }

    @Override
    public void execute() throws IOException {
        synchronized (selector) {
            //grab the connection off of the top of the server channel
            SocketChannel incomingClientChannel = serverSocketChannel.accept();

            if (incomingClientChannel == null) {
                return;
            }
            //make sure we dont block in our program
            incomingClientChannel.configureBlocking(false);

            synchronized (ThreadPoolManager.clientMessagesSent) {
                ThreadPoolManager.clientMessagesSent.put(incomingClientChannel, 0L);
            }
            //put the channel back into the keyset so we
            //can now read what it has to say to us
            //register the connection as readable for the selector to pick up
            incomingClientChannel.register(selector, SelectionKey.OP_READ);
            //mark the key as resolved and ready to take out of the queue
        }
    }

}
