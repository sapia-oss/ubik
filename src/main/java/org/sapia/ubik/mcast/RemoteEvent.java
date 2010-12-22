package org.sapia.ubik.mcast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * Models a multicast event. An instance of this class strictly encapsulates its
 * data in the form of strings, in order to avoid classloading issues when
 * serializing/deserializing in a networked environment. A multicast is sent to a
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
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class RemoteEvent implements java.io.Serializable {
  
  static final long serialVersionUID = 1L;
  
  // CLASS VARIABLES
  static final int BUFSZ   = 1048;
  static int       _inc    = 0;
  static final int MAX_INC = 1000;

  // MEMBER VARIABLES
  private String  _domain;
  private String  _type;
  private long    _id       = generateId();
  private String  _node;
  private byte[]  _data;
  private boolean _wasBytes;
  private boolean _sync;

  /**
   * Creates an instance of this class.
   *
   * @param domain the name of the domain to which this instance is targeted.
   * @param type the event's type, which in fact is its logical type.
   * @param data the event's data.
   */
  public RemoteEvent(String domain, String type, Object data)
    throws IOException {
    _domain   = domain;
    _type     = type;

    //		if (data == null){
    //			//noop;
    //		}
    //    else 
    if ((data != null) && data instanceof byte[]) {
      _wasBytes   = true;
      _data       = (byte[]) data;
    } else {
      ByteArrayOutputStream bos = new ByteArrayOutputStream(BUFSZ);
      ObjectOutputStream    ous = new ObjectOutputStream(bos);

      ous.writeObject(data);
      ous.flush();
      ous.close();
      _data = bos.toByteArray();
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
   * Returns this instance's domain name.
   *
   * @return a domain name, or <code>null</code> if this instance
   * is not targeted at a single domain.
   */
  public String getDomainName() {
    return _domain;
  }

  /**
   * Returns this instance's logical typeentifier.
   *
   * @return a logical typeentifier.
   */
  public String getType() {
    return _type;
  }

  /**
   * Returns this event's unique identifier.
   *
   * @return a unique ID, as a string.
   */
  public long getId() {
    return _id;
  }

  /**
   * Returns this instance's data.
   *
   * @return this event's data, or <code>null</code> if this
   * instance has no data.
   */
  public Object getData() throws IOException {
    if (_data != null) {
      if (_wasBytes) {
        return (byte[]) _data;
      } else {
        ByteArrayInputStream bis = new ByteArrayInputStream((byte[]) _data);
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

    return _data;
  }

  /**
   * Returns <code>true</code> if this instance was created with a
   * domain name - meaning that it was targeted at a single domain.
   *
   * @return <code>true</code> if this instance has a domain name.
   */
  public boolean hasDomainName() {
    return _domain != null;
  }

  /**
   * Returns <code>true</code> if this instance represents an event
   * that necessitates a synchronous response.
   */
  public boolean isSync() {
    return _sync;
  }

  /**
   * Returns the identifier of the node that sent this event.
   *
   * @return a node identifier,
   */
  public String getNode() {
    return _node;
  }

  RemoteEvent setNode(String node) {
    _node = node;

    return this;
  }

  RemoteEvent setSync() {
    _sync = true;

    return this;
  }

  private static synchronized long generateId() {
    if (_inc++ > MAX_INC) {
      _inc = 0;
    }

    return Long.parseLong("" + System.currentTimeMillis() + _inc);
  }
}
