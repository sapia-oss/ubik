package org.sapia.ubik.mcast;

import java.lang.ref.SoftReference;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.rmi.server.Log;


/**
 * Helper class that encasulates <code>AsyncEventListener</code>s and
 * <code>SyncEventListener</code>s, grouping them by "event type". This
 * class implements the dispatching of remote events to the encapsulated
 * listeners.
 *
 * @see org.sapia.ubik.mcast.AsyncEventListener
 * @see org.sapia.ubik.mcast.SyncEventListener
 * @see org.sapia.ubik.mcast.DomainName
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class EventConsumer {
  private Map<String, List<SoftReference<AsyncEventListener>>>  _asyncListenersByEvent = new ConcurrentHashMap<String, List<SoftReference<AsyncEventListener>>>();
  private Map<String, SoftReference<SyncEventListener>>  _syncListenersByEvent  = new ConcurrentHashMap<String, SoftReference<SyncEventListener>>();
  private Map<Object, String> _reverseMap = new WeakHashMap<Object, String>();
  private DomainName    _domain;
  private String        _node;

  /**
   * Creates an instance of this class, with the given node identifier, and the
   * given domain.
   */
  public EventConsumer(String node, String domain) {
    _domain   = DomainName.parse(domain);
    _node     = node;
    if(Log.isDebug()){
      Log.debug(getClass(), "Starting node: " + node + "@" + domain);
    }
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
   * @return this instance's node idenfier.
   */
  public String getNode() {
    return _node;
  }

  /**
   * Returns the object that represents this instance's domain name.
   *
   * @return this instance's <code>DomainName</code>
   */
  public DomainName getDomainName() {
    return _domain;
  }

  /**
   * Registers the given listener with the given "logical" event type.
   *
   * @param evtType a logical event type.
   * @param listener an <code>AsyncEventListener</code>.
   */
  public synchronized void registerAsyncListener(String evtType,
    AsyncEventListener listener) {
    List<SoftReference<AsyncEventListener>> lst = getAsyncListenersFor(evtType, true);

    if (!contains(lst, listener)) {
      lst.add(new SoftReference<AsyncEventListener>(listener));
      _reverseMap.put(listener, evtType);
    }
    else{
      Log.info(getClass(), "A listener is already registered for: " + evtType);
    }
  }

  /**
   * Registers the given listener with the given "logical" event type.
   *
   * @param evtType a logical event type.
   * @param listener a <code>SyncEventListener</code>.
   */
  public synchronized void registerSyncListener(String evtType,
    SyncEventListener listener) throws ListenerAlreadyRegisteredException {
    if (_syncListenersByEvent.get(evtType) != null) {
      throw new ListenerAlreadyRegisteredException(evtType);
    }

    _syncListenersByEvent.put(evtType, new SoftReference<SyncEventListener>(listener));
    _reverseMap.put(listener, evtType);
  }

  /**
   * Removes the given listener from this instance.
   *
   * @param listener the <code>SyncEventListener</code> to remove.
   */
  public synchronized void unregisterListener(SyncEventListener listener) {
    String evtId = (String) _reverseMap.remove(listener);

    if (evtId != null) {
      _syncListenersByEvent.remove(evtId);
    }
  }

  /**
   * Removes the given listener from this instance.
   *
   * @param listener the <code>AsyncEventListener</code> to remove.
   */
  public synchronized void unregisterListener(AsyncEventListener listener) {
    String evtId = (String) _reverseMap.remove(listener);

    if (evtId != null) {
      List<SoftReference<AsyncEventListener>> lst = getAsyncListenersFor(evtId, false);

      if (lst != null) {
        SoftReference<AsyncEventListener> contained;
        AsyncEventListener instance;

        for (int i = 0; i < lst.size(); i++) {
          contained = lst.get(i);
          instance = contained.get();

          if (instance == null) {
            lst.remove(i);
            i--;
            continue;
          }

          if (contained.get().equals(instance)) {
            lst.remove(i);
            break;
          }
        }
      }
    }
  }

  /**
   * Returns <code>true</code> if the passed in listener is held within this instance.
   *
   * @param listener an <code>AsyncEventListener</code>
   * @return <code>true</code> if the passed in listener is held within this instance.
   */
  public boolean containsAsyncListener(AsyncEventListener listener) {
    String type = (String) _reverseMap.get(listener);

    if (type != null) {
      List<SoftReference<AsyncEventListener>> listeners = _asyncListenersByEvent.get(type);

      return contains(listeners, listener);
    }

    return false;
  }

  /**
   * Checks if a <code>SyncEventListener</code> exists for the given event type
   *
   * @param evtType the type of event for which to perform the check.
   * @return <code>true</code> if a <code>SyncEventListener</code> exists
   * for the given event type.
   */
  public boolean hasSyncListener(String evtType) {
    return _syncListenersByEvent.get(evtType) != null;
  }

  /**
   * Returns <code>true</code> if the given listener is contained
   * by this instance.
   *
   * @param listener a <code>SyncEventListener</code>.
   * @return <code>true</code> if the given listener is contained
   * by this instance.
   */
  public boolean containsSyncListener(SyncEventListener listener) {
    return _reverseMap.get(listener) != null;
  }

  /**
   * Returns the number of listeners within this instance.
   * @return the number of listeners within this instance.
   */
  public int getCount() {
    return _reverseMap.size();
  }

  protected void onAsyncEvent(RemoteEvent evt) {
    DomainName dn = null;
    
    if(Log.isDebug()){
      Log.debug(getClass(), "Received remote event: " + evt.getType() + "@" + evt.getNode() + "@" + evt.getDomainName());
      Log.debug(getClass(), "Event from this node: " + evt.getNode().equals(_node));
    }

    if (evt.getDomainName() != null) {
      dn = DomainName.parse(evt.getDomainName());
    }

    if (matchesAll(dn, evt.getNode())) {
      if(Log.isDebug()){
        Log.debug(getClass(), "Notifying...");
      }
      notifyAsyncListeners(evt);
    } else if (matchesThis(dn, evt.getNode())) {
      if(Log.isDebug()){
        Log.debug(getClass(), "Notifying...");
      }      
      notifyAsyncListeners(evt);
    }
    else {
      if(Log.isDebug())
        Log.debug(getClass(), "Event was not matched: " + evt.getType());        
      
    }
  }
  
  protected boolean matchesAll(DomainName dn, String node){
    return dn == null && node != null &&
         !node.equals(_node);
  }
  
  protected boolean matchesThis(DomainName dn, String node){
    return (dn != null) && _domain.contains(dn) && 
          (node != null) &&
          !node.equals(_node);
  }  

  protected synchronized Object onSyncEvent(RemoteEvent evt) {
    DomainName dn = null;
    
    if(Log.isDebug()){
      Log.debug(getClass(), "Received remote event: " + evt.getType() + "@" + evt.getDomainName());
      Log.debug(getClass(), "Event from this node: " + evt.getNode().equals(_node));
    }    

    if (evt.getDomainName() != null) {
      dn = DomainName.parse(evt.getDomainName());
    }

    if ((dn == null) && (evt.getNode() != null) &&
          !evt.getNode().equals(_node)) {
      SoftReference<SyncEventListener> reference = _syncListenersByEvent.get(evt.getType());
      SyncEventListener sync = reference != null ? reference.get() : null;

      if (sync != null) {
        if(Log.isDebug()){
          Log.debug(getClass(), "Dispatching sync event to: " + sync);
        }        
        return sync.onSyncEvent(evt);
      } else {
        Log.debug(getClass(), "No listener for event: " + evt.getType());
        _syncListenersByEvent.remove(evt.getType());
      }
    } else if ((dn != null) && _domain.contains(dn) && (evt.getNode() != null) &&
          !evt.getNode().equals(_node)) {
      SoftReference<SyncEventListener> reference = _syncListenersByEvent.get(evt.getType());
      SyncEventListener sync = reference != null ? reference.get() : null;

      if (sync != null) {
        if(Log.isDebug()){
          Log.debug(getClass(), "Dispatching sync event to: " + sync);
        }                
        return sync.onSyncEvent(evt);
      } else {
        Log.debug(getClass(), "No listener for event: " + evt.getType());        
        _syncListenersByEvent.remove(evt.getType());
      }
    }

    return null;
  }

  private synchronized void notifyAsyncListeners(RemoteEvent evt) {
    List<SoftReference<AsyncEventListener>> lst      = getAsyncListenersFor(evt.getType(), false);

    if (lst != null) {
      if(lst.size() == 0){
        if(Log.isDebug())
          Log.debug(getClass(), "No listener for event: " + evt.getType());        
      }
      for (int i = 0; i < lst.size(); i++) {
        AsyncEventListener listener = lst.get(i).get();
        if (listener == null) {
          Log.debug(getClass(), "Async listener reference is null for: " + evt.getType());
          lst.remove(i);
          i--;

          continue;
        }
        Log.debug(getClass(), "Notifying async listener for: " + evt.getType() + " -> " + listener);
        listener.onAsyncEvent(evt);
      }
    }
    else{
      Log.debug(getClass(), "No async listeners for: " + evt.getType());
    }
  }

  private List<SoftReference<AsyncEventListener>> getAsyncListenersFor(String evtId, boolean create) {
    List<SoftReference<AsyncEventListener>> lst = _asyncListenersByEvent.get(evtId);

    if ((lst == null) && create) {
      lst = new ArrayList<SoftReference<AsyncEventListener>>();
      _asyncListenersByEvent.put(evtId, lst);
    }

    return lst;
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

}
