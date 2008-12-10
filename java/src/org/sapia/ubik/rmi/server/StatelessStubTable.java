/*
 * StatelessStubTable.java
 *
 * Created on October 31, 2005, 11:14 AM
 */

package org.sapia.ubik.rmi.server;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.rmi.naming.remote.archie.SyncPutEvent;

/**
 * This class manages stateless stubs on the client-side and insures that notications
 * are properly dispatched.
 *
 * @author yduchesne
 */
public class StatelessStubTable implements AsyncEventListener{
  
  private static Map                _stubs    = Collections.synchronizedMap(new HashMap());
  private static StatelessStubTable _instance = new StatelessStubTable();
  
  /** Creates a new instance of StatelessStubTable */
  StatelessStubTable() {
  }
  
  /**
   * @param ref a <code>RemoteRefStateless</code> that will be internally kept.
   */
  public static synchronized void registerStatelessRef(RemoteRefStateless ref){
    if(Log.isInfo())
      Log.info(StatelessStubTable.class, "Registering stateless stub");
    
    EventChannel channel = EventChannelSingleton.getEventChannelFor(ref._domain, ref._mcastAddress, ref._mcastPort);
    
    if(!channel.containsAsyncListener(_instance)){
      channel.registerAsyncListener(SyncPutEvent.class.getName(), _instance);      
    }
    ref.clean();
    doRegister(ref);
  }
  
  public void onAsyncEvent(RemoteEvent evt) {
    try {
      
      if(Log.isInfo())
        Log.info(getClass(), "Remote binding event received: " + evt.getType());      
      SyncPutEvent bEvt = (SyncPutEvent) evt.getData();
      
      Object       bound;
      try {
        bound = bEvt.getValue();
      } catch (IOException e) {
        Log.error(getClass(), "Error receiving bound object", e);

        return;
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
        Log.error(getClass(), "Error receiving bound object", e);

        return;
      }
      
      StubInvocationHandler handler = null;

      if (bound instanceof StubContainer) {
        handler = ((StubContainer) bound).getStubInvocationHandler();
      } else if (Proxy.isProxyClass(bound.getClass()) && bound instanceof Stub) {
        handler = (StubInvocationHandler) Proxy.getInvocationHandler(bound);
      }
      
      if ((handler != null) && handler instanceof RemoteRefStateless) {
        RemoteRefStateless other = (RemoteRefStateless) handler;

        synchronized(_stubs){
          List siblings = (List)_stubs.get(other._domain);
          if(siblings == null){
            siblings = new ArrayList();
            _stubs.put(other._domain,  siblings);
          }
          addSiblings(siblings, other);
        }

      }
    } catch (IOException e) {
      Log.error(getClass(), e);
    } catch (RuntimeException e) {
      Log.error(getClass(), e);
    }    
  }
  
  static synchronized void doRegister(RemoteRefStateless ref){
    synchronized(_stubs){
      List siblings = (List)_stubs.get(ref._domain);
      if(siblings == null){
        siblings = new ArrayList();
        _stubs.put(ref._domain,  siblings);
      }
      siblings.add(new SoftReference(ref));
      addSiblings(siblings, ref);      
    }
  }  
  
  static List getSiblings(String domain){
    return (List)_stubs.get(domain);
  }
  
  private static void addSiblings(List siblings, RemoteRefStateless other){
    for(int i = 0; i < siblings.size(); i++){
      SoftReference ref = (SoftReference)siblings.get(i);
      RemoteRefStateless remoteRef = (RemoteRefStateless)ref.get();
      if(remoteRef == null){
        siblings.remove(i--);
      }
      else{
        if(remoteRef._name.equals(other._name) && !remoteRef._oid.equals(other._oid)){
          Log.info(StatelessStubTable.class, "Binding received for name: " + other._name);
          if(remoteRef.clean()){
            other.addSibling(remoteRef);            
          }
          remoteRef.addSibling(other);
        }
      }
    }
  }
}
