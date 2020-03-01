package cs455.scaling.util;

import cs455.scaling.client.ClientData;

import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

public class Batch {
    private LinkedList<ClientData> clientDataList;
    private AtomicLong count;
    private Set<SocketChannel> socketChannels;
    //semaphore that prevents messages from being added while a new client head is being created
    private Semaphore addingSemaphore;

    public Batch() {
        clientDataList = new LinkedList<>();
        socketChannels = new HashSet<>();
        addingSemaphore = new Semaphore(1000);
        count = new AtomicLong(0);
    }

    public long getCount() {
        return count.get();
    }

    //add a message to a specific clients linked list
    public void addMessage(byte[] message, SocketChannel client) {

        //figure out which client the message is from and add it to that clients linked list

        /*
        This statement looks horrible, but if you trace through the parallel execution
        carefully, this section actually makes everything much faster and safer
         */
        //check if you need to add before synchronizing, so if a thread doesnt need to add
        //we dont slow down with synchronization barriers

        if (!socketChannels.contains(client)) {
            //multiple threads could enter here though for adding the same client
            //make sure they go sequentially after each other
            try {
                addingSemaphore.acquire(1000);
                //only add if you are the first thread though this block
                //if not skip adding, because the thread who passed synchronization first
                //already added it
                if (!socketChannels.contains(client)) {
                    //compound check then add operation needs to be synchronized
                    ClientData newClient = new ClientData(client);
                    //add puts element at end of linked list, so we can traverse list
                    //to add messages at the same time as adding clients
                    socketChannels.add(client);
                    clientDataList.add(newClient);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                addingSemaphore.release(1000);
            }
        }

        //traverses starting at head
        try {
            addingSemaphore.acquire();
            for (ClientData c : clientDataList) {
                //make do only for client that works
                if (c.getClientSocketChannel().equals(client)) {
                    c.addMessage(message);
                    synchronized (this) {
                        count.getAndIncrement();
                    }
                    //return so the rest of loop doesn't waste time
                    return;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            addingSemaphore.release();
        }
    }

    public void processBatch() {
        //compute hashes and send them back to each client

        for (ClientData clientData : clientDataList) {
            clientData.sendMessages();
        }
    }


}
