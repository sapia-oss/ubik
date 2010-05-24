package org.sapia.ubik.net;

import junit.framework.TestCase;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ThreadPoolTest extends TestCase {
  /**
   * Constructor for ThreadPoolTest.
   * @param arg0
   */
  public ThreadPoolTest(String arg0) {
    super(arg0);
  }

  public void testAcquire() throws Exception {
    ThreadPool   tp = new TestThreadPool("true", false, 5);
    PooledThread pt = (PooledThread) tp.acquire();
    super.assertTrue(pt.isAlive());
    pt.exec("aTask");
    Thread.sleep(500);
    pt = (PooledThread) tp.acquire();
    super.assertEquals(1, ((TestPooledThread) pt).count);
    super.assertEquals(1, tp.getCreatedCount());
    pt.exec("aTask");
    Thread.sleep(500);
    super.assertEquals(2, ((TestPooledThread) pt).count);
  }

  //	/**
  //	 * Constructor for ThreadPoolTest.
  //	 * @param arg0
  //	 */
  //	public ThreadPoolTest(String arg0) {
  //		super(arg0);
  //	}
  //  
  //	public void testAcquire() throws Exception{
  //		ThreadPool tp = new TestThreadPool("true", false, 5);
  //		PooledThread pt = (PooledThread)tp.acquire();
  //		super.assertTrue(pt.isAlive());
  //		pt.exec("aTask");
  //		Thread.sleep(500);
  //		pt = (PooledThread)tp.acquire();
  //		super.assertEquals(1, ((TestPooledThread)pt).count);
  //		super.assertEquals(1, tp.getCreatedCount());
  //		pt.exec("aTask");
  //		Thread.sleep(500);  	
  //		super.assertEquals(2, ((TestPooledThread)pt).count);
  //	}
  public void testShutDown() throws Exception {
    ThreadPool   tp  = new TestThreadPool("true", false, 5);
    PooledThread pt1 = (PooledThread) tp.acquire();
    PooledThread pt2 = (PooledThread) tp.acquire();
    tp.shutdown(5000);
    super.assertEquals(2, tp.size());

    try {
      tp.acquire();
      throw new Exception(
        "Shut down thread pool should have thrown exception on acquire()");
    } catch (IllegalStateException e) {
      //ok
    }
  }

  class TestPooledThread extends PooledThread {
    int count;

    /**
     * @see org.sapia.ubik.net.PooledThread#doExec(Object)
     */
    protected void doExec(Object task) {
      count++;
    }
  }

  class TestThreadPool extends ThreadPool {
    /**
     * Constructor for TestThreadPool.
     * @param name
     * @param daemon
     * @param maxSize
     */
    public TestThreadPool(String name, boolean daemon, int maxSize) {
      super(name, daemon, maxSize);
    }

    /**
     * @see org.sapia.ubik.net.ThreadPool#newThread()
     */
    protected PooledThread newThread() throws Exception {
      return new TestPooledThread();
    }
  }
}
