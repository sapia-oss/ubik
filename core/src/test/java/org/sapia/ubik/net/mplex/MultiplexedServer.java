package org.sapia.ubik.net.mplex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


/**
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
 */
public class MultiplexedServer {
  public static long START_TIME = System.currentTimeMillis();
  private MultiplexServerSocket _server;

  /**
   * Creates a new MultiplexedServer instance.
   */
  public MultiplexedServer() throws IOException {
    int backlog = 100;
//    _server = new MultiplexServerSocket(7777, backlog, Localhost.getLocalAddressForConfig());
    _server = new MultiplexServerSocket(7777, backlog);
    log("Started multiplex server on port 7777");
    log("Setting backlog queue to " + backlog);
    log("Setting " + _server.getAcceptorDaemonThread() +
      " acceptor daemon threads");
    log("Setting " + _server.getSelectorDaemonThread() +
      " selector daemon threads");

    MultiplexSocketConnector httpSocket = _server.createSocketConnector(new HttpStreamSelector(
          null, null));
    HttpHandler              httpHandler = new HttpHandler(httpSocket);
    Thread                   httpThread  = new Thread(httpHandler,
        "HTTP-Processor");
    httpThread.start();

    MultiplexSocketConnector objectSocket = _server.createSocketConnector(new ObjectStreamSelector());
    ObjectHandler            objectHandler = new ObjectHandler(objectSocket);
    Thread                   objectThread  = new Thread(objectHandler,
        "Object-Processor");
    objectThread.start();
  }

  public static void main(String[] args) {
    try {
      new MultiplexedServer().run();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public static synchronized void log(String log) {
    StringBuffer aBuffer = new StringBuffer();
    aBuffer.append(System.currentTimeMillis() - START_TIME).append(" [")
           .append(Thread.currentThread().getName()).append("] ").append(log);
    System.out.println(aBuffer.toString());
  }

  public void run() {
    log("Started default server socket handler...");

    int counter = 0;

    try {
      while (true) {
        // Wait for a connection
        Socket client = _server.accept();
        new Thread(new ObjectServer(client), "ObjectServer-" + (++counter)).start();
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        _server.close();
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }

  /**
   *
   * @
   */
  public static class HttpHandler implements Runnable {
    private MultiplexSocketConnector _socket;

    public HttpHandler(MultiplexSocketConnector socket) {
      _socket = socket;
    }

    public void run() {
      log("Started HTTP server socket handler...");

      int counter = 0;

      try {
        while (true) {
          // Wait for a connection
          Socket client = _socket.accept();
          new Thread(new HttpServer(client), "HttpServer-" + (++counter)).start();
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        try {
          _socket.close();
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
      }
    }
  }

  /**
   *
   * @
   */
  public static class HttpServer implements Runnable {
    private Socket _client;

    public HttpServer(Socket client) {
      _client = client;
    }

    public void run() {
      log("Starting HTTP server...");

      try {
        // Get the request
        InputStream           is      = _client.getInputStream();
        ByteArrayOutputStream request = new ByteArrayOutputStream();
        boolean               isDone  = false;
        byte[]                data    = new byte[1024];

        while (!isDone) {
          int length = is.read(data);

          if (length >= 0) {
            request.write(data, 0, length);
          }

          isDone = is.available() == 0;
        }

        // Printing out the request
        StringBuffer aBuffer = new StringBuffer("===> Got an HTTP request\n");
        String       aPost = request.toString("UTF-8");
        aBuffer.append(aPost.substring(aPost.lastIndexOf("\r\n\r\n") + 4));
        log(aBuffer.toString());

        // Generating the response
        _client.getOutputStream().write("HTTP ACK".getBytes("UTF-8"));
        _client.getOutputStream().flush();
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        try {
          _client.close();
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
      }
    }
  }

  /**
   *
   * @
   */
  public static class ObjectHandler implements Runnable {
    private MultiplexSocketConnector _socket;

    public ObjectHandler(MultiplexSocketConnector socket) {
      _socket = socket;
    }

    public void run() {
      log("Started Object server socket handler...");

      int counter = 0;

      try {
        while (true) {
          // Wait for a connection
          Socket client = _socket.accept();
          new Thread(new ObjectServer(client), "ObjectServer-" + (++counter)).start();
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        try {
          _socket.close();
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
      }
    }
  }

  /**
   *
   * @
   */
  public static class ObjectServer implements Runnable {
    private Socket _client;

    public ObjectServer(Socket client) {
      _client = client;
    }

    public void run() {
      log("Started Object server...");

      int count = 0;

      try {
        ObjectInputStream  request  = null;
        ObjectOutputStream response = null;

        while (true) {
          // Get the request
          if (request == null) {
            request = new ObjectInputStream(_client.getInputStream());
          }

          StringBuffer aBuffer = new StringBuffer(
              "===> Got an Java Object request [" + (++count) + "]\n");
          aBuffer.append(request.readObject());
          log(aBuffer.toString());

          // Generating the response
          if (response == null) {
            response = new ObjectOutputStream(_client.getOutputStream());
          }

          response.writeObject("Java Object Ack");
          response.flush();
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        try {
          _client.close();
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
      }
    }
  }

  /**
   *
   * @
   */
  public static class DefaultServer implements Runnable {
    private Socket _client;

    public DefaultServer(Socket client) {
      _client = client;
    }

    public void run() {
      log("Started Default server...");

      int count = 0;

      try {
        while (true) {
          // Get the request
          InputStream           is      = _client.getInputStream();
          ByteArrayOutputStream request = new ByteArrayOutputStream();
          boolean               isDone  = false;
          byte[]                data    = new byte[1024];

          while (!isDone) {
            int length = is.read(data);

            if (length >= 0) {
              request.write(data, 0, length);
            }

            isDone = is.available() == 0;
          }

          StringBuffer aBuffer = new StringBuffer(
              "===> Got a default request [" + (++count) + "]\n");
          aBuffer.append(request);
          log(aBuffer.toString());

          // Generating the response
          _client.getOutputStream().write("DEFAULT ACK".getBytes("UTF-8"));
          _client.getOutputStream().flush();
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        try {
          _client.close();
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
      }
    }
  }
}
