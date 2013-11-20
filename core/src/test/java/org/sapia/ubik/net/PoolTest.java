package org.sapia.ubik.net;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.sapia.ubik.util.pool.NoObjectAvailableException;
import org.sapia.ubik.util.pool.Pool;

/**
 * @author Yanick Duchesne
 */
public class PoolTest {

  @Test
  public void testAcquire() throws Exception {
    Pool<String> p = new TestPool();

    List<String> acquired = new ArrayList<String>();
    String o;

    for (int i = 0; i < 10; i++) {
      assertEquals("anObject" + i, o = p.acquire());
      acquired.add(o);
    }

    for (int i = 0; i < acquired.size(); i++) {
      p.release(acquired.get(i));
    }

    for (int i = 0; i < 10; i++) {
      assertEquals("anObject" + i, o = p.acquire());
      acquired.add(o);
    }
  }

  public void testAcquireTimeout() throws Exception {
    Pool<String> p = new TestPool(5);

    for (int i = 0; i < 5; i++) {
      p.acquire();
    }

    try {
      p.acquire(1000);
      throw new Exception("Object should not have been acquired");
    } catch (NoObjectAvailableException e) {
      // ok
    }
  }

  public void testShrink() throws Exception {
    Pool<String> p = new TestPool();
    List<String> acquired = new ArrayList<String>();
    String o;

    for (int i = 0; i < 10; i++) {
      o = p.acquire();
      acquired.add(o);
    }

    for (int i = 0; i < acquired.size(); i++) {
      p.release(acquired.get(i));
    }

    assertEquals("Unexpected created count", acquired.size(), p.getCreatedCount());
    p.shrinkTo(0);
    assertEquals("Created count should be 0", 0, p.getCreatedCount());
  }

  static class TestPool extends Pool<String> {

    public TestPool() {
      super();
    }

    public TestPool(int maxSize) {
      super(maxSize);
    }

    protected String doNewObject() throws Exception {
      return "anObject" + getCreatedCount();
    }
  }
}
