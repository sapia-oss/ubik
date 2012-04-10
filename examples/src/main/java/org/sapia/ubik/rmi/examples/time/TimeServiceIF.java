package org.sapia.ubik.rmi.examples.time;

import java.rmi.Remote;


/**
 *
 *
 */
public interface TimeServiceIF extends Remote {
  /**
   * Returns the current system time.
   *
   * @return The current system time.
   */
  public String getTime();
}
