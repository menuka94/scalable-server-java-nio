package cs455.scaling;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Playground {

    public static void main(String[] args) {
        if (args.length < 1) System.exit(1);
        int port = Integer.parseInt(args[0]);

        System.out.println("starting server on port " + port);

        try {
            Selector selector = Selector.open();


            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            InetSocketAddress address = new InetSocketAddress("localhost", port);

            serverSocketChannel.bind(address);

            serverSocketChannel.configureBlocking(false);

            int ops = serverSocketChannel.validOps();
            SelectionKey selectionKey = serverSocketChannel.register(selector, ops);


            while (true) {
                //System.out.println("selecting");
                //selects the keys, but doesnt give them to you yet
                selector.select();
                //actually get the keys so that we can use them
                Set<SelectionKey> keys = selector.selectedKeys();
                //get an iterator from the keyset that will feed us keys
                Iterator<SelectionKey> keyIterator = keys.iterator();

                //go over all the keys and handle them
                while (keyIterator.hasNext()) {
                    System.out.println("inside iterator loop: ");
                    SelectionKey currentKey = keyIterator.next();

                    //accept the key correctly
                    if (currentKey.isAcceptable()) {
                        System.out.println("accepting");
                        //grab the connection off of the top of the server channel
                        SocketChannel incommingClientChannel = serverSocketChannel.accept();
                        //make sure we dont block in our program
                        incommingClientChannel.configureBlocking(false);
                        //put the channel back into the keyset so we
                        //can now read what it has to say to us
                        incommingClientChannel.register(selector, SelectionKey.OP_READ);
                    }

                    //read the key if it needs to be read
                    else if (currentKey.isReadable()) {
                        System.out.println("reading: ");
                        //grab the channel we already made for it
                        SocketChannel clientChannel = (SocketChannel) currentKey.channel();

                        SocketAddress clientaddress = clientChannel.getRemoteAddress();
                        //make a buffer to read the message
                        ByteBuffer messageBuffer = ByteBuffer.allocate(512);
                        //read the message
                        clientChannel.read(messageBuffer);
                        //turn message and print it out for testing
                        String result = new String(messageBuffer.array());
                        System.out.println(result);


                    }
                    //if the key is not readable or acceptable, we dont care about it
                    //so we can just throw it away


                    //whatever happens make sure we take the key out of the set so we
                    //can move on to the next one
                    keyIterator.remove();
                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}