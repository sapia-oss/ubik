package org.sapia.ubik.mcast;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Strings;

/**
 * Models a synchronous response to a remote event.
 * 
 * @see org.sapia.ubik.mcast.EventChannel
 * 
 * @author Yanick Duchesne
 */
public class Response implements Externalizable {

  static final long serialVersionUID = 1L;

  /**
   * Corresponds to the OK status, signifying that the response was returned
   * normally.
   */
  public static final int STATUS_OK = 0;

  /**
   * Indicates that the remote node corresponding to this instance is probably
   * down.
   */
  public static final int STATUS_SUSPECT = 1;

  private ServerAddress sourceAddress;
  private long          eventId;
  private Object        data;
  private boolean       none;
  private int           status = STATUS_OK;

  /**
   * DO NOT CALL: meant for externalization only.
   */
  public Response() {
  }

  /**
   * @param eventId an event ID.
   * @param data some data to send as part of the event (must be {@link Serializable}/{@link Externalizable}).
   */
  public Response(ServerAddress sourceAddress, long eventId, Object data) {
    this.sourceAddress = sourceAddress;
    this.eventId       = eventId;
    this.data          = data;
  }

  /**
   * Returns <code>true</code> if this instance contains a {@link Throwable}.
   */
  public boolean isThrowable() {
    return (data != null) && data instanceof Throwable;
  }

  /**
   * Returns the {@link Throwable} held within this response.
   * 
   * @return a {@link Throwable}.
   * 
   * @see #isThrowable()
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
   * @return an {@link Object}, or null if this response has no data.
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
  
  /**
   * Indicates that the remote node is available, but that no
   * event listener was registered to process the remote event that was sent.
   * 
   * @return <code>true</code> if no remote event listener was setup to handle
   * the remote event to which this instance corresponds.
   */
  public boolean isNone() {
    return none;
  }

  public Response setStatusSuspect() {
    status = STATUS_SUSPECT;

    return this;
  }
  
  /**
   * Indicates if the remote node is suspect (that is: if some network problem
   * occurred that prevented the call to it to succeed - it could be that the node
   * is down, that a socket timeout occurred, or that there was some other type
   * of low-level cause for the communication not completing successfully).
   * 
   * @return <code>true</code> if the remote node is suspect.
   */
  public boolean isSuspect() {
    return status == STATUS_SUSPECT;
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    sourceAddress = (ServerAddress) in.readObject();
    eventId       = in.readLong();
    data          = in.readObject();
    none          = in.readBoolean();
    status        = in.readInt();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(sourceAddress);
    out.writeLong(eventId);
    out.writeObject(data);
    out.writeBoolean(none);
    out.writeInt(status);
  }
  
  public String toString() {
    return Strings.toString("eventId", eventId, "data", data, "status", status == STATUS_OK ? "OK" : "SUSPECT");
  }
}
