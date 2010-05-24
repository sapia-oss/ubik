package org.sapia.ubik.rmi.server.command;

import org.sapia.ubik.net.Timer;
import org.sapia.ubik.rmi.server.ShutdownException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * Models a queue of <code>Executable</code> instances.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ExecQueue {
  private LinkedList _queue    = new LinkedList();
  private boolean    _shutdown;

  /**
   * Constructor for CommandQueue.
   */
  public ExecQueue() {
    super();
  }

  /**
   * Adds an <code>Executable</code> to this queue.
   *
   * @param toExecute an <code>Executable</code>.
   */
  public synchronized void add(Executable toExecute) {
    if (_shutdown) {
      throw new ShutdownException();
    }

    _queue.add(toExecute);
    notifyAll();
  }

  /**
   * Removes all the <code>Executable</code> from this queue and
   * returns them; if the queue is empty, this method blocks until
   * a new item is added.
   *
   * @return a <code>List</code> of <code>Executable</code>.
   */
  public synchronized List removeAll()
    throws InterruptedException, ShutdownException {
    while (_queue.size() == 0) {
      if (_shutdown) {
        notify();
        throw new ShutdownException();
      }

      wait();
    }

    List toReturn = new ArrayList(_queue);
    _queue.clear();

    return toReturn;
  }

  /**
   * Shuts down this instance.
   *
   * @param timeout a timeout in millis. If this queue still has pending
   * objects after the timeout is reached, this method returns.
   */
  public synchronized void shutdown(long timeout) throws InterruptedException {
    _shutdown = true;
    notifyAll();

    Timer timer = new Timer(timeout);

    while (_queue.size() > 0) {
      wait(timeout);

      if (timer.isOver()) {
        break;
      }
    }
  }

  /**
   * Returns this queue's size.
   *
   * @return this queue's size (the number of items in this queue).
   */
  public int size() {
    return _queue.size();
  }

  /**
   * Removes the first <code>Executable</code> from this queue and returns it.
   *
   * @return an <code>Executable</code>.
   */
  public synchronized Executable remove()
    throws InterruptedException, ShutdownException {
    while (_queue.size() == 0) {
      if (_shutdown) {
        notify();
        throw new ShutdownException();
      }

      wait();
    }

    return (Executable) _queue.remove(0);
  }
}
