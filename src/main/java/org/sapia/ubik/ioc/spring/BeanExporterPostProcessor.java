package org.sapia.ubik.ioc.spring;

import javax.naming.NamingException;

import org.sapia.ubik.ioc.NamingService;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class BeanExporterPostProcessor implements BeanPostProcessor{
  
  NamingService _namingService;
  
  @Override
  public Object postProcessAfterInitialization(Object bean, String name)
      throws BeansException {
    if(bean instanceof NamingService){
      _namingService = (NamingService)bean;
    }
    
    else if(bean instanceof Export || bean.getClass().isAnnotationPresent(Export.class)){
      Export annot = bean.getClass().getAnnotation(Export.class);
      if(annot != null && annot.name() != null){
        
      }
      else{
        try{
          _namingService.bind(name, bean);
        }catch(NamingException e){
          throw new FatalBeanException(String.format("Could not export bean %s", name), e);
        }
      }
    }
    return bean;
  }
  
  @Override
  public Object postProcessBeforeInitialization(Object bean, String arg1)
      throws BeansException {
    return bean;
  }

}
