package org.sapia.ubik.mcast.memory;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.sapia.ubik.concurrent.BlockingRef;
import org.sapia.ubik.concurrent.NamedThreadFactory;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.MulticastAddress;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.mcast.Response;
import org.sapia.ubik.net.ServerAddress;

/**
 * This class implements a singleton that serves as a communication bridge 
 * between in-memory dispatchers.
 * 
 * @see InMemoryBroadcastDispatcher
 * 
 * @author yduchesne
 *
 */
public final class InMemoryDispatchChannel {

  private static long SYNC_RESPONSE_TIMEOUT                                     = 30000;
  
  private static InMemoryDispatchChannel                   instance             = new InMemoryDispatchChannel();
  
  
  private Category log = Log.createCategory(getClass());
  
  private Map<MulticastAddress, SoftReference<InMemoryBroadcastDispatcher>> broadcastDispatchers = 
  		new ConcurrentHashMap<MulticastAddress, SoftReference<InMemoryBroadcastDispatcher>>();
  		
  private Map<ServerAddress, SoftReference<InMemoryUnicastDispatcher>>  unicastDispatchers   = 
  		new ConcurrentHashMap<ServerAddress, SoftReference<InMemoryUnicastDispatcher>>();
  
  private ExecutorService asyncEventProcessor = 
      Executors.newFixedThreadPool(3, NamedThreadFactory.createWith("InMemoryBroadcastChannel"));

  private ExecutorService syncEventProcessor  = 
      Executors.newFixedThreadPool(3, NamedThreadFactory.createWith("InMemoryBroadcastChannel"));
  

  private InMemoryDispatchChannel() {}
  
  static InMemoryDispatchChannel getInstance() {
    return instance;
  }
  
  void registerDispatcher(InMemoryBroadcastDispatcher dispatcher) {
    broadcastDispatchers.put(dispatcher.getMulticastAddress(), new SoftReference<InMemoryBroadcastDispatcher>(dispatcher));
  }
  
  void unregisterDispatcher(InMemoryBroadcastDispatcher dispatcher) {
  	broadcastDispatchers.remove(dispatcher.getMulticastAddress());
  }
  
  boolean isRegistered(InMemoryBroadcastDispatcher dispatcher) {
    SoftReference<InMemoryBroadcastDispatcher> dispatcherRef = broadcastDispatchers.get(dispatcher.getMulticastAddress());
    return dispatcherRef != null && dispatcherRef.get() != null;
  }
  
  void registerDispatcher(InMemoryUnicastDispatcher dispatcher) {
    unicastDispatchers.put(dispatcher.getAddress(), new SoftReference<InMemoryUnicastDispatcher>(dispatcher));
  }
  
  void unregisterDispatcher(InMemoryUnicastDispatcher dispatcher) {
  	unicastDispatchers.remove(dispatcher.getAddress());
  } 
  
  boolean isRegistered(InMemoryUnicastDispatcher dispatcher) {
    SoftReference<InMemoryUnicastDispatcher> dispatcherRef = unicastDispatchers.get(dispatcher.getAddress());
    return dispatcherRef != null && dispatcherRef.get() != null;
  }
  
  void dispatch(final InMemoryBroadcastDispatcher from, final RemoteEvent event) {
    
    asyncEventProcessor.execute(new Runnable() {
      @Override
      public void run() {
      	
      	for(MulticastAddress addr : broadcastDispatchers.keySet()) {
      		SoftReference<InMemoryBroadcastDispatcher> ref = broadcastDispatchers.get(addr);
      		if (ref == null) {
      			broadcastDispatchers.remove(addr);
      		} else {
      			InMemoryBroadcastDispatcher dispatcher = ref.get();
      			if (dispatcher == null) {
      				broadcastDispatchers.remove(addr);
      			} else if (!from.equals(dispatcher)) {
      				dispatcher.getConsumer().onAsyncEvent(event);
      			}
      		}
      	}
      }
    });
  }
  
  void sendASync(final ServerAddress destination, final RemoteEvent event) {
    asyncEventProcessor.execute(new Runnable() {
      
      @Override
      public void run() {
      	for(ServerAddress addr : unicastDispatchers.keySet()) {
      		SoftReference<InMemoryUnicastDispatcher> ref = unicastDispatchers.get(addr);
      		if (ref == null) {
      			unicastDispatchers.remove(addr);
      		} else {
      			InMemoryUnicastDispatcher dispatcher = ref.get();
      			if (dispatcher == null) {
      				unicastDispatchers.remove(addr);
      			} else if (dispatcher.getAddress().equals(destination)) {
      				dispatcher.getConsumer().onAsyncEvent(event);
      			}
      		}
      	}
      }
      
    });
  }
  
  Object sendSync(final ServerAddress destination, final RemoteEvent event) throws InterruptedException {
    
    final BlockingRef<Response> resp = new BlockingRef<Response>();
    
    syncEventProcessor.execute(new Runnable() {
      
      @Override
      public void run() {
        
        for(ServerAddress addr : unicastDispatchers.keySet()) {
      		SoftReference<InMemoryUnicastDispatcher> ref = unicastDispatchers.get(addr);
      		if (ref == null) {
      			unicastDispatchers.remove(addr);
      		} else {
      			InMemoryUnicastDispatcher dispatcher = ref.get();
      			if (dispatcher == null) {
      				unicastDispatchers.remove(addr);
      			} else if (dispatcher.getAddress().equals(destination)) {
              if(dispatcher.getConsumer().hasSyncListener(event.getType())) {
              	log.debug("Dispatching to sync listener: %s", event.getType());
                resp.set(new Response(event.getId(), dispatcher.getConsumer().onSyncEvent(event)));
                return;
              } else {
              	log.debug("No listener found for %s", event.getType());
                resp.set(new Response(event.getId(), null).setNone());
                return;
              }
      			}
      		}
        }
        log.debug("Could not dispatch %s (no listener found)", event.getType());
        resp.set(new Response(event.getId(), null).setNone().setStatusSuspect());
      }
      
    });
    
    Response r = resp.await(SYNC_RESPONSE_TIMEOUT);
    
    if (r == null) {
    	r =  new Response(event.getId(), null).setNone().setStatusSuspect();
    }
    return r;
  }  
  
}
