package org.sapia.ubik.rmi.server.gc;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.module.Module;
import org.sapia.ubik.module.ModuleContext;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.oid.DefaultOID;
import org.sapia.ubik.rmi.server.oid.OID;
import org.sapia.ubik.rmi.server.stats.Hits;
import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.rmi.server.transport.TransportManager;
import org.sapia.ubik.taskman.Task;
import org.sapia.ubik.taskman.TaskContext;
import org.sapia.ubik.taskman.TaskManager;
import org.sapia.ubik.util.Props;

/**
 * This class implements a the client-side distributed garbage collection algorithm.
 *
 * @author Yanick Duchesne
 */
public class ClientGC implements Module, Task, ClientGCMBean {
  
  /* time at which GC checks for unreferenced objects */
  public static final long GC_CLEAN_INTERVAL = 10000;

  /* size of OID batches sent to the server */
  public static final int GC_CLEAN_SIZE = 1000;
  
  private static Category    log                = Log.createCategory(ClientGC.class);
  
  private HostReferenceTable objByHosts         = new HostReferenceTable();
  private TaskManager        taskMan;
  private TransportManager   transport;

  private long               gcInterval         = GC_CLEAN_INTERVAL;
  private int                gcBatchSize        = GC_CLEAN_SIZE;
  private int                threshold; 
  private int                lastGcCount;
  private long               lastGcTime;  
  
  private Hits               gcRefPerMin;
  private Hits               gcDerefPerMin;
  private Hits               gcConnectionsPerMin;
  private Hits               forcedGcPerHour;
  
  
  @Override
  public void init(ModuleContext context) {
    Props props = new Props().addProperties(System.getProperties());
  
    this.gcInterval            = props.getLongProperty(Consts.CLIENT_GC_INTERVAL, GC_CLEAN_INTERVAL);
    this.gcBatchSize           = props.getIntProperty(Consts.CLIENT_GC_BATCHSIZE, GC_CLEAN_SIZE);
    this.threshold             = props.getIntProperty(Consts.CLIENT_GC_THRESHOLD, 0);
    
    log.info("Will run every %s ms.", gcInterval);
  }
  
  @Override
  public void start(ModuleContext context) {
    taskMan   = context.lookup(TaskManager.class);
    taskMan.addTask(new TaskContext("ubik.rmi.client.GC", gcInterval), this);
    transport = context.lookup(TransportManager.class);
    
    this.gcConnectionsPerMin   = Stats.getInstance().getHitsBuilder(
                                   getClass(), 
                                   "ConnectionsPerMin", 
                                   "The number of connections per minute create to send GC commands")
                                   .perMinute().build();
    
    this.gcRefPerMin           = Stats.getInstance().getHitsBuilder(
                                   getClass(),
                                   "RefPerMin",
                                   "The number of remote object that are referenced per minute")
                                   .perMinute().build();

    this.gcDerefPerMin         = Stats.getInstance().getHitsBuilder(
                                   getClass(), 
                                   "DerefPerMin",
                                   "The number of remote object that are dereferenced per minute")
                                   .perMinute().build();
    
    this.forcedGcPerHour       = Stats.getInstance().getHitsBuilder(
                                   getClass(),
                                  "ForcedGcPerHour",
                                  "The number of forced JVM gc per hour")
                                  .perHour().build();
    
    context.registerMbean(this);
  }
  
  @Override
  public void stop() {
  }

  /**
   * Registers an object identifier, associated to a stub. These mappings are
   * kept by server(every remote object is kept internally on a per-server basis).
   *
   * @param address the {@link ServerAddress} representing the server from which
   * the remote instance comes from.
   * @param oid the {@link OID} of the remote instance to keep.
   * @param remote the remote instance (a stub) to track.
   */
  public void register(ServerAddress address, OID oid, Object remote) {
    HostReference ref = objByHosts.getHostReferenceFor(address);
    gcRefPerMin.hit();
    int count = ref.add(oid, remote);
    log.debug("Registered remote object: %s from %s - got %s ref count for this OID", oid, address, count);

  }
  
  public void exec(TaskContext ctx) {

    log.debug("Running client GC...");
    log.debug("Host count: %s", objByHosts.getHostAddresses().size());
    lastGcTime  = System.currentTimeMillis(); 
    lastGcCount = objByHosts.gc();
    
    if (threshold > 0 && objByHosts.getRemoteObjectCount() >= threshold) {
      forcedGcPerHour.hit();
      Runtime.getRuntime().gc();
    }      
  }
  
  /////// JMX-Related
  
  public void setBatchSize(int size) {
    gcBatchSize = size;
  }
  
  public int getBatchSize() {
    return gcBatchSize;
  }
  
  public void setThreshold(int t) {
    threshold = t;
  }

  public int getThreshold() {
    return threshold;
  }

  public long getInterval() {
    return gcInterval;
  }
  
  public int getRemoteObjectCount() {
    return objByHosts.getRemoteObjectCount();
  }
  
  public Date getLastGcTime() {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(lastGcTime);
    return cal.getTime();
  }
  
  public double getGcPerMin() {
    return gcDerefPerMin.getValue();
  }
  
  public double getForcedGcPerHour() {
    return forcedGcPerHour.getValue();
  }
  
  public int getLastGcCount() {
    return lastGcCount;
  }
  
  // ==========================================================================
  //                               INNER CLASSES     
  // ==========================================================================
  
