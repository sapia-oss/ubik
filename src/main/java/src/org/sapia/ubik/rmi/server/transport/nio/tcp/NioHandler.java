package org.sapia.ubik.rmi.server.transport.nio.tcp;

import java.io.EOFException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;

import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.sapia.ubik.net.Request;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.Config;
import org.sapia.ubik.rmi.server.Log;
import org.sapia.ubik.rmi.server.RMICommand;
import org.sapia.ubik.rmi.server.invocation.InvokeCommand;
import org.sapia.ubik.rmi.server.perf.PerfAnalyzer;
import org.sapia.ubik.rmi.server.perf.Topic;

/**
 * An instance of this class is hooked into Mina's request handling mechanism. It
 * receives Ubik commands and executes them.
 * 
 * @author yduchesne
 *
 */
public class NioHandler extends IoHandlerAdapter{
  
  private Perf          _perf          = new Perf();  
  private ServerAddress _addr;
  
  public NioHandler(ServerAddress addr){
    _addr = addr;
  }
  
  public void sessionCreated(IoSession sess) throws Exception {
  }  

  public void exceptionCaught(IoSession sess, Throwable err) throws Exception {
    Log.error(this.getClass(), err);
    sess.close();
  }
  
  public void messageReceived(IoSession sess, Object msg) throws Exception {
    NioTcpRmiServerConnection conn = new NioTcpRmiServerConnection(
      _addr, sess, msg
    );
    
    Request              req = new Request(conn, _addr);

    if (Log.isDebug()) {
      Log.debug(getClass(), "handling request");
    }

    RMICommand cmd;
    Object     resp = null;

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
        if (_perf.remoteCall.isEnabled()) {
          if (cmd instanceof InvokeCommand) {
            _perf.remoteCall.start();
          }
        }

        resp = cmd.execute();

        if (_perf.remoteCall.isEnabled()) {
          if (cmd instanceof InvokeCommand) {
            _perf.remoteCall.end();
          }
        }
      } catch (Throwable t) {
        t.printStackTrace();
        t.fillInStackTrace();
        resp = t;
        if (_perf.remoteCall.isEnabled()) {
          if (cmd instanceof InvokeCommand) {
            _perf.remoteCall.end();
          }
        }        
      }

      if (_perf.sendResponse.isEnabled()) {
        if (cmd instanceof InvokeCommand) {
          _perf.sendResponse.start();
        }
      }

      conn.send(resp, cmd.getVmId(), cmd.getServerAddress().getTransportType());

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

        return;
      }
    } catch (ClassNotFoundException e) {
      e.fillInStackTrace();
      Log.error(getClass(), "Class not found while receiving sending request", e);

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
  
  private String className(){
    return getClass().getName();
  }  
  
  ////// Inner classes
  
  class Perf {
    Topic remoteCall = PerfAnalyzer.getInstance().getTopic(className() + ".RemoteCall");
    Topic sendResponse = PerfAnalyzer.getInstance().getTopic(className() + ".SendResponse");
  }    
}
