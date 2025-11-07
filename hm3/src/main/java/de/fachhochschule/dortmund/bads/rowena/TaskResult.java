package de.fachhochschule.dortmund.bads.rowena;

/**
 * Represents the result of a task execution in the AGV simulation.
 * Contains timing information and execution details.
 */
public class TaskResult {
	private final int taskId;
	private final long submissionTime;
	private final long startTime;
	private final long completionTime;
	private final String assignedAGVId;

	public TaskResult(int taskId, long submissionTime, long startTime, long completionTime, String assignedAGVId) {
		this.taskId = taskId;
		this.submissionTime = submissionTime;
		this.startTime = startTime;
		this.completionTime = completionTime;
		this.assignedAGVId = assignedAGVId;
	}

	public int getTaskId() {
		return taskId;
	}

	public long getSubmissionTime() {
		return submissionTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getCompletionTime() {
		return completionTime;
	}

	public String getAssignedAGVId() {
		return assignedAGVId;
	}

	/**
	 * Calculates the time the task spent waiting for an AGV to become available.
	 * @return wait time in milliseconds
	 */
	public long getWaitTime() {
		return startTime - submissionTime;
	}

	/**
	 * Calculates the actual execution time of the task.
	 * @return execution time in milliseconds
	 */
	public long getExecutionTime() {
		return completionTime - startTime;
	}

	/**
	 * Calculates the total time from submission to completion.
	 * @return total time in milliseconds
	 */
	public long getTotalTime() {
		return completionTime - submissionTime;
	}

	@Override
	public String toString() {
		return String.format("TaskResult[id=%d, agv=%s, wait=%dms, exec=%dms, total=%dms]",
				taskId, assignedAGVId, getWaitTime(), getExecutionTime(), getTotalTime());
	}
}
