package org.sapia.ubik.concurrent;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

public class ThreadShutdownTest {
  
  @Test
  public void testShutdown() throws Exception {
    
    final AtomicReference<Boolean> interrupted = new AtomicReference<Boolean>(false);
    final BlockingRef<Boolean>     started = new BlockingRef<Boolean>();
    Thread thread = createThread(new Runnable() {
    
      @Override
      public void run() {
        started.set(true);
        try {
          Thread.sleep(100000);
        } catch (InterruptedException e) {
          interrupted.set(true);
        }
        
      }
      
    });
    
    thread.start();
    
    started.await();    
    
    ThreadShutdown.create(thread)
      .setInterruptDelay(1000)
      .setMaxAttempts(3)
      .shutdown();
    
    assertTrue(!thread.isAlive());
    assertTrue(interrupted.get());
  }
  
  private Thread createThread(Runnable r) {
    return new Thread(r);
  }

}
