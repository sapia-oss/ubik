package org.sapia.ubik.rmi.naming.remote.archie;

import org.sapia.archie.Name;
import org.sapia.archie.NamePart;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class SyncGetEvent extends SyncEvent implements java.io.Serializable {
  /**
   * Constructor for SyncGetEvent.
   */
  public SyncGetEvent(Name nodePath, NamePart name) {
    super(nodePath, name);
  }
}
