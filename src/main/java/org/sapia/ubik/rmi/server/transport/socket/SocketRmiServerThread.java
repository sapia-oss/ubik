package org.sapia.ubik.rmi.server.transport.socket;

import java.io.EOFException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.PooledThread;
import org.sapia.ubik.net.Request;
import org.sapia.ubik.rmi.server.Config;
import org.sapia.ubik.rmi.server.Log;
import org.sapia.ubik.rmi.server.RMICommand;
import org.sapia.ubik.rmi.server.invocation.InvokeCommand;
import org.sapia.ubik.rmi.server.perf.PerfAnalyzer;
import org.sapia.ubik.rmi.server.perf.Topic;
import org.sapia.ubik.rmi.server.transport.RmiConnection;


/**
 * Implements a thread in a <code>SocketRmiServer</code> instance.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class SocketRmiServerThread extends PooledThread {

  private Perf _perf = new Perf();
  Connection           _current;

  SocketRmiServerThread() {
  }

  /**
   * @see java.lang.Thread#interrupt()
   */
  public void interrupt() {
    super.interrupt();

    if (_current != null) {
      _current.close();
    }
  }

  /**
   * @see org.sapia.ubik.net.PooledThread#doExec(Object)
   */
  protected void doExec(Object task) {
    if (Log.isDebug()) {
      Log.debug(getClass(), "handling request");
    }

    Request req = (Request) task;
    _current = req.getConnection();

    RMICommand cmd;
    Object     resp = null;

    while (true) {
      try {
        if (Log.isDebug()) {
          Log.debug(getClass(), "receiving command from: " + req.getConnection().getServerAddress());
        }

        cmd = (RMICommand) req.getConnection().receive();

        if (Log.isDebug()) {
          Log.debug(getClass(),
            "command received: " + cmd.getClass().getName() + " from " +
            req.getConnection().getServerAddress() + '@' + cmd.getVmId());
        }
        
        cmd.init(new Config(req.getServerAddress(), req.getConnection()));
        
        long start = System.currentTimeMillis();
        if(_tps != null && _tps.isEnabled()){
          _tps.hit();
        }        

        try {
          if (_perf.remoteCall.isEnabled()) {
            if (cmd instanceof InvokeCommand) {
              _perf.remoteCall.start();
            }
          }

          resp = cmd.execute();
          end(start);

          if (_perf.remoteCall.isEnabled()) {
            if (cmd instanceof InvokeCommand) {
              _perf.remoteCall.end();
            }
          }
        } catch (Throwable t) {
          resp = t;
          end(start);
        }

        if (_perf.sendResponse.isEnabled()) {
          if (cmd instanceof InvokeCommand) {
            _perf.sendResponse.start();
          }
        }

        ((RmiConnection) req.getConnection()).send(resp, cmd.getVmId(),
          cmd.getServerAddress().getTransportType());

        if (_perf.sendResponse.isEnabled()) {
          if (cmd instanceof InvokeCommand) {
            _perf.sendResponse.end();
          }
        }
      } catch (RuntimeException e) {
        Log.error(getClass(), "RuntimeException caught sending response", e);

        try {
          e.fillInStackTrace();
          req.getConnection().send(e);
        } catch (IOException e2) {
          req.getConnection().close();

          break;
        }
      } catch (ClassNotFoundException e) {
        e.fillInStackTrace();
        Log.error(getClass(),
          "Class not found while receiving sending request", e);

        try {
          req.getConnection().send(e);
        } catch (IOException e2) {
          e2.fillInStackTrace();
          req.getConnection().close();

          break;
        }
      } catch (EOFException e) {
        e.fillInStackTrace();
        req.getConnection().close();

        break;
      } catch (java.net.SocketException e) {
        e.fillInStackTrace();
        req.getConnection().close();

        break;
      } catch (NotSerializableException e) {
        e.fillInStackTrace();
        Log.error(getClass().getName(),
          "Could not serialize class while sending response", e);

        try {
          req.getConnection().send(e);
        } catch (IOException e2) {
          req.getConnection().close();

          break;
        }
      } catch (InvalidClassException e) {
        e.fillInStackTrace();
        Log.error(getClass(), "Class is invalid; object could not be sent", e);

        e.fillInStackTrace();

        try {
          req.getConnection().send(e);
        } catch (IOException e2) {
          req.getConnection().close();

          break;
        }
      } catch (java.io.IOException e) {
        e.fillInStackTrace();

        try {
          req.getConnection().send(e);
        } catch (IOException e2) {
          req.getConnection().close();

          break;
        }
      }
    }
  }
  
  @Override
  protected void handleExecutionException(Exception e) {
    Log.warning(getClass(), "Error executing thread", e);
  }
  
  private void end(long start){
    if(_duration != null && _duration.isEnabled()){
      _duration.incrementLong(System.currentTimeMillis() - start);
    }
  }
  
  private String className(){
    return getClass().getName();
  }
  
  ////// Inner classes
  
  class Perf {
    Topic remoteCall = PerfAnalyzer.getInstance().getTopic(className() + ".RemoteCall");
    Topic sendResponse = PerfAnalyzer.getInstance().getTopic(className() + ".SendResponse");
  }
}
