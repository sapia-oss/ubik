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
public class SemaphoreTest extends TestCase {
  /**
   * Constructor for SemaphoreTest.
   * @param arg0
   */
  public SemaphoreTest(String arg0) {
    super(arg0);
  }

  public void testAcquire() throws Exception {
    Semaphore s = new Semaphore(3);
    s.acquireFor(new Runnable() {
        public void run() {
        }
      });
    s.acquireFor(new Runnable() {
        public void run() {
        }
      });
    s.acquireFor(new Runnable() {
        public void run() {
        }
      });

    try {
      s.acquireFor(new Runnable() {
          public void run() {
          }
        });
      throw new Exception("Thread creation should not have been authorized");
    } catch (MaxThreadReachedException e) {
      // ok
    }
  }

  public void testRelease() throws Exception {
    Semaphore s = new Semaphore(3);
    Thread    t;
    t = s.acquireFor(new Runnable() {
          public void run() {
          }
        });
    t.start();
    Thread.sleep(1000);
    s.acquireFor(new Runnable() {
        public void run() {
        }
      });
    s.acquireFor(new Runnable() {
        public void run() {
        }
      });
    s.acquireFor(new Runnable() {
        public void run() {
        }
      });
  }
}
