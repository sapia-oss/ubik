package org.sapia.ubik.ioc.spring;

import javax.naming.NamingException;

import org.sapia.ubik.ioc.NamingService;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Checks for the {@link Export} annotation on beans, and performs remoting
 * accordingly.
 * 
 * @author yduchesne
 * 
 */
public class BeanExporterPostProcessor implements BeanPostProcessor {

  NamingService namingService;

  @Override
  public Object postProcessAfterInitialization(Object bean, String name) throws BeansException {
    if (bean instanceof NamingService) {
      namingService = (NamingService) bean;
    }

    else if (bean instanceof Export || bean.getClass().isAnnotationPresent(Export.class)) {
      Export annot = bean.getClass().getAnnotation(Export.class);
      if (annot != null && annot.name() != null) {

      } else {
        try {
          namingService.bind(name, bean);
        } catch (NamingException e) {
          throw new FatalBeanException(String.format("Could not export bean %s", name), e);
        }
      }
    }
    return bean;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String arg1) throws BeansException {
    return bean;
  }

}
