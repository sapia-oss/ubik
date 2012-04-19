package org.sapia.ubik.util.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sapia.ubik.util.Strings;
import org.sapia.ubik.util.cli.Opt.Type;

/**
 * Models a command entered on the command line.
 * 
 * @author yduchesne
 *
 */
public class Cmd {
	
	private List<Opt> 			 opts       = new ArrayList<Opt>();
	private Map<String, Opt> optsByName = new HashMap<String, Opt>();
	
	private Cmd(List<Opt> opts) {
		this.opts.addAll(opts);
		for (Opt o : opts) {
			optsByName.put(o.getName(), o);
		}
  }
	
	/**
	 * @param name the name of the {@link Opt} instance to return.
	 * @return the {@link Opt} for the given name, or <code>null</code> if no such instance is found.
	 */
	public Opt getOpt(String name) {
		return optsByName.get(name);
	}
	
	/**
	 * @return this instance's {@link List} of options.
	 */
	public List<Opt> getOpts() {
	  return Collections.unmodifiableList(opts);
  }
	
	/**
	 * Returns a copy of this instance, but only with the options of the given type.
	 * @param type the {@link Type} of option to return.
	 * @return a new {@link Cmd} instance, with the desired options.
	 */
	public Cmd filter(Type type) {
		List<Opt> filtered = new ArrayList<Opt>();
		for (Opt o : this.opts) {
			if(o.getType().equals(type)) {
				filtered.add(o);
			}
		}
		return new Cmd(filtered);
	}
	
	/**
	 * @param name the name of the switch to check for.
	 * @return <code>true</code> if this instance contains an {@link Opt} instance with the given name
	 * and of type {@link Type#SWITCH}.
	 */
	public boolean hasSwitch(String name) {
		Opt opt = optsByName.get(name);
		if (opt == null) {
			return false;
		} 
		return opt.getType() == Type.SWITCH;
	}
	
	public static Cmd fromArgs(String[] args) {
		
		List<Opt> opts = new ArrayList<Opt>();
		
		for (int i = 0; i < args.length; i++) {
			String argName = args[i];
			if (argName.startsWith("-")) {
				if(argName.length() == 1) {
					continue;
				}
				if (i + 1 < args.length) {
					String argValue = args[i + 1];
					if(!argValue.startsWith("-")) {
						opts.add(new Opt(Opt.Type.SWITCH, argName.substring(1), argValue));
						i++;
					} else {
						opts.add(new Opt(Opt.Type.SWITCH, argName.substring(1), null));
					}
				} else {
					opts.add(new Opt(Opt.Type.SWITCH, argName.substring(1), null));
				}
			} else {
				opts.add(new Opt(Opt.Type.ARG, argName, null));
			}
		}
		
		return new Cmd(opts);
		
	} 
	
	@Override
	public String toString() {
	  return Strings.toStringFor(this, "opts", opts, "optsByName", optsByName);
	}

}
