package org.sapia.ubik.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Implemements a builder of {@link ThreadFactory} instances.
 * 
 * @author yduchesne
 *
 */
public class ThreadFactoryBuilder {
  
  private boolean daemon = true;
  private String  baseName;
  
  public ThreadFactoryBuilder withDaemonEnabled() {
    daemon = true;
    return this;
  }
  
  public ThreadFactoryBuilder withDaemonDisabled() {
    daemon = false;
    return this;
  }
  
  public ThreadFactoryBuilder withBaseName(String name) {
    baseName = name;
    return this;
  }
  
  public ThreadFactory build() {
    Assertions.illegalState(baseName == null, "Base name to use for threads has not be set");
    return new ThreadFactoryImpl();
  }
  
  private class ThreadFactoryImpl implements ThreadFactory {

    private AtomicInteger count = new AtomicInteger();

    @Override
    public Thread newThread(Runnable r) {
      Thread t = new Thread(baseName + "-" + count.getAndIncrement());
      t.setDaemon(daemon);
      return t;
    }
    
  }
}
