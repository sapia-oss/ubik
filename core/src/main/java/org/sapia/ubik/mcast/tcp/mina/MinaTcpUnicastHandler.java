package org.sapia.ubik.mcast.tcp.mina;

import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.mcast.Response;

/**
 * An instance of this class is hooked into Mina's request handling mechanism. It
 * receives Ubik commands and executes them.
 * 
 * @author yduchesne
 *
 */
public class MinaTcpUnicastHandler extends IoHandlerAdapter{
  
  private Category       log      = Log.createCategory(getClass());
  private EventConsumer  consumer;

  
  public MinaTcpUnicastHandler(EventConsumer consumer){
    this.consumer = consumer;
  }
  
  public void sessionCreated(IoSession sess) throws Exception {
    log.debug("Connection created from %s", sess.getRemoteAddress());
  }  

  public void exceptionCaught(IoSession sess, Throwable err) throws Exception {
    log.error("Exception caught", err);
    sess.close();
  }
  
  public void messageReceived(IoSession sess, Object o) throws Exception {
    
    try {
      if (o instanceof RemoteEvent) {
        RemoteEvent evt = (RemoteEvent) o;
        
        if (evt.isSync()) {
          if (consumer.hasSyncListener(evt.getType())) {
            log.debug("Received sync remote event %s from %s, notifying listener", evt.getType(), evt.getNode());
            Object response = consumer.onSyncEvent(evt);
            sess.write(new Response(evt.getId(), response));
          } else {
            log.debug("Received sync remote event %s from %s, no listener to notify", evt.getType(), evt.getNode());
            sess.write(new Response(evt.getId(), null).setNone());
          }
        } else {
          log.debug("Received async remote event %s from %s, no listener to notify", evt.getType(), evt.getNode());
          consumer.onAsyncEvent(evt);
        }
      } else {
        log.error("Object not a remote event: " + o.getClass().getName() + "; " + o);
      }
    } catch (Exception e) {
      log.error("Error caught handling request", e);
    }     
  }
  
}
