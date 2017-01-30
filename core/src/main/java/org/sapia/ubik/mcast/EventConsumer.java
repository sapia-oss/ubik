package org.sapia.ubik.mcast;

import java.lang.ref.SoftReference;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.sapia.ubik.concurrent.NamedThreadFactory;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.util.Chrono;
import org.sapia.ubik.util.SoftReferenceList;

/**
 * Helper class that encasulates {@link AsyncEventListener} s and
 * {@link SyncEventListener}s, grouping them by "event type". This class
 * implements the dispatching of remote events to the encapsulated listeners.
 *
 * @see org.sapia.ubik.mcast.AsyncEventListener
 * @see org.sapia.ubik.mcast.SyncEventListener
 * @see org.sapia.ubik.mcast.DomainName
 *
 * @author Yanick Duchesne
 */
public class EventConsumer {

  private static final SoftReferenceList<AsyncEventListener> EMPTY_ASYNC_LISTENERS = new SoftReferenceList<AsyncEventListener>();

  private Category log = Log.createCategory(getClass());
  private Map<String, SoftReferenceList<AsyncEventListener>> asyncListenersByEvent = new ConcurrentHashMap<String, SoftReferenceList<AsyncEventListener>>();
  private Map<String, SoftReference<SyncEventListener>> syncListenersByEvent = new ConcurrentHashMap<String, SoftReference<SyncEventListener>>();
  private Map<Object, String> reverseMap = Collections.synchronizedMap(new WeakHashMap<Object, String>());
  private volatile DomainName domain;
  private String node;
  private ExecutorService executor;

  /**
   * Creates an instance of this class, with the given node identifier, and the
   * given domain.
   */
  public EventConsumer(String node, String domain, int threadCount) {
    this.domain = DomainName.parse(domain);
    this.node = node;
    this.executor = Executors.newFixedThreadPool(threadCount, NamedThreadFactory.createWith("Ubik.EventConsumer.Consumer").setDaemon(true));
    log.debug("Starting node: %s@%s", node, domain);
  }
  
  /**
   * Creates an instance of this class with the given domain. Internally creates
   * a globally unique node identifier.
   */
  public EventConsumer(String domain, int threadCount) throws UnknownHostException {
    this(UUID.randomUUID().toString().replace("-", ""), domain, threadCount);
  }

  public void stop() {
    if (executor != null) {
      executor.shutdownNow();
    }
  }
  
  /**
   * Returns the node identifier of this instance.
   *
   * @return this instance's node identifier.
   */
  public String getNode() {
    return node;
  }

  /**
   * Returns the object that represents this instance's domain name.
   *
   * @return this instance's {@link DomainName}.
   */
  public DomainName getDomainName() {
    return domain;
  }

  /**
   * @param newDomain the new domain to change to.
   */
  public void changeDomain(String newDomain) {
    this.domain = DomainName.parse(newDomain);
  }

  /**
   * Registers the given listener with the given "logical" event type.
   *
   * @param evtType
   *          a logical event type.
   * @param listener
   *          an {@link AsyncEventListener}.
   */
  public void registerAsyncListener(String evtType, AsyncEventListener listener) {
    SoftReferenceList<AsyncEventListener> lst = getAsyncListenersFor(evtType, true);
    synchronized (lst) {
      if (!lst.contains(listener)) {
        lst.add(listener);
        reverseMap.put(listener, evtType);
      } else {
        log.info("A listener is already registered for %s", evtType);
      }
    }
  }

  /**
   * Registers the given listener with the given "logical" event type.
   *
   * @param evtType
   *          a logical event type.
   * @param listener
   *          a {@link SyncEventListener}.
   */
  public void registerSyncListener(String evtType, SyncEventListener listener) throws ListenerAlreadyRegisteredException {
    synchronized (syncListenersByEvent) {
      if (syncListenersByEvent.get(evtType) != null) {
        throw new ListenerAlreadyRegisteredException(evtType);
      }
      syncListenersByEvent.put(evtType, new SoftReference<SyncEventListener>(listener));
      reverseMap.put(listener, evtType);
    }
  }

