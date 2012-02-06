package org.sapia.ubik.rmi.naming.remote;

import java.rmi.Remote;

import javax.naming.Context;


/**
 * Specifies the behavior a a remote {@link Context}.
 * 
 * @author Yanick Duchesne
 *
 */
public interface RemoteContext extends Remote, Context {
  
  /**
   * @return this instance's {@link DomainInfo}.
   */
  public DomainInfo getDomainInfo();
}
