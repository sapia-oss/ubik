package org.sapia.ubik.rmi.server.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.sapia.ubik.concurrent.NamedThreadFactory;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Props;


/**
 * This queue stores outgoing {@link Response} objects on the server-side, until they are processed.
 * Instances of this class share an {@link ExecutorService}, which provides the threads that are used 
 * to notify this instance's listeners (see {@link OutQueueListener}) upon items being added to it
 * (see {@link #add(Response)}).
 * <p>
 * The number of threads that this instance holds can be configure through the {@link Consts#SERVER_CALLBACK_OUTQUEUE_THREADS}
 * property (it defaults to 2).
 *
 * @author Yanick Duchesne
 */
public class OutQueue extends ExecQueue<Response> {

  /**
   * An instance of this class is notified whenever the {@link OutQueue#add(Response)} method
   * is called.
   */
  public interface OutQueueListener {
    
    /**
     * @param destination the destination to which the responses are to be sent.
     * @param responses a {@link List} of {@link Response}s to send.
     */
    public void onResponses(Destination destination, List<Response> responses);
  }

  public static final int DEFAULT_OUTQUEUE_THREADS = 2;
  
  /**
   * Shared {@link ExecutorService} instance, used to notify {@link OutQueueListener}s on instances
   * of this class about pending responses.
   */
  private static volatile ExecutorService notifier;  
    
  private Category                log          = Log.createCategory(getClass());
  private Destination             destination;

  private List<OutQueueListener>  listeners    = Collections.synchronizedList(new ArrayList<OutQueueListener>());
  
  public OutQueue(Destination destination) {
    this.destination = destination;
    if(notifier == null) {
      createNotifier();
    }
  }
  
  private synchronized void createNotifier() {
    if(notifier == null) {
      notifier = Executors.newFixedThreadPool(
                   Props.getSystemProperties().getIntProperty(
                     Consts.SERVER_CALLBACK_OUTQUEUE_THREADS, 
                     DEFAULT_OUTQUEUE_THREADS
                   ), 
                   NamedThreadFactory
                     .createWith("ubik.rmi.callback.outqueue.thread").setDaemon(true)
                 );
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          notifier.shutdown();
        }
      });
    }
  }
  
  public void addQueueListener(OutQueueListener listener) {
    listeners.add(listener);
  }
  
  @Override
  public void add(Response toExecute) {
    super.add(toExecute);
    notifier.execute(new Runnable(){
      @Override
      public void run() {
        synchronized(listeners) {
          List<Response> responses = getAll();          
          for(OutQueueListener listener : listeners) {
            try {
              if(responses.size() > 0) {
                listener.onResponses(destination, responses);
              }
            } catch (RuntimeException e) {
              log.error("Runtime error notifying listeners for queue " + destination, e);
            }
          }
        }
      }
    });
  }
  
  @Override
  public synchronized void shutdown(long timeout) throws InterruptedException {
    super.shutdown(timeout);
    notifier.shutdown();
  }
 
  public boolean isShutdown() {
   return notifier.isShutdown(); 
  }
 

}
