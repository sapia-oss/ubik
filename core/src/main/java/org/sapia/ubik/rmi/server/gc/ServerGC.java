package org.sapia.ubik.rmi.server.gc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.javasimon.Counter;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.module.Module;
import org.sapia.ubik.module.ModuleContext;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.ObjectTable;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.oid.DefaultOID;
import org.sapia.ubik.rmi.server.oid.OID;
import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.taskman.Task;
import org.sapia.ubik.taskman.TaskContext;
import org.sapia.ubik.taskman.TaskManager;
import org.sapia.ubik.util.Collections2;
import org.sapia.ubik.util.Condition;
import org.sapia.ubik.util.Props;
import org.sapia.ubik.util.Strings;

/**
 * This class implements the server-side distributed garbage collection
 * algorithm.
 * 
 * @author Yanick Duchesne
 */
public class ServerGC implements Module, Task, ServerGCMBean {

  /*
   * delay after which client that haved not performed a ping are considered
   * down.
   */
  public static final long GC_TIMEOUT = ClientGC.GC_CLEAN_INTERVAL * 3 * 15;

  /* interval at which GC checks timed-out clients. */
  public static final long GC_INTERVAL = ClientGC.GC_CLEAN_INTERVAL * 3;

  private Category log = Log.createCategory(getClass());
  private ObjectTable objectTable;
  private long gcTimeout = GC_TIMEOUT;
  private long gcInterval = GC_INTERVAL;

  private Counter gcRef = Stats.createCounter(getClass(), "RefPerMin", "The number of remote references that were registered");

  private Counter gcDeref = Stats.createCounter(getClass(), "DeRefPerMin", "The number of remote references that were deregistered");

  private Map<VmId, ClientInfo> clientTable = new ConcurrentHashMap<VmId, ClientInfo>();

  @Override
  public void init(ModuleContext context) {

    objectTable = context.lookup(ObjectTable.class);
    TaskManager taskman = context.lookup(TaskManager.class);

    Props props = new Props().addProperties(System.getProperties());
    gcInterval = props.getLongProperty(Consts.SERVER_GC_INTERVAL, GC_INTERVAL);
    gcTimeout = props.getLongProperty(Consts.SERVER_GC_TIMEOUT, GC_TIMEOUT);
    if (gcInterval > 0) {
      taskman.addTask(new TaskContext("ubik.rmi.server.GC", gcInterval), this);
    } else {
      log.warning("Will be disabled; client timeouts will not be monitored");
    }

    context.registerMbean(getClass(), this);

  }

  @Override
  public void start(ModuleContext context) {
  }

  @Override
  public void stop() {
  }

  /**
   * Returns the total number of references held on the given object identifier.
   * 
   * @param id
   *          a {@link VmId}.
   * @param oid
   *          an {@link DefaultOID}.
   * 
   * @return a reference count, as an <code>int</code>.
   */
  public int getRefCount(VmId id, OID oid) {
    return getClientInfo(id).getRefCount(oid);
  }

  /**
   * Returns the total number of references held on the given object by the
   * client whose host corresponds to the passed in {@link VmId}.
   * 
   * @param id
   *          a {@link VmId}
   * @param oid
   *          an {@link DefaultOID}
   */
  public int getSpecificCount(VmId id, OID oid) {
    return getClientInfo(id).getSpecificCount(oid);
  }

  /**
   * Returns true if this instance contains the passed in {@link VmId}.
   * 
   * @return <code>true</code> if this instance contains the passed in
   *         {@link VmId}.
   */
  public boolean containsClient(VmId id) {
    return clientTable.containsKey(id);
  }

  /**
   * Increments the reference count of the given object identifier, for the
   * client whose {@link VmId} is given.
   * 
   * @param id
   *          the client's {@link VmId}.
   * @param oid
   *          the {@link OID} of the object whose reference count should be
   *          incremented.
   */
  public void reference(VmId id, OID oid) {
    log.debug("Referencing from: %s on object: %s ", id, oid);

    gcRef.increase();
    ClientInfo inf = getClientInfo(id);
    inf.reference(oid);
  }

  /**
   * Registers a given object internally so that it is not garbage collected
   * before clients themselves garbage collect it.
   * 
   * @param id
   *          the {@link VmId} of the client to whom a stub corresponding to the
   *          passed in object is returned (this in fact creates a remote
   *          reference on the object).
   * @param oid
   *          the {@link OID} that identifies the passed in object locally.
   * @param o
   *          the {@link Object} for which a stub is eventually returned to the
   *          client.
   */
  public void registerRef(VmId id, OID oid, Object o) {
    log.info("Reference created from: %s on object: %s - %s", id, oid, o.getClass().getName());

    gcRef.increase();
    ClientInfo inf = getClientInfo(id);
    inf.registerRef(oid, o);
  }

