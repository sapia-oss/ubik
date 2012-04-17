package org.sapia.ubik.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Abstracts system time, convenient for unit testing. 
 * 
 * @author yduchesne
 *
 */
public interface Clock {
	
	/**
	 * Implements the {@link Clock} interface over the {@link System} class.
	 * 
	 * @see System#currentTimeMillis()
	 */
	public static class SystemClock implements Clock {
		
		private static SystemClock instance = new SystemClock();
		
		private SystemClock() {}
		
		/**
		 * Delegates to {@link System#currentTimeMillis()}.
		 */
		@Override
		public long currentTimeMillis() {
			return System.currentTimeMillis();
		}
		
		/**
		 * @return the {@link SystemClock} singleton.
		 */
		public static SystemClock getInstance() {
	    return instance;
    }
		
	}
	
	// ---------------------------------------------------------------------------
	
	/**
	 * A mutable {@link Clock} class: an instance of this class can have its time
	 * explicitely set. Use this class for testing purposes only.
	 *
	 */
	public static class MutableClock implements Clock {
		
		private AtomicLong currentTime = new AtomicLong();
		
		public void setCurrentTimeMillis(long currentTime) {
	    this.currentTime.set(currentTime);
    }
		
		public void setCurrentTime(long currentTime, TimeUnit timeunit) {
	    this.currentTime.set(TimeUnit.MILLISECONDS.convert(currentTime, timeunit));
    }
		
		public void increaseCurrentTimeMillis(long amount) {
			currentTime.addAndGet(amount);
		}
		
		public void decreaseCurrentTimeMillis(long amount) {
			currentTime.addAndGet(-amount);
		}		

		@Override
		public long currentTimeMillis() {
		  return currentTime.get();
		}
		
		public static MutableClock getInstance() {
			return new MutableClock();
		}
	}
	
	// ===========================================================================
	
	/**
	 * @return the current time, in millis.
	 */
	public long currentTimeMillis();
}
