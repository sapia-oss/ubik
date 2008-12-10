package org.sapia.ubik.rmi.naming.remote;

import java.io.Serializable;

import org.sapia.ubik.mcast.DomainName;


/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class DomainInfo implements Serializable {
  private DomainName _domain;
  private String     _mcastAddress;
  private int        _mcastPort;

  public DomainInfo(DomainName domain, String mcastAddress, int mcastPort) {
    _domain         = domain;
    _mcastAddress   = mcastAddress;
    _mcastPort      = mcastPort;
  }

  /**
   * Returns the domain name of the remote context.
   *
   * @see RemoteContext
   * @return a <code>DomainName</code>
   */
  public DomainName getDomainName() {
    return _domain;
  }

  /**
   * Returns a multicast address.
   *
   * @return a multicast address.
   */
  public String getMulticastAddress() {
    return _mcastAddress;
  }

  /**
   * A multicast port.
   *
   * @return a port.
   */
  public int getMulticastPort() {
    return _mcastPort;
  }
}
