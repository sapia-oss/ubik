package org.sapia.ubik.rmi.server.transport.mina;

import java.net.InetSocketAddress;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.Request;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.interceptor.MultiDispatcher;
import org.sapia.ubik.rmi.server.Config;
import org.sapia.ubik.rmi.server.command.RMICommand;
import org.sapia.ubik.rmi.server.transport.CommandHandler;

/**
 * An instance of this class is hooked into Mina's request handling mechanism.
 * It receives Ubik commands and executes them.
 * 
 * @author yduchesne
 * 
 */
public class MinaServerHandler extends IoHandlerAdapter {

  private Category log = Log.createCategory(getClass());
  private ServerAddress addr;
  private CommandHandler handler;

  public MinaServerHandler(MultiDispatcher dispatcher, ServerAddress addr) {
    this.addr = addr;
    handler = new CommandHandler(dispatcher, getClass());
  }

  public void sessionCreated(IoSession sess) throws Exception {
    log.debug("Connection created from %s", sess.getRemoteAddress());
  }

  public void exceptionCaught(IoSession sess, Throwable err) throws Exception {
    log.error("Exception caught", err);
    sess.closeNow();
  }

  public void messageReceived(IoSession sess, Object msg) throws Exception {
    InetSocketAddress remoteAddr = (InetSocketAddress) sess.getRemoteAddress();
    log.debug(getClass(), "Handling request from %s:%s", remoteAddr.getHostString(), remoteAddr.getPort());
    MinaRmiServerConnection conn = new MinaRmiServerConnection(new MinaAddress(remoteAddr.getHostString(), remoteAddr.getPort()), sess, msg);
    Request req = new Request(conn, addr);

    RMICommand cmd;

    try {
      cmd = (RMICommand) req.getConnection().receive();
    } catch (Exception e) {
      log.error("Could not handle request", e);
      return;
    }

    log.debug("Command received: %s from %s@%s", cmd.getClass().getName(), req.getConnection().getServerAddress(), cmd.getVmId());

    cmd.init(new Config(req.getServerAddress(), req.getConnection()));

    handler.handleCommand(cmd, req.getConnection());
  }

}
