package org.sapia.ubik.rmi.server.command;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.sapia.ubik.concurrent.NamedThreadFactory;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.server.command.OutQueue.OutQueueListener;
import org.sapia.ubik.rmi.server.transport.TransportManager;

/**
 * An instance of this class manages {@link OutQueue} instances, which are kept on a per-destination
 * basis (that is, one {@link OutQueue} is used per destination).
 * 
 * @author yduchesne
 *
 */
public class OutqueueManager  {
	
  public static final int DEFAULT_OUTQUEUE_THREADS = 2;
  
  private Category                   log          = Log.createCategory(getClass());
  private Map<Destination, OutQueue> queuesByHost = new ConcurrentHashMap<Destination, OutQueue>();
  private ResponseSenderFactory      senders;
  
  /**
   * Shared {@link ExecutorService} instance, used to notify {@link OutQueueListener}s on {@link OutQueue} instances
   * about pending responses.
   */
  private static ExecutorService notifier;  

  /**
   * @param transports the {@link TransportManager}.
   * @param queue the {@link CallbackResponseQueue}.
   */
  OutqueueManager(TransportManager transports, CallbackResponseQueue queue, int maxOutqueueThreads) {
    this(new ResponseSenderFactoryImpl(transports, queue), maxOutqueueThreads);
  }
  
  /**
   * Creates an instance of this class with the given {@link ResponseSenderFactory}, which
   * is internally used to create {@link ResponseSender}s.
   * 
   * @param senderFactory a {@link ResponseSenderFactory}.
   */
  public OutqueueManager(ResponseSenderFactory senderFactory, int maxOutqueueThreads) {
    senders = senderFactory;
    notifier = Executors.newFixedThreadPool(
    		maxOutqueueThreads, 
        NamedThreadFactory
          .createWith("ubik.rmi.callback.outqueue.thread").setDaemon(true)
      );
    
  }
  
  /**
   * Returns the {@link OutQueue} instance corresponding to the specified {@link Destination}.
   * {@link Response} objects are indeed kept on a per-host basis, so all responses
   * corresponding to a given host are sent at once to the latter, in the same trip - this
   * eventually spares remote calls.
   * <p>
   * If an {@link OutQueue} does not yet exist for a given {@link Destination}, it is internally
   * created and cached. This method will also register an {@link OutQueueListener} to a newly
   * created {@link OutQueue}, so that it is notified when given responses are available for 
   * a {@link Destination}.
   * <p>
   * Internally, this class uses a {@link ResponseSender} to send the responses to the proper
   * destination.
   *
   * @return an {@link OutQueue} for the given {@link Destination}.
   */
  public OutQueue getQueueFor(Destination dest) {
    OutQueue out = queuesByHost.get(dest);
    if (out == null) {
      out = createOutQueueFor(dest);
    }
    return out;
  }
  
  synchronized void shutdown(long shutdownTimeout) {
    Iterator<OutQueue> queues = queuesByHost.values().iterator();
    while (queues.hasNext()) {
      try {
        queues.next().shutdown(shutdownTimeout);
      } catch (InterruptedException e) {
        break;
      }
    }
    notifier.shutdown();
  }
  
  private synchronized OutQueue createOutQueueFor(Destination dest) {
    OutQueue out = queuesByHost.get(dest);
    if (out == null) {
      out = new OutQueue(notifier, dest);
      out.addQueueListener(new OutQueueListener() {
        @Override
        public void onResponses(final Destination destination, final List<Response> responses) {
          log.debug("Sending %s responses to %s", responses.size(), destination);
          senders.getResponseSenderFor(destination).sendResponses(destination, responses);          
        }
      });
      queuesByHost.put(dest, out);
    }
    return out;    
  }
  
}
