package org.sapia.ubik.rmi.server;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.InetAddress;


/**
 * A VM unique identifier.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class VmId implements java.io.Externalizable {
  public static final long serialVersionUID = 1L;
  private static VmId      _instance;
  private static Object _unique = new Object();

  static {
    try {
      _instance = new VmId(InetAddress.getLocalHost().getHostAddress().hashCode() ^
          VmId.class.hashCode() ^ UIDGenerator.createdUID());
    } catch (Throwable t) {
      t.printStackTrace();
      new IllegalStateException("could not initialize VM identifier");
    }
  }

  private long _id;
  private int  _hashCode;

  /** Do not use; meant for externalization only. */
  public VmId() {
  }

  VmId(long id) {
    _id         = id;
    _hashCode   = (int) (_id ^ (_id >>> 32)) ^ _unique.hashCode();
  }

  /**
   * @return the singleton instance of this VM's VmId.
   */
  public static VmId getInstance() {
    return _instance;
  }

  public boolean equals(VmId other) {
    return other._id == _id && _hashCode == other.hashCode();
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    try {
      return equals((VmId) other);
    } catch (ClassCastException e) {
      return false;
    }
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return _hashCode;
  }

  /**
   * @see java.io.Externalizable#readExternal(ObjectInput)
   */
  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
    _id         = in.readLong();
    _hashCode   = in.readInt();
  }

  /**
   * @see java.io.Externalizable#writeExternal(ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeLong(_id);
    out.writeInt(_hashCode);
  }

  public String toString() {
    return "[ id=" + _id + " ]";
  }
}
