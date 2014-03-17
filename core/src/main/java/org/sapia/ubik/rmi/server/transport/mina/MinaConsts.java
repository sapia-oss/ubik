package org.sapia.ubik.rmi.server.transport.mina;

/**
 * Holds Mina-specific configuration.
 * 
 * @author yduchesne
 *
 */
public interface MinaConsts {

  /**
   * Corresponds to the
   * <code>ubik.rmi.transport.nio.mina.server.io.threads</code> property,
   * used to specify the number of I/O selector threads (defaults to <code>Runtime.getRuntime().availableProcessors() + 1</code> ).
   */
  public static final String SERVER_IO_CORE_THREADS_KEY = "ubik.rmi.transport.nio.netty.server.io.core-threads";
}
