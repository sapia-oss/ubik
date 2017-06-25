package org.sapia.ubik.rmi.server.transport.mina;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioProcessor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TcpPortSelector;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.Server;

/**
 * This class implements the {@link Server} interface on top of a
 * {@link SocketAcceptor}.
 *
 * @author yduchesne
 *
 */
class MinaServer implements Server {

  private Category log = Log.createCategory(getClass());

  private SocketAcceptor    acceptor;
  private InetSocketAddress inetAddr;
  private MinaAddress       addr;
  private MinaServerHandler handler;

  /**
   * This constructor is called by a {@link MinaTransportProvider} instance. The
   * <code>maxThreads</code> argument allows specifying the maximum number of IO
   * processor threads that will be used by this instance.
   * <p>
   * See the <a
   * href="http://mina.apache.org/configuring-thread-model.html">threading
   * model</a> page on Mina's site for more details (the threading model for
   * this class is based on Mina's {@link ExecutorFilter}).
   *
   * @param inetAddr
   *          the {@link InetSocketAddress} on which the server should listen.
   * @param bufsize
   *          the size of buffers created internally to process data.
   * @param numAcceptorThreads
   *          the {@link ExecutorService} providing the NIO selector threads.
   * @param workerThreads
   *          the {@link ExecutorService} providing the worker threads.
   *
   * @throws IOException
   *           if a problem occurs while creating this instance.
   */
  MinaServer(InetSocketAddress inetAddr, int bufsize, ExecutorService acceptorThreads, ExecutorService workerThreads) throws IOException {
    this.acceptor        = new NioSocketAcceptor(new NioProcessor(acceptorThreads));
    acceptor.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new MinaCodecFactory()));
    acceptor.getFilterChain().addLast("threads", new ExecutorFilter(workerThreads));
    acceptor.getSessionConfig().setReadBufferSize(bufsize);
    acceptor.setReuseAddress(true);
    
    if (inetAddr.getPort() != 0) {
      log.info("Using port %s", inetAddr.getPort());
      this.inetAddr = inetAddr;
    } else {
      int randomPort = new TcpPortSelector().select();
      log.info("Using random port %s", randomPort);
      this.inetAddr = new InetSocketAddress(inetAddr.getAddress().getHostAddress(), randomPort);
    }
    log.info("Binding to address: %s", this.inetAddr);
    addr = new MinaAddress(this.inetAddr.getAddress().getHostAddress(), this.inetAddr.getPort());
    handler = new MinaServerHandler(Hub.getModules().getServerRuntime().getDispatcher(), addr);
    
    acceptor.setHandler(handler);
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.nio.tcp.AddressProvider#getUnicastAddress()
   */
  public ServerAddress getAddress() {
    return getServerAddress();
  }

  /**
   * @see org.sapia.ubik.rmi.server.Server#getServerAddress()
   */
  @Override
  public ServerAddress getServerAddress() {
    return addr;
  }

  /**
   * @see org.sapia.ubik.rmi.server.Server#start()
   */
  @Override
  public void start() throws RemoteException {
    try {
      log.info("Starting NIO TCP server on %s", inetAddr);
      acceptor.bind(inetAddr);
    } catch (IOException e) {
      throw new RemoteException("Could not start acceptor", e);
    }
  }

  /**
   * @see org.sapia.ubik.rmi.server.Server#close()
   */
  @Override
  public void close() {
    acceptor.dispose();
  }
  
}
