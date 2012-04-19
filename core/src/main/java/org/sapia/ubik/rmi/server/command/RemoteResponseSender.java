package org.sapia.ubik.rmi.server.command;

import java.util.List;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.rmi.server.transport.TransportManager;

class RemoteResponseSender implements ResponseSender {
  
  private Category         log        = Log.createCategory(getClass());
  private TransportManager transports;
  
  RemoteResponseSender(TransportManager transports) {
    this.transports = transports;
  }
  
  /**
   * @see org.sapia.ubik.rmi.command.ResponseSender#sendResponses(Destination, List)
   */
   public void sendResponses(Destination dest, List<Response> responses) {
     log.debug("Sending callback responses to %s", dest);

     if (responses.size() > 0) {
       RmiConnection conn = null;
       Connections   pool = null; 
       try {
         pool = transports.getConnectionsFor(dest.getServerAddress());
         conn = pool.acquire();
         conn.send(new CallbackResponseCommand(responses), dest.getVmId(), dest.getServerAddress().getTransportType());
         conn.receive();
       } catch (Exception e) {
         log.error("Error sending command to %s" + dest.getServerAddress(), e);
         if (conn != null) {
           pool.invalidate(conn);
         }
         return;
       }

       if (conn != null) {
         try {
           pool.release(conn);
         } catch (Exception e) {
           log.error("Error releasing connection", e);

         }
       }
     }
   }
}
