package org.sapia.ubik.rmi.server.transport.socket;

import java.util.Properties;

import org.sapia.ubik.rmi.Consts;

/**
 * Extends the {@link SocketTransportProvider} class, enabling support for
 * properties that are specific to the {@link MultiplexSocketTransportProvider}.
 * <p>
 * An instance of this class export objects using the
 * {@link MultiplexSocketTransportProvider}.
 * 
 * @author yduchesne
 * 
 */
public class MultiplexServerExporter extends SocketServerExporter {

  private int acceptorThreads;
  private int selectorThreads;

  /**
   * @param acceptorThreads
   *          the number of acceptor threads.
   * @return this instance.
   * @see MultiplexSocketTransportProvider#ACCEPTOR_THREADS
   */
  public MultiplexServerExporter acceptorThreads(int acceptorThreads) {
    this.acceptorThreads = acceptorThreads;
    return this;
  }

  /**
   * @return the number of acceptor threads.
   * @see #acceptorThreads(int)
   */
  public int getAcceptorThreads() {
    return acceptorThreads;
  }

  /**
   * @param selectorThreads
   *          the number of selector threads.
   * @return this instance.
   */
  public MultiplexServerExporter selectorThreads(int selectorThreads) {
    this.selectorThreads = selectorThreads;
    return this;
  }

  /**
   * @return the number of selector threads.
   * @see #selectorThreads(int)
   */
  public int getSelectorThreads() {
    return selectorThreads;
  }

  @Override
  protected void addProperties(Properties props) {
    props.setProperty(Consts.TRANSPORT_TYPE, MultiplexSocketTransportProvider.MPLEX_TRANSPORT_TYPE);
    props.setProperty(MultiplexSocketTransportProvider.ACCEPTOR_THREADS, Integer.toString(acceptorThreads));
    props.setProperty(MultiplexSocketTransportProvider.SELECTOR_THREADS, Integer.toString(MultiplexSocketTransportProvider.DEFAULT_SELECTOR_THREADS));
  }

}
