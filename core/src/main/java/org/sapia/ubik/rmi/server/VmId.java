package org.sapia.ubik.rmi.server;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

import org.sapia.ubik.util.Strings;

/**
 * An instance of this class identifies a JVM uniquely.
 * 
 * @author Yanick Duchesne
 */
public class VmId implements java.io.Externalizable {

  public static final long serialVersionUID = 1L;

  private static VmId instance;

  static {
    UUID uuid = UUID.randomUUID();
    instance = new VmId(uuid.getLeastSignificantBits(), uuid.getMostSignificantBits());
  }

  private long left, right;

  /** Do not use; meant for externalization only. */
  public VmId() {
  }

  VmId(long left, long right) {
    this.left = left;
    this.right = right;
  }

  /**
   * @return the singleton instance of this VM's VmId.
   */
  public static VmId getInstance() {
    return instance;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    if (other instanceof VmId) {
      VmId otherId = (VmId) other;
      return left == otherId.left && right == otherId.right;
    } else {
      return false;
    }
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return (int) ((right + left) * 31);
  }

  /**
   * @see java.io.Externalizable#readExternal(ObjectInput)
   */
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    left = in.readLong();
    right = in.readLong();
  }

  /**
   * @see java.io.Externalizable#writeExternal(ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeLong(left);
    out.writeLong(right);
  }

  public String toString() {
    return Strings.toString("id", Long.toHexString(Math.abs(left)) + Long.toHexString(Math.abs(right)));
  }
}
