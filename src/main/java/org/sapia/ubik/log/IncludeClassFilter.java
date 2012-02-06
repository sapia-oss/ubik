package org.sapia.ubik.log;

import java.util.HashSet;
import java.util.Set;

public class IncludeClassFilter implements LogFilter {
  
  private Set<String> classNames = new HashSet<String>();

  public IncludeClassFilter addClass(Class<?>... classes) {
    for(Class<?> c : classes) {
      classNames.add(c.getName());
    }
    return this;
  }
  
  @Override
  public boolean accepts(String source) {
    if(classNames.contains(source)) {
      return true;
    }
    return false;
  }
  
}
