package org.sapia.ubik.rmi.server;

import org.sapia.ubik.mcast.EventChannel;

public interface EventChannelTableMBean {

  /**
   * @return the number of {@link EventChannel} instances that are cached.
   */
  public int getEventChannelCount();
}
