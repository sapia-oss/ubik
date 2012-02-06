package org.sapia.ubik.rmi.naming.remote;

import java.io.IOException;
import java.lang.ref.SoftReference;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.net.ServerAddress;


/**
 * This class implements an {@link AsyncEventListener} that listens for appearing JNDI clients and servers on
 * a given {@link EventChannel}.
 * <p>
 * An instance of this class is meant to handle remote events corresponding to {@link JndiConsts#JNDI_CLIENT_PUBLISH}.
 * In response, it dispatches to the appearing client a {@link JndiConsts#JNDI_SERVER_DISCO} event.
 * <p>
 * An instance of this class must be explicitely registered/unregistered with/from an {@link EventChannel} using the 
 * {@link #registerWithChannel()} and {@link #unregisterFromChannel()} methods, respectively. The methods will add/remove
 * this instance from the underlying event channel, which will enable/disable the notification behavior, accordingly.
 * <p>
 * A reference to an instance of this class must be kept after registration with the event channel (an event channel keeps
 * listeners in {@link SoftReference}s). Not doing so will the instance subject to garbace collection. 
 * 
 * @author Yanick Duchesne
 */
public class ClientListener implements AsyncEventListener {
  
  private Category      log            = Log.createCategory(getClass());
  private EventChannel  channel;
  private ServerAddress serverAddress;

  /**
   * @param channel the {@link EventChannel} that this instance should use when dispatching events.
   * @param addr the {@link ServerAddress} corresponding to the address of the server node that this instance
   * will send to appearing client nodes, as part of the discovery process.
   */
  public ClientListener(EventChannel channel, ServerAddress addr) {
    this.channel       = channel;
    this.serverAddress = addr;
  }
  
  /**
   * Registers this instance with its internally kept {@link EventChannel}.
   */
  public void registerWithChannel() {
    this.channel.registerAsyncListener(JndiConsts.JNDI_CLIENT_PUBLISH, this);
    this.channel.registerAsyncListener(JndiConsts.JNDI_SERVER_DISCO, this);
  }
  
  /**
   * Unregisters this instance from its internally kept {@link EventChannel}.
   */
  public void unregisterFromChannel() {
    this.channel.unregisterAsyncListener(this);
  }

  /**
   * This method expects {@link RemoteEvent}s of type {@link JndiConsts#JNDI_CLIENT_PUBLISH}.
   * 
   * @param evt a {@link RemoteEvent}.
   */
  public void onAsyncEvent(RemoteEvent evt) {
    if (evt.getType().equals(JndiConsts.JNDI_CLIENT_PUBLISH)) {
      ServerAddress addr = channel.getView().getAddressFor(evt.getNode());

      try {
        if (addr != null) {        
          log.debug("Dispatching JNDI discovery event to expecting client: ", addr);
          channel.dispatch(addr, JndiConsts.JNDI_SERVER_DISCO, serverAddress);          
        } else{
          log.debug("Dispatching JNDI discovery event to whole domain");
          channel.dispatch(JndiConsts.JNDI_SERVER_DISCO, serverAddress);
        }
      } catch (IOException e) {
        log.error("Could not dispatch client presence", e);
      }
    }
  }
}
