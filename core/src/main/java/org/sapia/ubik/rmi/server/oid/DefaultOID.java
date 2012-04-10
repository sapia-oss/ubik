package org.sapia.ubik.rmi.server.oid;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.util.Strings;


/**
 * This class models a unique remote object identifier. An instance of this
 * class uniquely identifies a remote object, even among multiple VMs.
 *
 * @author Yanick Duchesne
 */
public class DefaultOID implements Externalizable, OID {
  static final long serialVersionUID = 1L;
  private static final Object unique = new Object();
  private long      id;
  private int       hashCode;

  /** Do not call; used for externalization only. */
  public DefaultOID() {
  }

  /**
   * Creates an instance of this class with the given identifier string.
   */
  public DefaultOID(long id) {
    this.id    = id;
    hashCode   = (int) (id ^ (id >>> 32)) ^ unique.hashCode();
  }

  /**
   * Creates a hashcode with this instance's internal ID string.
   *
   * @return a hashcode
   */
  public int hashCode() {
    return hashCode;
  }

  /**
   * @return <code>true</code> if the object passed in is another object identifier
   * representing the same remote reference has this.
   */
  public boolean equals(Object o) {
    if(o instanceof DefaultOID) {
      return (((DefaultOID) o).id == id) && (hashCode == o.hashCode());
    }
    return false;
  }

  /**
   * @see java.io.Externalizable#readExternal(ObjectInput)
   */
  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
    id         = in.readLong();
    hashCode   = in.readInt();
  }

  /**
   * @see java.io.Externalizable#writeExternal(ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeLong(id);
    out.writeInt(hashCode);
  }
  
  public String toString() {
    return Strings.toString("id", id, "hashCode", Integer.toHexString(hashCode));
  }
}
