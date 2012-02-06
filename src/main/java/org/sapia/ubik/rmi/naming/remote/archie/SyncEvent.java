package org.sapia.ubik.rmi.naming.remote.archie;

import java.io.Serializable;

import org.sapia.archie.Name;
import org.sapia.archie.NamePart;


/**
 * Holds basic synchronization data.
 * 
 * @author Yanick Duchesne
 */
public class SyncEvent implements Serializable {
  
  static final long serialVersionUID = 1L;
  
  private Name     nodePath;
  private NamePart name;

  /**
   * Constructor for SyncEvent.
   */
  public SyncEvent(Name nodePath, NamePart name) {
    this.nodePath   = nodePath;
    this.name       = name;
  }

  /**
   * @return the name of the object to synchronize.
   */
  public NamePart getName() {
    return name;
  }

  /**
   * @return the path of the node to which the object to synchronize belongs.
   */
  public Name getNodePath() {
    return nodePath;
  }
}
