package org.sapia.ubik.util;

/**
 * A utility for performing evaluations and offering null safety.
 * 
 * @author yduchesne
 *
 */
public class Equals {
	
  private Equals() {
		
  }

  /**
   * Performs an equality evaluation by taking into account <code>null</code> references.
   * 
   * @param o1 an {@link Object}.
   * @param o2 another {@link Object}.
   * @return <code>true</code> if both objects are equal, as per their {@link Object#equals(Object)} 
   * method, or if they are both <code>null</code>.
   */
  public static boolean isTrue(Object o1, Object o2) {
	  if (o1 == null && o2 == null) {
		  return true;
	  } 
	  if (o1 == null && o2 != null) {
		  return false;
	  }
	  if (o1 != null && o2 == null) {
		  return false;
	  }
	  
	  return o1.equals(o2);
  }
}
