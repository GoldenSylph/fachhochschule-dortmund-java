package de.fachhochschule.dortmund.bads.hm1.bedrin.systems;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.hm1.bedrin.resources.Resource;
import de.fachhochschule.dortmund.bads.hm1.bedrin.systems.logic.ClockingSimulation;

public class Process {
	private static final Logger LOGGER = LogManager.getLogger();
	
	public static final int TIMEOUT_SHUTDOWN = 5000; // milliseconds
	
	protected List<Operation> operations;
	
	public Process() {
		this.operations = new ArrayList<>();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Process created with empty operations list");
		}
	}
	
	public double processDuration() {
		// assuming that the duration of the process is equals to the age of the oldest operation
		final int currentTime = ((ClockingSimulation) Systems.CLOCKING.getLogic()).getCurrentTime();
		final int oldestOperationTime = this.operations.stream()
				.map(e -> e.getCreationTime())
				.min(Integer::compare)
				.orElse(currentTime);
		
		double duration = currentTime - oldestOperationTime;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Process duration calculated: {} (current time: {}, oldest operation time: {})", 
						duration, currentTime, oldestOperationTime);
		}
		return duration;
	}
	
	public List<Future<Resource>> processOperations() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Starting process operations with {} operations", this.operations.size());
		}
		
		// start all of the agents (resources) concurrently in ExecutorService
		// oversee the the end of the thread pool
		int threadsCount = this.operations.stream().map(Operation::getResourcesCount).reduce(Integer::sum).orElseThrow();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Creating thread pool with {} threads for process operations", threadsCount);
		}
		
		ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
		List<Future<Resource>> futures = null;
		
		long startTime = System.currentTimeMillis();
		
		try {
			List<Resource> allResources = this.operations.stream().map(e -> {
				List<Resource> res = new ArrayList<>();
				for (int i = 0; i < e.getResourcesCount(); i++) {
					res.add(e.getResource(i));
				}
				return res;
			}).flatMap(List::stream).toList();
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Submitting {} resources to executor service", allResources.size());
			}
			
			futures = executor.invokeAll(allResources);
			
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Successfully submitted all operations to executor service");
			}
			
		} catch (InterruptedException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Process operations interrupted: {}", e.getMessage(), e);
			}
			Thread.currentThread().interrupt(); // Restore interrupted status
		} finally {
			shutdownExecutor(executor, startTime);
		}
		
		return futures;
	}
	
	private void shutdownExecutor(ExecutorService executor, long startTime) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Shutting down executor service");
		}
		
		executor.shutdown();
		
		try {
		    if (!executor.awaitTermination(TIMEOUT_SHUTDOWN, TimeUnit.MILLISECONDS)) {
		    	if (LOGGER.isWarnEnabled()) {
		    		LOGGER.warn("Executor did not terminate within {} ms, forcing shutdown", TIMEOUT_SHUTDOWN);
		    	}
		    	executor.shutdownNow();
		    	
		    	if (!executor.awaitTermination(TIMEOUT_SHUTDOWN, TimeUnit.MILLISECONDS)) {
		    		if (LOGGER.isErrorEnabled()) {
		    			LOGGER.error("Executor did not terminate even after forced shutdown");
		    		}
		    	}
		    } else {
		    	long executionTime = System.currentTimeMillis() - startTime;
		    	if (LOGGER.isInfoEnabled()) {
		    		LOGGER.info("Process operations completed successfully in {}ms", executionTime);
		    	}
		    }
		} catch (InterruptedException e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Interrupted while waiting for executor termination: {}", e.getMessage());
			}
			executor.shutdownNow();
			Thread.currentThread().interrupt(); // Restore interrupted status
		}
	}
	
	public void addOperation(Operation operation) {
		this.operations.add(operation);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Added operation: {}. Total operations: {}", operation, this.operations.size());
		}
	}
	
	public int getOperationsCount() {
		int count = this.operations.size();
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Operations count requested: {}", count);
		}
		return count;
	}
	
	public Operation getOperation(int index) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Getting operation at index: {}", index);
		}
		return this.operations.get(index);
	}
}