  class ObjectReference {
    
    private List<Reference<Object>>   references = new CopyOnWriteArrayList<Reference<Object>>();
    
    ObjectReference() {
    }
    
    int count() {
      return references.size();
    }
    
    void addReferenceTo(Object referent) {
      references.add(new WeakReference<Object>(referent));
    }
    
    boolean isNull() {
      for(Reference<Object> ref : references) {
        if(ref.get() != null) {
          return false;
        }
      }
      return true;
    }
    
  }
  
  /**
   * Keeps the remote objects coming from a given host.
   */
  class HostReference {
    
    private HostReferenceTable          owner;
    private ServerAddress               hostAddress;
    private Map<OID, ObjectReference>   remoteReferences = new ConcurrentHashMap<OID, ObjectReference>();
    
    HostReference(HostReferenceTable owner, ServerAddress hostAddress) {
      this.owner       = owner;
      this.hostAddress = hostAddress;
    }
    
    ServerAddress getHostAddress() {
      return hostAddress;
    }
    
    synchronized int add(OID oid, Object remoteObject) {
      ObjectReference ref = remoteReferences.get(oid);
      if(ref == null) {
        ref = new ObjectReference();
        remoteReferences.put(oid, ref);
      }
      
      
      ref.addReferenceTo(remoteObject);
      return ref.count();
    }
    
    int count() {
      return remoteReferences.size();
    }
    
    int gc() {
     
      log.debug("%s remote objects from %s", remoteReferences.size(), hostAddress);

      if (remoteReferences.size() == 0) {
        log.debug("No remote objects associated to host %s", hostAddress);
        owner.removeHostReferenceFor(hostAddress);
        return 0;
      } else {
        
        List<OID> collected = new ArrayList<OID>();
        int collectedCount  = 0;
        
        
        log.trace("Got the following remote references for remote host %s", hostAddress);
        for(OID oid : remoteReferences.keySet()) { 
          ObjectReference remoteRef = remoteReferences.get(oid);
          
          if(log.isTrace()) {
            log.trace("  => OID: %s", oid);
            for(Reference<Object> ref : remoteRef.references) {
              log.trace("    %s", ref.get());
            }
          }
          
          if (remoteRef.isNull()) {
            log.debug("Remote object %s is null, adding to stale references (will be cleared)", oid);
            remoteReferences.remove(oid);
            collected.add(oid);
            collectedCount++;
            if(collected.size() >= gcBatchSize) {
              doSend(collected, hostAddress);
              collected.clear();
            }
          }
        }
        
        // making sure we send the remaining ones,
        // or that we ping the remote host (otherwise it will 
        // clear the references to this client)
        if(collected.size() > 0 || collectedCount == 0) {
          doSend(collected, hostAddress);
        }
        return collectedCount;
      }
    }
    
    private void doSend(List<OID> toSend, ServerAddress addr) {
      
      log.debug("Sending GC command to %s; cleaning %s objects", addr, toSend.size());

      Connections   conns = null;
      RmiConnection conn  = null;

      try {
        conns = transport.getConnectionsFor(addr);
        conn  = conns.acquire();
        gcDerefPerMin.hit(toSend.size());
        gcConnectionsPerMin.hit();
        
        conn.send(new CommandGc(toSend.toArray(new DefaultOID[toSend.size()]), toSend.size()));
        conn.receive();
        conns.release(conn);
      } catch (Throwable e) {
        if (e instanceof RemoteException || e.getCause() instanceof RemoteException) {
          log.info("Error sending GC command to server %s - cleaning up corresponding remote objects", e, addr);
          owner.forceRemoveHostReferenceFor(addr);
        }
        if (conns != null && conn != null) {
          conns.invalidate(conn);
          conns.clear();
        }        
      }
    }
  }
  
  // --------------------------------------------------------------------------
  
  class HostReferenceTable {
   
    private Map<ServerAddress, HostReference> hostRefs = new ConcurrentHashMap<ServerAddress, HostReference>();
    private Object                            lock     = new Object();
    
    HostReferenceTable() {
    }
    
    HostReference getHostReferenceFor(ServerAddress hostAddress) {
      synchronized(lock) {
        HostReference ref = hostRefs.get(hostAddress);
        if(ref == null) {
          ref = new HostReference(this, hostAddress);
          hostRefs.put(hostAddress, ref);
        }
        return ref;
      }
    }
    
    void removeHostReferenceFor(ServerAddress hostAddress) {
      synchronized(lock) {
        HostReference ref = hostRefs.get(hostAddress);
        if(ref != null && ref.count() == 0) {
          hostRefs.remove(hostAddress);
        }
      }
    }
    
    void forceRemoveHostReferenceFor(ServerAddress hostAddress) {
      synchronized(lock) {
        hostRefs.remove(hostAddress);
      }      
    }
    
    Set<ServerAddress> getHostAddresses() {
      return hostRefs.keySet();
    }
    
    int getRemoteObjectCount() {
      int count = 0;
      for(HostReference ref : hostRefs.values()) {
        count += ref.count();
      }
      return count;
    }
    
    int gc() {
      int gcCount = 0;
      for(HostReference ref : hostRefs.values()) {
        log.debug("Performing GC for %s", ref.getHostAddress());
        gcCount += ref.gc();
      }
      return gcCount;
    }
    
  }

}
