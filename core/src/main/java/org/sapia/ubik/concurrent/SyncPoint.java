package org.sapia.ubik.concurrent;

import org.sapia.ubik.util.Pause;

/**
 * An instance of this class can be used to synchronize two threads.
 * 
 * @author yduchesne
 * 
 */
public class SyncPoint {

  private volatile boolean available;

  /**
   * This method waits until this instance's {@link #notifyCompletion()} method
   * is called.
   * 
   * @throws InterruptedException
   *           if the calling thread is interrupted while waiting.
   * 
   */
  public synchronized void await() throws InterruptedException {
    while (!available) {
      wait();
    }
    available = false;
  }

  /**
   * This method waits until this instance's {@link #notifyCompletion()} method
   * is called, or until the given timeout has passed.
   * 
   * @return <code>true</code> if this this method returns because the
   *         {@link #notifyCompletion()} method was called on this instance, or
   *         <code>false</code> if this method returns because the given timeout
   *         has been reached and {@link #notifyCompletion()} has not been
   *         called within that delay.
   * 
   * @param timeout
   *          the amount of time (in millis) to wait for this instance's
   *          completion.
   * @throws InterruptedException
   *           if the calling thread is interrupted while waiting.
   * 
   * @see #notifyCompletion()
   */
  public synchronized boolean await(long timeout) throws InterruptedException {
    Pause delay = new Pause(timeout);
    while (!available && !delay.isOver()) {
      wait(delay.remainingNotZero());
    }
    if (available) {
      available = false;
      return true;
    } else {
      return false;
    }
  }

  /**
   * Notifies any waiting thread about completion.
   * 
   * @see #await()
   * @see #await(long)
   */
  public synchronized void notifyCompletion() {
    this.available = true;
    notify();
  }

}
