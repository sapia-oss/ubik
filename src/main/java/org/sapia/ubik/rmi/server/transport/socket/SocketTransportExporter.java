package org.sapia.ubik.rmi.server.transport.socket;

import java.rmi.RemoteException;
import java.util.Properties;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;

/**
 * An instance of this class exports given objects as remote objects using the {@link SocketTransportProvider}. 
 * 
 * @author yduchesne
 *
 */
public class SocketTransportExporter {
	
	private int 	 maxThreads;
	private String bindAddress;
	private int    port;
	
	/**
	 * @param maxThreads the maximum number of worker threads on the server side (note that the {@link SocketTransportProvider} will
	 * consult the {@link Consts#SERVER_MAX_THREADS} property first, and will use this value as a fallback).
	 * @return this instance.
	 */
	public SocketTransportExporter setMaxThreads(int maxThreads) {
	  this.maxThreads = maxThreads;
	  return this;
  }
	
	/**
	 * @return the maximum number of worker threads on the server-side.
	 * @see #setMaxThreads(int) 
	 */
	public int getMaxThreads() {
	  return maxThreads;
  }
	
	/**
	 * @param bindAddress the address corresponding to the network interface to which the server should bind - defaults to all
	 * network interfaces if not set.
	 * @return this instance.
	 */
	public SocketTransportExporter setBindAddress(String bindAddress) {
	  this.bindAddress = bindAddress;
	  return this;
  }
	
	/**
	 * @return the bind addresss (see {@link #setBindAddress(String)})
	 */
	public String getBindAddress() {
	  return bindAddress;
  }
	
	/**
	 * @param port the port on which the server should listen (defaults to a random port if not set).
	 * @return this instance.
	 */
	public SocketTransportExporter setPort(int port) {
	  this.port = port;
	  return this;
  }
	
	/**
	 * @return the port on which the server should listen.
	 * @see #setPort(int)
	 */
	public int getPort() {
	  return port;
  }
	
	/**
	 * @param toExport the {@link Object} to export. 
	 * @return the remote object corresponding to the stub resulting form the export.
	 * @throws RemoteException if a problem occurred trying to export the given object.
	 */
	public Object export(Object toExport) throws RemoteException {
		Properties props = new Properties();
		if (maxThreads > 0) {
			props.setProperty(SocketTransportProvider.MAX_THREADS, Integer.toString(maxThreads));
		}
		
		if (bindAddress != null) {
			props.setProperty(SocketTransportProvider.BIND_ADDRESS, bindAddress);
		}
		
		if(port > 0) {
			props.setProperty(SocketTransportProvider.PORT, Integer.toString(port));
		}
		props.setProperty(Consts.TRANSPORT_TYPE, SocketTransportProvider.SOCKET_TRANSPORT_TYPE);
		
		return Hub.exportObject(toExport);
	}
}
