package org.sapia.ubik.mcast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.sapia.ubik.net.ServerAddress;


/**
 * Models a multicast event. An instance of this class strictly encapsulates its
 * data in the form of an array of bytes, in order to avoid classloading issues when
 * serializing/deserializing in a networked environment across JVMs. A multicast is sent to a
 * domain, or to all domains, according to the domain name information that is
 * kept an the event.
 * <p>
 * Furthermore, a multicast event has a "type", which provides the event's
 * logical type - and allows applications to register for events of a given logical
 * type.
 * <p>
 * Finally, data can also be passed.
 *
 *
 * @author Yanick Duchesne
 */
public class RemoteEvent implements java.io.Serializable {
  
  static final long serialVersionUID = 1L;
  
  // CLASS VARIABLES
  static final int BUFSZ   = 1048;
  static int       inc     = 0;
  static final int MAX_INC = 1000;

  // MEMBER VARIABLES
  private String        domain;
  private String        type;
  private long          id = generateId();
  private String        node;
  private byte[]        data;
  private boolean       wasBytes;
  private boolean       sync;
  private ServerAddress unicastAddress;

  /**
   * Creates an instance of this class.
   *
   * @param domain the name of the domain to which this instance is targeted.
   * @param type the event's type, which in fact is its logical type.
   * @param data the event's data.
   */
  public RemoteEvent(String domain, String type, Object data)
    throws IOException {
    this.domain   = domain;
    this.type     = type;

    if ((data != null) && data instanceof byte[]) {
      this.wasBytes   = true;
      this.data       = (byte[]) data;
    } else {
      ByteArrayOutputStream bos = new ByteArrayOutputStream(BUFSZ);
      ObjectOutputStream    ous = new ObjectOutputStream(bos);

      ous.writeObject(data);
      ous.flush();
      ous.close();
      this.data = bos.toByteArray();
    }
  }

  /**
   * Creates an instance of this class that is targeted at all domains.
   *
   * @param type the event's type, which in fact is its logical type.
   * @param data the event's data.
   */
  public RemoteEvent(String type, Object data) throws IOException {
    this(null, type, data);
  }

  /**
   * 
   * @return the unicast {@link ServerAddress} of the node from which this event originates,
   * or <code>null</code> if no such address has been set (or most likely if the node cannot
   * be connected to directly).
   */
  public ServerAddress getUnicastAddress() {
    return unicastAddress;
  }
  
  /**
   * Sets the unicast addresss of the node from which this event originates.
   * 
   * @param addr a {@link ServerAddress}.
   */
  public void setUnicastAddress(ServerAddress addr){
    unicastAddress = addr;
  }
  
  /**
   * Returns this instance's domain name.
   *
   * @return a domain name, or <code>null</code> if this instance
   * is not targeted at a single domain.
   */
  public String getDomainName() {
    return domain;
  }

  /**
   * Returns this instance's logical type identifier.
   *
   * @return a logical type identifier.
   */
  public String getType() {
    return type;
  }

  /**
   * Returns this event's unique identifier.
   *
   * @return a unique ID, as a string.
   */
  public long getId() {
    return id;
  }

  /**
   * Returns this instance's data.
   *
   * @return this event's data, or <code>null</code> if this
   * instance has no data.
   */
  public Object getData() throws IOException {
    if (data != null) {
      if (wasBytes) {
        return (byte[]) data;
      } else {
        ByteArrayInputStream bis = new ByteArrayInputStream((byte[]) data);
        ObjectInputStream    ois = new ObjectInputStream(bis);
        

        try {
          Object obj = ois.readObject();
          return obj;
        } catch (ClassNotFoundException e) {
          throw new IOException(e.getClass().getName() + " caught: " +
            e.getMessage());
        } finally {
          ois.close();
        }
      }
    }

    return data;
  }

  /**
   * Returns <code>true</code> if this instance was created with a
   * domain name - meaning that it was targeted at a single domain.
   *
   * @return <code>true</code> if this instance has a domain name.
   */
  public boolean hasDomainName() {
    return domain != null;
  }

  /**
   * Returns <code>true</code> if this instance represents an event
   * that necessitates a synchronous response.
   */
  public boolean isSync() {
    return sync;
  }
  
  /**
   * @return sets this instance's <code>sync</code> flag to true.
   * @see {@link #isSync()}.
   */
  public RemoteEvent setSync() {
    sync = true;

    return this;
  }  

  /**
   * Returns the identifier of the node that sent this event.
   *
   * @return a node identifier,
   */
  public String getNode() {
    return node;
  }

  /**
   * @param node a node identifier.
   * @return this instance.
   */
  public RemoteEvent setNode(String node) {
    this.node = node;

    return this;
  }

  private static synchronized long generateId() {
    if (inc++ > MAX_INC) {
      inc = 0;
    }

    return Long.parseLong("" + System.currentTimeMillis() + inc);
  }
}
