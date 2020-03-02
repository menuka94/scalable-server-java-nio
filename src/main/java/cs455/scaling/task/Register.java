package cs455.scaling.task;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Register implements Task {
    private static final Logger log = LogManager.getLogger(Register.class);
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private SelectionKey key;

    public Register(Selector selector, ServerSocketChannel serverSocketChannel, SelectionKey key) {
        this.selector = selector;
        this.serverSocketChannel = serverSocketChannel;
        this.key = key;
    }

    @Override
    public void execute() throws IOException {
        log.info("Register.execute()");
        // Grab the incoming socket from the serverSocketChannel
        SocketChannel clientSocketChannel = serverSocketChannel.accept();
        // Configure it to be a new channel and key that our selector should monitor
        clientSocketChannel.configureBlocking(false);
        clientSocketChannel.register(selector, SelectionKey.OP_READ);
        clientSocketChannel.register(selector, key.interestOps() & ~SelectionKey.OP_ACCEPT);
        log.info("\t\tNew Client Registered");
    }
}
