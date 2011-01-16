package org.sapia.ubik.rmi.server;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;


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

  static {
      UUID uuid = UUID.randomUUID();
      _instance = new VmId(uuid.getLeastSignificantBits(), uuid.getMostSignificantBits());
  }

  private long _left, _right;

  /** Do not use; meant for externalization only. */
  public VmId() {
  }

  VmId(long left, long right) {
    _left = left;
    _right = right;
  }

  /**
   * @return the singleton instance of this VM's VmId.
   */
  public static VmId getInstance() {
    return _instance;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    if(other instanceof VmId){
      VmId otherId = (VmId)other;
      return _left == otherId._left && _right == otherId._right;
    }
    else{
      return false;
    }
  }
    
  public boolean equals(VmId otherId) {
    return _left == otherId._left && _right == otherId._right;
  } 

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return (int)(_right + _left * 31);
  }

  /**
   * @see java.io.Externalizable#readExternal(ObjectInput)
   */
  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
    _left  = in.readLong();
    _right = in.readInt();
  }

  /**
   * @see java.io.Externalizable#writeExternal(ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeLong(_left);
    out.writeLong(_right);
  }

  public String toString() {
    return "[ id=" + _left + _right + " ]";
  }
}
