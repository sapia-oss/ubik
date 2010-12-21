package org.sapia.ubik.mcast;

import org.sapia.ubik.net.Pool;

/**
 * This class implements a pool of byte arrays.
 * 
 * @author yduchesne
 */
public class ByteArrayPool extends Pool<byte[]>{
  
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
  
  protected byte[] onAcquire(byte[] bytes) throws Exception {
    if(bytes.length != _bufSize){
      return new byte[_bufSize];
    }
    else{
      return bytes;
    }
  }  
  
  protected byte[] doNewObject() throws Exception{
    return new byte[_bufSize];
  }
  
}
