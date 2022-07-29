/*
 * Copyright 2009-2021 Contributors (see credits.txt)
 *
 * This file is part of jEveAssets.
 *
 * jEveAssets is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * jEveAssets is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jEveAssets; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package de.ronnywalter.eve.jobs;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class ThreadWorker {

	private static final int MAIN_THREADS = 100;
	private static final int SUB_THREADS = 100;
	private static final ExecutorService RETURN_THREAD_POOL = Executors.newFixedThreadPool(SUB_THREADS);
	//private static final ExecutorService RETURN_THREAD_POOL = Executors.newCachedThreadPool();

	public static void start(Collection<? extends Runnable> updaters) {
		start(updaters, true);
	}

	public static void start(Collection<? extends Runnable> updaters, int start, int end) {
		start(updaters, true, start, end);
	}

	public static void start(Collection<? extends Runnable> updaters, boolean updateProgress) {
		start(updaters, updateProgress, 0, 100);
	}

	public static void start(Collection<? extends Runnable> updaters, boolean updateProgress, int start, int end) {
		ExecutorService threadPool = Executors.newFixedThreadPool(MAIN_THREADS);
		try {
			log.debug("Starting " + updaters.size() + " main threads");
			List<Future<?>> futures = new ArrayList<Future<?>>();
			for (Runnable runnable : updaters) {
				futures.add(threadPool.submit(runnable));
			}
			threadPool.shutdown();
			while (!threadPool.awaitTermination(500, TimeUnit.MICROSECONDS)) {
				log.debug("still waiting....");
			}
			//Get errors (if any)
			for (Future<?> future : futures) {
				future.get();
			}
		} catch (InterruptedException ex) {
			//No problem
		} catch (ExecutionException ex) {
			throwExecutionException(ex);
		}
	}

	public static <K> List<Future<K>> startReturn(Collection<? extends Callable<K>> updaters) throws InterruptedException {
		return startReturn(updaters, false);
	}
	
	public static <K> List<Future<K>> startReturn(Collection<? extends Callable<K>> updaters, boolean updateProgress) throws InterruptedException {
		return startReturn(updaters, updateProgress, 0, 100);
	}

	public static <K> List<Future<K>> startReturn(Collection<? extends Callable<K>> updaters, boolean updateProgress, int start, int end) throws InterruptedException {
		log.info("Starting " + updaters.size() + " sub threads");
		List<Future<K>> futures = new ArrayList<Future<K>>();
		for (Callable<K> callable : updaters) {
			futures.add(RETURN_THREAD_POOL.submit(callable));
		}
		int done = 0;
		while (done < futures.size()) {
			done = 0;
			for (Future<?> future : futures) {
				if (future.isDone()) {
					done++;
				}
			}
			Thread.sleep(500);
		}
		return futures;
	}

	public static class TaskCancelledException extends RuntimeException {
		
	}

	private static <E extends Exception> void throwExecutionException(ExecutionException ex) throws E {
		throwExecutionException(null, ex);
	}

	public static <E extends Exception> void throwExecutionException(Class<E> clazz, ExecutionException ex) throws E {
		Throwable cause = ex.getCause();
		if (clazz != null && cause.getClass().equals(clazz) ) {
			throw clazz.cast(cause);
		} else if (cause instanceof Error) {
			throw (Error) cause;
		} else if (cause instanceof RuntimeException) {
			throw (RuntimeException) cause;
		} else {
			throw new RuntimeException(cause);
		}
	}
}
