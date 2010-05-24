package org.sapia.ubik.net;

import junit.framework.*;

import java.util.*;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class PoolTest extends TestCase {
  /**
   * Constructor for PoolTest.
   * @param arg0
   */
  public PoolTest(String arg0) {
    super(arg0);
  }

  public void testAcquire() throws Exception {
    Pool   p = new TestPool();

    List   acquired = new ArrayList();
    Object o;

    for (int i = 0; i < 10; i++) {
      super.assertEquals("anObject" + i, o = p.acquire());
      acquired.add(o);
    }

    for (int i = 0; i < acquired.size(); i++) {
      p.release(acquired.get(i));
    }

    for (int i = 0; i < 10; i++) {
      super.assertEquals("anObject" + i, o = p.acquire());
      acquired.add(o);
    }
  }

  public void testAcquireTimeout() throws Exception {
    Pool p = new TestPool(5);

    for (int i = 0; i < 5; i++) {
      p.acquire();
    }

    try {
      p.acquire(1000);
      throw new Exception("object should not have been acquired");
    } catch (NoObjectAvailableException e) {
      // ok
    }
  }

  public void testShrink() throws Exception {
    Pool   p        = new TestPool();
    List   acquired = new ArrayList();
    Object o;

    for (int i = 0; i < 10; i++) {
      o = p.acquire();
      acquired.add(o);
    }

    for (int i = 0; i < acquired.size(); i++) {
      p.release(acquired.get(i));
    }

    super.assertEquals(10, p.getCreatedCount());
    super.assertEquals(10, p.size());
    p.shrinkTo(0);
    super.assertEquals(0, p.getCreatedCount());
    super.assertEquals(0, p.size());
  }

  static class TestPool extends Pool {
    /**
     * Constructor for TestPool.
     */
    public TestPool() {
      super();
    }

    /**
     * Constructor for TestPool.
     * @param maxSize
     */
    public TestPool(int maxSize) {
      super(maxSize);
    }

    /**
     * @see org.sapia.ubik.net.Pool#doNewObject()
     */
    protected Object doNewObject() throws Exception {
      return "anObject" + getCreatedCount();
    }
  }
}
