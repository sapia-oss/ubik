package org.sapia.ubik.concurrent;

import org.sapia.ubik.util.Delay;


/**
 * An instance of this class can be used to synchronize two threads on the creation of an object.
 * 
 * @author yduchesne
 *
 * @param <T>
 */
public class BlockingRef<T> {
  
  private T                object;
  private volatile boolean available;

  /**
   * @return the {@link Object} that was set, or  <code>null</code> if this instance's {@link Object}
   * was  set to <code>null</code>. 
   * 
   * @throws InterruptedException if the calling thread is interrupted while waiting.
   * 
   * @see #set(Object)
   */
  public synchronized T await() throws InterruptedException {
    while(!available) {
      wait();
    }
    return object;
  }

  /**
   * This method waits until this instance's {@link Object} is set, or until the given timeout has passed.
   * 
   * @return the {@link Object} that was set, or  <code>null</code> if this instance's {@link Object}
   * was set to <code>null</code>. 
   * 
   * @param timeout the amount of time (in millis) to wait for this instance's {@link Object} to be
   * set.
   * @throws InterruptedException if the calling thread is interrupted while waiting.
   * 
   * @see #set(Object)
   */
  public synchronized T await(long timeout) throws InterruptedException {
    Delay delay = new Delay(timeout);
    while(!available && !delay.isOver()) {
      wait(delay.remainingNotZero());
    }
    return object;
  }
  
  /**
   * Sets this instance's {@link Object}, an notifies any waiting thread.
   * 
   * @param object an arbitrary {@link Object}
   * 
   * @see #await()
   * @see #await(long)
   */
  public synchronized void set(T object) {
    this.object    = object;
    this.available = true;
    notify();
  }
  
  /**
   * Sets this instance's value to <code>null</code>.
   * 
   * @see #await()
   * @see #await(long)
   */
  public synchronized void setNull() {
    this.available = true;
    notify();
  }
  
}
