package org.sapia.ubik.rmi.server.transport.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;
import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.Defaults;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.rmi.server.transport.MarshalStreamFactory;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.rmi.server.transport.RmiObjectOutput;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Conf;

/**
 * Implements the {@link RmiConnection} interface over HTTP - more precisely,
 * over a Jakarta HTTP client. Data is sent using the POST method. </p> An
 * instance of this class is used on the client side.
 * 
 * @see org.sapia.ubik.rmi.server.transport.http.HttpRmiServerConnection
 * 
 * @author Yanick Duchesne
 */
public class HttpRmiClientConnection implements RmiConnection {
  
  private Category log = Log.createCategory(getClass());

  private static Stopwatch serializationTime = Stats.createStopwatch(HttpRmiClientConnection.class, "SerializationDuration",
      "Time required to serialize an object");

  private static Stopwatch sendTime = Stats.createStopwatch(HttpRmiClientConnection.class, "SendDuration",
      "Time required to send an object over the network");

  private static final int STATUS_OK = 200;

  private HttpAddress address;
  private HttpClient client;
  private byte[] responsePayload;
  private int bufsz = Conf.getSystemProperties().getIntProperty(Consts.MARSHALLING_BUFSIZE, Defaults.DEFAULT_MARSHALLING_BUFSIZE);

  /**
   * Creates an instance of this class with the given HTTP client and uri to
   * connect to.
   */
  public HttpRmiClientConnection(HttpClient client, HttpAddress address) {
    this.client = client;
    this.address = address;
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.RmiConnection#send(java.lang.Object,
   *      org.sapia.ubik.rmi.server.VmId, java.lang.String)
   */
  public void send(Object o, VmId associated, String transportType) throws IOException, RemoteException {
    HttpPost post = new HttpPost(address.toString());

    ByteArrayOutputStream bos = new ByteArrayOutputStream(bufsz);

    log.debug("Sending: %s", o);
    
    Split split = serializationTime.start();
    ObjectOutputStream mos = MarshalStreamFactory.createOutputStream(bos);
    if ((associated != null) && (transportType != null)) {
      ((RmiObjectOutput) mos).setUp(associated, transportType);
    }
    mos.writeObject(o);
    mos.flush();
    mos.close();
    split.stop();

    split = sendTime.start();
    byte[] data = bos.toByteArray();

    if (data.length > bufsz) {
      bufsz = data.length;
    }

    post.setEntity(new ByteArrayEntity(data));
    try {
      log.debug("Performing HTTP POST");
      HttpResponse response = client.execute(post);
      log.debug("Got HTTP response");
      
      if (response.getStatusLine().getStatusCode() != STATUS_OK) {
        throw new IOException("HTTP response error " + response.getStatusLine().getStatusCode() + " caught: "
            + response.getStatusLine().getReasonPhrase());
      }
      responsePayload = EntityUtils.toByteArray(response.getEntity());
    } catch (SocketException e) {
      throw new RemoteException("Connection error caught", e);
    } finally {
      post.releaseConnection();
    }
    split.stop();
  }

  /**
   * @see org.sapia.ubik.net.Connection#close()
   */
  public void close() {
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
  public Object receive() throws IOException, ClassNotFoundException, RemoteException {
    Assertions.illegalState(responsePayload == null, "Cannot receive; response payload not set");

    byte[] thePayload = responsePayload;
    responsePayload = null;
    ObjectInputStream is = MarshalStreamFactory.createInputStream(new ByteArrayInputStream(thePayload));

    try {
      return is.readObject();
    } catch (IOException ioe) {
      Log.error(HttpRmiClientConnection.class, "Could not read response data", ioe);
      throw ioe;
    } finally {
      is.close();
    }
  }
  
  @Override
  public Object receive(long timeout) throws IOException,
      ClassNotFoundException, RemoteException, SocketTimeoutException {
    return receive();
  }

  /**
   * @see org.sapia.ubik.net.Connection#send(java.lang.Object)
   */
  public void send(Object o) throws IOException, RemoteException {
    send(o, null, null);
  }
}
