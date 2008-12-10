package org.sapia.ubik.rmi.server.transport;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.perf.PerfAnalyzer;
import org.sapia.ubik.rmi.server.perf.Topic;


/**
 * This class is used to marshal outgoing requests.
 *
 * @see org.sapia.ubik.rmi.server.Server
 * @see org.sapia.ubik.rmi.server.transport.RmiConnection
 * @see org.sapia.ubik.rmi.server.transport.TransportProvider
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class MarshalOutputStream extends ObjectOutputStream {
  private VmId         _id;
  private String       _transportType;
  private static Perf _perf = new Perf();

  /**
   * Constructor for RmiOutputStream.
   */
  public MarshalOutputStream(OutputStream os) throws IOException {
    super(os);
    super.enableReplaceObject(true);
  }

  public void setUp(VmId id, String transportType) {
    _id              = id;
    _transportType   = transportType;
  }

  /**
   * @see java.io.ObjectOutputStream#replaceObject(Object)
   */
  protected Object replaceObject(Object obj) throws IOException {
    if (obj instanceof java.rmi.Remote) {
      if (_id == null) {
        throw new IllegalStateException("VmId not set on " +
          getClass().getName());
      }

      if (_perf.stubOutput.isEnabled()) {
        _perf.stubOutput.start();
      }

      Object remote = Hub.asRemote(obj, _id, _transportType);

      if (_perf.stubOutput.isEnabled()) {
        _perf.stubOutput.end();
      }

      return remote;
    } else {
      return obj;
    }
  }
  
  protected void writeObjectOverride(Object obj) throws IOException {
    super.writeUnshared(obj);
  }
  
  private static String className(){
    return MarshalOutputStream.class.getName();
  }
 

  static final class Perf{
    
    Topic stubOutput = PerfAnalyzer.getInstance().getTopic(className()+".StubOutput");
    
  }
}
