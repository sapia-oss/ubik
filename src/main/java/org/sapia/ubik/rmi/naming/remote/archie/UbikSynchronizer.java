package org.sapia.ubik.rmi.naming.remote.archie;

import org.sapia.archie.Archie;
import org.sapia.archie.Name;
import org.sapia.archie.NamePart;
import org.sapia.archie.NotFoundException;
import org.sapia.archie.sync.SynchronizedNode;
import org.sapia.archie.sync.Synchronizer;

import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.ListenerAlreadyRegisteredException;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.mcast.RespList;
import org.sapia.ubik.mcast.Response;
import org.sapia.ubik.mcast.SyncEventListener;

import java.io.IOException;

import javax.naming.NamingException;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class UbikSynchronizer implements Synchronizer, AsyncEventListener,
  SyncEventListener {
  private EventChannel _channel;
  private Archie       _root;

  UbikSynchronizer(EventChannel channel) throws NamingException {
    _channel   = channel;
    _channel   = channel;
    _channel.registerAsyncListener(SyncPutEvent.class.getName(), this);

    try {
      _channel.registerSyncListener(SyncGetEvent.class.getName(), this);
    } catch (ListenerAlreadyRegisteredException e) {
      NamingException ne = new NamingException("Could not start event channel");
      ne.setRootCause(e);
      throw ne;
    }

    if (!_channel.isStarted()) {
      if (_channel.isClosed()) {
        throw new IllegalStateException("Event channel is closed!");
      }

      try {
        _channel.start();
      } catch (java.io.IOException e) {
        NamingException ne = new NamingException(
            "Could not start event channel");
        ne.setRootCause(e);
        throw ne;
      }
    }
  }

  /**
   * @return the <code>EventChannel</code> that this instance uses.
   */
  public EventChannel getEventChannel() {
    return _channel;
  }

  /**
   * @param root the <code>SynchronizedNode</code> that acts as the root node.
   */
  public void setRoot(SynchronizedNode root) {
    _root = new Archie(root);
  }

  /**
   * @see org.sapia.archie.sync.Synchronizer#onGetValue(org.sapia.archie.Name, org.sapia.archie.NamePart)
   */
  public Object onGetValue(Name nodeAbsolutePath, NamePart valueName) {
    Response toReturn = null;
    RespList results;

    try {
      results = _channel.send(SyncGetEvent.class.getName(),
          new SyncGetEvent(nodeAbsolutePath, valueName));
    } catch (java.io.IOException ioe) {
      ioe.printStackTrace();

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
   * @see org.sapia.archie.sync.Synchronizer#onPutValue(org.sapia.archie.Name, org.sapia.archie.NamePart, java.lang.Object, boolean)
   */
  public void onPutValue(Name nodeAbsolutePath, NamePart valueName,
    Object value, boolean overwrite) {
    SyncPutEvent evt = new SyncPutEvent(nodeAbsolutePath, valueName, value,
        overwrite);

    try {
      _channel.dispatch(SyncPutEvent.class.getName(), evt);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * @see org.sapia.archie.sync.Synchronizer#onRemoveValue(org.sapia.archie.Name, org.sapia.archie.NamePart)
   */
  public void onRemoveValue(Name nodeAbsolutePath, NamePart name) {
    SyncRemoveEvent evt = new SyncRemoveEvent(nodeAbsolutePath, name);

    try {
      _channel.dispatch(SyncRemoveEvent.class.getName(), evt);
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
        SyncPutEvent     put  = (SyncPutEvent) evt.getData();
        SynchronizedNode node = (SynchronizedNode) root().lookupNode(put.getNodePath(),
            true);
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
        SyncGetEvent     get  = (SyncGetEvent) evt.getData();
        SynchronizedNode node = (SynchronizedNode) root().lookupNode(get.getNodePath(),
            true);

        return node.synchronizeGet(get.getName());
      } catch (Exception e) {
        e.printStackTrace();

        return e;
      }
    }

    return null;
  }

  private Archie root() {
    if (_root == null) {
      throw new IllegalStateException("Root node was not set");
    }

    return _root;
  }
}
