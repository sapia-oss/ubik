package org.sapia.ubik.rmi.server.gc;

import org.sapia.ubik.rmi.Consts;

public interface ServerGCMBean {

  /**
   * @see Consts#SERVER_GC_INTERVAL
   */
  public long getInterval();

  /**
   * @see Consts#SERVER_GC_TIMEOUT
   */
  public long getTimeout();

  public void setTimeout(long timeout);

  /**
   * @return the number of clients that the server GC keeps track of.
   */
  public int getClientCount();

}
