package org.sapia.ubik.rmi.examples;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.server.RMICommand;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.rmi.server.transport.TransportManager;

import java.io.IOException;

import java.rmi.RemoteException;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class HelloWorldCommand extends RMICommand {
  /**
   * @see org.sapia.ubik.rmi.server.RMICommand#execute()
   */
  public Object execute() throws Throwable {
    return "Hello World";
  }

  public static void main(String[] args) {
    // creating address of server we wish to connect to
    TCPAddress addr = new TCPAddress("localhost", 7070);

    RmiConnection conn = null;

    try {
      // acquiring connection
      conn = TransportManager.getConnectionsFor(addr).acquire();
    } catch (RemoteException e) {
      e.printStackTrace();
      System.exit(1);
    }

    try {
      conn.send(new HelloWorldCommand());
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    // always perform the receive!!!
    try {
      Object response = conn.receive();

      if (response instanceof Throwable) {
        Throwable err = (Throwable) response;
        err.fillInStackTrace();
        err.printStackTrace();
      } else {
        // should print 'Hello World'
        System.out.println(response);
      }

      // Very important: allows transport 
      // providers to implement connection
      // pooling.
      TransportManager.getConnectionsFor(addr).release(conn);
    } catch (RemoteException e) {
      e.printStackTrace();
      System.exit(1);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
