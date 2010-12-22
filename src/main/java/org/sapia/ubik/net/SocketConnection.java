package org.sapia.ubik.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import java.net.Socket;
import java.net.SocketException;

import java.rmi.RemoteException;


/**
 * A <code>Connection</code> implemented through a <code>Socket</code>.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class SocketConnection implements Connection {
  static final long            DEFAULT_RESET_INTERVAL = 2000;
  protected Socket             _sock;
  protected TCPAddress         _address;
  protected ClassLoader        _loader;
  protected ObjectInputStream  _is;
  protected ObjectOutputStream _os;
  protected long               _lastReset;
  protected long               _resetInterval = DEFAULT_RESET_INTERVAL;
  private int callCount = 0;

  public SocketConnection(Socket sock, ClassLoader loader) {
    this(sock);
    _loader = loader;
  }

  public SocketConnection(Socket sock) {
    _sock      = sock;
    _address   = new TCPAddress(sock.getInetAddress().getHostAddress(),
        sock.getPort());
  }
  
  /**
   * Sets the interval at which this instance calls the <code>reset</code>
   * method on the <code>ObjectOutputStream</code> that in uses internally for serializing
   * objects.
   * <p>
   * This instance performs the reset at every write, insuring that no stale
   * object is cached by the underlying <code>ObjectOutputStream</code>.
   *
   * @param interval the interval (in millis) at which this instance calls
   * the <code>reset</code> method of its <code>ObjectOutputStream</code>.
   */
  public void setResetInterval(long interval){
    _resetInterval = interval;
  }

  /**
   * @see Connection#send(Object)
   */
  public void send(Object o) throws IOException, RemoteException {
      writeHeader(_sock.getOutputStream(), _loader);
      doSend(o, _os);
  }

  /**
   * @see Connection#receive()
   */
  public Object receive()
    throws IOException, ClassNotFoundException, RemoteException {
    try {
      readHeader(_sock.getInputStream(), _loader);
      return _is.readObject();
    } catch (EOFException e) {
      throw new RemoteException("Communication with server interrupted; server probably disappeared",
        e);
    } catch (SocketException e) {
      throw new RemoteException("Connection could not be opened; server is probably down",
        e);
    }
  }

  /**
   * @see org.sapia.ubik.net.Connection#close()
   */
  public void close() {
    try {
      if (_os != null) {
        _os.reset();
        _os.close();
        _os = null;
      }

      if (_is != null) {
        _is.close();
        _is = null;
      }

      _sock.close();
    } catch (Throwable t) {
      //noop
    }
  }

  /**
   * @see Connection#getServerAddress()
   */
  public ServerAddress getServerAddress() {
    return _address;
  }

  /**
   * Returns this instance's internal input stream.
   *
   * @return an {@link InputStream}.
   */
  public InputStream getInputStream() throws IOException {
    return _sock.getInputStream();
  }

  /**
   * Returns this instance's internal output stream.
   *
   * @return an {@link OutputStream}.
   */
  public OutputStream getOuputStream() throws IOException {
    return _sock.getOutputStream();
  }

  /**
   * Template method internally called by this instance; the method should create
   * an {@link ObjectOutputStream} for the given parameters.
   *
   * @param os the {@link OutputStream} that the returned stream should wrap.
   * @param loader the {@link ClassLoader} that this instance corresponds to.
   * @return an {@link ObjectOutputStream}.
   * @throws IOException if a problem occurs creating the desired object.
   */
  protected ObjectOutputStream newOutputStream(OutputStream os,
    ClassLoader loader) throws IOException {
    return new ObjectOutputStream(os);
  }

  /**
   * Template method internally called by this instance; the method should create
   * an <code>ObjectInputStream</code> for the given parameters.
   * <p>
   * The returned instance can use the passed in classloader to resolve the classes of the
   * deserialized objects.
   *
   * @see ObjectInputStream#resolveClass(java.io.ObjectStreamClass)
   * @param is the {@link InputStream} that the returned stream should wrap.
   * @param loader the {@link ClassLoader} that this instance corresponds to.
   * @return an {@link ObjectInputStream}.
   * @throws IOException if a problem occurs creating the desired object.

   */
  protected ObjectInputStream newInputStream(InputStream is, ClassLoader loader)
    throws IOException {
    return new ObjectInputStream(is);
  }
  
  protected void doSend(Object toSend, ObjectOutputStream mos) throws IOException{
    try{
      _os.writeObject(toSend);
      _os.flush();
       
    } catch (java.net.SocketException e) {
      throw new RemoteException("Communication with server interrupted; server probably disappeared",
        e);
    } catch (EOFException e) { 
      throw new RemoteException("Communication with server interrupted; server probably disappeared",
        e);      
    }
  }
  
  protected void writeHeader(OutputStream os, ClassLoader loader) throws IOException{
    DataOutputStream dos = new DataOutputStream(os);    
    if (_os == null || (System.currentTimeMillis() - _lastReset) >= _resetInterval) {
      _lastReset = System.currentTimeMillis();
      dos.writeBoolean(true);      
      dos.flush();
      _os = null;
      _os = newOutputStream(os, loader);
      callCount = 0;
    }
    else{
      dos.writeBoolean(false);
      dos.flush();
    }
    callCount++;
  }
  
  protected void readHeader(InputStream is, ClassLoader loader) throws IOException{
    DataInputStream dis = new DataInputStream(is);
    
    boolean reset = dis.readBoolean();
    if(_is == null || reset){
      _is = null;
      _is = newInputStream(is, loader);
    }
  }
}
