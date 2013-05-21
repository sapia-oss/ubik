package org.sapia.ubik.rmi.server.transport.mina;

import java.rmi.RemoteException;
import java.util.Properties;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.util.Strings;

/**
 * A convenience class that can be used to export remote objects through the NIO TCP transport
 * (see {@link NioTcpTransportProvider}).
 * 
 * @author yduchesne
 *
 */
public class NioServerExporter {
  
  private int        port;
  private String     bindAddress;
  private int        bufferSize     = Consts.DEFAULT_MARSHALLING_BUFSIZE;
  private int        maxThreads;
  private Properties props          = new Properties();
  
  /**
   * @return the buffer size to use when performing marshalling/unmarshalling. Defaults to {@link Consts#DEFAULT_MARSHALLING_BUFSIZE}.
   */
  public int getBufferSize() {
    return bufferSize;
  }
  
  /** 
   * @param bufferSize a buffer size.
   * @return this instance.
   */
  public NioServerExporter bufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
    return this;
  }
  
  /**
   * @return the port to which remote objects should be bound (a random port will be used if not specified).
   */
  public int getPort() {
    return port;
  }
  
  /**
   * @param port a port to assign to this instance.
   * @return this instance.
   * @see #getPort()
   */
  public NioServerExporter port(int port) {
    this.port = port;
    return this;
  }
  
  /**
   * @return this instance's bind address (remote objects will be exported to all network interfaces if not specified).
   */
  public String getBindAddress() {
    return bindAddress;
  }
  
  /**
   * @param bindAddress the network address to which remote objects should be bound.
   * @return this instance.
   * @see #getBindAddress()
   */
  public NioServerExporter bindAddress(String bindAddress) {
    this.bindAddress = bindAddress;
    return this;
  }
  
  /**
   * @return the maximum number of worker threads that should be used by servers through which the remote 
   * objects this instance creates are exported (no maximum is defined by default).
   */
  public int getMaxThreads() {
    return maxThreads;
  }
  
  /**
   * @param maxThreads a maximum number of worker threads.
   * @return this instance
   * @see #getMaxThreads()
   */
  public NioServerExporter maxThreads(int maxThreads) {
    this.maxThreads = maxThreads;
    return this;
  }
  
  /**
   * Internally adds a property to this instance's encapsulated {@link Properties}.
   * 
   * @param name the name of the property to add.
   * @param value the property value.
   * @return this instance.
   */
  public NioServerExporter property(String name, String value) {
    props.setProperty(name, value);
    return this;
  }
  
  /**
   * This method internally calls {@link Hub#exportObject(Object, Properties)} to export the
   * given object, setting this instance's configuration as properties that are passed to the method.
   * <p>
   * It returns the stub resulting from the export operation.
   * 
   * @param toExport an {@link Object} to export.
   * @return the stub that was created.
   * 
   * @throws RemoteException if a problem occurred trying to export the object.
   */
  public Object export(Object toExport) throws RemoteException {
    props.setProperty(Consts.TRANSPORT_TYPE, NioTcpTransportProvider.TRANSPORT_TYPE);
    if(port > 0) {
      props.setProperty(NioTcpTransportProvider.PORT, Integer.toString(port));
    }
    if(bindAddress != null) {
      props.setProperty(NioTcpTransportProvider.BIND_ADDRESS, bindAddress);
    }
    if(bufferSize > 0) {
      props.setProperty(Consts.MARSHALLING_BUFSIZE, Integer.toString(bufferSize));
    }
    if(maxThreads > 0) {
      props.setProperty(Consts.SERVER_MAX_THREADS, Integer.toString(maxThreads));
    }
    
    return Hub.exportObject(toExport, props);
  }

  public String toString() {
    return Strings.toString(
        "port", port, 
        "bindAddress", bindAddress, 
        "bufferSize", bufferSize, 
        "maxThreads", maxThreads
    );
  }
}
