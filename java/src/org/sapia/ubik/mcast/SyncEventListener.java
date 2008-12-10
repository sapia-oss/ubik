package org.sapia.ubik.mcast;


/**
 * Specifies the behavior of listeners of <code>MulticastEvent</code>
 * instances.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface SyncEventListener {
  /**
   * Receives multicast events.
   *
   * @param evt a <code>RemoteEvent</code>.
   * @return an <code>Object</code> consisting of
   * the synchronous response that is expected from this
   * intance. This method can return <code>null</code>, if
   * such can be the case. If an exception must be signaled
   * to the caller, then it should be returned.
   */
  public Object onSyncEvent(RemoteEvent evt);
}
