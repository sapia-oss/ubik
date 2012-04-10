package org.sapia.ubik.rmi.examples;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.DefaultUbikServerSocketFactory;
import org.sapia.ubik.net.PooledThread;
import org.sapia.ubik.net.Request;
import org.sapia.ubik.net.SocketServer;
import org.sapia.ubik.net.ThreadPool;


/**
 * @author Yanick Duchesne
 */
public class MyServer extends SocketServer {
  /**
   * Constructor for MyServer.
   */
  public MyServer() throws java.io.IOException {
    super("test", 6666, new MyThreadPool(), new DefaultUbikServerSocketFactory());
  }

  public static void main(String[] args) {
    try {
      MyServer svr = new MyServer();
      Thread   t = new Thread(svr);
      t.start();
      svr.waitStarted();
      System.out.println("Server started");

      while (true) {
        Thread.sleep(100000);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static class MyThreadPool extends ThreadPool<Request> {
    MyThreadPool() {
      super("MyThreadPool", true, 10);
    }

    /**
     * @see org.sapia.ubik.util.pool.Pool#doNewObject()
     */
    protected PooledThread<Request> newThread(String name) throws Exception {
      return new MyPooledThread(name);
    }
  }

  static class MyPooledThread extends PooledThread<Request> {
    
    public MyPooledThread(String name) {
      super(name);
    }
    
    /**
     * @see org.sapia.ubik.net.PooledThread#doExec(Object)
     */
    protected void doExec(Request request) {
      Connection conn = request.getConnection();

      try {
        while (true) {
          System.out.println(conn.receive());
          conn.send("Hello");
        }
      } catch (Throwable t) {
        conn.close();
        t.printStackTrace();
      }
    }
    
    @Override
    protected void handleExecutionException(Exception e) {
      e.printStackTrace();
    }
  }
}
