package org.sapia.ubik.rmi.naming.remote;

import java.io.Serializable;

import org.sapia.ubik.mcast.DomainName;
import org.sapia.ubik.mcast.MulticastAddress;

/**
 * Wraps a {@link DomainName} and a {@link MulticastAddress}.
 * 
 * @author Yanick Duchesne
 */
public class DomainInfo implements Serializable {

  static final long serialVersionUID = 1L;

  private DomainName domain;
  private MulticastAddress mcastAddress;

  /**
   * @param domain
   *          a {@link DomainName}.
   * @param mcastAddress
   *          a {@link MulticastAddress}.
   */
  public DomainInfo(DomainName domain, MulticastAddress mcastAddress) {
    this.domain = domain;
    this.mcastAddress = mcastAddress;
  }

  /**
   * Returns the domain name of the remote context.
   * 
   * @see RemoteContext
   * @return this instance's corresponding {@link DomainName}.
   */
  public DomainName getDomainName() {
    return domain;
  }

  /**
   * @return this instance's {@link MulticastAddress}.
   */
  public MulticastAddress getMulticastAddress() {
    return mcastAddress;
  }

}
