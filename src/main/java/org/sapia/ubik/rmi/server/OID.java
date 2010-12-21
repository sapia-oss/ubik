package org.sapia.ubik.rmi.server;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


/**
 * This class models a unique remote object identifier. An instance of this
 * class uniquely identifies a remote object, even among multiple VMs.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class OID implements Externalizable, Comparable<OID> {
  static final long serialVersionUID = 1L;
  private static final Object _unique = new Object();
  private long      _id;
  private int       _hashCode;
  private String    _codebase = System.getProperty("java.rmi.server.codebase");

  /** Do not call; used for externalization only. */
  public OID() {
  }

  /**
   * Creates an instance of this class with the given identifier string.
   */
  public OID(long id) {
    _id         = id;
    _hashCode   = (int) (id ^ (id >>> 32)) ^ _unique.hashCode();
  }

  /**
   * Creates a hashcode with this instance's internal ID string.
   *
   * @return a hashcode
   */
  public int hashCode() {
    return _hashCode;
  }

  /**
   * @return <code>true</code> if the object passed in is another object identifier
   * representing the same remote reference has this.
   */
  public boolean equals(Object o) {
    try {
      return (((OID) o)._id == _id) && (_hashCode == o.hashCode());
    } catch (ClassCastException e) {
      return false;
    }
  }

  /**
   * @see java.io.Externalizable#readExternal(ObjectInput)
   */
  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
    _id         = in.readLong();
    _hashCode   = in.readInt();
    _codebase   =(String)in.readObject();
  }

  /**
   * @see java.io.Externalizable#writeExternal(ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeLong(_id);
    out.writeInt(_hashCode);
    out.writeObject(_codebase);
  }
  
  /**
   * @return the remote codebase of the object to which this instance corresponds,
   * or <code>null</code> if no such codebase has been set.
   */
  public String getCodebase(){
    return _codebase;
  }

  public String toString() {
    return new StringBuffer().append("[id=").append(_id).append(", hashCode=")
                             .append(_hashCode).append("]").toString();
  }

  public int compareTo(OID other) {
    long diff = _id - other._id;

    if (diff < 0) {
      return -1;
    } else if (diff == 0) {
      return _hashCode - other.hashCode();
    } else {
      return 1;
    }
  }
}
