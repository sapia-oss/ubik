package org.sapia.ubik.rmi.server.transport.udp;

import org.sapia.ubik.net.udp.ObjectStreamFactory;
import org.sapia.ubik.net.udp.UDPConnection;
import org.sapia.ubik.net.udp.UDPServerAddress;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.transport.MarshalOutputStream;
import org.sapia.ubik.rmi.server.transport.RmiConnection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.rmi.RemoteException;


/**
 * @author Yanick Duchesne
 * 17-Jun-2003
 */
public class UdpRmiConnection extends UDPConnection implements RmiConnection {
  private ObjectStreamFactory _fac = new UdpObjectStreamFactory();

  /**
   * Constructor for UdpRmiConnection.
   * @param addr
   * @param bufsz
   * @param timeout
   */
  public UdpRmiConnection(UDPServerAddress addr, int bufsz, int timeout) {
    super(addr, bufsz, timeout);
  }

  /**
   * Constructor for UdpRmiConnection.
   * @param localServer
   * @param pack
   * @param bufsz
   * @param timeout
   */
  public UdpRmiConnection(DatagramSocket localServer, DatagramPacket pack,
    int bufsz, int timeout) {
    super(localServer, pack, bufsz, timeout);
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.RmiConnection#send(Object, VmId, String)
   */
  public void send(Object o, VmId associated, String transportType)
    throws IOException, RemoteException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream(_bufsz);
    MarshalOutputStream   ous = (MarshalOutputStream) _fac.toOutput(bos);
    ous.setUp(associated, transportType);
    ous.writeObject(o);
    ous.flush();
    ous.close();
    super.send(bos.toByteArray());
  }

  /**
   * @see org.sapia.ubik.net.udp.UDPConnection#getFactory()
   */
  protected ObjectStreamFactory getFactory() {
    return _fac;
  }

  /*////////////////////////////////////////////////////////////////////
                               INNER CLASSES
  ////////////////////////////////////////////////////////////////////*/
  public static final class UdpObjectStreamFactory
    implements ObjectStreamFactory {
    /**
     * @see org.sapia.ubik.net.udp.ObjectStreamFactory#toOutput(OutputStream)
     */
    public ObjectOutputStream toOutput(OutputStream os)
      throws IOException {
      return new MarshalOutputStream(os);
    }
  }
}
