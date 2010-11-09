package org.sapia.ubik.rmi.examples.time;

import org.sapia.ubik.rmi.Remote;

@Remote(interfaces={TimeServiceIF.class})
public class AnnotatedTimeService extends TimeServiceImpl{
  
}
