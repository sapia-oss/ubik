package org.sapia.ubik.net;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * An instance of this class is used to perform random port selection on a given host.
 * 
 * @author yduchesne
 *
 */
public class TcpPortSelector {
  
  private static final int MIN_PORT    = 1025;
  private static final int MAX_PORT    = 65535;
  private static final int MAX_ATTEMPS = 100;

  /**
   * @param addr the {@link InetAddress} corresponding to the local address
   * on which to acquire a port.
   * @return the port that was selected.
   * @throws IOException if a problem occurred while acquiring the port.
   */
  public int select(InetAddress addr) throws IOException{
    return select(addr.getHostAddress());
  }
  
  /**
   * @param host the local host address on which to acquire a port.
   * @return the port that was selected.
   * @throws IOException if a problem occurred while acquiring the port.
   */
  public int select(String host) throws IOException {
    int port = doSelect(host);
    try{
      Thread.sleep(new Random().nextInt(100));
    }catch(InterruptedException e){
      throw new IllegalStateException("Thread interrupted");
    }
    if(isTaken(host, port)){
      port = doSelect(host);
    }
    if(isTaken(host, port)){
      throw new IOException("No port could be randomly acquired on : " + host);
    }
    return port;
  }  
  
  private int doSelect(String host) throws IOException{
    Random rand = new Random();
    int attempts = 0;
    while(attempts < MAX_ATTEMPS){
      int current = MIN_PORT + rand.nextInt(MAX_PORT - MIN_PORT); 
      try{
        Socket sock = new Socket(host, current);
        try{
          sock.close();
        }catch(IOException e){}
        // port taken ... keep going
        attempts++;
      }catch(UnknownHostException e){
        throw new IllegalArgumentException("Unknown host");
      }catch(ConnectException e){
        // found an available port
        return current;
      }catch(IOException e){
        throw e;
      }
    }
    throw new IOException("No port could be randomly acquired on : " + host);
  }
  
  private boolean isTaken(String host, int port){
    try{
      Socket sock = new Socket(host, port);
      try{
        sock.close();
      }catch (IOException e) {}
      return true;
    }catch(ConnectException e){
      return false;
    }catch(Exception e){
      return false;
    }
  }
}
