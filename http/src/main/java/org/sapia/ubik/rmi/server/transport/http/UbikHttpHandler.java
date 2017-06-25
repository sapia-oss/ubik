package org.sapia.ubik.rmi.server.transport.http;

import java.util.concurrent.ExecutorService;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.Uri;
import org.sapia.ubik.rmi.server.Config;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.command.RMICommand;
import org.sapia.ubik.rmi.server.transport.CommandHandler;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

/**
 * An instance of this class handles incoming requests and delegates them to
 * {@link HttpRmiServerThread}s internally kept in a pool.
 * 
 * @author yduchesne
 */
class UbikHttpHandler implements Handler {
  private Category log = Log.createCategory(getClass());
  private HttpAddress     addr;
  private CommandHandler  handler;
  private ExecutorService threads;

  UbikHttpHandler(Uri localHostUri, ExecutorService executor) {
    addr    = new HttpAddress(localHostUri);
    handler = new CommandHandler(Hub.getModules().getServerRuntime().getDispatcher(), getClass());
    threads = executor;
  }

  @Override
  public void handle(Request req, Response res) {
    final HttpRmiServerConnection conn = new HttpRmiServerConnection(HttpAddress.newDefaultInstance(req.getClientAddress().getHostString(), req
        .getClientAddress().getPort()), req, res);
    try {
      threads.submit(new Runnable() {
        @Override
        public void run() {
          doHandle(new org.sapia.ubik.net.Request(conn, addr));
        }
      });
    } catch (Exception e) {
      Log.error(getClass(), "Error handling request", e);
    }
  }

  private void doHandle(org.sapia.ubik.net.Request req) {
    log.debug("Handling request");

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

  @Override
  public void shutdown() {
    threads.shutdown();
  }

}
