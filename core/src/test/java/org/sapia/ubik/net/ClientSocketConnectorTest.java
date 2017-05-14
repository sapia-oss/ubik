package org.sapia.ubik.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.net.ClientSocketConnector.Submitter;

public class ClientSocketConnectorTest {

  private ClientSocketConnector connector;
  private ExecutorService executor;
  private Submitter submitter;
  TestTcpServer server;

  @Before
  public void setUp() throws Exception {
    executor = Executors.newCachedThreadPool();
    submitter = new Submitter() {
      @Override
      public Future<Void> submit(Callable<Void> task) {
        return executor.submit(task);
      }
    };
  }

  @After
  public void tearDown() throws Exception {
    executor.shutdown();
    if (server != null) {
      server.stop();
    }
  }

  @Test(expected = IOException.class)
  public void testConnect_with_timeout() throws Exception {
    connector = new ClientSocketConnector(new InetSocketAddress("localhost", 1025), new DefaultRMIClientSocketFactory(),
        submitter);
    connector.connect(100, TimeUnit.MILLISECONDS);
  }

  @Test(expected = IllegalStateException.class)
  public void testConnect_with_undefined_error() throws Exception {
    connector = new ClientSocketConnector(new InetSocketAddress("localhost", 1025), new DefaultRMIClientSocketFactory(),
        new Submitter() {
          @Override
          public Future<Void> submit(Callable<Void> task) {
            throw new IllegalStateException("ERROR!");
          }
        });
    connector.connect(100, TimeUnit.MILLISECONDS);
  }

  @Test
  public void testConnect_success() throws Exception {
    TestTcpServer server = new TestTcpServer(1025);
    server.start();
    connector = new ClientSocketConnector(new InetSocketAddress("localhost", 1025), new DefaultRMIClientSocketFactory(),
        submitter);
    try {
      Socket s = connector.connect(200, TimeUnit.MILLISECONDS);
      s.close();
    } finally {
      server.stop();
    }
  }
}
