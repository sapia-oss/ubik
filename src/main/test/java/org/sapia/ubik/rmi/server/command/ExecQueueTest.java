package org.sapia.ubik.rmi.server.command;

import junit.framework.TestCase;

import org.sapia.ubik.rmi.server.ShutdownException;


/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ExecQueueTest extends TestCase {
  public ExecQueueTest(String name) {
    super(name);
  }

  public void testShutdown() throws Exception {
    ExecQueue queue = new ExecQueue();
    queue.add(new Executable() {
        public Object execute() throws Throwable {
          return null;
        }
      });

    long start = System.currentTimeMillis();
    queue.shutdown(1000);

    try {
      queue.add(null);
      throw new Exception("ShutdownException not thrown");
    } catch (ShutdownException e) {
      //ok;
    }

    super.assertTrue((System.currentTimeMillis() - start) > 700);
  }

  public void testShutdownWithRemoveAll() throws Exception {
    final ExecQueue queue = new ExecQueue();
    queue.add(new Executable() {
        public Object execute() throws Throwable {
          return null;
        }
      });

    Thread remover = new Thread(new Runnable() {
          public void run() {
            try {
              Thread.sleep(700);
              queue.removeAll();
            } catch (Throwable t) {
            }
          }
        });
    remover.start();

    long start = System.currentTimeMillis();
    queue.shutdown(2000);
    super.assertEquals(0, queue.size());

    try {
      queue.add(null);
      throw new Exception("ShutdownException not thrown");
    } catch (ShutdownException e) {
      //ok;
    }
  }

  public void testShutdownWithRemove() throws Exception {
    final ExecQueue queue = new ExecQueue();
    queue.add(new Executable() {
        public Object execute() throws Throwable {
          return null;
        }
      });

    Thread remover = new Thread(new Runnable() {
          public void run() {
            try {
              Thread.sleep(700);
              queue.remove();
            } catch (Throwable t) {
            }
          }
        });
    remover.start();

    long start = System.currentTimeMillis();
    queue.shutdown(2000);
    super.assertEquals(0, queue.size());

    try {
      queue.add(null);
      throw new Exception("ShutdownException not thrown");
    } catch (ShutdownException e) {
      //ok;
    }

    super.assertTrue((System.currentTimeMillis() - start) > 700);
  }
}
