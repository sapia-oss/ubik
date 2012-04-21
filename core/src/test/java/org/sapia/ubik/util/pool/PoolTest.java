package org.sapia.ubik.util.pool;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

public class PoolTest {
	
	private ExecutorService threads = Executors.newCachedThreadPool();
	
	private TestPool pool;
	
	@Before
  public void setUp() {
		pool = new TestPool(5);
		pool.setDefaultAcquireTimeout(0);
	}


	@Test
	public void testAcquire() throws Exception {
		
		final CountDownLatch latch = new CountDownLatch(5);
		for(int i = 0; i < 5; i++) {
			threads.execute(new Runnable() {
				@Override
				public void run() {
					try {
						latch.countDown();
						String s = pool.acquire();
						Thread.sleep(1000);
						pool.release(s);
					} catch (InterruptedException e) {
						
					}
				}
			});	
		}

		long start = System.currentTimeMillis();
		latch.await(1000, TimeUnit.MILLISECONDS);
		pool.setDefaultAcquireTimeout(1000);
		pool.acquire();
		assertTrue("Object should not have been leased immediately", System.currentTimeMillis() - start >= 500);
	}

	@Test(expected=NoObjectAvailableException.class)
	public void testAcquireWithTimeout() throws Exception {
		final CountDownLatch latch = new CountDownLatch(5);
		for(int i = 0; i < 5; i++) {
			threads.execute(new Runnable() {
				@Override
				public void run() {
					try {
						latch.countDown();
						String s = pool.acquire();
						Thread.sleep(1000);
						pool.release(s);
					} catch (InterruptedException e) {
						
					}
				}
			});	
		}
		
		latch.await();
		pool.acquire(10);
	}

	@Test
	public void testRelease() throws Exception {
		int borrowedCount = pool.getBorrowedCount();
		String s = pool.acquire();
		assertEquals(borrowedCount + 1, pool.getBorrowedCount());
		pool.release(s);
		assertEquals(borrowedCount, pool.getBorrowedCount());
		assertEquals(borrowedCount + 1, pool.getAvailableCount());
	}


	@Test
	public void testGetCreatedCount() throws Exception {
		String s = pool.acquire();
		assertEquals(1, pool.getCreatedCount());
		pool.release(s);
		assertEquals(1, pool.getCreatedCount());		
	}

	@Test
	public void testShrinkTo() throws Exception {
		List<String> borrowed = new ArrayList<String>();
		for(int i = 0; i < 5; i++) {
			borrowed.add(pool.acquire());
		}
		for(String s : borrowed) {
			pool.release(s);
		}
		pool.shrinkTo(0);
		assertEquals(0, pool.getAvailableCount());
		assertEquals(0, pool.getCreatedCount());
	}

	@Test
	public void testClear() throws Exception {
		List<String> borrowed = new ArrayList<String>();
		for(int i = 0; i < 5; i++) {
			borrowed.add(pool.acquire());
		}
		for(String s : borrowed) {
			pool.release(s);
		}
		pool.clear();
		assertEquals(0, pool.getAvailableCount());
		assertEquals(0, pool.getCreatedCount());
	}

	@Test
	public void testInvalidate() throws Exception {
		String s = pool.acquire();
		assertEquals(1, pool.getCreatedCount());
		pool.invalidate(s);
		assertEquals(0, pool.getCreatedCount());
	}

	@Test
	public void testFill() throws Exception {
		pool.fill(5);
		assertEquals(5, pool.getAvailableCount());
	}
	
	@Test
	public void testFillBeyondMaxSize() throws Exception {
		pool.fill(10);
		assertEquals(5, pool.getAvailableCount());
	}	

	
	@Test
	public void testAcquireCreateWithMaxCreatedCountReached() throws Exception {
		pool.fill(5);
		List<String> borrowed = new ArrayList<String>();
		for(int i = 0; i < 5; i++) {
			borrowed.add(pool.acquire());
		}
		assertNull(pool.acquireCreate());
	}

	@Test
	public void testCleanup() throws Exception {
		pool.fill(1);
		pool.clear();
		assertEquals(1, pool.cleanupCount);
	}

	@Test
	public void testOnAcquire() throws Exception {
		pool.acquire();
		assertEquals(1, pool.onAcquireCount);
	}

	@Test
	public void testOnRelease() throws Exception {
		String s = pool.acquire();
		pool.release(s);
		assertEquals(1, pool.onReleaseCount);
	}

	
	public static class TestPool extends Pool<String> {
		
		int cleanupCount;
		int onAcquireCount;
		int onReleaseCount;
		
		public TestPool(int maxSize) {
			super(maxSize);
    }
		
		@Override
		protected String doNewObject() throws Exception {
		  return "pooled-"+super.getCreatedCount();
		}
		
		@Override
		protected String onAcquire(String o) throws Exception {
			onAcquireCount++;
			return o;
		}
		
		@Override
		protected void onRelease(String o) {
			onReleaseCount++;
		}
		
		@Override
		protected void cleanup(String pooled) {
			cleanupCount++;
		}
		
	}
}
