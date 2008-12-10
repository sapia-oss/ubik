package org.sapia.ubik.rmi.server.transport.http;

import java.io.EOFException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;

import org.sapia.ubik.net.PooledThread;
import org.sapia.ubik.net.Request;
import org.sapia.ubik.rmi.server.Config;
import org.sapia.ubik.rmi.server.Log;
import org.sapia.ubik.rmi.server.RMICommand;
import org.sapia.ubik.rmi.server.invocation.InvokeCommand;
import org.sapia.ubik.rmi.server.perf.PerfAnalyzer;
import org.sapia.ubik.rmi.server.transport.RmiConnection;


/**
 * An instance of this class executes Ubik RMI commands.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
class HttpRmiServerThread extends PooledThread {
  private PerfAnalyzer _perf = PerfAnalyzer.getInstance();

  /**
   * @see org.sapia.ubik.net.PooledThread#doExec(java.lang.Object)
   */
  protected void doExec(Object task) {
    if (Log.isDebug()) {
      Log.debug(getClass(), "handling request");
    }

    Request       req  = (Request) task;
    RmiConnection conn = (RmiConnection) req.getConnection();
    RMICommand    cmd;
    Object        resp = null;

    try {
      if (Log.isDebug()) {
        Log.debug(getClass(), "receiving command");
      }

      cmd = (RMICommand) req.getConnection().receive();

      if (Log.isDebug()) {
        Log.debug(getClass(),
          "command received: " + cmd.getClass().getName() + " from " +
          req.getConnection().getServerAddress() + '@' + cmd.getVmId());
      }

      cmd.init(new Config(req.getServerAddress(), req.getConnection()));

      try {
        if (_perf.isEnabled()) {
          if (cmd instanceof InvokeCommand) {
            _perf.getTopic(getClass().getName() + ".RemoteCall").start();
          }
        }

        resp = cmd.execute();

        if (_perf.isEnabled()) {
          if (cmd instanceof InvokeCommand) {
            _perf.getTopic(getClass().getName() + ".RemoteCall").end();
          }
        }
      } catch (Throwable t) {
        t.printStackTrace();
        t.fillInStackTrace();
        resp = t;
      }

      if (_perf.isEnabled()) {
        if (cmd instanceof InvokeCommand) {
          _perf.getTopic(getClass().getName() + ".SendResponse").start();
        }
      }

      conn.send(resp, cmd.getVmId(), cmd.getServerAddress().getTransportType());

      if (_perf.isEnabled()) {
        if (cmd instanceof InvokeCommand) {
          _perf.getTopic(getClass().getName() + ".SendResponse").end();
        }
      }
    } catch (RuntimeException e) {
      Log.error(getClass(), "RuntimeException caught sending response", e);

      try {
        e.fillInStackTrace();
        req.getConnection().send(e);
      } catch (IOException e2) {
        req.getConnection().close();

        return;
      }
    } catch (ClassNotFoundException e) {
      e.fillInStackTrace();
      Log.error(getClass(), "Class not found while receiving request", e);

      try {
        req.getConnection().send(e);
      } catch (IOException e2) {
        e2.fillInStackTrace();
        req.getConnection().close();

        return;
      }
    } catch (EOFException e) {
      e.fillInStackTrace();
      req.getConnection().close();

      return;
    } catch (java.net.SocketException e) {
      e.fillInStackTrace();
      req.getConnection().close();

      return;
    } catch (NotSerializableException e) {
      e.fillInStackTrace();
      Log.error(getClass().getName(),
        "Could not serialize class while sending response", e);

      try {
        req.getConnection().send(e);
      } catch (IOException e2) {
        req.getConnection().close();

        return;
      }
    } catch (InvalidClassException e) {
      e.fillInStackTrace();
      Log.error(getClass(), "Class is invalid; object could not be sent", e);

      e.fillInStackTrace();

      try {
        req.getConnection().send(e);
      } catch (IOException e2) {
        req.getConnection().close();

        return;
      }
    } catch (java.io.IOException e) {
      e.fillInStackTrace();

      try {
        req.getConnection().send(e);
      } catch (IOException e2) {
        req.getConnection().close();

        return;
      }
    }
  }
}
