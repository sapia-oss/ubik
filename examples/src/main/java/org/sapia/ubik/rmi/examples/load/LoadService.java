package org.sapia.ubik.rmi.examples.load;

public interface LoadService {
	
	public static final String JNDI_NAME = "LoadService";

	public void perform();
}
