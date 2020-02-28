package cs455.tutorial;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainServer {
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private static HashMap<SelectionKey, ClientSession> clientMap = new HashMap<>();

    public MainServer(InetSocketAddress listenAddress) throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        // configure serverSocketChannel as non-blocking
        serverSocketChannel.configureBlocking(false);
        // open Selector and register the serverSocketChannel to be interested in accepting
        // connections
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        serverSocketChannel.bind(listenAddress);

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                loop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    private void loop() throws IOException {
        // select all keys currently pending an operation
        // returns instantly, does not block.
        // selector.select() will block.
        selector.selectNow();

        // loop through all the selected keys
        for (SelectionKey selectionKey : selector.keys()) {
            // if the selectionKey is invalid, skip it and go to the next one
            if (!selectionKey.isValid()) {
                continue;
            }

            // if the selectionKey is acceptable, handle accepting
            // isAcceptable(): tests whether this key's channel is ready to accept a new connection.
            if (selectionKey.isAcceptable()) {
                SocketChannel acceptedChannel = serverSocketChannel.accept();
                if (acceptedChannel == null) {
                    continue;
                }
                acceptedChannel.configureBlocking(false);
                SelectionKey readKey = acceptedChannel.register(selector, SelectionKey.OP_READ);
                clientMap.put(readKey, new ClientSession(readKey, acceptedChannel));
                System.out.println("New Client IP: " + acceptedChannel.getRemoteAddress() + ", " +
                        "total clients = " + MainServer.clientMap.size());
            }

            // if the selectionKey is readable, handle reading
            if (selectionKey.isReadable()) {
                ClientSession clientSession = clientMap.get(selectionKey);
                if (clientSession == null) {
                    continue;
                }
                clientSession.read();
            }
        }
        // clear the keys that have been handled
        selector.selectedKeys().clear();
    }

    public static void main(String[] args) throws IOException {
        new MainServer(new InetSocketAddress("localhost", 1337));

    }
}
