package org.sapia.ubik.rmi.naming.remote;

/**
 * Holds constants pertaining to JNDI.
 *
 * @author Yanick Duchesne
 */
public interface JndiConsts extends org.sapia.ubik.rmi.Consts {
  
  /**
   * The default port on which a JNDI server listens.
   */
  public static final int    DEFAULT_PORT        = 1099;
  
  /**
   * Corresponds to the remote event that is dispatched when a JNDI server node appears on 
   * the network.
   */
  public static final String JNDI_SERVER_PUBLISH = "ubik/rmi/naming/server/publish";

  /**
   * Corresponds to the remote event that is dispatched when a JNDI server node wishes to notify
   * another node (or other nodes) that it exists.
   */
  public static final String JNDI_SERVER_DISCO   = "ubik/rmi/naming/server/disco";
  
  /**
   * Corresponds to the remote event that is dispatched when a JNDI client node appears
   * on the network.
   */
  public static final String JNDI_CLIENT_PUBLISH = "ubik/rmi/naming/client/publish";
}
