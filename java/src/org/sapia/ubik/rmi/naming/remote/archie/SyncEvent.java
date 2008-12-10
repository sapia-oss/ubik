package org.sapia.ubik.rmi.naming.remote.archie;

import org.sapia.archie.Name;
import org.sapia.archie.NamePart;

import java.io.Serializable;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class SyncEvent implements Serializable {
  private Name     _nodePath;
  private NamePart _name;

  /**
   * Constructor for SyncEvent.
   */
  public SyncEvent(Name nodePath, NamePart name) {
    _nodePath   = nodePath;
    _name       = name;
  }

  /**
   * @return the name of the object to synchronize.
   */
  public NamePart getName() {
    return _name;
  }

  /**
   * @return the path of the node to which the object to synchronize belongs.
   */
  public Name getNodePath() {
    return _nodePath;
  }
}
