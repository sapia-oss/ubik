package org.sapia.ubik.net.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.rmi.RemoteException;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.ServerAddress;


/**
 * @author Yanick Duchesne
 * 16-Jun-2003
 */
public class UDPConnection implements Connection {
  protected int            _bufsz;
  private UDPServerAddress _addr;
  private DatagramPacket   _pack;
  private DatagramSocket   _sock;
  private int              _timeout;

  /**
   * Constructor for UDPConnection.
   */
  public UDPConnection(UDPServerAddress addr, int bufsz, int timeout) {
    _addr      = addr;
    _bufsz     = bufsz;
    _timeout   = timeout;
  }

  /**
   * Constructor for UDPConnection.
   */
  public UDPConnection(DatagramSocket localServer, DatagramPacket pack,
    int bufsz, int timeout) {
    _addr      = new UDPServerAddress(pack.getAddress(), pack.getPort());
    _pack      = pack;
    _sock      = localServer;
    _bufsz     = bufsz;
    _timeout   = timeout;
  }

  /**
   * @see org.sapia.ubik.net.Connection#close()
   */
  public void close() {
  }

  /**
   * @see org.sapia.ubik.net.Connection#getServerAddress()
   */
  public ServerAddress getServerAddress() {
    return _addr;
  }

  /**
   * @see org.sapia.ubik.net.Connection#receive()
   */
  public Object receive()
    throws IOException, ClassNotFoundException, RemoteException {
    if (_sock == null) {
      _sock = new DatagramSocket(_addr.getPort());
      _sock.setSoTimeout(_timeout);
    }

    DatagramPacket pack;

    if (_pack == null) {
      pack = new DatagramPacket(new byte[_bufsz], _bufsz);
    } else {
      pack    = _pack;
      _pack   = null;
    }

    _sock.receive(pack);

    return Util.fromDatagram(pack);
  }

  /**
   * @see org.sapia.ubik.net.Connection#send(Object)
   */
  public void send(Object o) throws IOException, RemoteException {
    byte[] bytes = Util.toBytes(o, _bufsz, getFactory());
    send(bytes);
  }

  protected void send(byte[] bytes) throws IOException, RemoteException {
    if (_sock == null) {
      _sock = new DatagramSocket();
      _sock.setSoTimeout(_timeout);
    }

    DatagramPacket pack = new DatagramPacket(bytes, 0, bytes.length,
        _addr.getInetAddress(), _addr.getPort());
    _sock.send(pack);
  }

  protected ObjectStreamFactory getFactory() {
    return ObjectStreamFactory.DEFAULT_FACTORY;
  }
}
