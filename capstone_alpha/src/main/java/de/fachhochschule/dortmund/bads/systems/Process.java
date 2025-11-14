package de.fachhochschule.dortmund.bads.systems;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.CoreConfiguration;
import de.fachhochschule.dortmund.bads.exceptions.ProcessExecutionException;
import de.fachhochschule.dortmund.bads.model.Task;
import de.fachhochschule.dortmund.bads.resources.BeveragesBox;
import de.fachhochschule.dortmund.bads.resources.Resource;
import de.fachhochschule.dortmund.bads.systems.logic.AGVTaskDispatcher;
import de.fachhochschule.dortmund.bads.systems.logic.ClockingSimulation;

public class Process {
	private static final Logger LOGGER = LogManager.getLogger();

	public static final int TIMEOUT_SHUTDOWN = 5000; // milliseconds

	protected List<Operation> operations;
	private Task parentTask; // Reference to the task that owns this process

	public Process() {
		this.operations = new ArrayList<>();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Process created with empty operations list");
		}
	}

	/**
	 * Set the parent task for this process.
	 * This is used to link tasks to AGV operations.
	 *
	 * @param task the parent task
	 */
	public void setParentTask(Task task) {
		this.parentTask = task;
	}
	
	public void addOperation(Operation operation) {
		if (operation == null) {
			throw new ProcessExecutionException("Cannot add null operation to process");
		}
		this.operations.add(operation);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Operation added to process. Total operations: {}", this.operations.size());
		}
	}
	
	public double processDuration() {
		if (this.operations.isEmpty()) {
			throw new ProcessExecutionException("Cannot calculate duration of process with no operations");
		}
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
		if (this.operations.isEmpty()) {
			throw new ProcessExecutionException("Cannot process operations - no operations defined");
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Starting process operations with {} operations", this.operations.size());
		}

		// Dispatch AGV tasks for BeveragesBox resources
		dispatchAGVTasks();

		// start all of the agents (resources) concurrently in ExecutorService
		// oversee the the end of the thread pool
		int threadsCount = this.operations.stream().map(Operation::getResourcesCount).reduce(Integer::sum).orElse(0);

		// If no operations or no resources, return empty list
		if (threadsCount == 0) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("No resources to process, returning empty futures list");
			}
			return new ArrayList<>();
		}

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
			throw new ProcessExecutionException("Process execution was interrupted", e);
		} finally {
			shutdownExecutor(executor, startTime);
		}

		return futures;
	}

	/**
	 * Dispatch AGV tasks for any BeveragesBox resources in this process
	 */
	private void dispatchAGVTasks() {
		AGVTaskDispatcher dispatcher = CoreConfiguration.INSTANCE.getAGVTaskDispatcher();

		if (dispatcher == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("AGV Task Dispatcher not initialized - AGVs will not move for this task");
			}
			return;
		}

		if (parentTask == null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("No parent task set for this process - skipping AGV dispatch");
			}
			return;
		}

		// Scan all operations for BeveragesBox resources
		int beveragesDispatched = 0;
		for (Operation operation : operations) {
			for (int i = 0; i < operation.getResourcesCount(); i++) {
				Resource resource = operation.getResource(i);
				if (resource instanceof BeveragesBox) {
					BeveragesBox box = (BeveragesBox) resource;

					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("Dispatching AGV for beverage: {} (Task: {})",
							box.getBeverageName(), parentTask.getTaskId());
					}

					boolean dispatched = dispatcher.assignTaskToAGV(parentTask, box);
					if (dispatched) {
						beveragesDispatched++;
					} else {
						if (LOGGER.isWarnEnabled()) {
							LOGGER.warn("Failed to dispatch AGV for beverage: {} (Task: {})",
								box.getBeverageName(), parentTask.getTaskId());
						}
					}
				}
			}
		}

		if (beveragesDispatched > 0 && LOGGER.isInfoEnabled()) {
			LOGGER.info("Dispatched {} AGV operations for Task {}", beveragesDispatched, parentTask.getTaskId());
		}
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
