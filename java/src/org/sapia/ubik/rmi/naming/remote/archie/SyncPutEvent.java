package org.sapia.ubik.rmi.naming.remote.archie;

import org.sapia.archie.Name;
import org.sapia.archie.NamePart;

import java.io.IOException;
import java.io.Serializable;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class SyncPutEvent extends SyncEvent implements Serializable {
  private Object  _toBind;
  private boolean _overwrite;

  /**
   * Constructor for SyncPutEvent.
   */
  public SyncPutEvent(Name nodePath, NamePart name, Object toBind,
    boolean overwrite) {
    super(nodePath, name);
    _toBind      = toBind;
    _overwrite   = overwrite;
  }

  /**
   * @return the <code>Object</code> that was put.
   */
  public Object getValue() throws IOException, ClassNotFoundException {
    return _toBind;
  }

  public boolean getOverwrite() {
    return _overwrite;
  }
}
