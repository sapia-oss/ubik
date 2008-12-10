/*
 * ByteArrayPool.java
 *
 * Created on November 3, 2005, 10:02 PM
 */

package org.sapia.ubik.mcast;

import org.sapia.ubik.net.Pool;

/**
 * This class implements a pool of byte arrays.
 * @author yduchesne
 */
public class ByteArrayPool extends Pool{
  
  private int _bufSize;
  
  /** Creates a new instance of ByteArrayPool */
  public ByteArrayPool(int bufSize) {
    _bufSize = bufSize;
  }
  
  public void setBufSize(int bufSize){
    _bufSize = bufSize;
  }
  
  public int getBufSize(){
    return _bufSize;
  }
  
  protected Object onAcquire(Object o) throws Exception {
    byte[] bytes = (byte[])o;
    if(bytes.length != _bufSize){
      return new byte[_bufSize];
    }
    else{
      return o;
    }
  }  
  
  protected Object doNewObject() throws Exception{
    return new byte[_bufSize];
  }
  
}
