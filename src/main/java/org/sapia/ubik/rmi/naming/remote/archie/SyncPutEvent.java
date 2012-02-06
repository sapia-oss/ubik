package org.sapia.ubik.rmi.naming.remote.archie;

import java.io.IOException;
import java.io.Serializable;

import org.sapia.archie.Name;
import org.sapia.archie.NamePart;

/**
 * Corresponds to a bind that is disributed to other JNDI nodes.
 * 
 * @author Yanick Duchesne
 */
public class SyncPutEvent extends SyncEvent implements Serializable {
  
  static final long serialVersionUID = 1L;
  
  private Object  toBind;
  private boolean overwrite;

  /**
   * Constructor for SyncPutEvent.
   */
  public SyncPutEvent(Name nodePath, NamePart name, Object toBind,
    boolean overwrite) {
    super(nodePath, name);
    this.toBind      = toBind;
    this.overwrite   = overwrite;
  }

  /**
   * @return the {@link Object} that was put.
   */
  public Object getValue() throws IOException, ClassNotFoundException {
    return toBind;
  }

  /**
   * @return <code>true</code> if the object held by this instance should overwrite any existing
   * object under the same name as this instance's.
   */
  public boolean getOverwrite() {
    return overwrite;
  }
}
