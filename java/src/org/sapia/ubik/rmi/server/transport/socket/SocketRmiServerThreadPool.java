package org.sapia.ubik.rmi.server.transport.socket;

import org.sapia.ubik.net.PooledThread;
import org.sapia.ubik.net.ThreadPool;


/**
 * Implements a pool of <code>SocketRmiServerThread</code>s in a <code>SocketRmiServer</code> instance.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class SocketRmiServerThreadPool extends ThreadPool {
  /**
   * Constructor for RMIServerThreadPool.
   * @param name
   * @param daemon
   * @param maxSize
   */
  public SocketRmiServerThreadPool(String name, boolean daemon, int maxSize) {
    super(name, daemon, maxSize);
  }

  /**
   * @see org.sapia.ubik.net.ThreadPool#newThread()
   */
  protected PooledThread newThread() throws Exception {
    return new SocketRmiServerThread();
  }
}
