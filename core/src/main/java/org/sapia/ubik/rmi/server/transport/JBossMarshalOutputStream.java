package org.sapia.ubik.rmi.server.transport;

import java.io.IOException;
import java.io.OutputStream;

import org.jboss.serial.io.JBossObjectOutputStream;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.ServerTable;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.rmi.server.stats.Timer;

public class JBossMarshalOutputStream extends JBossObjectOutputStream implements RmiObjectOutput {
  
  private static Timer stubOutput = Stats.getInstance().createTimer(
                                      JBossMarshalOutputStream.class, 
                                      "StubOutput", 
                                      "Avg time to serialize a stub"
                                    );

  private VmId                  id;
  private String                transportType;
  private volatile ServerTable  serverTable = Hub.getModules().getServerTable();

  JBossMarshalOutputStream(OutputStream os) throws IOException {
    super(os);
    super.enableReplaceObject(true);
  }

  public void setUp(VmId id, String transportType) {
    this.id            = id;
    this.transportType = transportType;
  }

  /**
   * @see java.io.ObjectOutputStream#replaceObject(Object)
   */
  protected Object replaceObject(Object obj) throws IOException {
    if (obj instanceof java.rmi.Remote) {
      if (id == null) {
        throw new IllegalStateException("VmId not set on " +
          getClass().getName());
      }
      
      stubOutput.start();
      Object remote = serverTable.createRemoteObject(obj, id, transportType);
      stubOutput.end();

      return remote;
    } else {
      return obj;
    }
  }
  /*
  protected void writeObjectOverride(Object obj) throws IOException {
    super.writeUnshared(obj);
  } */ 

}
