package org.sapia.ubik.log;

import java.util.HashSet;
import java.util.Set;

/**
 * A {@link LogFilter} that filters according to inclusion and exclusion patterns. 
 * The performance of this filter is sub-optimal, since it does string matching for every
 * pattern it holds. Use it only for debugging purposes.
 * 
 * @author yduchesne
 *
 */
public class LogNameFilter implements LogFilter {
	
	private Set<String> inclusionPatterns = new HashSet<String>();
	private Set<String> exclusionPatterns = new HashSet<String>();
	
	public LogNameFilter exclude(String...pattern) {
		for(String p : pattern) {
			exclusionPatterns.add(p);
		}
		return this;
	}
	
	public LogNameFilter include(String...pattern) {
		for(String p : pattern) {
			inclusionPatterns.add(p);
		}
		return this;
	}
	
	@Override
	public boolean accepts(String source) {
		if(exclusionPatterns.size() > 0 && inclusionPatterns.size() > 0) {
			return contains(source, inclusionPatterns) && !contains(source, exclusionPatterns);
	  }	else if(exclusionPatterns.size() == 0) {
			return contains(source, inclusionPatterns);
		} else {
			return !contains(source, exclusionPatterns);
		}
	}
	
	private static boolean contains(String toCheck, Set<String> patterns) {
		for(String pattern : patterns) {
			if(toCheck.contains(pattern)) {
				return true;
			}
		}
		return false;
	}

}
