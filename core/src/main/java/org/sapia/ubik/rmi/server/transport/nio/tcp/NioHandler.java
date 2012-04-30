package org.sapia.ubik.rmi.server.transport.nio.tcp;

import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.Request;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.interceptor.MultiDispatcher;
import org.sapia.ubik.rmi.server.Config;
import org.sapia.ubik.rmi.server.command.RMICommand;
import org.sapia.ubik.rmi.server.transport.CommandHandler;

/**
 * An instance of this class is hooked into Mina's request handling mechanism. It
 * receives Ubik commands and executes them.
 * 
 * @author yduchesne
 *
 */
public class NioHandler extends IoHandlerAdapter{
  
  private Category       log      = Log.createCategory(getClass());
  private ServerAddress  addr;
  private CommandHandler handler;
  
  public NioHandler(MultiDispatcher dispatcher, ServerAddress addr){
    this.addr = addr;
    handler = new CommandHandler(dispatcher, getClass());
  }
  
  public void sessionCreated(IoSession sess) throws Exception {
    log.debug("Connection created from %s", sess.getRemoteAddress());
  }  

  public void exceptionCaught(IoSession sess, Throwable err) throws Exception {
    log.error("Exception caught", err);
    sess.close();
  }
  
  public void messageReceived(IoSession sess, Object msg) throws Exception {
    NioTcpRmiServerConnection conn = new NioTcpRmiServerConnection(addr, sess, msg);
    Request                   req  = new Request(conn, addr);

    log.debug(getClass(), "Handling request");
    
    RMICommand cmd;
    
    try {
      cmd = (RMICommand) req.getConnection().receive();
    } catch (Exception e) {
      log.error("Could not handle request", e);
      return;
    }

    log.debug("Command received: %s from %s@%s", 
              cmd.getClass().getName(), 
              req.getConnection().getServerAddress(), 
              cmd.getVmId());
    
    cmd.init(new Config(req.getServerAddress(), req.getConnection()));
    
    handler.handleCommand(cmd, req.getConnection());
  }
  
}
