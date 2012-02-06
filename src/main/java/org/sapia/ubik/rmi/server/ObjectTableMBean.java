package org.sapia.ubik.rmi.server;

public interface ObjectTableMBean {

  /**
   * @return the total number of references that this instance holds.
   */
  public int getRefCount();
}
