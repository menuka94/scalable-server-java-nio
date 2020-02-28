# Java NIO

### 3 Main Constructs
    * Channels - communication channels, associated with an underlying socket connection
    * Selectors - Object through which a single thread can monitor and service multiple channels
    * SelectionKeys - Each channel registered with the selector has an associated key
        * Interest Set
            * What kind of activity the selector will monitor for
        * Ready Set
            * What operations the channel associated with the key is ready for
            
            
            
### Selector Registration
* When you register a chanel with the selector you need to set what the selector will monitor
for
    * Connect, Accept, Read, Write
* The server socket channel will be registered with accept for example
* A key is associated with each registered channel
* You may set multiple interests for each key (read and write)
    * Interest set
        * Select monitors all channels
* Takes action when there is activity on any one channel (ready set)


### Interest Set
* Writing to and reading from a channel are independent of the interest set
* The interest is only used by the selector
* If you do not register write interest with the interest set, you can still write to the
channel from any random thread
     
     
#### When would you want to register write interest
* When channel.write() returns 0, this means 0 bytes were written and the channel is not
     currently writable
     * Could be full
* Rather than spinning in the while loop, you can register write interest with the selector
, then when the channel does become writable the selector.select() call will pick it up
* Rare situation
* Remember to turn off write interest when done so the selector does not continuosly loop
```java
ByteBuffer buffer = ByteBuffer.wrap(byteArray);
    while (true) {
        socketChannel.write(buffer);       
    }    
```

### Read and Write Interest

* selector.select() blocks, selector.selectNow() does not block
* Clients registered for read interest
* Channels will typically always be writable
* Registering write interest will mean there is almost always activity on that channel
* Could de-registering read interest after you already know there is something to read so the
 selector does not keep looping
* Not writable: buffer is full




