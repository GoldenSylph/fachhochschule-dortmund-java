package de.fachhochschule.dortmund.bads.hm1.bedrin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class Process {
	public static final int TIMEOUT_SHUTDOWN = 5000; // milliseconds
	
	public abstract List<? extends Operation> getOperations();

	public double processDuration() {
		// assuming that the duration of the process is equals to the age of the oldest operation
		final long currentTime = new java.util.Date().getTime();
		final long oldestOperationTime = getOperations().stream()
				.map(e -> e.getCreationTime().getTime())
				.min(Double::compare)
				.orElse(currentTime);
		return (currentTime - oldestOperationTime) / 1000.0;
	}
	
	public List<Future<Resource>> processResources() {
		// load programs into operations resources concurrently
		// start all of the agents (resources) concurrently in ExecutorService
		// oversee the the end of the thread pool
		List<? extends Operation> operations = getOperations();
		int threadsCount = operations.stream().map(Operation::getResourcesCount).reduce(Integer::sum).orElseThrow();
		ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
		List<Future<Resource>> futures = null;
		try {
			futures = executor.invokeAll(operations.stream().map(e -> {
				List<Resource> res = new ArrayList<>();
				for (int i = 0; i < e.getResourcesCount(); i++) {
					res.add(e.getResource(i));
				}
				return res;
			}).flatMap(List::stream).toList());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			executor.shutdown();
			try {
			    if (!executor.awaitTermination(TIMEOUT_SHUTDOWN, TimeUnit.MILLISECONDS)) {
			    	executor.shutdownNow();
			    } 
			} catch (InterruptedException e) {
				executor.shutdownNow();
			}
		}
		return futures;
	}
}
