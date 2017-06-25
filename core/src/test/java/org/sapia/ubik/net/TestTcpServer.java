package org.sapia.ubik.net;

import org.sapia.ubik.concurrent.ConfigurableExecutor.ThreadingConfiguration;
import org.sapia.ubik.rmi.threads.Threads;


public class TestTcpServer extends SocketServer {

  private Thread serverThread;
  
  public TestTcpServer(int port) throws java.io.IOException {
    super("test", port, new TestThreadPool(), new DefaultUbikServerSocketFactory());
  }

  public void start() throws Exception {
      serverThread = new Thread(this);
      serverThread.start();
      waitStarted();
  }

  public void stop() {
    super.close();
    serverThread.interrupt();
    try {
      serverThread.join();
    } catch (InterruptedException e) {
      // noop
    }
  }
 

  static class TestThreadPool extends WorkerPool<Request> {
    TestThreadPool() {
      super(Threads.createWorkerPool());
    }

    @Override
    protected Worker<Request> newWorker() {
      return new TestWorker();
    }
  }

  static class TestWorker implements Worker<Request> {
    
    @Override
    public void execute(Request request) {
      Connection conn = request.getConnection();
      try {
        while (true) {
          Object payload = conn.receive();
          conn.send("echo: " + payload);
        }
      } catch (Throwable t) {
        conn.close();
        t.printStackTrace();
      }
    }
    
  }
}
