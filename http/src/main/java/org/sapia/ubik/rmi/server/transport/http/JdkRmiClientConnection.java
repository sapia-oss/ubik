package org.sapia.ubik.rmi.server.transport.http;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.rmi.RemoteException;

import org.javasimon.Split;
import org.javasimon.Stopwatch;
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
import org.sapia.ubik.util.IoUtils;
import org.sapia.ubik.util.SysClock;
import org.sapia.ubik.util.SysClock.RealtimeClock;
import org.sapia.ubik.util.TimeValue;

/**
 * Implements the {@link RmiConnection} over the JDK's {@link URL} class.
 * 
 * @author yduchesne
 */
public class JdkRmiClientConnection implements RmiConnection {
  
  /**
   * Abstracts what type of {@link JdkRmiClientConnection} is returned.
   * 
   */
  public interface JdkRmiClientConnectionFactory {
    
    /**
     * @return a new {@link JdkRmiClientConnection}.
     */
    JdkRmiClientConnection newConnection();
  }
    
  enum State {
      READ,
      WRITE,
      IDLE;
  }  

  private static Stopwatch serializationTime = Stats.createStopwatch(JdkRmiClientConnection.class, "SerializationDuration",
      "Time required to serialize an object");

  private static Stopwatch sendTime = Stats.createStopwatch(JdkRmiClientConnection.class, "SendDuration",
      "Time required to send an object over the network");

  private static final String POST_METHOD = "POST";
  private static final String CONTENT_LENGTH_HEADER = "Content-Length";

  private SysClock          clock = RealtimeClock.getInstance();
  private HttpAddress       address;
  private URL               url;
  private volatile boolean  closed;
  private HttpURLConnection conn;
  private int               bufsz          = Conf.getSystemProperties().getIntProperty(
                                                 Consts.MARSHALLING_BUFSIZE, 
                                                 Defaults.DEFAULT_MARSHALLING_BUFSIZE
                                             );
  private int               connectTimeOut = Conf.getSystemProperties().getIntProperty(
                                                 Consts.HTTP_CLIENT_CONNECT_TIMEOUT, 
                                                 Defaults.DEFAULT_HTTP_CLIENT_CONNECTION_TIMEOUT
                                             );
  private int               readTimeout    = Conf.getSystemProperties().getIntProperty(
                                                 Consts.HTTP_CLIENT_READ_TIMEOUT, 
                                                 Defaults.DEFAULT_HTTP_CLIENT_READ_TIMEOUT
                                             );
  
  
  private State             state         = State.IDLE;
  private long              lastReadStart;

  public JdkRmiClientConnection() {
  }

  // --------------------------------------------------------------------------
  // Connection interface
  
  @Override
  public void send(Object o, VmId associated, String transportType) throws IOException, RemoteException {
      try {
          state = State.WRITE;
          doSend(o, associated, transportType);
      } finally {
          state = State.IDLE;
      }
  }

  @Override
  public void send(Object o) throws IOException, RemoteException {
    send(o, null, null);
  }

  @Override
  public void close() {
    if (!closed) {
      doClose();
      closed = true;
    }
  }

  @Override
  public ServerAddress getServerAddress() {
    return address;
  }

  @Override
  public Object receive() throws IOException, ClassNotFoundException, RemoteException {
      try {
          lastReadStart = clock.currentTimeMillis();
          state = State.READ;
          return doReceive();
      } finally {
          state = State.IDLE;
      }
  }
  
  @Override
  public Object receive(long timeout) throws IOException,
      ClassNotFoundException, RemoteException, SocketTimeoutException {
      lastReadStart = clock.currentTimeMillis();
      state = State.READ;
      try {
          return doReceive(timeout);
      } finally {
          state = State.IDLE;
      }
  }
  
  // --------------------------------------------------------------------------
  // Instance methods
  
  /**
   * @return <code>true</code> if this instance is deemed in a read timeout situation.
   */
  public boolean isInReadTimeout() {
    return state == State.READ && clock.currentTimeMillis() - lastReadStart > readTimeout;
  }
  
  /**
   * @return <code>true</code> if this instance is closed, <code>false</code> otherwise.
   */
  public boolean isClosed() {
    return closed;
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
  
  void setClock(SysClock clock) {
    this.clock = clock;
  }
  
  // --------------------------------------------------------------------------
  // Visible for testing
  
  State getState() {
    return state;
  }
  
  void setReadTimeout(TimeValue readTimeout) {
    this.readTimeout = (int) readTimeout.getValueInMillis();
  }
  
  protected Object doReceive() throws IOException, ClassNotFoundException, RemoteException {
    Assertions.illegalState(conn == null, "Cannot receive; data was not posted");

    ObjectInputStream is = null;
    try {
      is = MarshalStreamFactory.createInputStream(conn.getInputStream());
      return is.readObject();
    } catch (SocketException | SocketTimeoutException | EOFException e) {
      throw new RemoteException("Network issue trying to receive response from " + url, e);
    } finally {
      IoUtils.closeSilently(is);
    }
  }
  
  protected Object doReceive(long timeout) throws IOException,
      ClassNotFoundException, RemoteException, SocketTimeoutException {
    Assertions.illegalState(conn == null, "Cannot receive; data was not posted");
    
    conn.setConnectTimeout((int) timeout);
    ObjectInputStream is = null;

    try {
      is = MarshalStreamFactory.createInputStream(conn.getInputStream());
      return is.readObject();
    } catch (SocketException | SocketTimeoutException | EOFException e) {
      throw new RemoteException("Network issue trying to receive response from " + url, e);
    } finally {
      IoUtils.closeSilently(is);
    }
  }
  
  protected void doSend(Object o, VmId associated, String transportType) throws IOException, RemoteException {
    try {
      conn = (HttpURLConnection) url.openConnection();
      conn.setDoInput(true);
      conn.setDoOutput(true);
      conn.setUseCaches(false);
      conn.setConnectTimeout(connectTimeOut);
      conn.setReadTimeout(readTimeout);
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
    } catch (SocketException | SocketTimeoutException | EOFException e) {
      throw new RemoteException("Network issue trying to send request to " + url, e);
    }
  }
  
  protected void doClose() {
    if (conn != null) {
      conn.disconnect();
    }
  }
}
