package org.sapia.ubik.net.mplex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import org.sapia.ubik.net.SocketConnection;

/**
 * @author Yanick Duchesne
 */
public class MultiplexedClient {
  public static final long START_TIME = System.currentTimeMillis();
  public static final int SLEEP_FACTOR = 200;
  private static int _totalThreadsHttp;
  private static long _totalTimeHttp;
  private static long _minTimeHttp;
  private static long _maxTimeHttp;
  private static long _iterationHttp;
  private static int _totalThreadsObject;
  private static long _totalTimeObject;
  private static long _minTimeObject;
  private static long _maxTimeObject;
  private static long _iterationObject;
  private static final byte TYPE_HTTP = 1;
  private static final byte TYPE_OBJECT = 2;
  private static boolean statsLogged = false;

  /**
   * Constructor for MyClient.
   */
  public MultiplexedClient() {
    super();
  }

  public static synchronized void addStat(long delta, byte type) {
    if (type == TYPE_HTTP) {
      _totalTimeHttp += delta;
      _iterationHttp++;
      _minTimeHttp = ((_minTimeHttp == 0) ? delta : Math.min(_minTimeHttp, delta));
      _maxTimeHttp = Math.max(_maxTimeHttp, delta);
    } else if (type == TYPE_OBJECT) {
      _totalTimeObject += delta;
      _iterationObject++;
      _minTimeObject = ((_minTimeObject == 0) ? delta : Math.min(_minTimeObject, delta));
      _maxTimeObject = Math.max(_maxTimeObject, delta);
    }
  }

  public static synchronized void logStat() {
    StringBuffer aBuffer = new StringBuffer();
    aBuffer.append(System.currentTimeMillis() - START_TIME).append("\tHTTP\tthread\t").append(_totalThreadsHttp).append("\tcount\t")
        .append(_iterationHttp).append("\tavg time\t").append(_totalTimeHttp / _iterationHttp).append("\tmin time\t").append(_minTimeHttp)
        .append("\tmax time\t").append(_maxTimeHttp).append("\ttot time\t").append(_totalTimeHttp).append("\n\tOBJECT\tthread\t")
        .append(_totalThreadsObject).append("\tcount\t").append(_iterationObject).append("\tavg time\t").append(_totalTimeObject / _iterationObject)
        .append("\tmin time\t").append(_minTimeObject).append("\tmax time\t").append(_maxTimeObject).append("\ttot time\t").append(_totalTimeObject)
        .append("\n");

    System.out.println(aBuffer.toString());
  }

  public static void log(Object log) {
    log((log == null) ? "null" : log.toString());
  }

  public static synchronized void log(String log) {
    StringBuffer aBuffer = new StringBuffer();
    aBuffer.append(System.currentTimeMillis() - START_TIME).append(" [").append(Thread.currentThread().getName()).append("] ").append(log);

    // System.out.println(aBuffer.toString());
    if ((_iterationHttp > 0) && (_iterationObject > 0) && (((_iterationHttp + _iterationObject) % 100) == 0)) {
      if (!statsLogged) {
        statsLogged = true;
        logStat();
      }
    } else {
      statsLogged = false;
    }
  }

  public static void main(String[] args) {
    for (int i = 1; i <= 25; i++) {
      Thread objectClient = new Thread(new ObjectClient(), "object-client-" + i);
      objectClient.start();
      _totalThreadsObject++;

      Thread httpClient = new Thread(new HttpClient(), "http-client-" + i);
      httpClient.start();
      _totalThreadsHttp++;

      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
      }
    }
  }

  /**
   * 
   * @
   */
  public static class ObjectClient implements Runnable {
    private Random _random = new Random();
    private SocketConnection _conn;

    private SocketConnection getConnection() throws IOException {
      if (_conn == null) {
        log("Getting new connection to port 7777...");
        _conn = new SocketConnection("test", new java.net.Socket("localhost", 7777), 512);
        log("Connected to " + _conn.getServerAddress());
      }

      return _conn;
    }

    public void run() {
      try {
        while (true) {
          try {
            log("Sending...");

            long start = System.currentTimeMillis();
            Object request = "This is foo!!!";
            getConnection().send(request);

            log("Receiving...");

            Object response = getConnection().receive();
            addStat(System.currentTimeMillis() - start, TYPE_OBJECT);
            log(response);

            // Sleep between 0.2 and 10 seconds
            Thread.sleep(_random.nextInt(50) * SLEEP_FACTOR);
          } catch (IOException ioe) {
            ioe.printStackTrace();

            if (_conn == null) {
              break;
            }

            log("Resetting connection...");
            _conn.close();
            _conn = null;
          }
        }
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }

  /**
   * 
   * @
   */
  public static class HttpClient implements Runnable {
    private Random _random = new Random();

    private HttpURLConnection getConnection() throws IOException {
      log("Getting new HTTP connection to port 7777...");

      URL anURL = new URL("http://localhost:7777");
      HttpURLConnection _conn = (HttpURLConnection) anURL.openConnection();
      _conn.setRequestMethod("POST");
      _conn.setDoInput(true);
      _conn.setDoOutput(true);
      _conn.setUseCaches(false);
      log("Connected to localhost:7777");

      return _conn;
    }

    public void run() {
      try {
        while (true) {
          HttpURLConnection conn = null;

          try {
            long start = System.currentTimeMillis();
            conn = getConnection();

            log("Sending HTTP...");

            OutputStream os = conn.getOutputStream();
            os.write("This is HTTP foo!!!".getBytes());
            os.flush();

            log("Receiving HTTP...");

            InputStream is = conn.getInputStream();
            ByteArrayOutputStream response = new ByteArrayOutputStream();

            boolean isDone = false;
            byte[] data = new byte[1024];

            while (!isDone) {
              int length = is.read(data);

              if (length >= 0) {
                response.write(data, 0, length);
              }

              isDone = is.available() == 0;
            }

            addStat(System.currentTimeMillis() - start, TYPE_HTTP);
            log(response.toString("UTF-8"));

            // Sleep between 0.2 and 10 seconds
            Thread.sleep(_random.nextInt(50) * SLEEP_FACTOR);
          } catch (IOException ioe) {
            ioe.printStackTrace();
          } finally {
            if (conn == null) {
              break;
            }

            conn.disconnect();
          }
        }
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }
}
