package org.sapia.ubik.concurrent;

/**
 * A concurrent utility that implements synchronized set/get primitives on an
 * object reference.
 * 
 * @author yduchesne
 * 
 * @param <T>
 *          the generic type of the object to synchronize
 */
public class SynchronizedRef<T> {

  private T ref;

  /**
   * @return this instance's object.
   */
  public synchronized T get() {
    return ref;
  }

  /**
   * @param ref
   *          an object to assign to this instance.
   */
  public synchronized void set(T ref) {
    this.ref = ref;
  }

  /**
   * Sets this instance's internal object to <code>null</code>.
   */
  public synchronized void unset() {
    this.ref = null;
  }

  /**
   * @return <code>true</code> if this instance's object is <code>null</code>.
   */
  public synchronized boolean isUnset() {
    return ref == null;
  }

  /**
   * @return <code>true</code> if this instance's object is NOT
   *         <code>null</code>.
   */
  public synchronized boolean isSet() {
    return ref != null;
  }

}