  /**
   * Dereferences a given object identifier.
   * 
   * @param id
   *          the {@link VmId} of the client from which the dereferencing call
   *          comes.
   * @param oid
   *          the {@link DefaultOID} to dereference.
   */
  public void dereference(VmId id, OID oid) {
    gcDeref.increase();
    ClientInfo inf = getClientInfo(id);
    if (log.isDebug()) {
      Object toDereference = objectTable.getObjectFor(oid);
      log.debug("Dereferencing from JVM: %s, on object: %s (OID: %s)", id, toDereference, oid);
    }

    inf.dereference(oid);
  }

  /**
   * Touches the client info of the {@link VmId} passed in.
   * 
   * @param id
   *          a {@link VmId}
   */
  public void touch(VmId id) {
    log.debug("Touching client info of vm id %s", id);

    synchronized (clientTable) {
      ClientInfo info = clientTable.get(id);

      if (info != null) {
        info.touch();
      } else {
        log.info("No client info found for vm id %s", id);
      }
    }
  }

  public void exec(TaskContext ctx) {
    log.debug("Runner server GC...");
    removeTimedOutClients();
  }

  public void clear() {
    synchronized (clientTable) {
      clientTable.clear();
      objectTable.clear();
    }
  }

  ClientInfo getClientInfo(VmId id) {
    synchronized (clientTable) {
      ClientInfo inf = clientTable.get(id);

      if (inf == null) {
        inf = new ClientInfo(id);
        clientTable.put(id, inf);
      }

      return inf;
    }
  }

  // //// JMX-related

  public long getInterval() {
    return gcInterval;
  }

  public long getTimeout() {
    return gcTimeout;
  }

  public void setTimeout(long timeout) {
    gcTimeout = timeout;
  }

  public int getClientCount() {
    return clientTable.size();
  }

  // //// Private methods

  /**
   * Removes the clients that have not performed a ping for a given amount of
   * time. This delay can be set through the
   * <code>ubik.rmi.server.gc.timeout</code> property.
   */
  private synchronized void removeTimedOutClients() {
    synchronized (clientTable) {
      final ClientInfo[] infos = clientTable.values().toArray(new ClientInfo[clientTable.size()]);

      Collections2.forEach(infos, new Condition<ClientInfo>() {
        @Override
        public boolean apply(ClientInfo item) {
          if (!item.isValid(gcTimeout)) {
            log.info("Removing timed-out client's references %s", item.vmid());
            item.unregisterRefs();
            clientTable.remove(item.vmid());
          }

          if (log.isTrace()) {
            log.trace("Got the following objects for client JVM %s", item.id);
            for (OID oid : item.oids.keySet()) {
              log.trace("  => OID: %s, Object=%s", oid, objectTable.getObjectFor(oid));
            }
          }
          return true;
        }
      });
    }
  }

  // //////////////////////////////////////////////////////////
  // INNER CLASSES
  // //////////////////////////////////////////////////////////

  class ClientInfo {
    private Map<OID, AtomicInteger> oids = new HashMap<OID, AtomicInteger>();
    private volatile long lastAccess = System.currentTimeMillis();
    private VmId id;

    ClientInfo(VmId id) {
      this.id = id;
      log.info("Created a new client info for vmId %s", id);
    }

    VmId vmid() {
      return id;
    }

    void touch() {
      lastAccess = System.currentTimeMillis();
      log.debug("Touched this client info: %s", toString());
    }

    boolean isValid(long timeout) {
      return (System.currentTimeMillis() - lastAccess) < timeout;
    }

    void reference(OID oid) {
      synchronized (oids) {
        AtomicInteger count = oids.get(oid);

        if (count == null) {
          count = new AtomicInteger();
          oids.put(oid, count);
        }

        count.incrementAndGet();
        objectTable.reference(oid);
      }
    }

    void registerRef(OID oid, Object obj) {
      synchronized (oids) {
        AtomicInteger count = new AtomicInteger();
        count.incrementAndGet();
        oids.put(oid, count);
        objectTable.register(oid, obj);
      }
    }

    void dereference(OID oid) {
      synchronized (oids) {
        AtomicInteger count;

        if ((count = oids.get(oid)) != null) {
          objectTable.dereference(oid, count.get());
          oids.remove(oid);
        }
      }
    }

    void unregisterRefs() {
      synchronized (oids) {
        OID[] oidsArray = this.oids.keySet().toArray(new OID[this.oids.size()]);

        Collections2.forEach(oidsArray, new Condition<OID>() {
          @Override
          public boolean apply(OID oid) {
            log.debug("Dereferencing: %s", oid);
            AtomicInteger count = oids.get(oid);
            if (count != null) {
              objectTable.dereference(oid, count.get());
            }
            return true;
          }
        });

        oids.clear();
      }
    }

    int getSpecificCount(OID oid) {
      AtomicInteger c = oids.get(oid);

      if (c == null) {
        return 0;
      } else {
        return c.get();
      }
    }

    int getRefCount(OID oid) {
      return objectTable.getRefCount(oid);
    }

    public String toString() {
      return Strings.toStringFor(this, "vmId", id, "oidCount", oids.size(), "lastAccess", lastAccess);
    }
  }

}
