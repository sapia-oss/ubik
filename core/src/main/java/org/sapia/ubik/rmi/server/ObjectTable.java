package org.sapia.ubik.rmi.server;

import java.rmi.server.Unreferenced;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.javasimon.Counter;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.module.Module;
import org.sapia.ubik.module.ModuleContext;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.NoSuchObjectException;
import org.sapia.ubik.rmi.server.oid.DefaultOID;
import org.sapia.ubik.rmi.server.oid.OID;
import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.rmi.server.stub.Stub;
import org.sapia.ubik.rmi.server.stub.StubContainer;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Conf;

/**
 * A server-side class that performs reference counting and that is used in
 * distributed garbage collection.
 * 
 * @author Yanick Duchesne
 */
public class ObjectTable implements ObjectTableMBean, Module {

  private static final float DEFAULT_LOAD_FACTOR = 0.75f;
  private static final int DEFAULT_INIT_CAPACITY = 2000;

  private Category log = Log.createCategory(getClass());
  private Map<OID, Ref> refs;

  private Counter numRef = Stats.createCounter(getClass(), "NumRef", "Number of object references created");

  private Counter numDeref = Stats.createCounter(getClass(), "NumDeref", "Number of objects dereferenced");

  private Counter objectReadPerSec = Stats.createCounter(getClass(), "ReadPerSec", "Number of object references that are read");

  public ObjectTable() {
  }

  @Override
  public void init(ModuleContext context) {
    Conf pu = new Conf().addProperties(System.getProperties());
    float loadFactor = pu.getFloatProperty(Consts.OBJECT_TABLE_LOAD_FACTOR, DEFAULT_LOAD_FACTOR);
    int initCapacity = pu.getIntProperty(Consts.OBJECT_TABLE_INITCAPACITY, DEFAULT_INIT_CAPACITY);
    refs = new ConcurrentHashMap<OID, Ref>(initCapacity, loadFactor);
    context.registerMbean(this);
  }

  @Override
  public void start(ModuleContext context) {
  }

  @Override
  public void stop() {
  }

  /**
   * Registers the given object (for which a stub will eventually be sent on the
   * client side) with the given object identifier.
   * 
   * @param oid
   *          the {@link OID} of the object passed in.
   * @param o
   *          the object whose stub will be sent to the client.
   */
  public synchronized void register(OID oid, Object o) {
    Ref ref = (Ref) refs.get(oid);
    if (ref == null) {
      ref = new Ref(oid, o);
      refs.put(oid, ref);
    }
    numRef.increase();
    ref.inc();
    log.debug("Created reference to %s (%s). Got %s", ref.oid, ref.obj, ref.count.get());
  }

  /**
   * Increases the reference count of the object whose identifier is passed as a
   * parameter.
   * 
   * @param oid
   *          the {@link DefaultOID} of the object whose reference count should
   *          be incremented.
   */
  public synchronized void reference(OID oid) {
    Ref ref = (Ref) refs.get(oid);
    if (ref == null) {
      log.debug("Could not create reference to: %s (no such OID)", oid);
      throw new NoSuchObjectException("No object reference for: " + oid);
    }
    numRef.increase();
    ref.inc();
    log.debug("Referred to %s (%s). Got %s", ref.oid, ref.obj, ref.count.get());

  }

  /**
   * Decrements the reference count of the object whose identifier is given.
   * 
   * @param oid
   *          the {@link DefaultOID} of an object whose reference count is to be
   *          decremented.
   * @param decrement
   *          the value that should be substracted from the OID's reference
   *          count.
   */
  public synchronized void dereference(OID oid, int decrement) {
    Ref ref = (Ref) refs.get(oid);
    if (ref != null) {
      log.debug("Dereferencing %s (%s) by %s (current count is %s)", oid, ref.obj, decrement, ref.count());
      ref.dec(decrement);
      if (ref.count() <= 0) {
        log.debug("%s (%s) available for GC", oid, ref.obj);
        numDeref.increase(decrement);
        refs.remove(oid);

        if (ref.obj instanceof Unreferenced) {
          ((Unreferenced) ref.obj).unreferenced();
        }
      } else {
        log.debug("%s (%s) still has %s remote references", oid, ref.obj, ref.count());
      }
    }
  }

