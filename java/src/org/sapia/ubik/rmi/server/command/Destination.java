package org.sapia.ubik.rmi.server.command;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.VmId;


/**
 * Models a destination: a <code>ServerAddress</code> and a <code>VmId</code>.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Destination {
  private ServerAddress _addr;
  private VmId          _vmId;
  private int           _hashCode;

  public Destination(ServerAddress addr, VmId vmId) {
    _addr       = addr;
    _vmId       = vmId;
    _hashCode   = _vmId.toString().hashCode();
  }

  /**
   * Returns this instance's server address.
   *
   * @return a <code>ServerAddress</code>.
   */
  public ServerAddress getServerAddress() {
    return _addr;
  }

  /**
   * Returns a <code>VmId</code>.
   *
   * @return a <code>VmId</code>.
   */
  public VmId getVmId() {
    return _vmId;
  }

  public int hashCode() {
    return _hashCode;
  }

  public boolean equals(Object obj) {
    try {
      Destination other = (Destination) obj;

      return (_addr.hashCode() == other._addr.hashCode()) &&
      (_vmId.hashCode() == other._vmId.hashCode());
    } catch (ClassCastException e) {
      return false;
    }
  }

  public String toString() {
    return "[ vmId=" + _vmId + ", address=" + _addr + " ]";
  }
}
