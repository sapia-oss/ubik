package org.sapia.ubik.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
 * A {@link Connection} implemented through a {@link Socket}.
 *
 * @author Yanick Duchesne
 */
public class SocketConnection implements Connection {
  static final long            DEFAULT_RESET_INTERVAL = 2000;
  protected Socket             sock;
  protected TCPAddress         address;
  protected ClassLoader        loader;
  protected ObjectInputStream  is;
  protected ObjectOutputStream os;
  protected long               lastReset;
  protected long               resetInterval 					= DEFAULT_RESET_INTERVAL;
  private   int                bufsize;

  public SocketConnection(Socket sock, ClassLoader loader, int bufsize) {
    this(sock, bufsize);
    this.loader = loader;
  }

  public SocketConnection(Socket sock, int bufsize) {
    this.sock      = sock;
    this.bufsize   = bufsize;
    address        = new TCPAddress(sock.getInetAddress().getHostAddress(), sock.getPort());
  }
  
  /**
   * Sets the interval at which this instance calls the {@link ObjectOutputStream#reset()}
   * method on the {@link ObjectOutputStream} that in uses internally for serializing
   * objects.
   * <p>
   * This instance performs the reset at every write, insuring that no stale
   * object is cached by the underlying {@link ObjectOutputStream}.
   *
   * @param interval the interval (in millis) at which this instance calls
   * the <code>reset</code> method of its {@link ObjectOutputStream}.
   */
  public void setResetInterval(long interval){
    resetInterval = interval;
  }

  /**
   * @see Connection#send(Object)
   */
  public void send(Object o) throws IOException, RemoteException {
      writeHeader(sock.getOutputStream(), loader);
      doSend(o, os);
  }

  /**
   * @see Connection#receive()
   */
  public Object receive()
    throws IOException, ClassNotFoundException, RemoteException {
    try {
      readHeader(sock.getInputStream(), loader);
      return is.readObject();
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
      if (os != null) {
        os.reset();
        os.close();
        os = null;
      }

      if (is != null) {
        is.close();
        is = null;
      }

      sock.close();
    } catch (Throwable t) {
      //noop
    }
  }

  /**
   * @see Connection#getServerAddress()
   */
  public ServerAddress getServerAddress() {
    return address;
  }

  /**
   * Returns this instance's internal input stream.
   *
   * @return an {@link InputStream}.
   */
  public InputStream getInputStream() throws IOException {
    return sock.getInputStream();
  }

  /**
   * Returns this instance's internal output stream.
   *
   * @return an {@link OutputStream}.
   */
  public OutputStream getOuputStream() throws IOException {
    return sock.getOutputStream();
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
   * an {@link ObjectInputStream} for the given parameters.
   * <p>
   * The returned instance can use the passed in {@link ClassLoader} to resolve the classes 
   * of the deserialized objects.
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
      mos.writeObject(toSend);
      mos.flush();
       
    } catch (java.net.SocketException e) {
      throw new RemoteException("Communication with server interrupted; server probably disappeared",
        e);
    } catch (EOFException e) { 
      throw new RemoteException("Communication with server interrupted; server probably disappeared",
        e);      
    }
  }
  
  protected void writeHeader(OutputStream outputStream, ClassLoader loader) throws IOException{
    DataOutputStream dos = new DataOutputStream(outputStream);    
    if (os == null || (System.currentTimeMillis() - lastReset) >= resetInterval) {
      lastReset = System.currentTimeMillis();
      dos.writeBoolean(true);      
      dos.flush();
      os = null;
      os = newOutputStream(new BufferedOutputStream(outputStream, bufsize), loader);
    } else{
      dos.writeBoolean(false);
      dos.flush();
    }
  }
  
  protected void readHeader(InputStream inputStream, ClassLoader loader) throws IOException{
    DataInputStream dis = new DataInputStream(inputStream);
    
    boolean reset = dis.readBoolean();
    if(is == null || reset){
      is = null;
      is = newInputStream(new BufferedInputStream(inputStream, bufsize), loader);
    }
  }
}
