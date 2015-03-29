package org.sapia.ubik.rmi.server.transport.http;

import java.rmi.Remote;
import java.util.Iterator;

public class RemoteIterator implements Remote, Iterator<Integer> {
  
  private Iterator<Integer> delegate;
  
  public RemoteIterator(Iterator<Integer> delegate) {  
    this.delegate = delegate;
  }
  
  @Override
  public boolean hasNext() {
    return delegate.hasNext();
  }
  
  public Integer next() {
    return delegate.next();
  }
  
  public void remove() {
    delegate.remove();
  }

}
