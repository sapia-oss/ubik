package org.sapia.ubik.mcast.memory;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.sapia.ubik.concurrent.BlockingRef;
import org.sapia.ubik.concurrent.NamedThreadFactory;
import org.sapia.ubik.mcast.BroadcastDispatcher;
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
final class InMemoryDispatchChannel {

  private static long SYNC_RESPONSE_TIMEOUT                                     = 5000;
  
  private static InMemoryDispatchChannel                   instance             = new InMemoryDispatchChannel();
  
  private List<SoftReference<InMemoryBroadcastDispatcher>> broadcastDispatchers = Collections.synchronizedList(
                                                                     new ArrayList<SoftReference<InMemoryBroadcastDispatcher>>()
                                                                   );
  
  private List<SoftReference<InMemoryUnicastDispatcher>>   unicastDispatchers   = Collections.synchronizedList(
                                                                     new ArrayList<SoftReference<InMemoryUnicastDispatcher>>()
                                                                   );  
  
  private ExecutorService                                  eventProcessor       = Executors.newFixedThreadPool(
                                                                     3,
                                                                     NamedThreadFactory.createWith("InMemoryBroadcastChannel")
                                                                   );

  private InMemoryDispatchChannel() {}
  
  static InMemoryDispatchChannel getInstance() {
    return instance;
  }
  
  void registerDispatcher(InMemoryBroadcastDispatcher dispatcher) {
    broadcastDispatchers.add(new SoftReference<InMemoryBroadcastDispatcher>(dispatcher));
  }
  
  void unregisterDispatcher(InMemoryBroadcastDispatcher dispatcher) {
    synchronized(broadcastDispatchers) {
      for(int i = 0; i < broadcastDispatchers.size(); i++) {
        SoftReference<InMemoryBroadcastDispatcher> dispatcherRef = broadcastDispatchers.get(i);
        BroadcastDispatcher registered = dispatcherRef.get();
        if(registered == null) {
          broadcastDispatchers.remove(i--);
        } else if(registered.equals(dispatcher)) {
          broadcastDispatchers.remove(i--);
        }
      }
    }
  }
  
  boolean isRegistered(InMemoryBroadcastDispatcher dispatcher) {
    synchronized(broadcastDispatchers) {
      for(int i = 0; i < broadcastDispatchers.size(); i++) {
        SoftReference<InMemoryBroadcastDispatcher> dispatcherRef = broadcastDispatchers.get(i);
        BroadcastDispatcher registered = dispatcherRef.get();
        if(registered == null) {
          broadcastDispatchers.remove(i--);
        } else if(registered.equals(dispatcher)) {
          return true;
        }
      }
    }
    return false;
  }
  
  void registerDispatcher(InMemoryUnicastDispatcher dispatcher) {
    unicastDispatchers.add(new SoftReference<InMemoryUnicastDispatcher>(dispatcher));
  }
  
  void unregisterDispatcher(InMemoryUnicastDispatcher dispatcher) {
    synchronized(unicastDispatchers) {
      for(int i = 0; i < unicastDispatchers.size(); i++) {
        SoftReference<InMemoryUnicastDispatcher> dispatcherRef = unicastDispatchers.get(i);
        InMemoryUnicastDispatcher registered = dispatcherRef.get();
        if(registered == null) {
          unicastDispatchers.remove(i--);
        } else if(registered.equals(dispatcher)) {
          unicastDispatchers.remove(i--);
        }
      }
    }
  } 
  
  boolean isRegistered(InMemoryUnicastDispatcher dispatcher) {
    synchronized(unicastDispatchers) {
      for(int i = 0; i < unicastDispatchers.size(); i++) {
        SoftReference<InMemoryUnicastDispatcher> dispatcherRef = unicastDispatchers.get(i);
        InMemoryUnicastDispatcher registered = dispatcherRef.get();
        if(registered == null) {
          unicastDispatchers.remove(i--);
        } else if(registered.equals(dispatcher)) {
          return true;
        }
      }
    }
    return false;
  }
  
  void dispatch(final InMemoryBroadcastDispatcher from, final RemoteEvent event) {
    
    eventProcessor.execute(new Runnable() {
      @Override
      public void run() {
        synchronized(broadcastDispatchers) {
          for(int i = 0; i < broadcastDispatchers.size(); i++) {
            SoftReference<InMemoryBroadcastDispatcher> ref = broadcastDispatchers.get(i);
            InMemoryBroadcastDispatcher dispatcher = ref.get();
            if(dispatcher == null) {
              broadcastDispatchers.remove(i--);
            } else if(!from.equals(dispatcher)) {
              dispatcher.getConsumer().onAsyncEvent(event);
            }
          }
        }
      }
    });
  }
  
  void sendASync(final ServerAddress destination, final RemoteEvent event) {
    eventProcessor.execute(new Runnable() {
      
      @Override
      public void run() {
        synchronized(unicastDispatchers) {
          for(int i = 0; i < unicastDispatchers.size(); i++) {
            SoftReference<InMemoryUnicastDispatcher> ref = unicastDispatchers.get(i);
            InMemoryUnicastDispatcher dispatcher = ref.get();
            if(dispatcher == null) {
              unicastDispatchers.remove(i--);
            } else  if(dispatcher.getAddress().equals(destination)) {
              dispatcher.getConsumer().onAsyncEvent(event);
            }
          }
        }        
      }
      
    });
  }
  
  Object sendSync(final ServerAddress destination, final RemoteEvent event) throws InterruptedException {
    
    final BlockingRef<Response> resp = new BlockingRef<Response>();
    
    eventProcessor.execute(new Runnable() {
      
      @Override
      public void run() {
        synchronized(unicastDispatchers) {
          for(int i = 0; i < unicastDispatchers.size(); i++) {
            SoftReference<InMemoryUnicastDispatcher> ref = unicastDispatchers.get(i);
            InMemoryUnicastDispatcher dispatcher = ref.get();
            if(dispatcher == null) {
              unicastDispatchers.remove(i--);
            } else if(dispatcher.getAddress().equals(destination)) {
              if(dispatcher.getConsumer().hasSyncListener(event.getType())) {
                resp.set(new Response(event.getId(), dispatcher.getConsumer().onSyncEvent(event)));
              } else {
                resp.set(new Response(event.getId(), null).setNone());                
              }
            }

          }
        }        
      }
      
    });
    
    return resp.await(SYNC_RESPONSE_TIMEOUT);
  }  
  
}
