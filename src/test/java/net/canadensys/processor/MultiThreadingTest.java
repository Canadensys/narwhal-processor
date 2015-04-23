package net.canadensys.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.canadensys.processor.numeric.NumericPairDataProcessor;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Testing multi-threading usage of ProcessingResult and NumericPairDataProcessor
 * 
 * @author canadensys
 *         Based on http://garygregory.wordpress.com/2011/09/09/multi-threaded-unit-testing/
 */
public class MultiThreadingTest {

	private static final int NUMBER_OF_DATA = 1000;

	/**
	 * Generate a list of random alphabetic strings
	 * 
	 * @param size
	 * @param strLength
	 * @return
	 */
	public List<String> newRandomDataList(int size, int strLength) {
		List<String> dataList = new ArrayList<String>();
		for (int i = 0; i < size; i++) {
			dataList.add(RandomStringUtils.randomAlphabetic(strLength));
		}
		return dataList;
	}

	@Test
	public void test1Thread() throws InterruptedException, ExecutionException {
		testThread(1);
	}

	@Test
	public void test2Threads() throws InterruptedException, ExecutionException {
		testThread(2);
	}

	@Test
	public void test4Threads() throws InterruptedException, ExecutionException {
		testThread(4);
	}

	@Test
	public void test8Threads() throws InterruptedException, ExecutionException {
		testThread(8);
	}

	@Test
	public void test16Threads() throws InterruptedException, ExecutionException {
		testThread(16);
	}

	@Test
	public void test32Threads() throws InterruptedException, ExecutionException {
		testThread(32);
	}

	/**
	 * Create and run testing thread(s)
	 * 
	 * @param threadCount
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private void testThread(final int threadCount) throws InterruptedException, ExecutionException {
		ProcessingResult pr = new ProcessingResult();
		NumericPairDataProcessor nm = new NumericPairDataProcessor();

		List<Callable<Integer>> tasks = new ArrayList<Callable<Integer>>();
		for (int i = 0; i < threadCount; i++) {
			// new set of wrong data(alphabetic instead of numeric to generate errors)
			// for each thread but same NumericPairDataProcessor and ProcessingResult
			final NumPairJob npt = new NumPairJob(nm, pr, newRandomDataList(NUMBER_OF_DATA, 7), newRandomDataList(NUMBER_OF_DATA, 6));
			Callable<Integer> task = new Callable<Integer>() {
				@Override
				public Integer call() {
					npt.run();
					return npt.pr.getErrorList().size();
				}
			};
			tasks.add(task);
		}

		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		// call all threads and wait for completion
		List<Future<Integer>> futures = executorService.invokeAll(tasks);

		// Validate
		Assert.assertEquals(futures.size(), threadCount);
		// x2 because they are numeric pairs
		Assert.assertEquals(NUMBER_OF_DATA * threadCount * 2, pr.getErrorList().size());
	}

	/**
	 * Inner class to wrap a NumPair processing job
	 * 
	 * @author canadensys
	 * 
	 */
	private class NumPairJob {
		private NumericPairDataProcessor processor;
		private ProcessingResult pr;

		private List<String> dataList1;
		private List<String> dataList2;

		public NumPairJob(NumericPairDataProcessor processor, ProcessingResult pr, List<String> dataList1, List<String> dataList2) {
			this.processor = processor;
			this.pr = pr;
			this.dataList1 = dataList1;
			this.dataList2 = dataList2;
		}

		public void run() {
			for (int i = 0; i < dataList1.size(); i++) {
				processor.process(dataList1.get(i), dataList2.get(i), Double.class, pr);
			}
		}
	}

}
