package org.sapia.ubik.mcast;

import java.util.Map;

import org.sapia.ubik.net.ServerAddress;

/**
 * Specifies the behavior common to multicast addresses.
 * 
 * @author yduchesne
 * 
 */
public interface MulticastAddress extends ServerAddress {

  /**
   * @return the {@link Map} of parameters that can be used to recreate an
   *         {@link MulticastAddress} corresponding to this instance.
   */
  public Map<String, String> toParameters();

}
