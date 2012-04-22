package org.sapia.ubik.rmi.naming.remote;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

/**
 * Use this class to perform JNDI lookups in a convenient way.
 *  
 * @author yduchesne
 *
 */
public class Lookup {

	private Context context;
	
	private Lookup(Context context) {
		this.context = context;
  }
	
	/**
	 * Uses the given {@link JNDIContextBuilder} to build a {@link Context} and create
	 * an instance of this class that will wrap that context.
	 * 
	 * @param builder a {@link JNDIContextBuilder}.
	 * @return a new instance of this class.
	 * @throws NamingException of an JNDI-related problem occurs.
	 */
	public static Lookup with(JNDIContextBuilder builder) throws NamingException {
		return with(builder.build());
	}
	
	/**
	 * @param ctx a {@link Context}.
	 * @return a new instance of this class - which will use the given {@link Context}.
	 */
	public static Lookup with(Context ctx) {
		return new Lookup(ctx);
	}

	/**
	 * 
	 * @param type the type of the object to lookup.
	 * @param name the name of the object to lookup.
	 * @return the object corresponding to the given name.
	 * @throws NameNotFoundException if the object with the given name was not found.
	 * @throws NamingException if a lower-level JNDI problem occurs.
	 */
	public <T> T forName(Class<T> type, String name) throws NameNotFoundException, NamingException {
		return type.cast(context.lookup(name));
	}
}
