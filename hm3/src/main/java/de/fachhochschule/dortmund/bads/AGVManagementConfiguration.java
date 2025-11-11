package de.fachhochschule.dortmund.bads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Configuration for AGV Fleet Management. Controls AGV fleet size and battery
 * management. Note: Charging stations are now defined as StorageCell instances
 * in Storage, not configured here.
 */
public enum AGVManagementConfiguration implements IConfiguration {
	INSTANCE;

	private static final Logger LOGGER = LogManager.getLogger();

	private int numberOfAGVs = 5;
	private long chargeDurationMillis = 5000;
	private long maxWaitForChargingMillis = 10000;
	private double batteryLowThreshold = 0.20;
	private boolean enableAutoCharging = true;
	private boolean isAutowired = false;

	@Override
	public IConfiguration autowire() {
		if (isAutowired) {
			LOGGER.warn("AGVManagementConfiguration already autowired");
			return this;
		}

		LOGGER.info("AGVManagementConfiguration autowired");
		LOGGER.info("  AGVs: {}", numberOfAGVs);
		LOGGER.info("  Note: Charging stations are defined in Storage as CHARGING_STATION type cells");

		isAutowired = true;
		return this;
	}

	public int getNumberOfAGVs() {
		return numberOfAGVs;
	}

	public AGVManagementConfiguration setNumberOfAGVs(int n) {
		this.numberOfAGVs = n;
		return this;
	}

	public long getChargeDurationMillis() {
		return chargeDurationMillis;
	}

	public AGVManagementConfiguration setChargeDurationMillis(long ms) {
		this.chargeDurationMillis = ms;
		return this;
	}

	public long getMaxWaitForChargingMillis() {
		return maxWaitForChargingMillis;
	}

	public AGVManagementConfiguration setMaxWaitForChargingMillis(long ms) {
		this.maxWaitForChargingMillis = ms;
		return this;
	}

	public double getBatteryLowThreshold() {
		return batteryLowThreshold;
	}

	public AGVManagementConfiguration setBatteryLowThreshold(double t) {
		this.batteryLowThreshold = t;
		return this;
	}

	public boolean isAutoChargingEnabled() {
		return enableAutoCharging;
	}

	public AGVManagementConfiguration setAutoChargingEnabled(boolean e) {
		this.enableAutoCharging = e;
		return this;
	}
}