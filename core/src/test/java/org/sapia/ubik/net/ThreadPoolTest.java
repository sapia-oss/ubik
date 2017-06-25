package org.sapia.ubik.net;

import static junit.framework.Assert.assertTrue;

import java.util.concurrent.ExecutorService;

import org.junit.Test;
import org.sapia.ubik.concurrent.ConfigurableExecutor.ThreadingConfiguration;
import org.sapia.ubik.concurrent.SyncPoint;
import org.sapia.ubik.rmi.threads.Threads;

public class ThreadPoolTest {

  @Test
  public void testAcquire() throws Exception {
    TestThreadPool tp = new TestThreadPool(Threads.createWorkerPool());
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

    public TestThreadPool(ExecutorService executor) {
      super(executor);
    }

    @Override
    protected Worker<SyncPoint> newWorker() {
      return new TestWorker();
    }
  }
}
