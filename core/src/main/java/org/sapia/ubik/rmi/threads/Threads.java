package org.sapia.ubik.rmi.threads;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.sapia.ubik.concurrent.ConfigurableExecutor;
import org.sapia.ubik.concurrent.ConfigurableExecutor.ThreadingConfiguration;
import org.sapia.ubik.concurrent.NamedThreadFactory;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.Defaults;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Conf;

/**
 * Centralizes access to specialized {@link ExecutorService} instances. This class manages the following
 * types of thread pools, available across Ubik components, according to the needs of each:
 * <ul>
 *   <li>A global worker pool, for processing incoming requests on the server side.
 *   <li>A global outbound task pool, mainly dedicated to task writing payload to a connection.
 * </ul>
 * 
 * <p>Note also that this class offers a method to create dedicates pools for using in conjunction with
 * NIO selectors. Such pools can't be shared and therefore there is no "global", shared {@link ExecutorService}
 * for that purpose. Rather, such pools are created by clients, invoking either the {@link #createIoInboundPool(String)}
 * or the {@link #createIoInboundPool(String, int)} method. 
 * 
 * <p>From a configuration perspective, the both global pools are configured with system properties. For 
 * the worker pool, the following properties are considered (with defaults provided if not set):
 * <ul>
 *   <li> {@link Consts#SERVER_CORE_THREADS}
 *   <li> {@link Consts#SERVER_MAX_THREADS}
 *   <li> {@link Consts#SERVER_THREADS_KEEP_ALIVE}
 *   <li> {@link Consts#SERVER_THREADS_QUEUE_SIZE}
 * </ul>
 * 
 * <p>In a similar manner, the following properties are used to configure the global outbound
 * I/O thread pool (also with defaults provided when not set):
 * <ul>
 *   <li> {@link Consts#SERVER_OUTBOUND_CORE_THREADS}
 *   <li> {@link Consts#SERVER_OUTBOUND_MAX_THREADS}
 *   <li> {@link Consts#SERVER_OUTBOUND_THREADS_KEEP_ALIVE}
 *   <li> {@link Consts#SERVER_OUTBOUND_QUEUE_SIZE}
 * </ul>
 * 
 * <p>This class registered a shutdown hook with the JVM to ensure proper shutdown of the global pools upon termination.
 * 
 * 
 * @author yduchesne
 *
 */
public class Threads {

  private static final Conf             GLOBAL_CONF           = Conf.getSystemProperties();

  private static final AtomicInteger    GLOBAL_WORKER_REFS    = new AtomicInteger();

  private static final AtomicInteger    GLOBAL_OUTBOUND_REFS  = new AtomicInteger();

  private static GlobalExecutorService  globalWorkers         = doCreateWorkerPool();
      
  private static GlobalExecutorService  globalOutboundSenders = doCreateIoOutboundPool();
    
  private static volatile boolean      jvmShuttingDown;
  
