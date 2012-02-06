package org.sapia.ubik.concurrent;


/**
 * An instance of this class performs thread shutdown.
 * 
 * @author yduchesne
 *
 */
public class ThreadShutdown {

  /**
   * The default maximum number of thread shutdown attempts (<code>3</code>).
   */
  public static final int  DEFAULT_MAX_ATTEMPTS      = 3;
  
  /**
   * The default number of milliseconds between each attempt (<code>3333</code>).
   */
  public static final long DEFAULT_INTERRUPT_DELAY   = 3333;
  
  private int    maxAttempts    = DEFAULT_MAX_ATTEMPTS;
  private long   interruptDelay = DEFAULT_INTERRUPT_DELAY;
  private Thread thread;
  
  
  private ThreadShutdown(Thread toShutDown) {
    this.thread = toShutDown;
  }
  
  /**
   * @param toShutDown a {@link Thread} to shut down.
   * @return a new instance of this class.
   */
  public static ThreadShutdown create(Thread toShutDown) {
    ThreadShutdown waiter = new ThreadShutdown(toShutDown);
    return waiter;
  }
  
  /**
   * @param delay the delay between each attempt, in millis.
   * @return this instance.
   */
  public ThreadShutdown setInterruptDelay(long delay) {
    this.interruptDelay = delay;
    return this;
  }
  
  /**
   * 
   * @param maxAttempts the maximum number of attempts that this instance should
   * try interrupting the thread that it holds.
   * 
   * @return this instance.
   */
  public ThreadShutdown setMaxAttempts(int maxAttempts) {
    this.maxAttempts = maxAttempts;
    return this;
  }
  
  /**
   * Attempts interrupting the thread that this instance holds, up to the configured
   * maximum number of attempts.
   * 
   * @throws InterruptedException if the calling thread is itself interrupted while
   * performing this operation.
   */
  public void shutdown() throws InterruptedException {
    int attempts = 0;
    while(thread != null && thread.isAlive() && attempts < maxAttempts) {
      thread.interrupt();
      thread.join(interruptDelay);
      attempts++;
    }
  }
  
  /**
   * Same as {@link #shutdown()}, except that no {@link InterruptedException} is thrown
   * if the calling thread is itself interrupted while performing the shutdown.
   */
  public void shutdownLenient() {
    try {
      shutdown();
    } catch (InterruptedException e) {
      // noop
    }
  }
}