  /**
   * Returns the object whose identifier is passed in.
   * 
   * @param oid
   *          the {@link DefaultOID} corresponding to the identifier of the
   *          object to return
   * @return the {@link Object} whose identifier is passed in.
   * @throws NoSuchObjectException
   *           if no object exists for the given identifier
   */
  public Object getObjectFor(OID oid) throws NoSuchObjectException {
    objectReadPerSec.increase();
    return getRefFor(oid).getObject();
  }

  /**
   * Returns the reference whose identifier is passed in.
   * 
   * @param oid
   *          the {@link DefaultOID} corresponding to the identifier of the
   *          object to return
   * @return the {@link Ref} whose identifier is passed in.
   * @throws NoSuchObjectException
   *           if no object exists for the given identifier
   */
  public Ref getRefFor(OID oid) throws NoSuchObjectException {
    Ref ref = (Ref) refs.get(oid);
    if ((ref != null) && (ref.count() > 0)) {
      return ref;
    }
    log.debug("No object reference for: %s", oid);
    throw new NoSuchObjectException("No object reference for: " + oid);
  }

  /**
   * Removes the given object from this instance.
   * 
   * @return <code>true</code> if the given object was removed from this
   *         instance.
   */
  public boolean remove(Object o) {
    Ref[] refArray = (Ref[]) refs.values().toArray(new Ref[refs.size()]);
    boolean removed = false;

    for (int i = 0; i < refArray.length; i++) {
      if (refArray[i].obj.equals(o)) {
        refs.remove(refArray[i].oid);
        removed = true;
      }
    }

    return removed;
  }

  /**
   * Removes all objects whose class was loaded by the given classloader.
   * 
   * @param loader
   *          a {@link ClassLoader}.
   * 
   * @return <code>true</code> if any objects were removed that correspond to
   *         the given classloader.
   */
  public boolean remove(ClassLoader loader) {
    Ref[] refArray = (Ref[]) refs.values().toArray(new Ref[refs.size()]);
    boolean removed = false;

    for (int i = 0; i < refArray.length; i++) {
      if (refArray[i].obj.getClass().getClassLoader().equals(loader)) {
        refs.remove(refArray[i].oid);

        if (refArray[i].obj instanceof Unreferenced) {
          ((Unreferenced) refArray[i].obj).unreferenced();
        }

        removed = true;
      }
    }

    return removed;
  }

  /**
   * Returns the reference count of the object whose identifier is given.
   * 
   * @return the reference count of the object corresponding to the
   *         {@link DefaultOID} passed in.
   */
  public int getRefCount(OID oid) {
    Ref ref = (Ref) refs.get(oid);

    if (ref == null) {
      return 0;
    } else {
      return ref.count();
    }
  }

  /**
   * Returns the total number of references that this instance holds.
   * 
   * @return the reference count of the object corresponding to the
   *         {@link DefaultOID} passed in.
   */
  public int getRefCount() {
    Ref[] refArray = (Ref[]) refs.values().toArray(new Ref[refs.size()]);
    int total = 0;
    for (int i = 0; i < refArray.length; i++) {
      total = total += refArray[i].count();
    }
    return total;
  }

  /**
   * Clears this instance's internal {@link Ref}s.
   */
  public synchronized void clear() {
    refs.clear();
  }

  /**
   * Resets the reference count corresponding to the given {@link DefaultOID} to
   * 0.
   * 
   * @param oid
   *          an {@link DefaultOID}
   */
  public synchronized void clear(OID oid) {
    Ref ref = (Ref) refs.get(oid);

    if (ref != null) {
      ref.count.set(0);
    }
  }

  /*
   * //////////////////////////////////////////////////////////////////// INNER
   * CLASSES
   * ////////////////////////////////////////////////////////////////////
   */
  public static class Ref {
    AtomicInteger count = new AtomicInteger();
    Object obj;
    OID oid;

    Ref(OID oid, Object o) {
      this.obj = o;
      this.oid = oid;
    }

    void dec() {
      count.decrementAndGet();
    }

    void dec(int value) {
      if (count.addAndGet(-value) < 0) {
        count.set(0);
      }
    }

    void inc() {
      count.incrementAndGet();
    }

    int count() {
      return count.get();
    }

    public Object getObject() {
      return obj;
    }

    public OID getOID() {
      return oid;
    }
  }
}
