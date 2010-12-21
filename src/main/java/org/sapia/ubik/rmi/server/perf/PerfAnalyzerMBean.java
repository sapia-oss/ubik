package org.sapia.ubik.rmi.server.perf;

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

public class PerfAnalyzerMBean implements DynamicMBean{
  
  public MBeanInfo getMBeanInfo() {
    return buildMBeanInfo();
  }
  
  public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
    if(attribute.equals("Enabled") || attribute.equals("IsEnabled")){
      return new Boolean(PerfAnalyzer.getInstance().isEnabled());
    }
    else{
      Topic top = PerfAnalyzer.getInstance().getTopic(attribute);
      if(top == null){
        throw new AttributeNotFoundException(attribute);
      }
      return new Double(top.duration());
    }
  }
  
  public AttributeList getAttributes(String[] attributes) {
    AttributeList lst = new AttributeList(attributes.length);
    for(int i = 0; i < attributes.length; i++){
      if(attributes[i].equals("Enabled") || attributes[i].equals("IsEnabled")){
        lst.add(new Attribute(attributes[i], new Boolean(PerfAnalyzer.getInstance().isEnabled())));
      }  
      else{
        Topic top = PerfAnalyzer.getInstance().getTopic(attributes[i]);
        if(top == null){
          lst.add(new Attribute(attributes[i], new Double(0)));  
        } 
        else{
          lst.add(new Attribute(attributes[i], new Double(top.duration())));
        }  
      }
    }
    return lst;
  }
  
  public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
    if(actionName.equals("Enable")){
      PerfAnalyzer.getInstance().enable();
    }
    else if(actionName.equals("Disable")){
      PerfAnalyzer.getInstance().disable();
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
    Collection<Topic> topics = PerfAnalyzer.getInstance().getTopics();
    MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[topics.size()+1];
    
    attributes[0] = new MBeanAttributeInfo(
        "Enabled",
        Boolean.class.getName(),
        "Indicates if performance stats are enabled",
        true,
        false,
        true);

    int count = 1;
    for(Topic tp : topics){
      MBeanAttributeInfo attr = getAttributeFor(tp);
      attributes[count] = attr;
      count++;
    }
    
    MBeanOperationInfo[] ops = new MBeanOperationInfo[2];
    
    ops[0] = new MBeanOperationInfo(
        "Enable",
        "Enables performance stats",
        new MBeanParameterInfo[0],
        null,
        MBeanOperationInfo.ACTION
        );
    
    ops[1] = new MBeanOperationInfo(
        "Disable",
        "Disables performance stats",
        new MBeanParameterInfo[0],
        null,
        MBeanOperationInfo.ACTION
        );    
    
    return new MBeanInfo(
        PerfAnalyzer.class.getName(),
        "Performance Analyzer",
        attributes,
        new MBeanConstructorInfo[0],
        new MBeanOperationInfo[0],
        new MBeanNotificationInfo[0]);
  }
  
  private MBeanAttributeInfo getAttributeFor(Topic top){
    MBeanAttributeInfo info = new MBeanAttributeInfo(
       top.getName(),
       Double.class.getName(),
       null,
       true,
       false,
       false
    );
    return info;
  }
  
}
