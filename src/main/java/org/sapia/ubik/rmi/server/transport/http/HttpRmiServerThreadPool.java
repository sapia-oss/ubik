package org.sapia.ubik.rmi.server.transport.http;

import org.sapia.ubik.net.PooledThread;
import org.sapia.ubik.net.ThreadPool;


/**
 * A pool of <code>HttpRmiServerThread</code> instances.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
class HttpRmiServerThreadPool extends ThreadPool {
  /**
   * Creates an instance of this class.
   */
  HttpRmiServerThreadPool(boolean daemon, int maxSize) {
    super("ubik.rmi.server.HttpServerThread", daemon, maxSize);
  }

  /**
   * @see org.sapia.ubik.net.ThreadPool#newThread()
   */
  protected PooledThread newThread() throws Exception {
    return new HttpRmiServerThread();
  }
}
