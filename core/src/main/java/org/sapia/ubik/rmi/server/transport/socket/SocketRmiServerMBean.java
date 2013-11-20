package org.sapia.ubik.rmi.server.transport.socket;

/**
 * @author yduchesne
 * 
 */
public interface SocketRmiServerMBean {

  /**
   * @return the IP address or host name on which this server listens.
   */
  public String getAddress();

  /**
   * @return the port on which this server listens.
   */
  public int getPort();

  /**
   * @return the number of request processing threads.
   */
  public int getThreadCount();

}
