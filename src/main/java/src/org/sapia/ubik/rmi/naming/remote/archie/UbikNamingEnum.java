package org.sapia.ubik.rmi.naming.remote.archie;

import org.sapia.archie.Node;
import org.sapia.archie.jndi.JndiNamingEnum;

import java.util.Iterator;

import javax.naming.Context;


/**
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 * @author Yanick Duchesne
 */
public class UbikNamingEnum extends JndiNamingEnum implements java.rmi.Remote {
  /** Creates a new instance of UbikNamingEnum */
  public UbikNamingEnum(Iterator entries, Iterator nodes, int listType) {
    super(entries, nodes, listType);
  }

  protected Context newJndiContext(Node node) {
    return new UbikRemoteContext((UbikSyncNode) node);
  }
}
