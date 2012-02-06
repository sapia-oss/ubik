package org.sapia.ubik.net;

import static junit.framework.Assert.*;
import org.junit.Test;
import org.sapia.ubik.util.pool.PooledObjectCreationException;


public class ThreadPoolTest {

  @Test
  public void testAcquire() throws Exception {
    ThreadPool<String>   tp = new TestThreadPool("true", false, 5);
    PooledThread<String> pt = tp.acquire();
    assertTrue(pt.isAlive());
    pt.exec("aTask");
    Thread.sleep(500);
    pt = tp.acquire();
    assertEquals(1, ((TestPooledThread) pt).count);
    assertEquals(1, tp.getCreatedCount());
    pt.exec("aTask");
    Thread.sleep(500);
    assertEquals(2, ((TestPooledThread) pt).count);
  }

  @Test(expected=PooledObjectCreationException.class)
  public void testShutDown() throws Exception {
    ThreadPool<String>   tp  = new TestThreadPool("true", false, 5);
    tp.shutdown(5000);
    tp.acquire();
  }
  
  // --------------------------------------------------------------------------

  class TestPooledThread extends PooledThread<String> {
    int count;
    
    public TestPooledThread(String name) {
      super(name);
    }

    protected void doExec(String task) {
      count++;
    }
    
    @Override
    protected void handleExecutionException(Exception e) {
      e.printStackTrace();
    }
  }

  class TestThreadPool extends ThreadPool<String> {
    public TestThreadPool(String name, boolean daemon, int maxSize) {
      super(name, daemon, maxSize);
    }

    protected PooledThread<String> newThread(String name) throws Exception {
     return new TestPooledThread(name); 
    }
  }
}
