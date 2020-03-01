package cs455.scaling.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class AcceptConnection extends Task {
    private ServerSocketChannel serverSocketChannel;
    // private SelectionKey key;
    private Selector selector;

    public AcceptConnection(ServerSocketChannel s, Selector sel) {
        serverSocketChannel = s;
        selector = sel;
    }

    public void resolve() throws IOException {

        synchronized (selector) {
            //System.out.println("Resolving accept connection task");
            //grab the connection off of the top of the server channel
            SocketChannel incomingClientChannel = serverSocketChannel.accept();

            if (incomingClientChannel == null) {
                return;
            }
            //make sure we dont block in our program
            incomingClientChannel.configureBlocking(false);


            synchronized (ThreadPoolManager.cMessagesSent) {
                ThreadPoolManager.cMessagesSent.put(incomingClientChannel, 0L);
            }
            //put the channel back into the keyset so we
            //can now read what it has to say to us
            //register the connection as readable for the selector to pick up
            incomingClientChannel.register(selector, SelectionKey.OP_READ);
            //mark the key as resolved and ready to take out of the queue


        }


    }

}
