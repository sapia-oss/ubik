package org.sapia.ubik.rmi.server.command;

import org.sapia.ubik.net.Timer;


/**
 * A client-side lock on which the caller of an asynchronous call-back
 * waits for the corresponding call-back's response.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ResponseLock {
  private static int    _count    = 0;
  private Object        _response;
  private ResponseQueue _queue;
  private String        _id;
  private boolean       _ready;

  /**
   * Constructor for ResponseLock.
   */
  ResponseLock(ResponseQueue parent) {
    _queue   = parent;
    _id      = generateId();
  }

  /**
   * Returns this lock's unique identifier.
   *
   * @return a unique identifier as a <code>String</code>.
   */
  public String getId() {
    return _id;
  }

  /**
   * Releases this lock (clears it from memory).
   */
  public void release() {
    _queue.removeLock(_id);
  }

  /***
   * Waits for the response of an asynchronous call-back. The caller
   * will wait for the length of time specified by the given timeout; if
   * no response comes in before the given timeout, a
   * <code>ResponseTimeOutException</code> is thrown.
   *
   * @param timeout a timeout, in milliseconds.
   * @throws ResponseTimeOutException if no response comes in before the specified timeout.
   * @throws InterruptedException if the caller is interrupted while waiting for the response.
   * @return a response, as an <code>Object</code>.
   */
  public synchronized Object waitResponse(long timeout)
    throws InterruptedException, ResponseTimeOutException {
    Timer timer = new Timer(timeout);

    while (!_ready) {
      wait(timeout);

      if (timer.isOver() && !_ready) {
        release();
        throw new ResponseTimeOutException();
      }
    }

    release();

    return _response;
  }

  /**
   * Sets this lock's response.
   *
   * @param an <code>Object</code> corresponding to an asynchronous response.
   */
  public synchronized void setResponse(Object r) {
    _response   = r;
    _ready      = true;
    notify();
  }

  private static synchronized String generateId() {
    if (_count > 999) {
      _count = 0;
    }

    return "" + System.currentTimeMillis() + (_count++);
  }
}
