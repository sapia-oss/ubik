package org.sapia.ubik.net;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class MyServer extends SocketServer {
  /**
   * Constructor for MyServer.
   */
  public MyServer() throws java.io.IOException {
    super(6666, new MyThreadPool(), new DefaultUbikServerSocketFactory());
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

  static class MyThreadPool extends ThreadPool {
    MyThreadPool() {
      super("myThread", true, 10);
    }

    /**
     * @see org.sapia.ubik.net.Pool#doNewObject()
     */
    protected PooledThread newThread() throws Exception {
      return new MyPooledThread();
    }
  }

  static class MyPooledThread extends PooledThread {
    /**
     * @see org.sapia.ubik.net.PooledThread#doExec(Object)
     */
    protected void doExec(Object task) {
      Connection conn = ((Request) task).getConnection();

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
