package org.sapia.ubik.rmi.naming.remote.archie;

import java.io.IOException;
import java.util.List;

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
import org.sapia.ubik.mcast.Defaults;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.EventChannelRef;
import org.sapia.ubik.mcast.ListenerAlreadyRegisteredException;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.mcast.RespList;
import org.sapia.ubik.mcast.Response;
import org.sapia.ubik.mcast.SyncEventListener;
import org.sapia.ubik.mcast.TimeoutException;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Conf;

/**
 * Synchronizes distributed JNDI nodes by using an {@link EventChannel}.
 *
 * @author Yanick Duchesne
 */
public class UbikSynchronizer implements Synchronizer, AsyncEventListener, SyncEventListener {

  private static final int SYNC_GET_BATCH_SIZE = Conf.getSystemProperties().getIntProperty(
      Consts.JNDI_SYNC_LOOKUP_BATCH_SIZE,
      Defaults.DEFAULT_JNDI_SYNC_LOOKUP_BATCH_SIZE
  );

  private Category        log = Log.createCategory(getClass());
  private EventChannelRef channel;
  private Archie          root;

  UbikSynchronizer(EventChannelRef channel) throws NamingException {
    this.channel = channel;
    channel.get().registerAsyncListener(SyncPutEvent.class.getName(), this);

    try {
      channel.get().registerSyncListener(SyncGetEvent.class.getName(), this);
    } catch (ListenerAlreadyRegisteredException e) {
      NamingException ne = new NamingException("Could not start event channel");
      ne.setRootCause(e);
      throw ne;
    }
  }

  /**
   * @return the {@link EventChannelRef} pointing to the EventChannel that this instance uses.
   */
  public EventChannelRef getEventChannel() {
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
  @Override
  public Object onGetValue(Name nodeAbsolutePath, NamePart valueName) {

    try {
      List<ServerAddress> nodeAddresses = channel.get().getView().getNodeAddresses();
      log.debug("Getting value for %s. Got %s nodes to look up from", nodeAbsolutePath, nodeAddresses.size());
      for (List<ServerAddress> addr : Collects.splitAsLists(nodeAddresses, SYNC_GET_BATCH_SIZE)) {
        try {
          RespList results = channel.get().send(addr, SyncGetEvent.class.getName(), new SyncGetEvent(nodeAbsolutePath, valueName));
          log.debug("Got %s results for %s", results.count(), nodeAbsolutePath);
          for (int i = 0; i < results.count(); i++) {
             Response res = results.get(i);
             if (!res.isThrowable() && !res.isNone()) {
               Object remote = res.getData();
               if (remote != null) {
                 return remote;
               }
             }
          }
        } catch (TimeoutException e) {
          // noop
        }
      }
    } catch (java.io.IOException ioe) {
      log.error("I/O Error caught dispatching SyncGetEvent", ioe);
      return null;
    } catch (InterruptedException e) {
      log.info("Thread interrupted while dispatching SyncGetEvent; returning null");
      return null;
    }
    return null;
  }

  /**
   * @see org.sapia.archie.sync.Synchronizer#onPutValue(org.sapia.archie.Name,
   *      org.sapia.archie.NamePart, java.lang.Object, boolean)
   */
  @Override
  public void onPutValue(Name nodeAbsolutePath, NamePart valueName, Object value, boolean overwrite) {
    log.debug("Dispatching put for %s (%s)", nodeAbsolutePath, value);
    SyncPutEvent evt = new SyncPutEvent(nodeAbsolutePath, valueName, value, overwrite);

    try {
      channel.get().dispatch(SyncPutEvent.class.getName(), evt).get();
    } catch (Exception e) {
      log.error("I/O error dispatching SyncPutEvent", e);
    }
  }

  /**
   * @see org.sapia.archie.sync.Synchronizer#onRemoveValue(org.sapia.archie.Name,
   *      org.sapia.archie.NamePart)
   */
  @Override
  public void onRemoveValue(Name nodeAbsolutePath, NamePart name) {
    SyncRemoveEvent evt = new SyncRemoveEvent(nodeAbsolutePath, name);

    try {
      log.debug("Dispatching remove  for %s", name);
      channel.get().dispatch(SyncRemoveEvent.class.getName(), evt).get();
    } catch (Exception e) {
      log.error("I/O error dispatching SyncRemoteEvent", e);
    }
  }

  /**
   * @see org.sapia.ubik.mcast.AsyncEventListener#onAsyncEvent(org.sapia.ubik.mcast.RemoteEvent)
   */
  @Override
  public void onAsyncEvent(RemoteEvent evt) {
    if (evt.getType().equals(SyncPutEvent.class.getName())) {
      try {
        SyncPutEvent put = (SyncPutEvent) evt.getData();
        log.debug("Received SyncPutEvent: %s", put.getNodePath());
        SynchronizedNode node = (SynchronizedNode) root().lookupNode(put.getNodePath(), true);
        node.synchronizePut(put.getName(), put.getValue(), true);
      } catch (NotFoundException e) {
        // noop
      } catch (Exception e) {
        log.error("I/O error receiving SyncPutEvent", e);
      }
    }
  }

  /**
   * @see org.sapia.ubik.mcast.SyncEventListener#onSyncEvent(org.sapia.ubik.mcast.RemoteEvent)
   */
  @Override
  public Object onSyncEvent(RemoteEvent evt) {
    if (evt.getType().equals(SyncGetEvent.class.getName())) {
      try {
        SyncGetEvent get = (SyncGetEvent) evt.getData();
        log.debug("Received SyncGetEvent: %s", get.getNodePath());
        SynchronizedNode node = (SynchronizedNode) root().lookupNode(get.getNodePath(), true);

        return node.synchronizeGet(get.getName());
      } catch (Exception e) {
        log.error("I/O error receiving SyncGetEvent", e);

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
