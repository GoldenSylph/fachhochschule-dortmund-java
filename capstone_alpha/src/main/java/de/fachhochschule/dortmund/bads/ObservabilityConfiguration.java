package de.fachhochschule.dortmund.bads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Configuration for Observability subsystem. Handles monitoring, metrics
 * collection, and event tracking.
 */
public enum ObservabilityConfiguration implements IConfiguration {
	INSTANCE;

	private static final Logger LOGGER = LogManager.getLogger();

	private boolean enableMetrics = true;
	private boolean enableEventTracking = true;
	private long metricsCollectionIntervalMillis = 5000;
	private int eventBufferSize = 1000;
	private boolean enablePerformanceMonitoring = true;
	private boolean isAutowired = false;

	@Override
	public IConfiguration autowire() {
		if (isAutowired) {
			LOGGER.warn("ObservabilityConfiguration already autowired");
			return this;
		}

		LOGGER.info("ObservabilityConfiguration autowired");
		LOGGER.info("  Metrics: {}, Event Tracking: {}", enableMetrics, enableEventTracking);

		isAutowired = true;
		return this;
	}

	public boolean isMetricsEnabled() {
		return enableMetrics;
	}

	public ObservabilityConfiguration setMetricsEnabled(boolean e) {
		this.enableMetrics = e;
		return this;
	}

	public boolean isEventTrackingEnabled() {
		return enableEventTracking;
	}

	public ObservabilityConfiguration setEventTrackingEnabled(boolean e) {
		this.enableEventTracking = e;
		return this;
	}

	public long getMetricsCollectionIntervalMillis() {
		return metricsCollectionIntervalMillis;
	}

	public ObservabilityConfiguration setMetricsCollectionIntervalMillis(long ms) {
		this.metricsCollectionIntervalMillis = ms;
		return this;
	}

	public int getEventBufferSize() {
		return eventBufferSize;
	}

	public ObservabilityConfiguration setEventBufferSize(int size) {
		this.eventBufferSize = size;
		return this;
	}

	public boolean isPerformanceMonitoringEnabled() {
		return enablePerformanceMonitoring;
	}

	public ObservabilityConfiguration setPerformanceMonitoringEnabled(boolean e) {
		this.enablePerformanceMonitoring = e;
		return this;
	}
}