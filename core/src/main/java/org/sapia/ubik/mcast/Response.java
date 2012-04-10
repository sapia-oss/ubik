package org.sapia.ubik.mcast;

import org.sapia.ubik.net.ServerAddress;


/**
 * Models a synchronous response to a remote event.
 *
 * @see org.sapia.ubik.mcast.EventChannel#send(ServerAddress, String, Object)
 * @see org.sapia.ubik.mcast.EventChannel#send(String, Object)
 * @see Response
 *
 * @author Yanick Duchesne
 */
public class Response implements java.io.Serializable {
  
  static final long serialVersionUID = 1L;
  
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
  
  private long            eventId;
  private Object          data;
  private boolean         none;
  private int             status = STATUS_OK;
  /**
   * Constructor for Response.
   */
  public Response(long eventId, Object data) {
    this.eventId   = eventId;
    this.data      = data;
  }

  /**
   * Returns <code>true</code> if this instance contains a {@link Throwable}.
   */
  public boolean isError() {
    return (data != null) && data instanceof Throwable;
  }

  /**
   * Returns the {@link Throwable} held within this response.
   *
   * @return a {@link Throwable}.
   *
   * @see #isError()
   */
  public Throwable getThrowable() {
    if (data != null) {
      return (Throwable) data;
    }

    return null;
  }

  /**
   * Returns the data held by this instance.
   *
   * @return an {@link Object}, or null if this response
   * has no data.
   */
  public Object getData() {
    return data;
  }

  /**
   * Returns this instance's status.
   *
   * @see #STATUS_OK
   * @see #STATUS_SUSPECT
   */
  public int getStatus() {
    return status;
  }

  public Response setNone() {
    none = true;

    return this;
  }

  public boolean isNone() {
    return none;
  }

  public Response setStatusSuspect() {
    status = STATUS_SUSPECT;

    return this;
  }

  public String toString() {
    return "[ eventId=" + eventId + ", data=" + data + "] ";
  }
}
