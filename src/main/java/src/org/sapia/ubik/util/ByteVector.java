package org.sapia.ubik.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * This class can conveniently be used as a replacement for the JDK's 
 * <code>ByteArrayInputStream</code> and <code>ByteArrayOutputStream</code>
 * classes.
 * <p>
 * This class implements a vector of byte arrays (it encapsulates an array of
 * byte arrays). An instance of this class will "grow" (according to a predefined
 * increment) as new bytes are added to it (and if its current size does not allow 
 * for these new bytes to fit in).
 * <p>
 * In addition, each byte array that is internally kept (and stores the actual
 * bytes written to this instance) is created with a fixed capacity (which can 
 * be specified when creating an instance of this class).
 * <p>
 * An instance of this class internally keeps track of its current position in
 * the vector. That position is incremented according to the reads and writes that
 * are performed. Thus, existing bytes are not overwritten and are not read twice.
 * <p>
 * An instance of this class can be reused without destroying the internal byte
 * arrays that have been created. Similarly, reading bytes from the instance
 * does not require copying the whole bytes that the instance stores to an 
 * intermediary array that is returned to the caller - instead, this instance
 * directly supports reads (this is a workaround to the JDK's ByteArrayOutputStream
 * class and its <code>toByteArray()</code> method.
 * <p>
 * <b>WARNING: THIS CLASS IS NOT THREAD-SAFE</b>.
 *
 *
 * @author Yanick Duchesne
 *
 * @see ByteVectorInputStream
 * @see ByteVectorOutputStream
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2005 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ByteVector {
  
  public static final int DEFAULT_ARRAY_CAPACITY = 200;
  public static final int DEFAULT_INCREMENT      = 10;  
  
  protected int _arrayPos, _markPos, _markOffset, _arrayCount;
  protected int _capacity  = DEFAULT_ARRAY_CAPACITY;
  protected int _increment = DEFAULT_INCREMENT;  
  
  protected ByteArray[] _arrays = new ByteArray[_increment];

  /**
   * Creates an instance of this class with the default capacity and increment.
   */
  public ByteVector(){}
  
  /**
   * Creates an instance of this class with the given capacity and increment. 
   * The capacity corresponds to the size of the internal bytes arrays (that are
   * created to store data). The increment corresponds to the amount of internal
   * byte arrays that will be created when this instance will grow.
   *
   * @param capacity some capacity.
   * @param increment some increment.
   */  
  public ByteVector(int capacity, int increment){
    _capacity = capacity;
    _increment = increment;
  }
  
  
  /**
   * Internally sets the given position as a mark. The internal position counter
   * will internally be set to that mark upon the <code>reset</code> method
   * being called.
   *
   * @param mark some mark.
   * @see #reset()
   */
  public void mark(int mark){
    if(mark < _capacity){
      _markPos = 0;
      _markOffset = mark; 
    }
    else{
      int pos      = mark/_capacity;
      int offset   = mark%_capacity;
      if(pos >= _arrayCount){
        throw new IndexOutOfBoundsException(""+mark);
      }
      _markPos    = pos;
      _markOffset = offset;
    }
  }
  
  /**
   * Resets the internal position to the specified mark. The position will be 
   * set to 0 if no mark has been specified.
   *
   * @see #mark(int)
   */
  public void reset(){
    _arrayPos = _markPos;
    _arrays[_arrayPos]._pos = _markOffset;
    for(int i = _arrayPos+1; i < _arrayCount; i++){
      _arrays[i]._pos = 0;
    }
  }
  
  /**
   * Clears the data that this instance holds, making it suitable for reuse.
   * 
   * @param freeMemory if <code>true</code>, the internal byte arrays that have
   * been created will be dereferenced (otherwise, they will be reused).
   */
  public void clear(boolean freeMemory){
    _arrayPos = 0;
    _markOffset = 0;
    _markPos = 0;
    if(freeMemory){
      _arrays = new ByteArray[_increment];
    }
    else{
      for(int i = 0; i < _arrayCount; i++){
        _arrays[i]._pos   = 0;
        _arrays[i]._limit = 0;        
      }      
    }
    _arrayCount = 0;
  }

  /**
   * Returns the number of bytes that this instance holds.
   */
  public int length(){
    if(_arrayCount == 0){
      return 0;
    }
    return (_arrayCount-1)*_capacity+_arrays[_arrayCount-1]._limit;    
  }
  
  /**
   * @return the current internal position of this instance, in terms of the 
   * bytes it holds.
   */
  public int position(){
    if(_arrayCount == 0){
      return 0;
    }
    else if(_arrayPos == 0){
      return _arrays[_arrayPos]._pos;
    }
    if(_arrayPos >= _arrayCount){
      return (_arrayCount-1) * _capacity  + _arrays[_arrayCount-1]._pos;      
    }
    else{
      return _arrayPos * _capacity  + _arrays[_arrayPos]._pos;
    }
  }
  
  /**
   * Returns the number of remaining bytes in this instance.
   *
   * @return <code>lenth() - position()</code>
   */
  public int remaining(){
    return length() - position();
  }
  
  int arrayPosition(){
    return _arrayPos;
  }
  
  int arrayCount(){
    return _arrayCount;
  }  
  
  /**
   * @return <code>true</code> if this instance's current internal position is
   * less than the number of bytes it holds.
   */
  public boolean hasRemaining(){
    return _arrayPos < _arrayCount && _arrays[_arrayPos]._pos < _arrays[_arrayPos]._limit; 
  }
  
  /**
   * Returns the bytes that this instance holds (starting from this instance's
   * current position).
   */
  public byte[] toByteArray(){
    if(_arrayCount == 0 || _arrays[0] == null){
      return new byte[0];
    }
    byte[] b = new byte[length()];
    int index = 0;
    for(int i = _arrayPos; i < _arrayCount; i++){
      for(int j = 0; j < _arrays[i]._limit; j++){
        b[index] = _arrays[i]._bytes[j];
        index++;
      }
    }
    return b;
  }
  
  /**
   * @return the next byte that this instance holds, or <code>-1</code>
   * if this instance holds no more bytes.
   */
  public int read(){
    if(_arrayCount == 0 || _arrayPos >= _arrayCount){
      return -1;
    }
    ByteArray arr = _arrays[_arrayPos];
    if(arr == null) return -1;
    if(arr._pos < arr._limit){
      int b = arr._bytes[arr._pos++] & 0xff;
      if(arr._pos >= arr._limit){
        _arrayPos++;
      }      
      return b;
    }
    return -1;
  }  
  
  /**
   * @param b a byte array that will be filled with the bytes that were read.
   *
   * @return the number of bytes that were read.
   */
  public int read(byte[] b){
    return read(b, 0, b.length);
  }
  
  /**
   * @param b a byte array that will be filled with the bytes that were read.
   * @off the offset from which bytes should be inserted into the given array.
   * @len the number of bytes to fill in the given array, starting from the 
   * given offset.
   * @return the number of bytes that were read.
   */
  public int read(byte[] b, int off, int len){
    int read = 0;
    int total = 0;
    while(len > 0 && _arrayPos < _arrayCount){
      read = _arrays[_arrayPos].get(b, off, len);
      if(read == 0){
        break;
      }
      if(_arrays[_arrayPos]._pos >= _arrays[_arrayPos]._limit){
        _arrayPos++;
      }
      len = len - read;
      off = off + read;
      total += read;
    }    
    return total;
  }
  
  /**
   * @param a <code>ByteBuffer</code>.
   * @return the number of bytes that were read.
   */
  public int read(ByteBuffer buf){
    int read = 0;
    int total = 0;
    int len = buf.remaining();
    while(len > 0 && _arrayPos < _arrayCount){
      read = _arrays[_arrayPos].get(buf, len);
      if(read == 0){
        break;
      }
      if(_arrays[_arrayPos]._pos >= _arrays[_arrayPos]._limit){
        _arrayPos++;
      }
      len = len - read;
      total += read;
    }    
    return total;
    
  }
  
  /**
   * Reads this instance's bytes and transfers them to the given stream.
   *
   * @param out an <code>OutputStream</code>.
   */
  public void read(OutputStream out) throws IOException{
    int read;
    while(_arrayPos <  _arrayCount){
      read = _arrays[_arrayPos].get(out);
      if(read == 0){
        break;
      }
      if(_arrays[_arrayPos]._pos >= _arrays[_arrayPos]._limit){
        _arrayPos++;
      }      
    }
  }  
  
  /**
   * Writes the given bytes to this instance. 
   *
   * @param b the array of bytes to write.
   */
  public void write(byte[] b){
    write(b, 0, b.length);
  }
  
  public void write(int b){
    if(_arrayPos >= _arrayCount)
      increase();
    if(!_arrays[_arrayPos++].put((byte)b)){
      increase();
      _arrays[_arrayPos-1].put((byte)b);      
    }
  }  
  
  /**
   * Writes the given bytes to this instance.
   *
   * @param b the array of bytes to write.
   * @param off the offset from which to start taking the bytes in the given
   * array.
   * @param len the number of bytes to read, starting from the given offset.
   */
  public void write(byte[] b, int off, int len){
    int put = 0;
    while(put < len){
      if(_arrayPos >= _arrayCount){
        increase(); 
      }      
      put = _arrays[_arrayPos].put(b, off, len);
      if(put < len){
        _arrayPos++;
      }
      off += put;
      len -= put;
      put = 0;      
    }
  }
  
  /**
   * Writes the bytes contained in the given buffer to this instance.
   *
   * @param buf a <code>ByteBuffer</code>
   */
  public void write(ByteBuffer buf){
    while(buf.hasRemaining()){
      if(_arrayPos >= _arrayCount){
        increase(); 
      }      
      int len = buf.remaining();
      int put = _arrays[_arrayPos].put(buf);
      if(put < len){
        _arrayPos++;
      }
      put = 0;      
    }    
  }  
  
  /**
   * Skips the number of bytes within this instance. 
   *
   * @return the number of bytes that were actually skipped.
   */
  public long skip(long skip){
    if(_arrayCount == 0){
      return 0;
    }
    long total = 0;
    while(total < skip && _arrayPos < _arrayCount){
      ByteArray arr = _arrays[_arrayPos];
      
      if(arr == null)
        break;      
      
      int size = arr._limit - arr._pos;
      
      if(size <= 0) 
        break;
      
      if(total + size <= skip){
        arr._pos = arr._limit;
        total = total + size;        
        _arrayPos++;      
      }
      else{
        size = (int)(skip - total);
        arr._pos = arr._pos + size;
        total = total + size;
      }
    }
    return total;
  }
  
  private void increase(){
    if(_arrayPos >= _arrays.length){
      ByteArray[] newArray = new ByteArray[_arrays.length+_increment];
      System.arraycopy(_arrays, 0, newArray, 0, _arrays.length);
      _arrays = newArray;
    }
    if(_arrays[_arrayPos] == null){
      _arrays[_arrayPos] = new ByteArray(_capacity);
    }
    _arrayCount++;        
  }  
  
  ///////////////////////// ByteArray ///////////////////////////
  
  protected static class ByteArray{
    protected int _pos;
    protected int _limit;
    protected byte[] _bytes;
    protected ByteArray(int capacity){
      _bytes = new byte[capacity];
    }

    protected boolean put(byte b){
      if(_pos < _bytes.length){
        _bytes[_pos++] = b;
        _limit++;
      }
      return false;
    }
    
    protected int put(byte[] b, int offset, int len){
      if(_pos < _bytes.length){
        if(len <= _bytes.length - _pos){
          System.arraycopy(b, offset, _bytes, _pos, len);
          _pos += len;
          _limit += len;
          return len;
        }
        else{
          System.arraycopy(b, offset, _bytes, _pos, _bytes.length - _pos);
          int put = _bytes.length - _pos; 
          _pos += put;
          _limit += put;          
          return put;
        }
      }
      return 0;
    }
    
    protected int put(ByteBuffer buf){
      if(_pos < _bytes.length){
        if(buf.remaining() <= _bytes.length - _pos){
          int len = buf.remaining();
          buf.get(_bytes, _pos, buf.remaining());
          _pos += len;
          _limit += len;
          return len;
        }
        else{
          buf.get(_bytes, _pos, _bytes.length - _pos);
          int put = _bytes.length - _pos; 
          _pos += put;
          _limit += put;          
          return put;
        }
      }
      return 0;
    }    
    
    protected int get(byte[] b, int offset, int len){
      if(_pos < _limit){
        if(_limit - _pos <= len){
          System.arraycopy(_bytes, _pos, b, offset, _limit - _pos);
          int read = _limit - _pos; 
          _pos += read;
          return read;
        }
        else{
          System.arraycopy(_bytes, _pos, b, offset, len);
          _pos += len;
          return len;
        }
      }
      return 0;
    }
    
    protected int get(ByteBuffer buf, int len){
      if(_pos < _limit){
        if(_limit - _pos <= len){
          buf.put(_bytes, _pos, _limit - _pos);
          int read = _limit - _pos; 
          _pos += read;
          return read;
        }
        else{
          buf.put(_bytes, _pos, len);
          _pos += len;
          return len;
        }
      }
      return 0;
    }    
    
    protected int get(OutputStream out) throws IOException{
      if(_pos >= _limit){
        return 0;
      }
      out.write(_bytes, _pos, _limit);
      int read = _limit - _pos;
      _pos = _pos + read;
      return read;
    }
  }

}
