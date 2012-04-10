package org.sapia.ubik.rmi.server.command;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sapia.ubik.rmi.server.ShutdownException;


/**
 * @author Yanick Duchesne
 */
public class ExecQueueTest {

  @Test
  public void testShutdown() throws Exception {
    ExecQueue<Executable> queue = new ExecQueue<Executable>();
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

    assertTrue((System.currentTimeMillis() - start) > 700);
  }

  @Test
  public void testShutdownWithRemoveAll() throws Exception {
    final ExecQueue<Executable> queue = new ExecQueue<Executable>();
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

    queue.shutdown(5000);
    assertEquals(0, queue.size());

    try {
      queue.add(null);
      throw new Exception("ShutdownException not thrown");
    } catch (ShutdownException e) {
      //ok;
    }
  }

  @Test
  public void testShutdownWithRemove() throws Exception {
    final ExecQueue<Executable> queue = new ExecQueue<Executable>();
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
    assertEquals(0, queue.size());

    try {
      queue.add(null);
      throw new Exception("ShutdownException not thrown");
    } catch (ShutdownException e) {
      //ok;
    }

    assertTrue((System.currentTimeMillis() - start) > 700);
  }
}
