package org.sapia.ubik.concurrent;

import org.sapia.ubik.util.Delay;

/**
 * An instance of this class is used to synchronize threads at startup.
 * 
 * @author yduchesne
 * 
 */
public class ThreadStartup {

  private volatile boolean started = false;
  private Exception startException;

  /**
   * Waits until this instance's {@link #started} flag becomes <code>true</code>
   * .
   * 
   * @throws InterruptedException
   *           if the calling thread is interrupted while waiting.
   * @throws Exception
   *           the startup exception, if this instance's
   *           {@link #failed(Exception)} method was called.
   */
  public synchronized void await() throws InterruptedException, Exception {
    while (!started) {
      wait();
    }
  }

  /**
   * Waits until this instance's {@link #started} flag becomes <code>true</code>
   * , or until the given timeout is reached.
   * 
   * @param a
   *          timeout, in millis.
   * 
   * @throws InterruptedException
   *           if the calling thread is interrupted while waiting.
   * @throws Exception
   *           the startup exception, if this instance's
   *           {@link #failed(Exception)} method was called.
   */
  public synchronized void await(long timeout) throws InterruptedException, Exception {
    Delay delay = new Delay(timeout);
    while (!started && !delay.isOver()) {
      wait(delay.remainingNotZero());
    }
    if (startException != null) {
      throw startException;
    }
  }

  /**
   * Sets this instance's {@link #started} flag to <code>true</code>, and
   * notifies waiting threads.
   */
  public synchronized void started() {
    started = true;
    notifyAll();
  }

  /**
   * @return <code>true</code> if this is instance's {@link #started} flag is
   *         set to <code>true</code>, false otherwise.
   */
  public boolean isStarted() {
    return started || startException != null;
  }

  /**
   * Sets this instance's {@link #started} flag to <code>true</code>. Also sets
   * this instance' start exeption with the passed in exception.
   * <p>
   * This method internally notifies waiting threads.
   */
  public synchronized void failed(Exception e) {
    started = true;
    startException = e;
    notifyAll();
  }
}
