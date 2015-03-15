package org.sapia.ubik.mcast;

import java.io.IOException;
import java.net.ConnectException;
import java.rmi.RemoteException;

import org.sapia.ubik.concurrent.BlockingCompletionQueue;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.ThreadInterruptedException;

/**
 * Support class for implementing {@link UnicastDispatcher}s.
 * 
 * @author yduchesne
 *
 */
public abstract class UnicastDispatcherSupport implements UnicastDispatcher {
  
  protected Category log = Log.createCategory(getClass());

  /**
   * @param queue the {@link BlockingCompletionQueue} to which to add the {@link Response} object that this method
   * creates.
   * @param e the {@link Exception} that occurred.
   * @param evt the {@link RemoteEvent} whose sending was attempted.
   * @param addr the {@link ServerAddress} corresponding to the node to which the remote event was targeted.
   */
  protected void handleException(BlockingCompletionQueue<Response> queue, Exception e, RemoteEvent evt, ServerAddress addr) {
    
    if (e instanceof ClassNotFoundException) {
      log.warning("Could not deserialize response received from %s", e, addr);
      try {
        queue.add(new Response(addr, evt.getId(), e));
      } catch (IllegalStateException ise) {
        log.info("Could not add response to queue", ise, log.noArgs());
      }
    } else if (e instanceof TimeoutException) {
      log.warning("Response from %s not received in timely manner", addr);
      try {
        queue.add(new Response(addr, evt.getId(), e).setStatusSuspect());
      } catch (IllegalStateException ise) {
        log.info("Could not add response to queue", ise, log.noArgs());
      }
    } else if (e instanceof ConnectException) {
      log.warning("Remote node probably down: %s", e, addr);
      try {
        queue.add(new Response(addr, evt.getId(), e).setStatusSuspect());
      } catch (IllegalStateException ise) {
        log.info("Could not add response to queue", ise, log.noArgs());
      }
    } else if (e instanceof RemoteException){
      log.warning("Remote node probably down: %s", e, addr);
      try {
        queue.add(new Response(addr, evt.getId(), e).setStatusSuspect());
      } catch (IllegalStateException ise) {
        log.info("Could not add response to queue", ise, log.noArgs());
      }
    } else if (e instanceof IOException) {
      log.warning("IO error caught trying to send to %s", e, addr);
      try {
        queue.add(new Response(addr, evt.getId(), e));
      } catch (IllegalStateException ise) {
        log.info("Could not add response to queue", ise, log.noArgs());
      }
    } else if (e instanceof InterruptedException) {
      ThreadInterruptedException tie = new ThreadInterruptedException();
      throw tie;
    } else {
      log.warning("Error caught trying to send to %s", e, addr);
      try {
        queue.add(new Response(addr, evt.getId(), e));
      } catch (IllegalStateException ise) {
        log.info("Could not add response to queue", ise, log.noArgs());
      }            
    } 
    
  }
}
