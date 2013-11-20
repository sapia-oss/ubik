package org.sapia.ubik.rmi.server.transport.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.rmi.server.transport.MarshalStreamFactory;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.rmi.server.transport.RmiObjectOutput;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Props;

/**
 * Implements the {@link RmiConnection} over the JDK's {@link URL} class.
 * 
 * @author Yanick Duchesne
 */
public class JdkRmiClientConnection implements RmiConnection {

  private static Stopwatch serializationTime = Stats.createStopwatch(JdkRmiClientConnection.class, "SerializationDuration",
      "Time required to serialize an object");

  private static Stopwatch sendTime = Stats.createStopwatch(JdkRmiClientConnection.class, "SendDuration",
      "Time required to send an object over the network");

  private static final String POST_METHOD = "POST";
  private static final String CONTENT_LENGTH_HEADER = "Content-Length";
  private HttpAddress address;
  private URL url;
  private volatile boolean closed;
  private HttpURLConnection conn;
  private int bufsz = Props.getSystemProperties().getIntProperty(Consts.MARSHALLING_BUFSIZE, Consts.DEFAULT_MARSHALLING_BUFSIZE);

  public JdkRmiClientConnection() {
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.RmiConnection#send(java.lang.Object,
   *      org.sapia.ubik.rmi.server.VmId, java.lang.String)
   */
  public void send(Object o, VmId associated, String transportType) throws IOException, RemoteException {
    conn = (HttpURLConnection) url.openConnection();
    conn.setDoInput(true);
    conn.setDoOutput(true);
    conn.setUseCaches(false);
    conn.setRequestMethod(POST_METHOD);

    ByteArrayOutputStream bos = new ByteArrayOutputStream(bufsz);
    ObjectOutputStream mos = MarshalStreamFactory.createOutputStream(bos);

    if ((associated != null) && (transportType != null)) {
      ((RmiObjectOutput) mos).setUp(associated, transportType);
    }

    Split split = serializationTime.start();
    mos.writeObject(o);
    mos.flush();
    mos.close();
    split.stop();

    split = sendTime.start();
    byte[] data = bos.toByteArray();

    if (data.length > bufsz) {
      bufsz = data.length;
    }

    conn.setRequestProperty(CONTENT_LENGTH_HEADER, "" + data.length);

    OutputStream os = conn.getOutputStream();
    os.write(data);
    os.flush();
    os.close();
    split.stop();
  }

  /**
   * @see org.sapia.ubik.net.Connection#close()
   */
  public void close() {
    if ((conn != null) && !closed) {
      conn.disconnect();
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
  public Object receive() throws IOException, ClassNotFoundException, RemoteException {
    Assertions.illegalState(conn == null, "Cannot receive; data was not posted");

    ObjectInputStream is = MarshalStreamFactory.createInputStream(conn.getInputStream());

    try {
      return is.readObject();
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

  JdkRmiClientConnection setUp(HttpAddress addr) throws RemoteException {
    closed = false;

    if (url == null) {
      try {
        url = new URL(addr.toString());
      } catch (MalformedURLException e) {
        throw new RemoteException(addr.toString(), e);
      }
    }

    address = addr;
    return this;
  }
}
