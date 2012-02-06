package org.sapia.ubik.rmi.replication;

import java.io.Serializable;
import java.util.Set;

import org.sapia.ubik.net.ServerAddress;


/**
 * @author Yanick Duchesne
 */
public interface ReplicatedInvoker extends Serializable {
  
  /**
   * 
   * @param methodName then name of the method to invoke.
   * @param sig the method's signature.
   * @param params the parameter to pass to the method.
   * @return the result of the invocation.
   * @throws Throwable
   */
  public Object invoke(String methodName, Class<?>[] sig, Object[] params) throws Throwable;

  /**
   * @return the {@link Set} of {@link ServerAddress}es corresponding to the siblings
   * of the server in which this instance is executed.
   */
  public Set<ServerAddress> getSiblings();
}