  static {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
     public void run() {
        jvmShuttingDown = true;
        shutdown();
     }
   });
  }
  
  private Threads() {

  }
  
  // --------------------------------------------------------------------------
  // Visible for testing
  
  static void setJvmShuttown(boolean isShuttingDown) {
    jvmShuttingDown = isShuttingDown;
  }
  
  // --------------------------------------------------------------------------
  // Public methods
  
  /**
   * Returns an {@link ExecutorService} that in fact wraps the global server thread pool
   * (as returned by {@link #getGlobalWorkerPool()}). The {@link ExecutorService} returned
   * by this method guards the {@link ExecutorService#shutdown()} method so that the globally
   * shared pool will not be shut down if it is still in use by other clients.
   * 
   * <p>Note that the {@link ExecutorService#shutdownNow()} method of the returned executor
   * will throw an {@link UnsupportedOperationException} if invoked.
   * 
   * @return a new {@link ExecutorService}.
   */
  public static ExecutorService createWorkerPool() {
    Assertions.illegalState(jvmShuttingDown, "Cannot create ExecutorService: the JVM is currently shutting down");
    return new SharedExecutorService(GLOBAL_WORKER_REFS, (GlobalExecutorService) getGlobalWorkerPool());
  }

  /**
   * Returns an {@link ExecutorService} that in fact wraps the global outbound thread pool
   * (as returned by {@link #getGlobalIoOutboundPool()}). The {@link ExecutorService} returned
   * by this method guards the {@link ExecutorService#shutdown()} method so that the globally
   * shared pool will not be shut down if it is still in use by other clients.
   * 
   * <p>Note that the {@link ExecutorService#shutdownNow()} method of the returned executor
   * will throw an {@link UnsupportedOperationException} if invoked.
   * 
   * @return a new {@link ExecutorService}.
   */
  public static ExecutorService createIoOutboundPool() {
    Assertions.illegalState(jvmShuttingDown, "Cannot create ExecutorService: the JVM is currently shutting down");
    return new SharedExecutorService(GLOBAL_OUTBOUND_REFS, (GlobalExecutorService) getGlobalIoOutboundPool());
  }
  
  /**
   * This instance returns an {@link ExecutorService} that is meant to provide NIO selector threads. 
   * Internally, this method creates an {@link ExecutorService} through the {@link Executors#newCachedThreadPool()}
   * method.
   * 
   * @param name the base name of the threads that will be created by the returned {@link ExecutorService}.
   * @return a new {@link ExecutorService}.
   */
  public static ExecutorService createIoInboundPool(String name) {
    Assertions.illegalState(jvmShuttingDown, "Cannot create ExecutorService: the JVM is currently shutting down");
    return Executors.newCachedThreadPool(NamedThreadFactory.createWith("ubik." + name).setDaemon(true));
  }
  
  /**
   * This instance returns an {@link ExecutorService} that is meant to provide NIO selector threads. 
   * Internally, this method creates an {@link ExecutorService} through the {@link Executors#newFixedThreadPool(int)}
   * method.
   * 
   * @param name       the base name of the threads that will be created by the returned {@link ExecutorService}.
   * @param numThreads the fixed number of threads that the pool should have.
   * @return a new {@link ExecutorService}.
   */
  public static ExecutorService createIoInboundPool(String name, int numThreads) {
    Assertions.illegalState(jvmShuttingDown, "Cannot create ExecutorService: the JVM is currently shutting down");
    return Executors.newFixedThreadPool(numThreads, NamedThreadFactory.createWith("ubik." + name).setDaemon(true));
  }
  
  /**
   * Returns the {@link ExecutorService} used to execute server commands. Note that the returned
   * executor's <code>shutdown</code> methods are not implemented, since the executor is shared amongst
   * multiple clients.
   * 
   * @return the globally shared {@link ExecutorService}.
   */
  public static ExecutorService getGlobalWorkerPool() {
    Assertions.illegalState(jvmShuttingDown, "Cannot access global worker ExecutorService: the JVM is currently shutting down");
    if (globalWorkers.isShutdown()) {
      doInitializeGlobalWorkerPool();
    }
    return globalWorkers;
  }
  
  /**
   * Returns the {@link ExecutorService} used for executing outbound I/O tasks. Note that the returned
   * executor's <code>shutdown</code> methods are not implemented, since the executor is shared amongst
   * multiple clients.
   * 
   * @return the globally shared outbound I/O {@link ExecutorService}.
   */
  public static ExecutorService getGlobalIoOutboundPool() {
    Assertions.illegalState(jvmShuttingDown, "Cannot access global outbound I/O ExecutorService: the JVM is currently shutting down");
    if (globalOutboundSenders.isShutdown()) {
      doInitializeGlobalIoOutboundPool();
    }
    return globalOutboundSenders;
  }

  // --------------------------------------------------------------------------
  // Restricted methods

  /**
   * Shuts down the global thread pools.
   */
  static synchronized void shutdown() {
    if (!globalWorkers.isShutdown()) {
      globalWorkers.doShutdown();
      GLOBAL_WORKER_REFS.set(0);
    }
    
    if (!globalOutboundSenders.isShutdown()) {
      globalOutboundSenders.doShutdown();
      GLOBAL_OUTBOUND_REFS.set(0);
    }
  }
  
  
  private static synchronized void doInitializeGlobalWorkerPool() {
    if (globalWorkers.isShutdown()) {
      globalWorkers = doCreateWorkerPool();
      GLOBAL_WORKER_REFS.set(0);
    }
  }
  
  private static synchronized void doInitializeGlobalIoOutboundPool() {
    if (globalOutboundSenders.isShutdown()) {
      globalOutboundSenders = doCreateIoOutboundPool();
      GLOBAL_OUTBOUND_REFS.set(0);
    }
  }
  
  private static GlobalExecutorService doCreateWorkerPool() {
    ThreadingConfiguration threadConf = ThreadingConfiguration.newInstance()
      .setCorePoolSize(GLOBAL_CONF.getIntProperty(Consts.SERVER_CORE_THREADS, Defaults.DEFAULT_WORKER_CORE_POOL_SIZE))
      .setMaxPoolSize(GLOBAL_CONF.getIntProperty(Consts.SERVER_MAX_THREADS, Defaults.DEFAULT_WORKER_MAX_POOL_SIZE))
      .setQueueSize(GLOBAL_CONF.getIntProperty(Consts.SERVER_THREADS_QUEUE_SIZE, Defaults.DEFAULT_WORKER_QUEUE_SIZE))
      .setKeepAlive(GLOBAL_CONF.getTimeProperty(Consts.SERVER_THREADS_KEEP_ALIVE, Defaults.DEFAULT_WORKER_KEEP_ALIVE));
    
    ThreadFactory threadFactory = NamedThreadFactory.createWith("ubik.workers").setDaemon(true);
    
    return new GlobalExecutorService(threadConf, threadFactory);
  }
  
  private static GlobalExecutorService doCreateIoOutboundPool() {
    ThreadingConfiguration threadConf = ThreadingConfiguration.newInstance()
      .setCorePoolSize(GLOBAL_CONF.getIntProperty(Consts.SERVER_OUTBOUND_CORE_THREADS, Defaults.DEFAULT_OUTBOUND_CORE_POOL_SIZE))
      .setMaxPoolSize(GLOBAL_CONF.getIntProperty(Consts.SERVER_OUTBOUND_MAX_THREADS, Defaults.DEFAULT_OUTBOUND_MAX_POOL_SIZE))
      .setQueueSize(GLOBAL_CONF.getIntProperty(Consts.SERVER_OUTBOUND_QUEUE_SIZE, Defaults.DEFAULT_OUTBOUND_QUEUE_SIZE))
      .setKeepAlive(GLOBAL_CONF.getTimeProperty(Consts.SERVER_OUTBOUND_THREADS_KEEP_ALIVE, Defaults.DEFAULT_OUTBOUND_KEEP_ALIVE));
    
    ThreadFactory threadFactory = NamedThreadFactory.createWith("ubik.outbound");
    
    return new GlobalExecutorService(threadConf, threadFactory);
  }
  
  // ==========================================================================
  
  private static class GlobalExecutorService extends ConfigurableExecutor {
        
    private GlobalExecutorService(ThreadingConfiguration conf, ThreadFactory threadFactory) {
      super(conf, threadFactory);
    }
   
    
    @Override
    public synchronized void shutdown() {

    }

    @Override
    public List<Runnable> shutdownNow() {
      return Collections.emptyList();
    }

    
    private synchronized void doShutdown() {
      if (!isShutdown()) {
        super.shutdown();
      }
    }
  }
  
  // --------------------------------------------------------------------------

  private static class SharedExecutorService implements ExecutorService {

    private volatile boolean      isShutdown;
    private AtomicInteger         referenceCounter;
    private GlobalExecutorService globalThreadPool;

    private SharedExecutorService(AtomicInteger referenceCounter, GlobalExecutorService globalThreadPool) {
      this.referenceCounter = referenceCounter;
      referenceCounter.incrementAndGet();
      this.globalThreadPool = globalThreadPool;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
      return globalThreadPool.awaitTermination(timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
      globalThreadPool.execute(command);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
      return globalThreadPool.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
      return globalThreadPool.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
      return globalThreadPool.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
      return globalThreadPool.invokeAny(tasks, timeout, unit);
    }

    @Override
    public boolean isShutdown() {
      return isShutdown;
    }

    @Override
    public boolean isTerminated() {
      return isShutdown;
    }

    @Override
    public synchronized void shutdown() {
      if (!isShutdown) {
        if (referenceCounter.decrementAndGet() == 0) {
           globalThreadPool.doShutdown();
        }
        isShutdown = true;
      }
    }

    @Override
    public List<Runnable> shutdownNow() {
      throw new UnsupportedOperationException("shutdownNow() is not supported");
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
      return globalThreadPool.submit(task);
    }

    @Override
    public Future<?> submit(Runnable task) {
      return globalThreadPool.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
      return globalThreadPool.submit(task, result);
    }

  }

}
