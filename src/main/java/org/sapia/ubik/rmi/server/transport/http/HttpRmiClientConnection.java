package org.sapia.ubik.rmi.server.transport.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.transport.MarshalStreamFactory;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.rmi.server.transport.RmiObjectOutput;


/**
 * Implements the {@link RmiConnection} interface over HTTP - more precisely,
 * over a Jakarta HTTP client. Data is sent using the POST method.
 * </p>
 * An instance of this class is used on the client side.
 *
 * @see org.sapia.ubik.rmi.server.transport.http.HttpRmiServerConnection
 *
 * @author Yanick Duchesne
 */
public class HttpRmiClientConnection implements RmiConnection {
  private static final int DEFAULT_BUFSZ = 1024;
  private HttpAddress      address;
  private HttpClient       client;
  private PostMethod       post;
  private boolean          closed;
  private int              bufsz = DEFAULT_BUFSZ;

  /**
   * Creates an instance of this class with the given HTTP client and
   * uri to connect to.
   */
  public HttpRmiClientConnection() {
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.RmiConnection#send(java.lang.Object, org.sapia.ubik.rmi.server.VmId, java.lang.String)
   */
  public void send(Object o, VmId associated, String transportType)
    throws IOException, RemoteException {
    post = new PostMethod(address.toString());

    ByteArrayOutputStream bos = new ByteArrayOutputStream(bufsz);
    ObjectOutputStream    mos = MarshalStreamFactory.createOutputStream(bos);

    if ((associated != null) && (transportType != null)) {
      ((RmiObjectOutput)mos).setUp(associated, transportType);
    }

    mos.writeObject(o);
    mos.flush();
    mos.close();

    byte[] data = bos.toByteArray();

    if (data.length > bufsz) {
      bufsz = data.length;
    }

    post.setRequestContentLength(data.length);
    post.setRequestBody(new ByteArrayInputStream(data));
    client.executeMethod(post);
  }

  /**
   * @see org.sapia.ubik.net.Connection#close()
   */
  public void close() {
    if ((post != null) && !closed) {
      post.releaseConnection();
      closed = true;
    }
  }

  /**
   * @see org.sapia.ubik.net.Connection#getServerAddress()
   */
  public ServerAddress getServerAddress() {
    return address;
  }

  /**
   * @see org.sapia.ubik.net.Connection#receive()
   */
  public Object receive()
    throws IOException, ClassNotFoundException, RemoteException {
    if (post == null) {
      throw new IllegalStateException("Cannot receive; data was not posted");
    }

    ObjectInputStream is = MarshalStreamFactory.createInputStream(post.getResponseBodyAsStream());

    try {
      return is.readObject();
    } catch (IOException ioe) {
      Log.error(HttpRmiClientConnection.class, "Could not receive response", ioe);
      throw ioe;
    } finally {
      is.close();
    }
  }

  /**
   * @see org.sapia.ubik.net.Connection#send(java.lang.Object)
   */
  public void send(Object o) throws IOException, RemoteException {
    send(o, null, null);
  }

  HttpRmiClientConnection setUp(HttpClient client, HttpAddress addr) {
    this.closed    = false;
    this.client    = client;
    this.address   = addr;

    return this;
  }
}
