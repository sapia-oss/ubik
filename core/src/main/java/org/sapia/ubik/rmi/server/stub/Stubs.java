package org.sapia.ubik.rmi.server.stub;

import java.rmi.RemoteException;

import javax.naming.Name;

import org.sapia.ubik.mcast.DispatcherFactory;
import org.sapia.ubik.net.Connection;
import org.sapia.ubik.rmi.server.command.RMICommand;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.RmiConnection;

/**
 * Provides stub-related utility methods.
 * 
 * @author yduchesne
 *
 */
public class Stubs {
    
  /**
   * Checks if the given object is a stub.
   * 
   * @param toCheck the {@link Object} to check.
   * @return <code>true</code> if the given object is a Ubik stub.
   */
  public static boolean isStub(Object toCheck) {
  	if (toCheck instanceof Stub) {
  		Stub stub = (Stub) toCheck;
  		return stub.getStubInvocationHandler() != null;
  	}
  	return false;
  }
  
  /**
   * Returns the invocation handler that the given stub wraps.
   * 
   * @param object an {@link Object} that is expected to be a {@link Stub}.
   * @return the {@link StubInvocationHandler} that the stub/dynamic proxy wraps.
   */
  public static StubInvocationHandler getStubInvocationHandler(Object object) {
  	if (!(object instanceof Stub)) {
      throw new IllegalArgumentException(
          String.format(
              "Instance of %s not recognized as a Ubik stub", 
              object.getClass().getName()
          )
      );
    }
  	Stub stub = (Stub) object;
  	return stub.getStubInvocationHandler();
  }

  /**
   * Converts the given {@link StubInvocationHandler} to a stateless one. 
   * 
   * @param name the JNDI {@link Name} of the object to which the remote reference corresponds.
   * @param domain the domain in the context of which the intended remote reference will exist.
   * @param handler the {@link StubInvocationHandler} to convert.
   * @return a new {@link RemoteRefStateless} instance.
   */
  public static RemoteRefStateless convertToStatelessRemoteRef(
      Name      name, 
      String    domain, 
      StubInvocationHandler handler) {
    
    if(handler instanceof RemoteRefStateless) {
      return (RemoteRefStateless) handler;
    }
    
    RemoteRefStateless ref      = new RemoteRefStateless(
                                        name, 
                                        domain, 
                                        DispatcherFactory.getMulticastAddress(System.getProperties())
                                  );
    ref.getContextList().update(handler.getContexts());
    return ref;
  }
  
  /**
   * Tries sending the given command to the endpoint represented by the passed in {@link RemoteRefContext}:
   * 
   * <ol>
   *   <li>Acquires a {@link Connection} from the given context's connection pool (see {@link RemoteRefContext#getConnections()}.
   *   <li>Tries sending the given command using the connection.
   *   <li>If a {@link RemoteException} is caught, clears the pool (see {@link Connections#clear()} and tries another time.
   *   <li>If an exception other than {@link RemoteException} is caught, then that exception is thrown.
   *   <li>If sending the command succeeds, this method returns that command's return value - and also releases to its pool 
   *   the connection that was used.
   *   <li>If a {@link RemoteException} is thrown upon the second attempt, then it is rethrown to the caller.
   * </ol>
   * 
   * @param command the {@link RMICommand} to send.
   * @param target the {@link RemoteRefContext} corresponding to the endpoint to which the command should be sent.
   * @return the return value of the command.
   */
  public static Object trySendCommand(RMICommand command, RemoteRefContext target) throws RemoteException, Exception {
    try{
      return doTrySendCommand(target.getConnections(), command);
    }catch(RemoteException e){
      return doTrySendCommand(target.getConnections(), command);
    }
  }
  
  private static Object doTrySendCommand(Connections pool, RMICommand toSend) throws RemoteException, Exception {
    RmiConnection conn  = null;
    try {
      conn = pool.acquire();
      conn.send(toSend);
      Object returnValue = conn.receive();
      pool.release(conn);
      return returnValue;
    } catch (RemoteException re) {
      if(conn != null) {
        pool.invalidate(conn);
      }
      pool.clear();
      throw re;
    } catch (Exception e) {
      if(conn != null) {
        conn.close();
      }
      throw e;
    }
  }
  
}
