package org.sapia.ubik.mcast;

import java.lang.ref.SoftReference;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;


/**
 * Helper class that encasulates {@link AsyncEventListener} s and {@link SyncEventListener}s, 
 * grouping them by "event type". This class implements the dispatching of remote events to the encapsulated
 * listeners.
 *
 * @see org.sapia.ubik.mcast.AsyncEventListener
 * @see org.sapia.ubik.mcast.SyncEventListener
 * @see org.sapia.ubik.mcast.DomainName
 *
 * @author Yanick Duchesne
 */
public class EventConsumer {
  
  private static final List<SoftReference<AsyncEventListener>> EMPTY_ASYNC_LISTENERS  = Collections.synchronizedList(
                                                                                          new ArrayList<SoftReference<AsyncEventListener>>(1)
                                                                                        );
  private Category                                              log                   = Log.createCategory(getClass());
  private Map<String, List<SoftReference<AsyncEventListener>>>  asyncListenersByEvent = new ConcurrentHashMap<String, List<SoftReference<AsyncEventListener>>>();
  private Map<String, SoftReference<SyncEventListener>>         syncListenersByEvent  = new ConcurrentHashMap<String, SoftReference<SyncEventListener>>();
  private Map<Object, String>                                   reverseMap            = new WeakHashMap<Object, String>();
  private DomainName                                            domain;
  private String                                                node;

  /**
   * Creates an instance of this class, with the given node identifier, and the
   * given domain.
   */
  public EventConsumer(String node, String domain) {
    this.domain   = DomainName.parse(domain);
    this.node     = node;
    log.debug("Starting node: %s@%s", node, domain);
  }

