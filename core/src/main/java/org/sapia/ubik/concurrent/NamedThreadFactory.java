package org.sapia.ubik.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link ThreadFactory} that uses a {@link ThreadNameStrategy} to produce
 * named threads.
 * 
 * @author yduchesne
 *
 */
public class NamedThreadFactory implements ThreadFactory {
  
  /**
   * Specifies the behavior for providing thread names to a {@link NamedThreadFactory}.
   */
  public interface ThreadNameStrategy {
    
    /**
     * @param threadNumber the thread number.
     * @return a thread name.
     */
    public String getNameFor(int threadNumber);
    
  }
  
  // --------------------------------------------------------------------------
  // default strategy
  
  /**
   * A strategy that names thread according to the following format:
   * <p>
   * <i>baseName</id>-<i>threadNumber</id>.
   * 
   */
  public static class DefaultThreadNameStrategy implements ThreadNameStrategy {
   
    private String baseName;
    
    public DefaultThreadNameStrategy(String baseName) {
      this.baseName = baseName.toLowerCase().startsWith("ubik.") ? baseName : "ubik." + baseName;
    }
    
    @Override
    public String getNameFor(int threadNumber) {
      return baseName + "-" + threadNumber;
    }
  }
  
  // --------------------------------------------------------------------------
  // instance variables
  
  private ThreadNameStrategy strategy;
  private AtomicInteger      threadCount = new AtomicInteger();
  private boolean            daemon      = true;
  private ThreadGroup        group;
  private int                priority    = Thread.NORM_PRIORITY;
  
  NamedThreadFactory(ThreadNameStrategy strategy) {
    this.strategy = strategy;
  }
  
  /**
   * @param daemon indicates if this instance should create daemon threads (<code>true</code>) or not.
   * @return this instance.
   */
  public NamedThreadFactory setDaemon(boolean daemon) {
    this.daemon = daemon;
    return this;
  }
  
  /**
   * @param group the {@link ThreadGroup} to which the threads that this instance creates should belong.
   * @return this instance.
   */
  public NamedThreadFactory setGroup(ThreadGroup group) {
    this.group = group;
    return this;
  }
  
  /**
   * @param priority the priority to assign to the threads that are created.
   * @return this instance.
   */
  NamedThreadFactory setPriority(int priority) {
    this.priority = priority;
    return this;
  }
 
  /**
   * Creates a new thread, named according to this instance's {@link ThreadNameStrategy}. 
   */
  public Thread newThread(Runnable runnable) {
    String name   = strategy.getNameFor(threadCount.incrementAndGet());
    Thread thread = new Thread(group == null ? Thread.currentThread().getThreadGroup() : group, runnable, name);
    thread.setDaemon(daemon);
    thread.setPriority(priority);
    return thread;
  }

  /**
   * @param strategy a {@link ThreadNameStrategy}
   * @return a new {@link NamedThreadFactory} instance.
   */
  public static NamedThreadFactory createWith(ThreadNameStrategy strategy) {
    return new NamedThreadFactory(strategy);
  }
  
  /**
   * Internally creates a {@link DefaultThreadNameStrategy} with the given base name. The strategy
   * is passed to the returned factory.
   * 
   * @param baseName the base name to use when creating threads.
   * @return a new {@link NamedThreadFactory}.
   */
  public static NamedThreadFactory createWith(String baseName) {
    return createWith(new DefaultThreadNameStrategy(baseName));
  }
}
