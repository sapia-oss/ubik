package org.sapia.ubik.net;

import java.io.Serializable;


/**
 * This interface models the concept of "server address", which hides
 * the transport-specific details (host, port, etc.).
 * <p>
 * Implementations of this interface should override the <code>equals</code>
 * method.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface ServerAddress extends Serializable {
  /**
   * Returns the "transport type" of the server corresponding
   * to this server address. A transport type is an arbitrary,
   * logical identifier.
   *
   * @return a transport type, as a <code>String</code>.
   */
  public String getTransportType();

  /**
   * Implementations should override this method in manner consistent
   * with the method's specification (in the <code>Object</code> class).
   *
   * @see Object#equals(Object obj).
   */
  public boolean equals(Object o);
}
