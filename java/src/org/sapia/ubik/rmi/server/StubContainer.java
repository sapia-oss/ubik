package org.sapia.ubik.rmi.server;

import java.rmi.RemoteException;


/**
 * Implementations of this interface allow to bind stubs to remote JNDI servers
 * without the latter having to contain the stubs' interfaces in their classpath.
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface StubContainer extends java.io.Serializable {
  /**
   * Returns the stub that this instance contains.
   *
   * @return the stub that this instance contains.
   * @param a <code>ClassLoader</code>
   * @throws RemoteException if the stub could not be created.
   */
  public Object toStub(ClassLoader loader) throws RemoteException;

  /**
   * Returns the invocation handler that this instance wraps.
   *
   * @return a <code>StubInvocationHandler</code>.
   */
  public StubInvocationHandler getStubInvocationHandler();
}
