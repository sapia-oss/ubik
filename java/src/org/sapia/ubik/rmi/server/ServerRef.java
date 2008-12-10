package org.sapia.ubik.rmi.server;


/**
 * Encapsulates a <code>Server</code>, its OID, and stub.
 *
 * @see org.sapia.ubik.rmi.server.Server
 *
 * @author Yanick Duchesne
 * 10-Sep-2003
 */
class ServerRef {
  Server    server;
  RemoteRef ref;
  Stub      stub;
  OID       oid;

  /**
   * Constructor for ServerRef.
   */
  ServerRef(Server server, RemoteRef ref, Stub stub, OID oid) {
    this.server   = server;
    this.ref      = ref;
    this.stub     = stub;
    this.oid      = oid;
  }

  ServerRef() {
  }
}
