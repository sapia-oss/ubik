package org.sapia.ubik.rmi.replication;

import java.io.Serializable;

import java.util.Set;

import org.sapia.ubik.net.ServerAddress;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface ReplicatedInvoker extends Serializable {
  public Object invoke(String methodName, Class<?>[] sig, Object[] params)
    throws Throwable;

  /**
   * @return the {@link Set} of {@link ServerAddress}es corresponding to the siblings
   * of the server in which this instance is executed.
   */
  public Set<ServerAddress >getSiblings();
}
