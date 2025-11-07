package de.fachhochschule.dortmund.bads.rowena;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Simulates a system where M tasks compete for K available AGVs.
 * Uses ExecutorService with a fixed thread pool to represent K AGVs.
 * Provides comprehensive logging and statistics about task execution.
 */
public class AGVTaskSimulator {
	private static final Logger LOGGER = LogManager.getLogger();

	private final int numberOfAGVs;
	private final ExecutorService executorService;
	private final List<TaskResult> results;
	private long simulationStartTime;
	private long simulationEndTime;

	/**
	 * Creates a new AGV task simulator with K available AGVs.
	 *
	 * @param numberOfAGVs the number of AGVs (K) available for task execution
	 */
	public AGVTaskSimulator(int numberOfAGVs) {
		if (numberOfAGVs <= 0) {
			throw new IllegalArgumentException("Number of AGVs must be positive");
		}
		this.numberOfAGVs = numberOfAGVs;
		this.executorService = Executors.newFixedThreadPool(
				numberOfAGVs,
				r -> {
					Thread t = new Thread(r);
					t.setName("AGV-" + t.getId());
					return t;
				}
		);
		this.results = new ArrayList<>();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("AGVTaskSimulator created with {} AGVs", numberOfAGVs);
		}
	}

	/**
	 * Runs the simulation with M tasks.
	 *
	 * @param tasks list of M tasks to execute
	 * @return list of task results
	 */
	public List<TaskResult> runSimulation(List<SimulatedAGVTask> tasks) {
		if (tasks == null || tasks.isEmpty()) {
			throw new IllegalArgumentException("Tasks list cannot be null or empty");
		}

		int numberOfTasks = tasks.size();
		simulationStartTime = System.currentTimeMillis();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("=== AGV Task Simulation Started ===");
			LOGGER.info("Configuration: M={} tasks, K={} AGVs", numberOfTasks, numberOfAGVs);
			LOGGER.info("-----------------------------------");
		}

		// Submit all tasks and collect futures
		List<Future<TaskResult>> futures = new ArrayList<>();
		for (SimulatedAGVTask task : tasks) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[Task-{}] Submitted at T+{}ms",
						task.getTaskId(),
						System.currentTimeMillis() - simulationStartTime);
			}
			futures.add(executorService.submit(task));
		}

		// Collect results from all completed tasks
		results.clear();
		for (Future<TaskResult> future : futures) {
			try {
				TaskResult result = future.get();
				results.add(result);
			} catch (Exception e) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Error executing task: {}", e.getMessage(), e);
				}
			}
		}

		simulationEndTime = System.currentTimeMillis();

		// Print statistics
		printStatistics();

		return new ArrayList<>(results);
	}

	/**
	 * Shuts down the simulator and releases all resources.
	 */
	public void shutdown() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Shutting down AGVTaskSimulator...");
		}

		executorService.shutdown();
		try {
			if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
				executorService.shutdownNow();
				if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error("ExecutorService did not terminate");
					}
				}
			}
		} catch (InterruptedException e) {
			executorService.shutdownNow();
			Thread.currentThread().interrupt();
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("AGVTaskSimulator shut down successfully");
		}
	}

	/**
	 * Prints comprehensive statistics about the simulation.
	 */
	private void printStatistics() {
		if (results.isEmpty()) {
			LOGGER.warn("No results to display");
			return;
		}

		long totalSimulationTime = simulationEndTime - simulationStartTime;

		// Calculate statistics
		long totalWaitTime = 0;
		long totalExecutionTime = 0;
		long maxWaitTime = 0;
		long minWaitTime = Long.MAX_VALUE;
		long maxExecutionTime = 0;
		long minExecutionTime = Long.MAX_VALUE;

		for (TaskResult result : results) {
			long waitTime = result.getWaitTime();
			long execTime = result.getExecutionTime();

			totalWaitTime += waitTime;
			totalExecutionTime += execTime;

			maxWaitTime = Math.max(maxWaitTime, waitTime);
			minWaitTime = Math.min(minWaitTime, waitTime);
			maxExecutionTime = Math.max(maxExecutionTime, execTime);
			minExecutionTime = Math.min(minExecutionTime, execTime);
		}

		int completedTasks = results.size();
		double avgWaitTime = (double) totalWaitTime / completedTasks;
		double avgExecutionTime = (double) totalExecutionTime / completedTasks;

		// Calculate AGV utilization
		// Total possible AGV work time = numberOfAGVs * totalSimulationTime
		// Actual AGV work time = sum of all execution times
		double totalPossibleWorkTime = numberOfAGVs * totalSimulationTime;
		double agvUtilization = (totalExecutionTime / totalPossibleWorkTime) * 100;

		// Print statistics
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("");
			LOGGER.info("=== Simulation Complete ===");
			LOGGER.info("Configuration:");
			LOGGER.info("  Total Tasks (M): {}", completedTasks);
			LOGGER.info("  Available AGVs (K): {}", numberOfAGVs);
			LOGGER.info("  Tasks per AGV ratio (M/K): {:.2f}", (double) completedTasks / numberOfAGVs);
			LOGGER.info("");
			LOGGER.info("Timing Statistics:");
			LOGGER.info("  Total Simulation Time: {}ms", totalSimulationTime);
			LOGGER.info("  Average Wait Time: {:.2f}ms", avgWaitTime);
			LOGGER.info("  Min/Max Wait Time: {}ms / {}ms", minWaitTime, maxWaitTime);
			LOGGER.info("  Average Execution Time: {:.2f}ms", avgExecutionTime);
			LOGGER.info("  Min/Max Execution Time: {}ms / {}ms", minExecutionTime, maxExecutionTime);
			LOGGER.info("");
			LOGGER.info("Performance Metrics:");
			LOGGER.info("  AGV Utilization: {:.2f}%", agvUtilization);
			LOGGER.info("  Tasks Completed: {}", completedTasks);
			LOGGER.info("  Throughput: {:.2f} tasks/second",
					(double) completedTasks / (totalSimulationTime / 1000.0));
			LOGGER.info("===========================");
		}
	}

	/**
	 * Gets the results from the last simulation run.
	 *
	 * @return list of task results
	 */
	public List<TaskResult> getResults() {
		return new ArrayList<>(results);
	}

	/**
	 * Gets the number of AGVs in this simulator.
	 *
	 * @return number of AGVs (K)
	 */
	public int getNumberOfAGVs() {
		return numberOfAGVs;
	}

	/**
	 * Gets the total simulation time of the last run.
	 *
	 * @return simulation time in milliseconds
	 */
	public long getSimulationTime() {
		return simulationEndTime - simulationStartTime;
	}
}
