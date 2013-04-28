package org.sapia.ubik.net;

import static junit.framework.Assert.assertTrue;

import org.junit.Test;
import org.sapia.ubik.concurrent.SyncPoint;
import org.sapia.ubik.concurrent.ConfigurableExecutor.ThreadingConfiguration;


public class ThreadPoolTest {
  
  @Test
  public void testAcquire() throws Exception {
    TestThreadPool tp = new TestThreadPool("true", false, ThreadingConfiguration.newInstance().setCorePoolSize(5));
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
  	
    public TestThreadPool(String name, boolean daemon, ThreadingConfiguration conf) {
      super(name, daemon, conf);
    }
    
    @Override
    protected Worker<SyncPoint> newWorker() {
      return new TestWorker(); 
    }
  }
}
