package org.sapia.ubik.rmi.server.command;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.VmId;

/**
 * Models a destination: a {@link ServerAddress} and a {@link VmId}. An instance
 * of this class corresponds to a remote endpoint to which asynchronous
 * {@link Response}s are to be sent.
 * 
 * @author Yanick Duchesne
 */
public class Destination {
  private ServerAddress addr;
  private VmId vmId;

  public Destination(ServerAddress addr, VmId vmId) {
    this.addr = addr;
    this.vmId = vmId;
  }

  /**
   * @return this instance's {@link ServerAddress}.
   */
  public ServerAddress getServerAddress() {
    return addr;
  }

  /**
   * @return this instance's {@link VmId}.
   */
  public VmId getVmId() {
    return vmId;
  }

  /**
   * @return this instance's hash code.
   */
  public int hashCode() {
    return vmId.hashCode();
  }

  public boolean equals(Object obj) {
    if (obj instanceof Destination) {
      Destination other = (Destination) obj;
      return addr.equals(other.addr) && vmId.equals(other.vmId);
    }
    return false;
  }

  public String toString() {
    return "[ vmId=" + vmId + ", address=" + addr + " ]";
  }
}
