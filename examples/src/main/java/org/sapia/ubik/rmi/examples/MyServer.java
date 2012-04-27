package org.sapia.ubik.rmi.examples;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.DefaultUbikServerSocketFactory;
import org.sapia.ubik.net.Worker;
import org.sapia.ubik.net.Request;
import org.sapia.ubik.net.SocketServer;
import org.sapia.ubik.net.WorkerPool;


/**
 * @author Yanick Duchesne
 */
public class MyServer extends SocketServer {

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

  static class MyThreadPool extends WorkerPool<Request> {
    MyThreadPool() {
      super("MyThreadPool", true, 10);
    }

    @Override
    protected Worker<Request> newWorker() {
      return new MyWorker();
    }
  }

  static class MyWorker implements Worker<Request> {
    
    @Override
    public void execute(Request request) {
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
    
  }
}
