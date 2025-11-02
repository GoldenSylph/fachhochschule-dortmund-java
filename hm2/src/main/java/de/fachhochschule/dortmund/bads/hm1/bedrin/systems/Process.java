package de.fachhochschule.dortmund.bads.hm1.bedrin.systems;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import de.fachhochschule.dortmund.bads.hm1.bedrin.resources.Resource;
import de.fachhochschule.dortmund.bads.hm1.bedrin.systems.logic.ClockingSimulation;

public class Process {
	public static final int TIMEOUT_SHUTDOWN = 5000; // milliseconds
	
	protected List<Operation> operations;
	
	public Process() {
		this.operations = new ArrayList<>();
	}
	
	public double processDuration() {
		// assuming that the duration of the process is equals to the age of the oldest operation
		final int currentTime = ((ClockingSimulation) Systems.CLOCKING.getLogic()).getCurrentTime();
		final int oldestOperationTime = this.operations.stream()
				.map(e -> e.getCreationTime())
				.min(Integer::compare)
				.orElse(currentTime);
		return currentTime - oldestOperationTime;
	}
	
	public List<Future<Resource>> processOperations() {
		// start all of the agents (resources) concurrently in ExecutorService
		// oversee the the end of the thread pool
		int threadsCount = this.operations.stream().map(Operation::getResourcesCount).reduce(Integer::sum).orElseThrow();
		ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
		List<Future<Resource>> futures = null;
		try {
			futures = executor.invokeAll(this.operations.stream().map(e -> {
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
	
	public void addOperation(Operation operation) {
		this.operations.add(operation);
	}
	
	public int getOperationsCount() {
		return this.operations.size();
	}
	
	public Operation getOperation(int index) {
		return this.operations.get(index);
	}
}
