package org.sapia.ubik.rmi.server;

import java.rmi.NoSuchObjectException;
import java.rmi.server.Unreferenced;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;

import org.sapia.ubik.jmx.MBeanContainer;
import org.sapia.ubik.jmx.MBeanFactory;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.PropUtil;
import org.sapia.ubik.rmi.server.perf.Statistic;

/**
 * A server-side class that performs reference counting and that is used
 * in distributed garbage collection.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ObjectTable implements ObjectTableMBean, MBeanFactory{
  
  class RefCountStat extends Statistic{
    
    public RefCountStat() {
      super("ObjectTableRefCount");
    }
    public double getStat() {
      if(_refs == null) return 0;
      return _refs.size();
    }
    
  }
  
  static final float DEFAULT_LOAD_FACTOR = 0.75f;
  static final int DEFAULT_INIT_CAPACITY = 2000;
  
  Map _refs;
  Statistic _refCount = new RefCountStat();
  
  ObjectTable(){
    float loadFactor = DEFAULT_LOAD_FACTOR;
    int initCapacity = DEFAULT_INIT_CAPACITY;
    PropUtil pu = new PropUtil().addProperties(System.getProperties());
    loadFactor = pu.getFloat(Consts.OBJECT_TABLE_LOAD_FACTOR, DEFAULT_LOAD_FACTOR);
    initCapacity = pu.getIntProperty(Consts.OBJECT_TABLE_INITCAPACITY, DEFAULT_INIT_CAPACITY);
    _refs = Collections.synchronizedMap(new HashMap(initCapacity, loadFactor));
    Hub.statsCollector.addStat(_refCount);
  }

  /**
   * Registers the given object (for which a stub will eventually be
   * sent on the client side) with the given object identifier.
   *
   * @param oid the <code>OID</code> of the object passed in.
   * @param o the object whose stub will be sent to the client.
   */
  public synchronized void register(OID oid, Object o) {
    if (Log.isDebug()) {
      Log.debug(ObjectTable.class, "registering: " + oid);
    }

    Ref ref = (Ref) _refs.get(oid);

    if (ref == null) {
      ref = new Ref(oid, o);
      _refs.put(oid, ref);
    }

    ref.inc(); /* TO DO: REMOVE CLEAN OBJECTS */
  }

  /**
   * Increases the reference count of the object whose identifier
   * is passed as a parameter.
   *
   * @param oid the <code>OID</code> of the object whose reference count
   * should be incremented.
   */
  public synchronized void reference(OID oid) {
    if (Log.isDebug()) {
      Log.debug(ObjectTable.class, "referencing to: " + oid);
    }

    Ref ref = (Ref) _refs.get(oid);

    if (ref == null) {
      if(Log.isDebug()){
        Log.debug(getClass(), "No object reference for: " + oid);
        Log.debug(getClass(), "Current objects: " + _refs);
      }
      throw new NullPointerException("no object reference for: " + oid);
    }

    ref.inc();
  }

  /**
   * Decrements the reference count of the object whose identifier is given.
   *
   * @param oid the <code>OID</code> of an object whose reference count is
   * to be decremented.
   * @param decrement the value that should be substracted from the OID's reference count.
   */
  public synchronized void dereference(OID oid, int decrement) {
    Ref ref = (Ref) _refs.get(oid);

    if (ref != null) {
      ref.dec(decrement);

      if (ref.count() <= 0) {
        if (Log.isDebug()) {
          Log.debug(ObjectTable.class,
            "dereferencing: " + oid + " - available for GC");
        }

        _refs.remove(oid);

        if (ref._obj instanceof Unreferenced) {
          ((Unreferenced) ref._obj).unreferenced();
        }
      }
    }
  }

  /**
   * Returns the object whose identifier is passed in.
   *
   * @param oid the identifier of the object to return
   * @throws NoSuchObjectException if no object exists for the given identifier
   */
  public Object getObjectFor(OID oid) throws NoSuchObjectException {
    Ref ref = (Ref) _refs.get(oid);

    if ((ref != null) && (ref.count() > 0)) {
      return ref.get();
    } else {
      if(Log.isDebug()){
        Log.debug(getClass(), "No object reference for: " + oid);
        Log.debug(getClass(), "Current objects: " + _refs);
      }      
      throw new NullPointerException("no object reference for: " + oid);
    }
  }

  /**
   * Removes the given object from this instance.
   *
   * @return <code>true</code> if the given object was removed from this instance.
   */
  public boolean remove(Object o) {
    Ref[]   refs    = (Ref[]) _refs.values().toArray(new Ref[_refs.size()]);
    boolean removed = false;

    for (int i = 0; i < refs.length; i++) {
      if (refs[i]._obj.equals(o)) {
        _refs.remove(refs[i]._oid);
        removed = true;
      }
    }

    return removed;
  }

  /**
   * Removes all objects whose class was loaded by the given
   * classloader.
   *
   * @param loader a <code>ClassLoader</code>.
   *
   * @return <code>true</code> if any objects were removed that correspond to the
   * given classloader.
   */
  public boolean remove(ClassLoader loader) {
    Ref[]   refs    = (Ref[]) _refs.values().toArray(new Ref[_refs.size()]);
    boolean removed = false;

    for (int i = 0; i < refs.length; i++) {
      if (refs[i]._obj.getClass().getClassLoader().equals(loader)) {
        _refs.remove(refs[i]._oid);

        if (refs[i]._obj instanceof Unreferenced) {
          ((Unreferenced) refs[i]._obj).unreferenced();
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
   * <code>OID</code> passed in.
   */
  public int getRefCount(OID oid) {
    Ref ref = (Ref) _refs.get(oid);

    if (ref == null) {
      return 0;
    } else {
      return ref.count();
    }
  }
  
  public int getRefCount(){
    Ref[]   refs    = (Ref[]) _refs.values().toArray(new Ref[_refs.size()]);
    int total = 0;
    for(int i = 0; i < refs.length; i++){
      total = total += refs[i].count();
    }
    return total;
  }

  public synchronized void clear() {
    _refs.clear();
  }

  public Map getRefs() {
    return _refs;
  }

  public synchronized void clear(OID oid) {
    Ref ref = (Ref) _refs.get(oid);

    if (ref != null) {
      ref._count = 0;
    }
  }
  
  //////// MBeanFactory
  
  public MBeanContainer createMBean() throws Exception{
    ObjectName name = new ObjectName("sapia.ubik.rmi:type=ObjectTable");
    return new MBeanContainer(name, this);
  }

  /*////////////////////////////////////////////////////////////////////
                              INNER CLASSES
  ////////////////////////////////////////////////////////////////////*/
  protected static class Ref {
    int    _count;
    Object _obj;
    OID    _oid;

    Ref(OID oid, Object o) {
      _obj   = o;
      _oid   = oid;
    }

    void dec() {
      _count--;
    }

    void dec(int count) {
      _count = _count - count;

      if (_count < 0) {
        _count = 0;
      }
    }

    void inc() {
      _count++;
    }

    int count() {
      return _count;
    }

    Object get() {
      return _obj;
    }
  }
}
