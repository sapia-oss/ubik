package org.sapia.ubik.rmi.server.command;

import java.util.concurrent.atomic.AtomicLong;

import org.sapia.ubik.util.Delay;

/**
 * A client-side lock on which the caller of an asynchronous call-back waits for
 * the corresponding call-back's response.
 * 
 * @author Yanick Duchesne
 */
public class ResponseLock {

  private static final long MAX_VALUE = Long.MAX_VALUE - 10000;

  private static AtomicLong count = new AtomicLong();
  private Object response;
  private CallbackResponseQueue queue;
  private long id;
  private volatile boolean ready;

  /**
   * Constructor for ResponseLock.
   */
  ResponseLock(CallbackResponseQueue parent) {
    queue = parent;
    id = generateId();
  }

  /**
   * Returns this lock's unique identifier.
   * 
   * @return a this instance's unique identifier.
   */
  public long getId() {
    return id;
  }

  /**
   * Releases this lock (clears it from memory).
   */
  public void release() {
    queue.removeLock(id);
  }

  /***
   * Waits for the response of an asynchronous call-back. The caller will wait
   * for the length of time specified by the given timeout; if no response comes
   * in before the given timeout, a {@link ResponseTimeOutException} is thrown.
   * 
   * @param timeout
   *          a timeout, in milliseconds.
   * @throws ResponseTimeOutException
   *           if no response comes in before the specified timeout.
   * @throws InterruptedException
   *           if the caller is interrupted while waiting for the response.
   * @return a response, as an {@link Object}.
   */
  public synchronized Object await(long timeout) throws InterruptedException, ResponseTimeOutException {
    Delay timer = new Delay(timeout);

    while (!ready) {
      wait(timer.remainingNotZero());

      if (timer.isOver() && !ready) {
        release();
        throw new ResponseTimeOutException();
      }
    }

    release();
    return response;
  }

  /**
   * Sets this lock's response.
   * 
   * @param r
   *          an {@link Object} corresponding to an asynchronous response.
   */
  public synchronized void setResponse(Object r) {
    response = r;
    ready = true;
    notify();
  }

  private static long generateId() {
    long value = count.incrementAndGet();
    if (value >= MAX_VALUE) {
      count.compareAndSet(value, 0);
    }
    return value;
  }
}
