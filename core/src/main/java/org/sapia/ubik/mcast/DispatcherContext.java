package org.sapia.ubik.mcast;

import java.util.concurrent.ExecutorService;

import org.sapia.ubik.rmi.threads.Threads;
import org.sapia.ubik.util.Conf;

/**
 * The {@link DispatcherContext} holding initialization data passed to {@link UnicastDispatcher} factory instances.
 *  
 * @author yduchesne
 *
 */
public class DispatcherContext {
  
  /**
   * A factory of {@link ExecutorService} instances that provide socket acceptor/NIO selector threads.
   * 
   */
  public interface SelectorExecutorFactory {
    
    /**
     * @param name the base name to be assigned to the threads managed by the returned {@link ExecutorService}.
     * @return a new {@link ExecutorService}.
     */
    public ExecutorService getExecutor(String name);
    
  }

  private EventConsumer           consumer;
  private Conf                    conf;
  private SelectorExecutorFactory selectorThreads;
  private ExecutorService         workerThreads;
  private ExecutorService         ioOutboundThreads;
  
  
  private DispatcherContext(
      EventConsumer           consumer, 
      Conf                    conf, 
      SelectorExecutorFactory selectorThreads, 
      ExecutorService         workerThreads, 
      ExecutorService         ioOutboundThreads) {
      this.consumer = consumer;
      this.conf     = conf;
      this.selectorThreads   = selectorThreads;
      this.workerThreads     = workerThreads;
      this.ioOutboundThreads = ioOutboundThreads;
  }
  
  public DispatcherContext(EventConsumer consumer, SelectorExecutorFactory selectorThreads) {
    this(consumer, Conf.getSystemProperties(), selectorThreads, Threads.getGlobalWorkerPool(), Threads.getGlobalIoOutboundPool());
  }
  
  public DispatcherContext(EventConsumer consumer) {
    this(consumer, new SelectorExecutorFactory() {
      @Override
      public ExecutorService getExecutor(String name) {
        return Threads.createIoInboundPool(name);
      }
    });
  }
  
  public DispatcherContext(
      EventConsumer   consumer, 
      ExecutorService selectorThreads) {
    this(consumer, Conf.getSystemProperties(), new SelectorExecutorFactory() {
      @Override
      public ExecutorService getExecutor(String name) {
        return selectorThreads;
      }
    }, Threads.getGlobalWorkerPool(), Threads.getGlobalIoOutboundPool());
  }
  
  public DispatcherContext withWorkerThreads(ExecutorService newWorkerThreads) {
    return new DispatcherContext(consumer, conf, selectorThreads, newWorkerThreads, ioOutboundThreads);
  }
  
  public DispatcherContext withIoOutputThreads(ExecutorService newIoOutboundThreads) {
    return new DispatcherContext(consumer, conf, selectorThreads, workerThreads, newIoOutboundThreads);
  }
  
  public DispatcherContext withConf(Conf newConf) {
    return new DispatcherContext(consumer, newConf, selectorThreads, workerThreads, ioOutboundThreads);
  }
  
  public Conf getConf() {
    return conf;
  }
  
  public EventConsumer getConsumer() {
    return consumer;
  }
  
  public ExecutorService getIoOutboundThreads() {
    return ioOutboundThreads;
  }
  
  public SelectorExecutorFactory getSelectorThreads() {
    return selectorThreads;
  }
  
  public ExecutorService getWorkerThreads() {
    return workerThreads;
  }
}
