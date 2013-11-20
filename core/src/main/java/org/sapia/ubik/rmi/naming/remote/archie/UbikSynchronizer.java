package org.sapia.ubik.rmi.naming.remote.archie;

import java.io.IOException;

import javax.naming.NamingException;

import org.sapia.archie.Archie;
import org.sapia.archie.Name;
import org.sapia.archie.NamePart;
import org.sapia.archie.NotFoundException;
import org.sapia.archie.sync.SynchronizedNode;
import org.sapia.archie.sync.Synchronizer;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.ListenerAlreadyRegisteredException;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.mcast.RespList;
import org.sapia.ubik.mcast.Response;
import org.sapia.ubik.mcast.SyncEventListener;

/**
 * Synchronizes distributed JNDI nodes by using an {@link EventChannel}.
 * 
 * @author Yanick Duchesne
 */
public class UbikSynchronizer implements Synchronizer, AsyncEventListener, SyncEventListener {

  private Category log = Log.createCategory(getClass());
  private EventChannel channel;
  private Archie root;

  UbikSynchronizer(EventChannel channel) throws NamingException {
    this.channel = channel;
    channel.registerAsyncListener(SyncPutEvent.class.getName(), this);

    try {
      channel.registerSyncListener(SyncGetEvent.class.getName(), this);
    } catch (ListenerAlreadyRegisteredException e) {
      NamingException ne = new NamingException("Could not start event channel");
      ne.setRootCause(e);
      throw ne;
    }

    if (!channel.isStarted()) {
      if (channel.isClosed()) {
        throw new IllegalStateException("Event channel is closed!");
      }

      try {
        channel.start();
      } catch (java.io.IOException e) {
        NamingException ne = new NamingException("Could not start event channel");
        ne.setRootCause(e);
        ne.fillInStackTrace();
        throw ne;
      }
    }
  }

  /**
   * @return the {@link EventChannel} that this instance uses.
   */
  public EventChannel getEventChannel() {
    return channel;
  }

  /**
   * @param root
   *          the {@link SynchronizedNode} that acts as the root node.
   */
  public void setRoot(SynchronizedNode root) {
    this.root = new Archie(root);
  }

  /**
   * @see org.sapia.archie.sync.Synchronizer#onGetValue(org.sapia.archie.Name,
   *      org.sapia.archie.NamePart)
   */
  public Object onGetValue(Name nodeAbsolutePath, NamePart valueName) {
    Response toReturn = null;
    RespList results;

    try {
      results = channel.send(SyncGetEvent.class.getName(), new SyncGetEvent(nodeAbsolutePath, valueName));
    } catch (java.io.IOException ioe) {
      log.error("IO Error caught sending command", ioe);
      return null;
    } catch (InterruptedException e) {
      log.info("Thread interrupted while sending command; returning null");
      return null;
    }

    for (int i = 0; i < results.count(); i++) {
      toReturn = results.get(i);

      if ((toReturn.getData() != null) && !(toReturn.isError())) {
        return toReturn.getData();
      }
    }

    return null;
  }

  /**
   * @see org.sapia.archie.sync.Synchronizer#onPutValue(org.sapia.archie.Name,
   *      org.sapia.archie.NamePart, java.lang.Object, boolean)
   */
  public void onPutValue(Name nodeAbsolutePath, NamePart valueName, Object value, boolean overwrite) {
    log.debug("Dispatching put for %s (%s)", nodeAbsolutePath, value);
    SyncPutEvent evt = new SyncPutEvent(nodeAbsolutePath, valueName, value, overwrite);

    try {
      channel.dispatch(SyncPutEvent.class.getName(), evt);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * @see org.sapia.archie.sync.Synchronizer#onRemoveValue(org.sapia.archie.Name,
   *      org.sapia.archie.NamePart)
   */
  public void onRemoveValue(Name nodeAbsolutePath, NamePart name) {
    SyncRemoveEvent evt = new SyncRemoveEvent(nodeAbsolutePath, name);

    try {
      channel.dispatch(SyncRemoveEvent.class.getName(), evt);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * @see org.sapia.ubik.mcast.AsyncEventListener#onAsyncEvent(org.sapia.ubik.mcast.RemoteEvent)
   */
  public void onAsyncEvent(RemoteEvent evt) {
    if (evt.getType().equals(SyncPutEvent.class.getName())) {
      try {
        SyncPutEvent put = (SyncPutEvent) evt.getData();
        SynchronizedNode node = (SynchronizedNode) root().lookupNode(put.getNodePath(), true);
        node.synchronizePut(put.getName(), put.getValue(), true);
      } catch (NotFoundException e) {
        // noop
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * @see org.sapia.ubik.mcast.SyncEventListener#onSyncEvent(org.sapia.ubik.mcast.RemoteEvent)
   */
  public Object onSyncEvent(RemoteEvent evt) {
    if (evt.getType().equals(SyncGetEvent.class.getName())) {
      try {
        SyncGetEvent get = (SyncGetEvent) evt.getData();
        SynchronizedNode node = (SynchronizedNode) root().lookupNode(get.getNodePath(), true);

        return node.synchronizeGet(get.getName());
      } catch (Exception e) {
        e.printStackTrace();

        return e;
      }
    }

    return null;
  }

  private Archie root() {
    if (root == null) {
      throw new IllegalStateException("Root node was not set");
    }

    return root;
  }
}
