package org.sapia.ubik.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.sapia.ubik.concurrent.Spawn;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.util.IoUtils;

/**
 * Internally wraps a {@link Socket} instance, which is created created asynchronously.
 * 
 * @author yduchesne
 *
 */
public class ClientSocketConnector {
  
  /**
   * Abstracts asynchronous behavior - introduced for unit testing.
   */
  public interface Submitter {
    public Future<Void> submit(Callable<Void> task);
  }
  
  private static Category log = Log.createCategory(ClientSocketConnector.class);
  
  private InetSocketAddress         address;
  private RMIClientSocketFactoryExt socketFactory;
  private Submitter                 submitter;
  
  public ClientSocketConnector(InetSocketAddress address, RMIClientSocketFactoryExt socketFactory) {
    this(address, socketFactory, new Submitter() {
      @Override
      public Future<Void> submit(Callable<Void> task) {
        return Spawn.submit(task);
      }
    });
  }
  
  public ClientSocketConnector(InetSocketAddress address, RMIClientSocketFactoryExt socketFactory, Submitter submitter) {
    this.address       = address;
    this.socketFactory = socketFactory;
    this.submitter     = submitter;
  }
  
  public Socket connect(long timeout, TimeUnit unit) throws IOException {  
    final AtomicReference<Socket> socketRef = new AtomicReference<Socket>(socketFactory.createSocket());
    long start = System.currentTimeMillis();
    Future<?> result = submitter.submit(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        Socket socket = socketRef.get();
        socket.connect(address, (int) unit.toMillis(timeout));
        log.debug("Connection established to %s", address);
        return null;
      }
    });
    
    try {
      result.get(timeout, unit);
    } catch (ExecutionException e) {
      if (e.getCause() instanceof IOException) {
        log.error("System error creating socket connection to address " + address);
        throw (IOException) e.getCause();
      } else {
        throw new IllegalStateException("Unexpected error occurred", e);
      }
    } catch (TimeoutException e) {
      long duration = System.currentTimeMillis() - start;
      socketRef.get().close();
      result.cancel(true);
      throw new IOException(String.format("Could not connect within specified timeout. Took %s millis", duration));
    } catch (InterruptedException e) {
      throw new ThreadInterruptedException();
    }
    
    // safety check
    if (!socketRef.get().isConnected() || !socketRef.get().isBound()) {
      IoUtils.closeSilently(socketRef.get());
      throw new IOException("Socket not bound/connected: closing");
    }

    return socketRef.get();
  }
  
}
