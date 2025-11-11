package de.fachhochschule.dortmund.bads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Configuration for Task Management. Controls task queue size, prioritization,
 * and timeouts.
 */
public enum TaskManagementConfiguration implements IConfiguration {
	INSTANCE;

	private static final Logger LOGGER = LogManager.getLogger();

	private int maxConcurrentTasks = 10;
	private long taskTimeoutMillis = 30000;
	private boolean enableTaskPrioritization = true;
	private boolean isAutowired = false;

	@Override
	public IConfiguration autowire() {
		if (isAutowired) {
			LOGGER.warn("TaskManagementConfiguration already autowired");
			return this;
		}

		LOGGER.info("TaskManagementConfiguration autowired");
		LOGGER.info("  Max Concurrent: {}, Prioritization: {}", maxConcurrentTasks, enableTaskPrioritization);

		isAutowired = true;
		return this;
	}

	public int getMaxConcurrentTasks() {
		return maxConcurrentTasks;
	}

	public TaskManagementConfiguration setMaxConcurrentTasks(int max) {
		this.maxConcurrentTasks = max;
		return this;
	}

	public long getTaskTimeoutMillis() {
		return taskTimeoutMillis;
	}

	public TaskManagementConfiguration setTaskTimeoutMillis(long ms) {
		this.taskTimeoutMillis = ms;
		return this;
	}

	public boolean isTaskPrioritizationEnabled() {
		return enableTaskPrioritization;
	}

	public TaskManagementConfiguration setTaskPrioritizationEnabled(boolean e) {
		this.enableTaskPrioritization = e;
		return this;
	}
}
