package org.sapia.ubik.rmi.server.transport.http;

import java.rmi.RemoteException;
import java.util.Properties;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.util.Strings;

/**
 * A convenience class that can be used to export remote objects through the {@link NettyTransportProvider}.
 * 
 * (see {@link NettyTransportProvider}).
 * 
 * @author yduchesne
 *
 */
public class HttpServerExporter {
  
  private int        port;
  private String     bindAddress;
  private Properties props = new Properties();
  
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
  public HttpServerExporter port(int port) {
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
  public HttpServerExporter bindAddress(String bindAddress) {
    this.bindAddress = bindAddress;
    return this;
  }
  
  /**
   * Internally adds a property to this instance's encapsulated {@link Properties}.
   * 
   * @param name the name of the property to add.
   * @param value the property value.
   * @return this instance.
   */
  public HttpServerExporter property(String name, String value) {
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
    props.setProperty(Consts.TRANSPORT_TYPE, HttpConsts.HTTP_TRANSPORT_TYPE);
    if(port > 0) {
      props.setProperty(HttpTransportProvider.HTTP_PORT_KEY, Integer.toString(port));
    }
    if(bindAddress != null) {
      props.setProperty(HttpTransportProvider.HTTP_BIND_ADDRESS_KEY, bindAddress);
    }
    return Hub.exportObject(toExport, props);
  }

  public String toString() {
    return Strings.toString(
        "port", port, 
        "bindAddress", bindAddress
    );
  }
}
