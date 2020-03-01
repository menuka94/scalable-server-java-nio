package cs455.scaling.server;

//children of this class are actions that a worker thread can do.
//they hold the data they need, and execute what they need to do
//with their resolve method
public abstract class Task {

    //do the thing that needs to be done worker thread
    public abstract void resolve() throws Exception;

}
