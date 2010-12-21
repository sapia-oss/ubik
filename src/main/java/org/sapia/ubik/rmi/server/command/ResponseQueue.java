package org.sapia.ubik.rmi.server.command;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.net.Timer;


/**
 * This class implements a client-side response queue that internally
 * keeps response locks.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ResponseQueue {
  
  private static ResponseQueue      _instance  = new ResponseQueue();
  private Map<String, ResponseLock> _responses = new ConcurrentHashMap<String, ResponseLock>();
  private boolean                   _shutdown;

  /**
   * Constructor for ResponseQueue.
   */
  ResponseQueue() {
    super();
  }

  /***
   * Creates a response lock and returns it (internally maps the response
   * lock to an unique identifier which is also assigned to the lock.
   *
   * @return a <code>ResponseLock</code>.
   */
  public synchronized ResponseLock createResponseLock() {
    ResponseLock lock = new ResponseLock(this);

    if (_responses.get(lock.getId()) != null) {
      throw new IllegalStateException("response lock already exists for: " +
        lock.getId());
    }

    _responses.put(lock.getId(), lock);

    return lock;
  }

  /**
   * This method is called to notify this queue about incoming
   * responses.
   *
   * @param responses a <code>List</code> of <code>Response</code> instances.
   */
  public void onResponses(List<Response> responses) {
    Response     resp;
    ResponseLock lock;

    for (int i = 0; i < responses.size(); i++) {
      resp   = (Response) responses.get(i);
      lock   = (ResponseLock) _responses.get(resp.getId());

      if (lock != null) {
        lock.setResponse(resp.get());
      }
    }
  }

  public synchronized void shutdown(long timeout) throws InterruptedException {
    _shutdown = true;

    Timer timer = new Timer(timeout);

    while (_responses.size() > 0) {
      wait(timeout);

      if (timer.isOver()) {
        break;
      }
    }
  }

  public int size() {
    return _responses.size();
  }

  synchronized void removeLock(String id) {
    _responses.remove(id);

    if (_shutdown) {
      notify();
    }
  }

  /**
   * Returns this queue class' singleton.
   *
   * @return a <code>ResponseQueue</code>.
   */
  public static final ResponseQueue getInstance() {
    return _instance;
  }
}
