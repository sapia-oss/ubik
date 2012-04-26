package org.sapia.ubik.net;

import static junit.framework.Assert.assertTrue;

import org.junit.Test;
import org.sapia.ubik.concurrent.SyncPoint;


public class ThreadPoolTest {
  
  @Test
  public void testAcquire() throws Exception {
    TestThreadPool tp = new TestThreadPool("true", false, 5);
    SyncPoint sync = new SyncPoint();
    tp.submit(sync);
    assertTrue("Thread not released within delay", sync.await(3000));
  }
  
  // --------------------------------------------------------------------------

  class TestWorker implements Worker<SyncPoint> {
    int count;
    
    @Override
    public void execute(SyncPoint sync) {
      count++;
      sync.notifyCompletion();
    }
  }

  class TestThreadPool extends WorkerPool<SyncPoint> {
  	
    public TestThreadPool(String name, boolean daemon, int maxSize) {
      super(name, daemon, maxSize);
    }
    
    @Override
    protected Worker<SyncPoint> newWorker() {
      return new TestWorker(); 
    }
  }
}
