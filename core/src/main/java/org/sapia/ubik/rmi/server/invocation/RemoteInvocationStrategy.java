package org.sapia.ubik.rmi.server.invocation;

import java.io.IOException;
import java.rmi.RemoteException;

import org.javasimon.Split;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.module.ModuleContext;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.ClientRuntime;
import org.sapia.ubik.rmi.server.command.CallbackInvokeCommand;
import org.sapia.ubik.rmi.server.command.CommandModule;
import org.sapia.ubik.rmi.server.command.InvokeCommand;
import org.sapia.ubik.rmi.server.command.ResponseLock;
import org.sapia.ubik.rmi.server.command.CallbackResponseQueue;
import org.sapia.ubik.rmi.server.command.ResponseTimeOutException;
import org.sapia.ubik.rmi.server.invocation.InvocationDispatcher.InvocationStrategy;
import org.sapia.ubik.rmi.server.invocation.InvocationDispatcher.InvocationStats;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.MarshalledObject;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.util.Conf;

/**
 * An instance of this class sends a RMI method command (see
 * {@link InvokeCommand}) over the wire to the appropriate endpoint. It handles
 * callbacks, whereby the method call's return value is expected to be sent
 * asynchronously. In such a case, the
 * {@link #dispatchInvocation(InvocationStats, Connections, InvokeCommand)}
 * method will block on the client side until the return value is received, or
 * until a given timeout is reached.
 * 
 * @see Consts#CLIENT_CALLBACK_TIMEOUT
 * 
 * @author yduchesne
 * 
 */
public class RemoteInvocationStrategy implements InvocationStrategy {

  static final long DEFAULT_CALLBACK_TIMEOUT = 30000;

  private Category log = Log.createCategory(getClass());
  private CallbackResponseQueue responses;
  private ClientRuntime clientRuntime;
  private long timeout = DEFAULT_CALLBACK_TIMEOUT;

  @Override
  public void init(ModuleContext context) {
    timeout = Conf.getSystemProperties().getLongProperty(Consts.CLIENT_CALLBACK_TIMEOUT, DEFAULT_CALLBACK_TIMEOUT);
    clientRuntime = context.lookup(ClientRuntime.class);
    responses = context.lookup(CommandModule.class).getCallbackResponseQueue();
  }

  @Override
  public Object dispatchInvocation(InvocationStats perf, Connections pool, InvokeCommand cmd) throws IOException, ClassNotFoundException, Throwable {

    if (cmd instanceof CallbackInvokeCommand) {
      return doDispatchCallbackInvocation(perf, pool, (CallbackInvokeCommand) cmd);
    } else {
      return doDispatchInvocation(perf, pool, cmd);
    }

  }

  private Object doDispatchInvocation(InvocationStats perf, Connections pool, InvokeCommand cmd) throws IOException, ClassNotFoundException,
      Throwable {

    Object toReturn;

    Split acquireSplit = perf.acquireCon.start();
    RmiConnection conn = pool.acquire();
    acquireSplit.stop();

    try {

      Split invokeSendSplit = perf.invokeSend.start();
      conn.send(cmd, cmd.getVmId(), conn.getServerAddress().getTransportType());
      invokeSendSplit.stop();

      Split invokeReceiveSplit = perf.invokeReceive.start();
      toReturn = conn.receive();
      invokeReceiveSplit.stop();
      pool.release(conn);
    } catch (RemoteException e) {
      pool.invalidate(conn);
      pool.clear();
      throw e;
    } catch (Exception e) {
      pool.release(conn);
      throw e;
    }

    if (cmd.usesMarshalledObjects() && (toReturn != null)) {
      try {
        toReturn = ((MarshalledObject) toReturn).get(Thread.currentThread().getContextClassLoader());
      } catch (ClassCastException e) {
        String aMessage = "Could not cast to MarshalledObject: " + toReturn.getClass() + "\n" + toReturn;
        log.error(aMessage);
        throw new ClassCastException(aMessage);
      }
    }

    return toReturn;
  }

  private Object doDispatchCallbackInvocation(InvocationStats perf, Connections pool, CallbackInvokeCommand cmd) throws IOException,
      ClassNotFoundException, Throwable {

    Object toReturn;
    ResponseLock lock = responses.createResponseLock();

    log.debug("Sending callback invocation %s", lock.getId());

    cmd.setUp(lock.getId(), clientRuntime.getCallbackAddress(pool.getTransportType()));

    Split acquireConSplit = perf.acquireCon.start();
    RmiConnection conn = pool.acquire();
    acquireConSplit.stop();

    try {
      conn.send(cmd, cmd.getVmId(), conn.getServerAddress().getTransportType());
      log.debug("Waiting for ACK...");
      conn.receive();
      pool.release(conn);
    } catch (RemoteException e) {
      pool.invalidate(conn);
      pool.clear();
      throw e;
    } catch (Exception e) {
      pool.release(conn);
    }

    try {
      log.debug("Waiting for response...");
      toReturn = lock.await(timeout);
    } catch (ResponseTimeOutException e) {
      throw e;
    } catch (InterruptedException e) {
      lock.release();
      throw new java.io.IOException("Response queue thread interrupted");
    }

    if (cmd.usesMarshalledObjects() && (toReturn != null)) {
      try {
        toReturn = ((MarshalledObject) toReturn).get(Thread.currentThread().getContextClassLoader());
      } catch (ClassCastException e) {
        String aMessage = "Could not cast to MarshalledObject: " + toReturn.getClass() + "\n" + toReturn;
        log.error(aMessage);
        throw new ClassCastException(aMessage);
      }
    }

    return toReturn;
  }
}
