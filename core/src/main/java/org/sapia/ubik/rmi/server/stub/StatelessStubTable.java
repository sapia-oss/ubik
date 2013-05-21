package org.sapia.ubik.rmi.server.stub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.Name;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.DomainName;
import org.sapia.ubik.mcast.MulticastAddress;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.module.Module;
import org.sapia.ubik.module.ModuleContext;
import org.sapia.ubik.rmi.naming.remote.archie.SyncPutEvent;
import org.sapia.ubik.rmi.server.EventChannelTable;
import org.sapia.ubik.util.Collections2;

/**
 * This class manages stateless stubs on the client-side and insures that notications
 * are properly dispatched.
 *
 * @author yduchesne
 */
public class StatelessStubTable implements Module {

  private Category             log                = Log.createCategory(getClass());
  private List<DomainContexts> domainContexts     = new ArrayList<DomainContexts>();
  private EventChannelTable    eventChannelTable; 

  @Override
  public void init(ModuleContext context) {
  }
  
  @Override
  public void start(ModuleContext context) {
    eventChannelTable = context.lookup(EventChannelTable.class);  
  }
  
  @Override
  public synchronized void stop() {
    domainContexts.clear();
  }
  
  /**
   * @param ref a {@link RemoteRefStateless} that will be internally kept.
   */
  public void registerStatelessRef(RemoteRefStateless ref, Collection<RemoteRefContext> newContexts){
    log.info("Registering stateless stub");
    
    DomainName dn = DomainName.parse(ref.getDomain());
    DomainContexts dc = getDomainContextsFor(dn, ref.getMulticastAddress());
    
    dc.registerStatelessRef(ref, newContexts);
  }
  
  private synchronized DomainContexts getDomainContextsFor(DomainName domainName, MulticastAddress addr) {
    for(DomainContexts dc : domainContexts) {
      if(dc.domainName.contains(domainName)) {
        return dc;
      }
    }
    
    DomainContexts dc = new DomainContexts(eventChannelTable, domainName, addr);
    domainContexts.add(dc);
    dc.registerWithEventChannel();
    return dc;
  }
  
  // --------------------------------------------------------------------------
 
  static class DomainContexts implements AsyncEventListener {
    
    private Category            log                  = Log.createCategory(getClass());
    private DomainName          domainName;
    private MulticastAddress    multicastAddress;
    private EventChannelTable   eventChannelTable; 

    private Map<Name, Contexts> contextsByObjectName = new TreeMap<Name, Contexts>();
    
    DomainContexts(EventChannelTable table, DomainName domainName, MulticastAddress address) {
      this.eventChannelTable = table;
      this.domainName        = domainName;
      this.multicastAddress  = address;
    }
    
    synchronized void registerStatelessRef(final RemoteRefStateless ref, Collection<RemoteRefContext> newContexts) {
      Contexts contexts = contextsByObjectName.get(ref.getName());
      if(contexts == null) {
        contexts = new Contexts();
        contextsByObjectName.put(ref.getName(), contexts);
      }
      
      log.debug("Registering %s new stub endpoints %s", newContexts.size(), newContexts);

      // subscribe stub's context list to future 
      contexts.addUpdateListener(ref.getContextList());
   
      // adding ref's contexts to the Contexts instance
      contexts.addAll(newContexts);
      if(log.isTrace()) {
        log.trace("Incoming ref updated with %s endpoints: %s", ref.getContextList().count(), ref.getContextList());
        Collection<RemoteRefContext> contextsForObjectName = contexts.getContexts();
        log.trace("Got %s shared endpoints registered under name: %s", contextsForObjectName.size(), ref.getName());
        Collections2.forEach(contextsForObjectName, new org.sapia.ubik.util.Condition<RemoteRefContext>() {
          @Override
          public boolean apply(RemoteRefContext item) {
            log.trace("  => %s", item.toString());
            return true;
          }
        });
      }
      
      // this synchronizes the ContextList kept at the stub level with the shared
      // Contexts instance (using anonymous class to avoid circular dependency 
      // between Contexts and ContextList)
      final Contexts toSynchronize = contexts;
      ref.getContextList().addRemovalListener(new ContextList.RemovalListener() {
        @Override
        public void onRemoval(RemoteRefContext removed) {
          toSynchronize.remove(removed);
        }
      });
    }
    
    void registerWithEventChannel() {
      eventChannelTable
        .getEventChannelFor(domainName.toString(), multicastAddress)
        .registerAsyncListener(SyncPutEvent.class.getName(), this);
    }
    
    public synchronized void onAsyncEvent(RemoteEvent evt) {
      try {
        log.debug("Remote binding event received: %s", evt.getType());      
        SyncPutEvent bEvt = (SyncPutEvent) evt.getData();
        
        // force deserialization
        bEvt.getValue();
      } catch (IOException e) {
        log.error("IO error receiving bound object", e);
      } catch (ClassNotFoundException e) {
        log.error("Class not found error receiving bound object", e);
      } catch (RuntimeException e) {
        log.error("Runtime error receiving bound object", e);
      }
    }
  }
}
