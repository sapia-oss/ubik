package org.sapia.ubik.rmi.server.command;

import org.sapia.ubik.net.Timer;
import org.sapia.ubik.rmi.server.ShutdownException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This queue stores outgoing <code>Response</code> objects until
 * they are processed.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class OutQueue extends ExecQueue<Response> {
  static Map<Destination, OutQueue> _queuesByHost = new ConcurrentHashMap<Destination, OutQueue>();
  static ResponseSender  _sender  = new LocalResponseSender();
  static OutQueueMonitor _monitor;
  static boolean         _added;

  static {
    _monitor = new OutQueueMonitor();
    _monitor.setName("ubik.rmi.outqueue.Monitor");
    _monitor.setDaemon(true);
    _monitor.start();
  }

  /**
   * Constructor for OutQueue.
   */
  private OutQueue() {
  }

  /**
   * Shuts down all statically kept <code>OutQueue</code> instances.
   *
   * @param timeout a shutdown timeout - in millis.
   */
  public static void shutdownAll(long timeout) throws InterruptedException {
    Iterator<OutQueue> queues = _queuesByHost.values().iterator();
    while (queues.hasNext()) {
      queues.next().shutdown(timeout);
    }

    _monitor.shutdown(timeout);
  }

  /**
   * Returns the <code>OutQueue</code> instance corresponding to the specific hosts.
   * <code>Response</code> objects are indeed kept on a per-host basis, so all responses
   * corresponding to a given host are sent at once to the latter, in the same trip - this
   * eventually spares remote calls.
   *
   * @return an <code>OutQueue</code> for the given <code>Destination</code>.
   */
  public static synchronized final OutQueue getQueueFor(Destination dest) {
    OutQueue out = (OutQueue) _queuesByHost.get(dest);

    if (out == null) {
      out = new OutQueue();
      _queuesByHost.put(dest, out);
    }

    return out;
  }

  static void setResponseSender(ResponseSender s) {
    _sender = s;
  }

  /*////////////////////////////////////////////////////////////////////
                             INNER CLASSES
  ////////////////////////////////////////////////////////////////////*/
  static final class OutQueueMonitor extends Thread {
    Destination[]    hosts;
    OutQueue         queue;
    List<Response>   responses;
    boolean          shutdown;
    boolean          shutdownRequested;

    public void run() {
      while (true) {
        hosts = (Destination[]) _queuesByHost.keySet().toArray(new Destination[_queuesByHost.size()]);

        for (int i = 0; i < hosts.length; i++) {
          queue = (OutQueue) _queuesByHost.get(hosts[i]);

          if (queue.size() > 0) {
            try {
              try {
                responses = queue.removeAll();
              } catch (ShutdownException e) {
                shutdownRequested = true;

                continue;
              }

              if (_sender != null) {
                _sender.sendResponses(hosts[i], responses);
              }
            } catch (InterruptedException e) {
              return;
            }
          }
        }

        if (shutdownRequested) {
          doNotifyShutDown();

          return;
        }

        Thread.yield();

        try {
          waitAdded();
        } catch (InterruptedException e) {
          break;
        }
      }
    }

    synchronized void shutdown(long timeout) throws InterruptedException {
      Timer timer = new Timer(timeout);
      shutdownRequested = true;
      notify();

      while (!shutdown) {
        wait(timeout);

        if (timer.isOver()) {
          break;
        }
      }
    }

    private synchronized void doNotifyShutDown() {
      shutdown = true;
      notifyAll();
    }

    private synchronized void waitAdded() throws InterruptedException {
      while (!_added && !shutdownRequested) {
        wait();
      }

      _added = false;
    }

    synchronized void wakeUp() {
      _added = true;
      notify();
    }
  }
}
