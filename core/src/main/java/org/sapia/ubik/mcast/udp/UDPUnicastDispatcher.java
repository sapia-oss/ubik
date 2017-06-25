package org.sapia.ubik.mcast.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.sapia.ubik.concurrent.BlockingCompletionQueue;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.DispatcherContext;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.McastUtil;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.mcast.RespList;
import org.sapia.ubik.mcast.Response;
import org.sapia.ubik.mcast.TimeoutException;
import org.sapia.ubik.mcast.UnicastDispatcher;
import org.sapia.ubik.mcast.server.UDPServer;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TcpPortSelector;
import org.sapia.ubik.rmi.Defaults;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Localhost;
import org.sapia.ubik.util.TimeValue;

/**
 * Implements the {@link UnicastDispatcher} interface over UDP.
 *
 * @author yduchesne
 */
public class UDPUnicastDispatcher implements UnicastDispatcher {

  private static final long STARTUP_TIMEOUT = 5000;

  private Category log = Log.createCategory(getClass());
  private EventConsumer     consumer;
  private TimeValue         asyncAckTimeout = Defaults.DEFAULT_ASYNC_ACK_TIMEOUT;
  private ExecutorService   senders;
  private ExecutorService   workers;
  private UDPUnicastAddress addr;
  private UDPServer         server;

  public UDPUnicastDispatcher() {
  }

  @Override
  public void initialize(DispatcherContext context) {
    this.consumer   = context.getConsumer();
    this.senders    = context.getIoOutboundThreads();
    this.workers    = context.getWorkerThreads();
  }
 
  @Override
  public void start()  {
    Assertions.illegalState(consumer == null, "EventConsumer not set");
  
    try {
      server = new UDPServer(consumer.getNode(), new TcpPortSelector().select()) {
        
        @Override
        protected void handle(final DatagramPacket pack, final DatagramSocket sock) {
          workers.execute(new Runnable() {
            @Override
            public void run() {
              doHandle(pack, sock);
            }
          });
        }
        
        @Override
        protected void handlePacketSizeToShort(DatagramPacket pack) {
          log.error("Buffer size to short; set to: %s. This size is not enough to receive some incoming packets", server.getBufSize());
        }
        
      };
      server.start();
    } catch (IOException e) {
      throw new IllegalStateException("Could not start UDP server", e);
    }
   
    try {
      server.getStartupBarrier().await(STARTUP_TIMEOUT);
    } catch (InterruptedException e) {
      throw new IllegalStateException("Thread interrupted while waiting for startup", e);
    } catch (Exception e) {
      throw new IllegalStateException("Problem while starting up", e);
    }

    InetAddress inetAddr = server.getLocalAddress();
    if (inetAddr == null) {
      try {
        inetAddr = Localhost.getPreferredLocalAddress();
      } catch (UnknownHostException e) {
        throw new IllegalStateException(e);
      }
    }
    log.debug("Local address: %s", inetAddr.getHostAddress());
    addr = new UDPUnicastAddress(inetAddr, server.getPort());
  }

  @Override
  public void close() {
    if (server != null) {
      server.close();
    }
  }

  @Override
  public boolean dispatch(ServerAddress addr, String type, Object data) throws IOException {

    DatagramSocket sock = new DatagramSocket();

    sock.setSoTimeout((int) asyncAckTimeout.getValueInMillis());
    RemoteEvent evt = new RemoteEvent(consumer.getDomainName().toString(), type, data).setNode(consumer.getNode());
    evt.setUnicastAddress(addr);

    try {
      log.debug("dispatch() : %s, type: %s, data: %s", addr, type, data);
      UDPUnicastAddress inet = (UDPUnicastAddress) addr;
      doSend(inet.getInetAddress(), inet.getPort(), sock, McastUtil.toBytes(evt, server.getBufSize()), false, type);
      return true;
    } catch (TimeoutException e) {
      return false;
      // will not occur - see doSend();
    } finally {
      try {
        sock.close();
      } catch (RuntimeException e) {
      }
    }
  }

  @Override
  public Response send(ServerAddress addr, String type, Object data, TimeValue timeout) throws IOException {

    DatagramSocket sock = new DatagramSocket();
    sock.setSoTimeout((int) timeout.getValueInMillis());
    RemoteEvent evt = new RemoteEvent(consumer.getDomainName().toString(), type, data).setNode(consumer.getNode()).setSync();
    evt.setUnicastAddress(addr);
    UDPUnicastAddress inet = (UDPUnicastAddress) addr;

    try {
      return (Response) doSend(inet.getInetAddress(), inet.getPort(), sock, McastUtil.toBytes(evt, server.getBufSize()), true, type);
    } catch (TimeoutException e) {
      return new Response(addr, evt.getId(), e).setStatusSuspect();
    } finally {
      try {
        sock.close();
      } catch (RuntimeException e) {
      }
    }
  }

