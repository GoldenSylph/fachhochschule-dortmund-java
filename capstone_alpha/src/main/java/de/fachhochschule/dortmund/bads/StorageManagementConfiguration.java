package de.fachhochschule.dortmund.bads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Configuration for Storage Management. Controls storage capacity, compaction,
 * and utilization thresholds.
 */
public enum StorageManagementConfiguration implements IConfiguration {
	INSTANCE;

	private static final Logger LOGGER = LogManager.getLogger();

	private int defaultStorageCapacity = 100;
	private boolean enableAutoCompaction = true;
	private long compactionIntervalMillis = 60000;
	private double storageUtilizationThreshold = 0.85;
	private boolean isAutowired = false;

	@Override
	public IConfiguration autowire() {
		if (isAutowired) {
			LOGGER.warn("StorageManagementConfiguration already autowired");
			return this;
		}

		LOGGER.info("StorageManagementConfiguration autowired");
		LOGGER.info("  Capacity: {}, Auto-Compaction: {}", defaultStorageCapacity, enableAutoCompaction);

		isAutowired = true;
		return this;
	}

	public int getDefaultStorageCapacity() {
		return defaultStorageCapacity;
	}

	public StorageManagementConfiguration setDefaultStorageCapacity(int c) {
		this.defaultStorageCapacity = c;
		return this;
	}

	public boolean isAutoCompactionEnabled() {
		return enableAutoCompaction;
	}

	public StorageManagementConfiguration setAutoCompactionEnabled(boolean e) {
		this.enableAutoCompaction = e;
		return this;
	}

	public long getCompactionIntervalMillis() {
		return compactionIntervalMillis;
	}

	public StorageManagementConfiguration setCompactionIntervalMillis(long ms) {
		this.compactionIntervalMillis = ms;
		return this;
	}

	public double getStorageUtilizationThreshold() {
		return storageUtilizationThreshold;
	}

	public StorageManagementConfiguration setStorageUtilizationThreshold(double t) {
		this.storageUtilizationThreshold = t;
		return this;
	}
}
