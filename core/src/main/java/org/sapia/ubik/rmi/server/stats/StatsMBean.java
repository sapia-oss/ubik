package org.sapia.ubik.rmi.server.stats;

import java.util.Collection;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;

public class StatsMBean implements DynamicMBean{
  
  public MBeanInfo getMBeanInfo() {
    return buildMBeanInfo();
  }
  
  public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
    if(attribute.equals("Enabled") || attribute.equals("IsEnabled")){
      return new Boolean(Stats.getInstance().isEnabled());
    } 
    else {
      int dotIndex = attribute.lastIndexOf('.');
      if(dotIndex <= 0) {
        throw new AttributeNotFoundException("Invalid attribute name, expected <sourceName>.<statName>, got : " + attribute);
      }
      
      String sourceName = attribute.substring(0, dotIndex);
      String statName   = attribute.substring(dotIndex + 1);

      try {
        Statistic stat = Stats.getInstance().getStatisticFor(sourceName, statName);
        if(statName.equalsIgnoreCase("Enabled") || statName.equalsIgnoreCase("IsEnabled")){
          return new Boolean(stat.enabled);
        } else {
          return new Double(stat.getValue());
        }
      } catch (IllegalArgumentException e) {
        throw new AttributeNotFoundException(e.getMessage());
      }
    }
  }
  
  public AttributeList getAttributes(String[] attributes) {
    AttributeList lst = new AttributeList(attributes.length);
    
    
    for(String attrName : attributes){
      
      if(attrName.equals("Enabled") || attrName.equals("IsEnabled")){
        lst.add(new Attribute(attrName, new Boolean(Stats.getInstance().isEnabled())));
      } else {
        int dotIndex = attrName.lastIndexOf('.');
        if(dotIndex <= 0) {
          throw new IllegalArgumentException("Invalid attribute name, expected <sourceName>.<statName>, got : " + attrName);
        }
        String sourceName = attrName.substring(0, dotIndex);
        String statName   = attrName.substring(dotIndex + 1);

        Statistic stat = Stats.getInstance().getStatisticFor(sourceName, statName);
        lst.add(new Attribute(attrName, new Double(stat.getValue())));
      }
    }
    return lst;
  }
  
  public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
    if(actionName.equals("Enable")){
      Stats.getInstance().enable();
    }
    else if(actionName.equals("Disable")){
      Stats.getInstance().disable();
    }    
    return null;
  }
 
  public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
  }
  
  public AttributeList setAttributes(AttributeList attributes) {
    String[] attrNames = new String[attributes.size()];
    for(int i = 0; i < attributes.size(); i++){
      attrNames[i] = ((Attribute)attributes.get(i)).getName();
    }
    return getAttributes(attrNames);
  }
  
  private MBeanInfo buildMBeanInfo(){
    Collection<StatCapable> stats = Stats.getInstance().getStatistics();
    MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[stats.size()+1];
    
    attributes[0] = new MBeanAttributeInfo(
        "Enabled",
        Boolean.class.getName(),
        "Indicates if statistic generation is enabled",
        true,
        false,
        true);

    int count = 1;
    for(StatCapable stat : stats){
      MBeanAttributeInfo attr = getAttributeFor(stat);
      attributes[count] = attr;
      count++;
    }
    
    MBeanOperationInfo[] ops = new MBeanOperationInfo[2];
    
    ops[0] = new MBeanOperationInfo(
        "Enable",
        "Enables stats",
        new MBeanParameterInfo[0],
        "Enables statistic generation",
        MBeanOperationInfo.ACTION
        );
    
    ops[1] = new MBeanOperationInfo(
        "Disable",
        "Disables stats",
        new MBeanParameterInfo[0],
        "Disables statistic generation",
        MBeanOperationInfo.ACTION
        );    
    
    return new MBeanInfo(
        Stats.class.getName(),
        "Statistics Manager",
        attributes,
        new MBeanConstructorInfo[0],
        new MBeanOperationInfo[0],
        new MBeanNotificationInfo[0]);
  }
  
  private MBeanAttributeInfo getAttributeFor(StatCapable stat){
    MBeanAttributeInfo info = new MBeanAttributeInfo(
       stat.getSource() + "." + stat.getName(),
       Double.class.getName(),
       stat.getDescription(),
       true,
       false,
       false
    );
    return info;
  }
  
}