  @Override
  public RespList send(List<ServerAddress> addresses, final String type, Object data, final TimeValue timeout) throws IOException, InterruptedException {

    final BlockingCompletionQueue<Response> queue = new BlockingCompletionQueue<Response>(addresses.size());

    final RemoteEvent evt = new RemoteEvent(consumer.getDomainName().toString(), type, data).setNode(consumer.getNode()).setSync();
    evt.setUnicastAddress(addr);
    final byte[] bytes = McastUtil.toBytes(evt, server.getBufSize());

    for (int i = 0; i < addresses.size(); i++) {

      final UDPUnicastAddress addr = (UDPUnicastAddress) addresses.get(i);

      senders.execute(new Runnable() {

        @Override
        public void run() {
          DatagramSocket sock = null;
          try {
            sock = new DatagramSocket();
            sock.setSoTimeout((int) timeout.getValueInMillis());
            Response resp = (Response) doSend(addr.getInetAddress(), addr.getPort(), sock, bytes, true, type);
            queue.add(resp);
          } catch (TimeoutException e) {
            log.warning("Response from %s not received in timely manner", addr);

            try {
              queue.add(new Response(addr, evt.getId(), e).setStatusSuspect());
            } catch (IllegalStateException ise) {
              log.info("Could not add response to queue", ise, new Object[] {});
            }
          } catch (IOException e) {
            log.error("IO problem sending remote event to " + addr, e);
          } finally {
            if (sock != null)
              sock.close();
          }
        }
      });
    }

    return new RespList(queue.await(timeout.getValueInMillis()));
  }
  
  @Override
  public RespList send(ServerAddress[] addresses, final String type, Object[] data, final TimeValue timeout)
      throws IOException, InterruptedException {
    final BlockingCompletionQueue<Response> queue = new BlockingCompletionQueue<Response>(addresses.length);

    for (int i = 0; i < addresses.length; i++) {

      final UDPUnicastAddress addr = (UDPUnicastAddress) addresses[i];
      final RemoteEvent evt = new RemoteEvent(null, type, data[i]).setNode(consumer.getNode()).setSync();
      evt.setUnicastAddress(addr);
      final byte[] bytes = McastUtil.toBytes(evt, server.getBufSize());

      senders.execute(new Runnable() {

        @Override
        public void run() {
          DatagramSocket sock = null;
          try {
            sock = new DatagramSocket();
            sock.setSoTimeout((int) timeout.getValueInMillis());
            Response resp = (Response) doSend(addr.getInetAddress(), addr.getPort(), sock, bytes, true, type);
            queue.add(resp);
          } catch (TimeoutException e) {
            log.warning("Response from %s not received in timely manner", addr);

            try {
              queue.add(new Response(addr, evt.getId(), e).setStatusSuspect());
            } catch (IllegalStateException ise) {
              log.info("Could not add response to queue", ise, new Object[] {});
            }
          } catch (IOException e) {
            log.error("IO problem sending remote event to " + addr, e);
          } finally {
            if (sock != null)
              sock.close();
          }
        }
      });
    }

    return new RespList(queue.await(timeout.getValueInMillis()));  
  }
  

  @Override
  public ServerAddress getAddress() throws IllegalStateException {
    Assertions.illegalState(addr == null, "The address of this instance is not yet available");
    return addr;
  }

  private void doHandle(DatagramPacket pack, DatagramSocket sock) {
    try {
      Object o = McastUtil.fromDatagram(pack);

      if (o instanceof RemoteEvent) {
        RemoteEvent evt = (RemoteEvent) o;

        if (evt.isSync()) {
          InetAddress addr = pack.getAddress();
          int port = pack.getPort();

          // ------------------------------------------------------------------

          if (consumer.hasSyncListener(evt.getType())) {
            Object response = consumer.onSyncEvent(evt);

            try {
              doSend(addr, port, sock, McastUtil.toBytes(new Response(UDPUnicastDispatcher.this.getAddress(), evt.getId(), response), server.getBufSize()), false, evt.getType());
            } catch (TimeoutException e) {
              // will not occur - see doSend()
            }

            // ------------------------------------------------------------------

          } else {
            try {
              doSend(addr, port, sock, McastUtil.toBytes(new Response(UDPUnicastDispatcher.this.getAddress(), evt.getId(), null).setNone(), server.getBufSize()), false, evt.getType());
            } catch (TimeoutException e) {
              // will not occur - see doSend()
            }
          }
        } else {
          consumer.onAsyncEvent(evt);
        }
      } else {
        log.error("Object not a remote event: %s", o);
      }
    } catch (IOException e) {
      log.error("IO Problem handling remote event", e);
    } catch (ClassNotFoundException e) {
      log.error(e);
    }
  }

  private Object doSend(InetAddress addr, int port, DatagramSocket sock, byte[] bytes, boolean synchro, String type) throws IOException,
      TimeoutException {
    if (bytes.length > server.getBufSize()) {
      throw new IOException("Size of data larger than buffer size; increase this instance's buffer size through the setBufsize() method");
    }

    log.debug("doSend() : %s, event type: %s", addr, type);
    DatagramPacket pack = new DatagramPacket(bytes, 0, bytes.length, addr, port);

    sock.send(pack);

    if (synchro) {
      bytes = new byte[server.getBufSize()];
      pack = new DatagramPacket(bytes, bytes.length);

      try {
        sock.receive(pack);
      } catch (SocketTimeoutException e) {
        throw new TimeoutException();
      }

      try {
        return McastUtil.fromDatagram(pack);
      } catch (ClassNotFoundException e) {
        throw new IOException("Could not deserialize object", e);
      }
    } else {
      return null;
    }
  }
}
