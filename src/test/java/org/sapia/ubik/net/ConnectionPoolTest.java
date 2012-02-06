package org.sapia.ubik.net;

import static junit.framework.Assert.*;

import java.io.IOException;
import java.net.Socket;
import java.rmi.RemoteException;

import org.junit.Test;


public class ConnectionPoolTest {
  
  @Test
  public void testAcquireNoMaxSize() throws Exception {
    ConnectionPool pool = new ConnectionPool("test", 9999, new TestConnectionFactory(), -1);

    for (int i = 0; i < 100; i++) {
      pool.acquire();
    }
    
    assertEquals("100 connections should have been created", 100, pool.getCreatedCount());
  }

  @Test
  public void testAcquireWithMaxSize() throws Exception {
    ConnectionPool pool = new ConnectionPool("test", 9999, new TestConnectionFactory(), 3);

    pool.acquire(100);
    pool.acquire(100);
    pool.acquire(100);

    try {
      pool.acquire(100);
      throw new Exception("New connection should not have been created");
    } catch (Exception e) {
      // ok;
    }

    assertEquals(3, pool.getCreatedCount());
  }

  @Test
  public void testRelease() throws Exception {
    ConnectionPool pool = new ConnectionPool("test", 9999, new TestConnectionFactory(), 3);

    Connection     conn;
    pool.acquire(100);
    pool.acquire(100);
    conn = pool.acquire(100);
    assertEquals("Borrowed count should be 3", 3, pool.getBorrowedCount());

    pool.release(conn);
    assertEquals("Borrowed count should be 2", 2, pool.getBorrowedCount());

    pool.acquire(100);    
    assertEquals("Created count should be 3", 3, pool.getCreatedCount());
  }
  
  @Test
  public void testInvalidate() throws Exception {
    ConnectionPool pool = new ConnectionPool("test", 9999, new TestConnectionFactory(), 3);
    Connection     conn = pool.acquire();
    assertEquals("Created count should be 1", 1, pool.getCreatedCount());
    pool.invalidate(conn);
    assertEquals("Created count should be 0; should have been decremented", 0, pool.getCreatedCount());
  }
  
  static class TestConnection implements Connection {
    TCPAddress address = new TCPAddress("test", 8888);

    public void close() {
    }

    public ServerAddress getServerAddress() {
      return address;
    }

    public Object receive()
      throws IOException, ClassNotFoundException, RemoteException {
      return "ACK";
    }

    public void send(Object o) throws IOException, RemoteException {
    }
  }

  static class TestConnectionFactory implements ConnectionFactory {
    public Connection newConnection(Socket sock)
      throws IOException, UnsupportedOperationException {
      return new TestConnection();
    }

    public Connection newConnection(String host, int port)
      throws IOException {
      return new TestConnection();
    }
  }
}
