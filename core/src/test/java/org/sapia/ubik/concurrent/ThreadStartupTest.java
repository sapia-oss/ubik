package org.sapia.ubik.concurrent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

public class ThreadStartupTest {

  @Test
  public void testStarted() throws Exception {
    final ThreadStartup startup = new ThreadStartup();
    final AtomicBoolean started = new AtomicBoolean(false);
    
    Thread t = createThread(new Runnable() {
      @Override
      public void run() {
        started.set(true);
        startup.started();
      }
    });
    t.start();
    startup.await();
    
    assertTrue(started.get());
  }

  @Test
  public void testFailed() throws Exception {
    final ThreadStartup startup = new ThreadStartup();
    final AtomicBoolean started = new AtomicBoolean(false);
    
    Thread t = createThread(new Runnable() {
      @Override
      public void run() {
        started.set(true);
        startup.failed(new Exception("test"));
      }
    });
    t.start();
    
    try {
      startup.await();
    } catch (Exception e) {
      assertEquals("test", e.getMessage());
      assertTrue(started.get());  
    }
    
  }
  
  private Thread createThread(Runnable r) {
    return new Thread(r);
  }


}
