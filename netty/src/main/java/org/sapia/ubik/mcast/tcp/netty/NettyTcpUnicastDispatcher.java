package org.sapia.ubik.mcast.tcp.netty;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.DispatcherContext;
import org.sapia.ubik.mcast.UnicastDispatcher;
import org.sapia.ubik.mcast.tcp.BaseTcpUnicastDispatcher;
import org.sapia.ubik.net.ConnectionFactory;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TcpPortSelector;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.Defaults;
import org.sapia.ubik.util.Localhost;

/**
 * Netty-based implementation of the {@link UnicastDispatcher} interface.
 *
 * @author yduchesne
 *
 */
public class NettyTcpUnicastDispatcher extends BaseTcpUnicastDispatcher implements NettyMcastConsts {

  private Category log = Log.createCategory(getClass());
  
  private NettyTcpUnicastAddress address;
  private NettyTcpUnicastServer  server;
  private int                    marshallingBufferSize;
  
  @Override
  public void initialize(DispatcherContext context) {
    super.initialize(context);
    marshallingBufferSize = context.getConf().getIntProperty(Consts.MARSHALLING_BUFSIZE, Defaults.DEFAULT_MARSHALLING_BUFSIZE);

    try {
      log.info("Acquiring network address");
      String host = Localhost.getPreferredLocalAddress().getHostAddress();
      this.address = new NettyTcpUnicastAddress(host, new TcpPortSelector().select());
    } catch (Exception e) {
      throw new IllegalStateException("Could not acquire server address", e);
    }

    log.info("Network address acquired");
  }

  @Override
  public ServerAddress getAddress() throws IllegalStateException {
    return address;
  }

  @Override
  protected void doStart() {
    log.info("Starting...");

    server = new NettyTcpUnicastServer(
        context().getConsumer(), 
        address, 
        context().getSelectorThreads().getExecutor("unicast.netty"), 
        context().getWorkerThreads(),
        context().getConf().getIntProperty(Consts.UNICAST_INBOUND_THREADS, Defaults.DEFAULT_UNICAST_INBOUND_THREADS)
    );
    server.start();
    log.info("Started");
  }

  @Override
  protected String doGetTransportType() {
    return NettyTcpUnicastAddress.TRANSPORT_TYPE;
  }

  @Override
  protected ConnectionFactory doGetConnectionFactory() {
    return new NettyTcpUnicastConnectionFactory(marshallingBufferSize);
  }

  @Override
  protected void doClose() {
    log.debug("Proceeding to close");
    try {
      if (server != null) {
        log.debug("Closing server connections");
        server.close();
      }
    } finally {
    }
    log.debug("Closed");
  }

}
