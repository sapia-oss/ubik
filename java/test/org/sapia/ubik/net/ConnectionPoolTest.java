package org.sapia.ubik.net;

import junit.framework.TestCase;

import java.io.IOException;

import java.net.Socket;

import java.rmi.RemoteException;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ConnectionPoolTest extends TestCase {
  /**
   * Constructor for ConnectionPoolTest.
   * @param arg0
   */
  public ConnectionPoolTest(String arg0) {
    super(arg0);
  }

  public void testAcquireNoMaxSize() throws Exception {
    ConnectionPool pool = new ConnectionPool("test", 9999,
        new TestConnectionFactory(), new DefaultClientSocketFactory(), -1);

    for (int i = 0; i < 100; i++) {
      pool.acquire();
    }
  }

  public void testAcquireWithMaxSize() throws Exception {
    ConnectionPool pool = new ConnectionPool("test", 9999,
        new TestConnectionFactory(), new DefaultClientSocketFactory(), 3);

    pool.acquire(100);
    pool.acquire(100);
    pool.acquire(100);

    try {
      pool.acquire(100);
      throw new Exception("new connection should not have been created");
    } catch (Exception e) {
      // ok;
    }

    super.assertEquals(3, pool.getCount());
  }

  public void testRelease() throws Exception {
    ConnectionPool pool = new ConnectionPool("test", 9999,
        new TestConnectionFactory(), new DefaultClientSocketFactory(), 3);

    Connection     conn;
    pool.acquire(100);
    pool.acquire(100);
    conn = pool.acquire(100);
    pool.release(conn);
    pool.acquire(100);
    super.assertEquals(3, pool.getCount());
  }

  /*////////////////////////////////////////////////////////////////////
                              INNER CLASSES
  ////////////////////////////////////////////////////////////////////*/
  static class TestConnection implements Connection {
    TCPAddress _id = new TCPAddress("test", 8888);

    /**
    * @see org.sapia.ubik.net.Connection#close()
    */
    public void close() {
    }

    /**
     * @see org.sapia.ubik.net.Connection#getServerAddress()()
     */
    public ServerAddress getServerAddress() {
      return _id;
    }

    /**
     * @see org.sapia.ubik.net.Connection#receive()
     */
    public Object receive()
      throws IOException, ClassNotFoundException, RemoteException {
      return "ACK";
    }

    /**
     * @see org.sapia.ubik.net.Connection#send(Object)
     */
    public void send(Object o) throws IOException, RemoteException {
    }
  }

  static class TestConnectionFactory implements ConnectionFactory {
    /**
     * @see org.sapia.ubik.net.ConnectionFactory#newConnection(Socket)
     */
    public Connection newConnection(Socket sock)
      throws IOException, UnsupportedOperationException {
      return new TestConnection();
    }

    /**
     * @see org.sapia.ubik.net.ConnectionFactory#newConnection(String, int)
     */
    public Connection newConnection(String host, int port)
      throws IOException {
      return new TestConnection();
    }
  }
}
