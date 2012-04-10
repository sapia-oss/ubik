package org.sapia.ubik.rmi.server;

import org.sapia.ubik.rmi.server.oid.OID;
import org.sapia.ubik.rmi.server.stub.Stub;
import org.sapia.ubik.rmi.server.stub.StubInvocationHandler;
import org.sapia.ubik.util.Strings;


/**
 * Encapsulates a {@link Server}, its OID, and stub.
 *
 * @author Yanick Duchesne
 */
class ServerRef {
  private Server                server;
  private StubInvocationHandler handler;
  private Object                exportedObject;
  private Stub                  stub;
  private OID                   oid;

  /**
   * Constructor for ServerRef.
   */
  ServerRef(Server server, Object exportedObject, StubInvocationHandler handler, Stub stub, OID oid) {
    this.server         = server;
    this.exportedObject = exportedObject;
    this.handler        = handler;
    this.stub           = stub;
    this.oid            = oid;
  }
  
  Server getServer() {
    return server;
  }
  
  StubInvocationHandler getHandler() {
    return handler;
  }
  
  Object getExportedObject() {
    return exportedObject;
  }
  
  Stub getStub() {
    return stub;
  }
  
  OID getOid() {
    return oid;
  }

  public String toString() {
    return Strings.toString("oid", oid, "server", server, "handler", handler, "exportedObject", exportedObject);
  }
}
