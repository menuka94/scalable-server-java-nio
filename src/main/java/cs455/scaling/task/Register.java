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

    public Register(Selector selector, ServerSocketChannel serverSocketChannel) {
        this.selector = selector;
        this.serverSocketChannel = serverSocketChannel;
    }

    @Override
    public void execute() throws IOException {
        // Grab the incoming socket from the serverSocketChannel
        SocketChannel client = serverSocketChannel.accept();
        // Configure it to be a new channel and key that our selector should monitor
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        log.info("\t\tNew Client Registered");
    }
}
