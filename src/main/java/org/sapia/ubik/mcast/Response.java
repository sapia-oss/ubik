package org.sapia.ubik.mcast;


/**
 * Models a synchronous response to a remote event.
 *
 * @see org.sapia.ubik.mcast.EventChannel#send(ServerAddress, String, Object)
 * @see org.sapia.ubik.mcast.EventChannel#send(String, Object)
 * @see Response
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Response implements java.io.Serializable {
  /**
   * Corresponds to the OK status, signifying that the response
   * was returned normally.
   */
  public static final int STATUS_OK = 0;

  /**
   * Indicates that the remote node corresponding to this instance
   * is probably down.
   */
  public static final int STATUS_SUSPECT = 1;
  private long            _eventId;
  private Object          _data;
  private boolean         _none;
  private int             _status = STATUS_OK;

  /**
   * Constructor for Response.
   */
  Response(long eventId, Object data) {
    _eventId   = eventId;
    _data      = data;
  }

  /**
   * Returns <code>true</code> if this instance contains a <code>Throwable</code>.
   */
  public boolean isError() {
    return (_data != null) && _data instanceof Throwable;
  }

  /**
   * Returns the <code>Throwable</code> held within this response.
   *
   * @return a <code>Throwable</code>
   *
   * @see #isError()
   */
  public Throwable getThrowable() {
    if (_data != null) {
      return (Throwable) _data;
    }

    return null;
  }

  /**
   * Returns the data held by this instance.
   *
   * @return an <code>Object</code>, or null if this response
   * has no data.
   */
  public Object getData() {
    return _data;
  }

  /**
   * Returns this instance's status.
   *
   * @see #STATUS_OK
   * @see #STATUS_SUSPECT
   */
  public int getStatus() {
    return _status;
  }

  Response setNone() {
    _none = true;

    return this;
  }

  boolean isNone() {
    return _none;
  }

  Response setStatusSuspect() {
    _status = STATUS_SUSPECT;

    return this;
  }

  public String toString() {
    return "[ eventId=" + _eventId + ", data=" + _data + "] ";
  }
}