  /**
   * Removes the given listener from this instance.
   *
   * @param listener
   *          the {@link SyncEventListener} to remove.
   */
  public void unregisterListener(SyncEventListener listener) {
    String evtId = reverseMap.remove(listener);
    if (evtId != null) {
      syncListenersByEvent.remove(evtId);
    }
  }

  /**
   * Removes the given listener from this instance.
   *
   * @param listener
   *          the {@link AsyncEventListener} to remove.
   */
  public void unregisterListener(AsyncEventListener listener) {
    String type = reverseMap.remove(listener);

    if (type != null) {
      SoftReferenceList<AsyncEventListener> lst = getAsyncListenersFor(type, false);
      if (lst != null) {
        lst.remove(listener);
      }
    }
  }

  /**
   * Returns <code>true</code> if the passed in listener is held within this
   * instance.
   *
   * @param listener
   *          an {@link AsyncEventListener}.
   * @return <code>true</code> if the passed in listener is held within this
   *         instance.
   */
  public boolean containsAsyncListener(AsyncEventListener listener) {
    String type = reverseMap.get(listener);
    if (type != null) {
      SoftReferenceList<AsyncEventListener> listeners = asyncListenersByEvent.get(type);
      if (listeners != null) {
        return listeners.contains(listener);
      }
    }
    return false;
  }

  /**
   * Checks if a {@link SyncEventListener} exists for the given event type
   *
   * @param evtType
   *          the type of event for which to perform the check.
   * @return <code>true</code> if a {@link SyncEventListener} exists for the
   *         given event type.
   */
  public boolean hasSyncListener(String evtType) {
    return syncListenersByEvent.get(evtType) != null;
  }

  /**
   * Returns <code>true</code> if the given listener is contained by this
   * instance.
   *
   * @param listener
   *          a {@link SyncEventListener}.
   * @return <code>true</code> if the given listener is contained by this
   *         instance.
   */
  public boolean containsSyncListener(SyncEventListener listener) {
    return reverseMap.get(listener) != null;
  }

  /**
   * @return the total number of {@link AsyncEventListener}s and
   *         {@link SyncEventListener}s within this instance.
   */
  public int getListenerCount() {
    return reverseMap.size();
  }

  /**
   * Notification callback that internally dispatches the given remote event to
   * this instance's appropriate event {@link AsyncEventListener}s.
   * <p>
   * <b>IMPORTANT</b>: this method dispatches the event in a new consumer thread.
   *
   * @param evt
   *          a {@link RemoteEvent}.
   */
  public void onAsyncEvent(RemoteEvent evt) {
    DomainName dn = null;

    if (log.isDebug()) {
      log.debug("Received remote event: " + evt.getType() + "@" + evt.getNode() + "@" + evt.getDomainName());
      log.debug("This node: '%s'", node);
      log.debug("Event from this node: " + evt.getNode().equals(node));
    }

    if (evt.getDomainName() != null) {
      dn = DomainName.parse(evt.getDomainName());
    }

    if (matchesAll(dn, evt.getNode())) {
      Chrono chrono = new Chrono();
      log.debug("Notifying async listeners...");
      notifyAsyncListeners(evt);
      log.debug("Completed async notification of event %s in %s millis", evt.getType(), chrono.getElapsed());

    } else if (matchesThis(dn, evt.getNode())) {
      Chrono chrono = new Chrono();
      log.debug("Notifying async listeners...");
      notifyAsyncListeners(evt);
      log.debug("Completed async notification of event %s in %s millis", evt.getType(), chrono.getElapsed());

    } else {
      log.debug("Event was not matched: %s", evt.getType());
    }
  }

