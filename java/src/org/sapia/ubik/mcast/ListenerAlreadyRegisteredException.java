package org.sapia.ubik.mcast;


/**
 * Thrown when a synchronous event listener has already been
 * registered for a given event type.
 *
 * @see org.sapia.ubik.mcast.EventConsumer#registerSyncListener(String, SyncEventListener)
 * @see org.sapia.ubik.mcast.EventChannel#registerAsyncListener(String, AsyncEventListener)
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ListenerAlreadyRegisteredException extends Exception {
  /**
   * Constructor for ListenerAlreadyRegisteredException.
   */
  public ListenerAlreadyRegisteredException(String msg) {
    super(msg);
  }
}