  /**
   * Creates an instance of this class with the given domain.
   * Internally creates a globally unique node identifier.
   */
  public EventConsumer(String domain) throws UnknownHostException {
    this(UUID.randomUUID().toString(), domain);
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
   * Registers the given listener with the given "logical" event type.
   *
   * @param evtType a logical event type.
   * @param listener an {@link AsyncEventListener}.
   */
  public synchronized void registerAsyncListener(String evtType, AsyncEventListener listener) {
    List<SoftReference<AsyncEventListener>> lst = getAsyncListenersFor(evtType, true);

    if (!contains(lst, listener)) {
      lst.add(new SoftReference<AsyncEventListener>(listener));
      reverseMap.put(listener, evtType);
    }
    else{
      log.info("A listener is already registered for %", evtType);
    }
  }

  /**
   * Registers the given listener with the given "logical" event type.
   *
   * @param evtType a logical event type.
   * @param listener a {@link SyncEventListener}.
   */
  public synchronized void registerSyncListener(String evtType,
    SyncEventListener listener) throws ListenerAlreadyRegisteredException {
    if (syncListenersByEvent.get(evtType) != null) {
      throw new ListenerAlreadyRegisteredException(evtType);
    }
    syncListenersByEvent.put(evtType, new SoftReference<SyncEventListener>(listener));
    reverseMap.put(listener, evtType);
  }

  /**
   * Removes the given listener from this instance.
   *
   * @param listener the {@link SyncEventListener} to remove.
   */
  public synchronized void unregisterListener(SyncEventListener listener) {
    String evtId = (String) reverseMap.remove(listener);
    if (evtId != null) {
      syncListenersByEvent.remove(evtId);
    }
  }

  /**
   * Removes the given listener from this instance.
   *
   * @param listener the {@link AsyncEventListener} to remove.
   */
  public synchronized void unregisterListener(AsyncEventListener listener) {
    String evtId = (String) reverseMap.remove(listener);

    if (evtId != null) {
      List<SoftReference<AsyncEventListener>> lst = getAsyncListenersFor(evtId, false);

      if (lst != null) {
        SoftReference<AsyncEventListener> contained;
        AsyncEventListener instance;

        for (int i = 0; i < lst.size(); i++) {
          contained = lst.get(i);
          instance = contained.get();

          if (instance == null) {
            lst.remove(i--);
            continue;
          }

          if (contained.get().equals(instance)) {
            lst.remove(i--);
          }
        }
      }
    }
  }

  /**
   * Returns <code>true</code> if the passed in listener is held within this instance.
   *
   * @param listener an {@link AsyncEventListener}.
   * @return <code>true</code> if the passed in listener is held within this instance.
   */
  public boolean containsAsyncListener(AsyncEventListener listener) {
    String type = (String) reverseMap.get(listener);

    if (type != null) {
      List<SoftReference<AsyncEventListener>> listeners = asyncListenersByEvent.get(type);
      return contains(listeners, listener);
    }

    return false;
  }

  /**
   * Checks if a {@link SyncEventListener} exists for the given event type
   *
   * @param evtType the type of event for which to perform the check.
   * @return <code>true</code> if a {@link SyncEventListener} exists
   * for the given event type.
   */
  public boolean hasSyncListener(String evtType) {
    return syncListenersByEvent.get(evtType) != null;
  }

  /**
   * Returns <code>true</code> if the given listener is contained
   * by this instance.
   *
   * @param listener a {@link SyncEventListener}.
   * @return <code>true</code> if the given listener is contained
   * by this instance.
   */
  public boolean containsSyncListener(SyncEventListener listener) {
    return reverseMap.get(listener) != null;
  }

  /**
   * @return the total number of {@link AsyncEventListener}s and {@link SyncEventListener}s within this instance.
   */
  public int getListenerCount() {
    return reverseMap.size();
  }

  /**
   * Notification callback that internally dispatches the given remote event to this instance's 
   * appropriate event {@link AsyncEventListener}s.
   * <p>
   * <b>IMPORTANT</b>: this method dispatches the event in the thread that is performing the call.
   * 
   * @param evt a {@link RemoteEvent}.
   */
  public void onAsyncEvent(RemoteEvent evt) {
    DomainName dn = null;
    
    if(log.isDebug()){
      log.debug("Received remote event: " + evt.getType() + "@" + evt.getNode() + "@" + evt.getDomainName());
      log.debug("Event from this node: " + evt.getNode().equals(node));
    }

    if (evt.getDomainName() != null) {
      dn = DomainName.parse(evt.getDomainName());
    }

    if (matchesAll(dn, evt.getNode())) {
      log.debug("Notifying...");
      
      notifyAsyncListeners(evt);
    } else if (matchesThis(dn, evt.getNode())) {
      log.debug("Notifying...");
     
      notifyAsyncListeners(evt);
    } else {
      log.debug("Event was not matched: %s", evt.getType());        
    }
  }
  
  /**
   * Notification callback that internally dispatches the given remote event to this instance's {@link SyncEventListener}
   * that handles the given event's type.
   * <p>
   * The method returns the value that the {@link SyncEventListener} itself returns.
   * <p>
   * <b>IMPORTANT</b>: this method dispatches the event in the thread that is performing the call.
   * 
   * @param evt a {@link RemoteEvent}.
   * @return the {@link Object} that the matching {@link SyncEventListener} returned (or <code>null</code> if the listener
   * returns <code>null</code>, or if no listener was found for the given event's type).
   */
  public synchronized Object onSyncEvent(RemoteEvent evt) {
    DomainName dn = null;
    
    if(log.isDebug()){
      log.debug("Received remote event: " + evt.getType() + "@" + evt.getDomainName());
      log.debug("Event from this node: " + evt.getNode().equals(node));
    }    

    if (evt.getDomainName() != null) {
      dn = DomainName.parse(evt.getDomainName());
    }

    if ((dn == null) && (evt.getNode() != null) &&
          !evt.getNode().equals(node)) {
      SoftReference<SyncEventListener> reference = syncListenersByEvent.get(evt.getType());
      SyncEventListener sync = reference != null ? reference.get() : null;

      if (sync != null) {
        log.debug("Dispatching sync event to: %s", sync);
               
        return sync.onSyncEvent(evt);
      } else {
        log.debug("No listener for event: %s", evt.getType());
        syncListenersByEvent.remove(evt.getType());
      }
    } else if ((dn != null) && domain.contains(dn) && (evt.getNode() != null) &&
          !evt.getNode().equals(node)) {
      SoftReference<SyncEventListener> reference = syncListenersByEvent.get(evt.getType());
      SyncEventListener sync = reference != null ? reference.get() : null;

      if (sync != null) {
        log.debug("Dispatching sync event to: %s", sync);
        return sync.onSyncEvent(evt);
      } else {
        log.debug("No listener for event: %s", evt.getType());        
        syncListenersByEvent.remove(evt.getType());
      }
    }

    return null;
  }
  
  protected boolean matchesAll(DomainName dn, String node){
    return dn == null && node != null && !this.node.equals(node);
  }
  
  protected boolean matchesThis(DomainName dn, String node){
    return (dn != null) && domain.contains(dn) && 
          (node != null) &&
          !this.node.equals(node);
  }  

  protected boolean contains(List<SoftReference<AsyncEventListener>> listeners, AsyncEventListener listener) {
    if (listeners != null) {
      SoftReference<AsyncEventListener>      contained;
      AsyncEventListener instance;

      for (int i = 0; i < listeners.size(); i++) {
        contained = listeners.get(i);
        instance = contained.get();

        if (instance == null) {
          listeners.remove(i);
          i--;

          continue;
        }

        if (instance.equals(listener)) {
          return true;
        }
      }
    }

    return false;
  }

  private synchronized void notifyAsyncListeners(RemoteEvent evt) {
    List<SoftReference<AsyncEventListener>> lst      = getAsyncListenersFor(evt.getType(), false);
    if(lst.size() == 0){
      log.debug("No listener for event: %s", evt.getType());        
    }
    synchronized(lst) {
      for (int i = 0; i < lst.size(); i++) {
        AsyncEventListener listener = lst.get(i).get();
        if (listener == null) {
          log.debug("Async listener reference is null for: %s", evt.getType());
          lst.remove(i);
          i--;

          continue;
        }
        log.debug("Notifying async listener for: %s -> %s", evt.getType(), listener);
        listener.onAsyncEvent(evt);
      }
    }
  }

  private List<SoftReference<AsyncEventListener>> getAsyncListenersFor(String evtId, boolean create) {
    List<SoftReference<AsyncEventListener>> lst = asyncListenersByEvent.get(evtId);
    if ((lst == null)) {
      if(create) {
        lst = new ArrayList<SoftReference<AsyncEventListener>>();        
        asyncListenersByEvent.put(evtId, lst);
      } else {
        lst = EMPTY_ASYNC_LISTENERS;
      }
    }

    return lst;
  }

}