  /**
   * Notification callback that internally dispatches the given remote event to
   * this instance's {@link SyncEventListener} that handles the given event's
   * type.
   * <p>
   * The method returns the value that the {@link SyncEventListener} itself
   * returns.
   * <p>
   * <b>IMPORTANT</b>: this method dispatches the event in the thread that is
   * performing the call.
   *
   * @param evt
   *          a {@link RemoteEvent}.
   * @return the {@link Object} that the matching {@link SyncEventListener}
   *         returned (or <code>null</code> if the listener returns
   *         <code>null</code>, or if no listener was found for the given
   *         event's type).
   */
  public Object onSyncEvent(RemoteEvent evt) {
    DomainName dn = null;

    if (log.isDebug()) {
      log.debug("Received remote event: %s@%s ", evt.getType(), evt.getDomainName());
      log.debug("Event from this node: %s", evt.getNode().equals(node));
    }

    if (evt.getDomainName() != null) {
      dn = DomainName.parse(evt.getDomainName());
    }

    if ((dn == null) && (evt.getNode() != null) && !evt.getNode().equals(node)) {
      SoftReference<SyncEventListener> reference = syncListenersByEvent.get(evt.getType());
      SyncEventListener sync = reference != null ? reference.get() : null;

      if (sync != null) {
        log.debug("Dispatching sync event to: %s", sync);

        Chrono chrono = new Chrono();
        Object result = sync.onSyncEvent(evt);
        log.debug("Completed sync notification of event %s in %s millis", evt.getType(), chrono.getElapsed());
        return result;
      } else {
        log.debug("No listener for event: %s", evt.getType());
        syncListenersByEvent.remove(evt.getType());
      }
    } else if ((dn != null) && domain.contains(dn) && (evt.getNode() != null) && !evt.getNode().equals(node)) {
      SoftReference<SyncEventListener> reference = syncListenersByEvent.get(evt.getType());
      SyncEventListener sync = reference != null ? reference.get() : null;

      if (sync != null) {
        log.debug("Dispatching sync event to: %s", sync);
        Chrono chrono = new Chrono();
        Object result = sync.onSyncEvent(evt);
        log.debug("Completed sync notification of event %s in %s millis", evt.getType(), chrono.getElapsed());
        return result;
      } else {
        log.debug("No listener for event: %s", evt.getType());
        syncListenersByEvent.remove(evt.getType());
      }
    }

    return null;
  }

  protected boolean matchesAll(DomainName dn, String node) {
    return dn == null && node != null && !this.node.equals(node);
  }

  protected boolean matchesThis(DomainName dn, String node) {
    return (dn != null) && domain.contains(dn) && (node != null) && !this.node.equals(node);
  }

  private void notifyAsyncListeners(RemoteEvent evt) {
    SoftReferenceList<AsyncEventListener> lst = getAsyncListenersFor(evt.getType(), false);
    if (lst.getApproximateSize() == 0) {
      log.debug("No listener for event: %s", evt.getType());
    }
    synchronized (lst) {
      for (AsyncEventListener listener : lst) {
        log.debug("Notifying async listener for: %s -> %s", evt.getType(), listener);
        executor.execute(() -> {
          try {
            listener.onAsyncEvent(evt);
          } catch (Exception e) {
            log.warning("System error notifiying listener of async event %s -> &s", e, evt.getType(), listener);
          }
        });
      }
    }
  }

  private SoftReferenceList<AsyncEventListener> getAsyncListenersFor(String evtId, boolean create) {
    SoftReferenceList<AsyncEventListener> lst = asyncListenersByEvent.get(evtId);
    if ((lst == null)) {
      if (create) {
        synchronized (asyncListenersByEvent) {
          lst = asyncListenersByEvent.get(evtId);
          if (lst == null) {
            lst = new SoftReferenceList<AsyncEventListener>();
            asyncListenersByEvent.put(evtId, lst);
          }
        }
      } else {
        lst = EMPTY_ASYNC_LISTENERS;
      }
    }
    return lst;
  }
}
