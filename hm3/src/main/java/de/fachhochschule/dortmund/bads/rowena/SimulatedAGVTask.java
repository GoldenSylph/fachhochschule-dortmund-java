package de.fachhochschule.dortmund.bads.rowena;

import java.util.Random;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents a single task that requires an AGV to execute.
 * Simulates AGV work by performing a task for a specified duration.
 */
public class SimulatedAGVTask implements Callable<TaskResult> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Random RANDOM = new Random();

	private final int taskId;
	private final long submissionTime;
	private final long executionDuration;

	/**
	 * Creates a new simulated AGV task with a specified execution duration.
	 *
	 * @param taskId unique identifier for this task
	 * @param submissionTime timestamp when the task was submitted
	 * @param executionDuration how long the task should take to execute (in milliseconds)
	 */
	public SimulatedAGVTask(int taskId, long submissionTime, long executionDuration) {
		this.taskId = taskId;
		this.submissionTime = submissionTime;
		this.executionDuration = executionDuration;
	}

	/**
	 * Creates a new simulated AGV task with random execution duration between min and max.
	 *
	 * @param taskId unique identifier for this task
	 * @param submissionTime timestamp when the task was submitted
	 * @param minDuration minimum execution duration in milliseconds
	 * @param maxDuration maximum execution duration in milliseconds
	 */
	public SimulatedAGVTask(int taskId, long submissionTime, long minDuration, long maxDuration) {
		this.taskId = taskId;
		this.submissionTime = submissionTime;
		this.executionDuration = minDuration + RANDOM.nextInt((int) (maxDuration - minDuration + 1));
	}

	@Override
	public TaskResult call() throws Exception {
		// Record when the task actually starts execution
		long startTime = System.currentTimeMillis();
		String agvId = Thread.currentThread().getName();

		long waitTime = startTime - submissionTime;

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("[Task-{}] Started on {} at T+{}ms (wait: {}ms)",
					taskId, agvId, startTime - submissionTime, waitTime);
		}

		try {
			// Simulate task execution by sleeping
			Thread.sleep(executionDuration);

			long completionTime = System.currentTimeMillis();
			long executionTime = completionTime - startTime;

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("[Task-{}] Completed at T+{}ms (execution: {}ms)",
						taskId, completionTime - submissionTime, executionTime);
			}

			return new TaskResult(taskId, submissionTime, startTime, completionTime, agvId);

		} catch (InterruptedException e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("[Task-{}] Interrupted during execution", taskId);
			}
			Thread.currentThread().interrupt();
			throw e;
		}
	}

	public int getTaskId() {
		return taskId;
	}

	public long getSubmissionTime() {
		return submissionTime;
	}

	public long getExecutionDuration() {
		return executionDuration;
	}

	@Override
	public String toString() {
		return String.format("SimulatedAGVTask[id=%d, duration=%dms]", taskId, executionDuration);
	}
}
