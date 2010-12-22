package org.sapia.ubik.rmi.server.transport.socket;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.rmi.RemoteException;

import javax.management.ObjectName;

import org.sapia.ubik.jmx.MBeanContainer;
import org.sapia.ubik.jmx.MBeanFactory;
import org.sapia.ubik.net.DefaultUbikServerSocketFactory;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.SocketConnectionFactory;
import org.sapia.ubik.net.SocketServer;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.net.ThreadPool;
import org.sapia.ubik.net.UbikServerSocketFactory;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.Log;
import org.sapia.ubik.rmi.server.RMICommand;
import org.sapia.ubik.rmi.server.Server;

/**
 * A standard socket server that listens on a given port for
 * incoming {@link RMICommand} instances.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class SocketRmiServer extends SocketServer implements Server, SocketRmiServerMBean, MBeanFactory {
  private ServerAddress _addr;
  
  public SocketRmiServer(String bindAddr, int port, int maxThreads, long resetInterval)
    throws IOException {
    super(bindAddr, port, new SocketRmiConnectionFactory().setResetInterval(resetInterval),
      new SocketRmiServerThreadPool("ubik.rmi.server.SocketServerThread", true,
        maxThreads), new DefaultUbikServerSocketFactory());
    _addr = new TCPAddress(InetAddress.getByName(bindAddr).getHostAddress(), port);        
  }

  public SocketRmiServer(String bindAddr, int port, int maxThreads, long resetInterval,
    UbikServerSocketFactory factory) throws IOException {
    super(bindAddr, port, new SocketRmiConnectionFactory().setResetInterval(resetInterval),
      new SocketRmiServerThreadPool("ubik.rmi.server.SocketServerThread", true,
        maxThreads), factory);
    _addr = new TCPAddress(InetAddress.getByName(bindAddr).getHostAddress(), port);            
  }

  /**
   * Creates a new SocketRmiServer instance
   *
   * @param tp
   * @param server
   * @throws IOException
   */
  protected SocketRmiServer(ThreadPool tp, ServerSocket server, long resetInterval)
    throws IOException {
    super(new SocketRmiConnectionFactory().setResetInterval(resetInterval), tp, server);
    _addr = new TCPAddress(server.getInetAddress().getHostAddress(), server.getLocalPort());
  }

  /**
   * Creates a new SocketRmiServer instance
   *
   * @param maxThreads the max number of threads in the underlying pool.
   * @param server a {@link ServerSocket}
   * @param resetInterval the interval (in millis) at which the underlying {@link ObjectOutputStream}
   * should be reset.
   * @throws IOException
   */
  protected SocketRmiServer(int maxThreads, ServerSocket server, long resetInterval)
    throws IOException {
    super(new SocketRmiConnectionFactory().setResetInterval(resetInterval),
      new SocketRmiServerThreadPool("ubik.rmi.server.SocketServerThread", true,
        maxThreads), server);
    _addr = new TCPAddress(server.getInetAddress().getHostAddress(), server.getLocalPort());    
  }

  /**
   * Creates a new SocketRmiServer instance.
   *
   * @param fac a {@link SocketConnectionFactory}
   * @param tp a {@link ThreadPool}
   * @param server a {@link SocketServer}
   * @throws IOException
   */
  protected SocketRmiServer(SocketConnectionFactory fac, ThreadPool tp,
    ServerSocket server) throws IOException {
    super(fac, tp, server);
    _addr = new TCPAddress(server.getInetAddress().getHostAddress(), server.getLocalPort());
  }

  /**
   * @see org.sapia.ubik.rmi.server.Server#getServerAddress()()
   */
  public ServerAddress getServerAddress() {
    return _addr;
  }

  /**
   * @see org.sapia.ubik.rmi.server.Server#start()
   */
  public void start() throws RemoteException {
    Hub.statsCollector.addStat(super.getRequestDurationStat());
    Hub.statsCollector.addStat(super.getRequestsPerSecondStat());
    Log.debug(this.getClass(), "starting server");

    Thread t = new Thread(this);
    t.setName("ubik.rmi.server.SocketServer");
    t.setDaemon(true);
    t.start();

    try {
      waitStarted();
    } catch (InterruptedException e) {
      RemoteException re = new RemoteException("Thread interrupted during server startup",
          e);
      throw re;
    } catch (SocketException e) {
      RemoteException re = new RemoteException("Error while starting up", e);
      throw re;
    }
  }
  
  ////// MBean interface
  
  public int getThreadCount() {
    return super.getThreadCount();
  }
  
  public double getRequestDurationSeconds() {
    return super.getRequestDurationStat().getStat()/1000;
  }
  
  public double getRequestsPerSecond() {
    return super.getRequestsPerSecondStat().getStat();
  }
  
  ////// MBeanFactory
  
  public MBeanContainer createMBean() throws Exception{
    ObjectName name = new ObjectName("sapia.ubik.rmi:type=TcpSocketServer");
    return new MBeanContainer(name, this);
  }    

  /**
   * @see org.sapia.ubik.net.SocketServer#handleError(Throwable)
   */
  protected boolean handleError(Throwable t) {
    Log.error(getClass(), t);

    return false;
  }
}
