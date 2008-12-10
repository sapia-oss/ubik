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
   * @return the current RPS stat of the server.
   */
  public double getRequestsPerSecond();

  /**
   * @return the current transaction duration stat of the server (in seconds)
   */
  public double getRequestDurationSeconds();
  
  /**
   * @return the number of request processing threads.
   */
  public int getThreadCount();
  
  /**
   * Enables statistics.
   */
  public void enableStats();

  /**
   * Disables statistics.
   */  
  public void disableStats();
  
}
