package org.sapia.ubik.rmi.server.transport;

import java.io.IOException;
import java.io.OutputStream;

import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.jboss.serial.io.JBossObjectOutputStream;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.ServerTable;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.util.Assertions;

public class JBossMarshalOutputStream extends JBossObjectOutputStream implements
    RmiObjectOutput {

  private static Stopwatch stubOutput = Stats
      .createStopwatch(JBossMarshalOutputStream.class, "StubOutput",
          "Avg time to create a stub");

  private VmId id;
  private String transportType;
  private volatile ServerTable serverTable = Hub.getModules().getServerTable();

  JBossMarshalOutputStream(OutputStream os) throws IOException {
    super(os);
    super.enableReplaceObject(true);
  }

  public void setUp(VmId id, String transportType) {
    this.id = id;
    this.transportType = transportType;
  }

  /**
   * @see java.io.ObjectOutputStream#replaceObject(Object)
   */
  protected Object replaceObject(Object obj) throws IOException {
    if (obj instanceof java.rmi.Remote) {
      Assertions.illegalState(id == null, "VmId not set on %s", getClass()
          .getName());

      Split split = stubOutput.start();
      Object remote = serverTable.createRemoteObject(obj, id, transportType);
      split.stop();

      return remote;
    } else {
      return obj;
    }
  }

}
