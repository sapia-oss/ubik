package org.sapia.ubik.rmi.server;

import java.lang.reflect.InvocationHandler;


/**
 * Base interface of invocation handlers that implement the logic of dynamic stubs -
 * created by the Ubik RMI runtime.
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface StubInvocationHandler extends InvocationHandler {
  /**
   * Returns this instance's stub container.
   *
   * @return a <code>StubContainer</code>
   */
  public StubContainer toStubContainer(Object proxy);
  
  /**
   * Returns the object identifier of the remote object corresponding to this 
   * instance. 
   *
   * @return an <code>OID</code>.
   */
  public OID getOID();
  
}
