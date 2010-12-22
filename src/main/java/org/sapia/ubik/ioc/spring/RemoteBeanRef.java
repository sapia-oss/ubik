package org.sapia.ubik.ioc.spring;

/**
 * This bean acts as a reference to a remote service.
 * 
 * @author yduchesne
 *
 */
public class RemoteBeanRef{
  
  private String name;
  private Class<?>[] interfaces;
  
  public void setName(String name) {
    this.name = name;
  }
  
  /**
   * @return the name of the remote service (as it was bound to
   * the JNDI server).
   */
  public String getName() {
    return name;
  }

  /**
   * @param intf a single fully-qualified interface name.
   */
  public void setInterface(String intf) {
    setInterfaces(new String[]{intf});
  }

  /**
   * @param intfs an array of fully-qualified interface names.
   */
  public void setInterfaces(String[] intfs) {
    interfaces = new Class[intfs.length];
    for(int i = 0; i < intfs.length; i++){
      try{
        interfaces[i] = Class.forName(intfs[i]);
      }catch(ClassNotFoundException e){
        throw new IllegalArgumentException(String.format("Interface not found %s", intfs[i]), e);
      }
    }
  }

  /**
   * @return the array of interfaces that this instance holds.
   */
  public Class<?>[] getInterfaces() {
    return interfaces;
  }

}
