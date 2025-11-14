package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ObservabilityConfigurationTest {

	@BeforeEach
	void setUp() {
		// Reset configuration to defaults before each test
		ObservabilityConfiguration.INSTANCE
			.setMetricsEnabled(true)
			.setEventTrackingEnabled(true)
			.setMetricsCollectionIntervalMillis(5000)
			.setEventBufferSize(1000)
			.setPerformanceMonitoringEnabled(true);
	}

	@Test
	void testSingletonInstance() {
		assertNotNull(ObservabilityConfiguration.INSTANCE);
		assertSame(ObservabilityConfiguration.INSTANCE, ObservabilityConfiguration.INSTANCE);
	}

	@Test
	void testDefaultValues() {
		assertTrue(ObservabilityConfiguration.INSTANCE.isMetricsEnabled());
		assertTrue(ObservabilityConfiguration.INSTANCE.isEventTrackingEnabled());
		assertEquals(5000, ObservabilityConfiguration.INSTANCE.getMetricsCollectionIntervalMillis());
		assertEquals(1000, ObservabilityConfiguration.INSTANCE.getEventBufferSize());
		assertTrue(ObservabilityConfiguration.INSTANCE.isPerformanceMonitoringEnabled());
	}

	@Test
	void testSetMetricsEnabled() {
		ObservabilityConfiguration.INSTANCE.setMetricsEnabled(false);
		assertFalse(ObservabilityConfiguration.INSTANCE.isMetricsEnabled());
		
		ObservabilityConfiguration.INSTANCE.setMetricsEnabled(true);
		assertTrue(ObservabilityConfiguration.INSTANCE.isMetricsEnabled());
	}

	@Test
	void testSetEventTrackingEnabled() {
		ObservabilityConfiguration.INSTANCE.setEventTrackingEnabled(false);
		assertFalse(ObservabilityConfiguration.INSTANCE.isEventTrackingEnabled());
		
		ObservabilityConfiguration.INSTANCE.setEventTrackingEnabled(true);
		assertTrue(ObservabilityConfiguration.INSTANCE.isEventTrackingEnabled());
	}

	@Test
	void testSetMetricsCollectionIntervalMillis() {
		ObservabilityConfiguration.INSTANCE.setMetricsCollectionIntervalMillis(3000);
		assertEquals(3000, ObservabilityConfiguration.INSTANCE.getMetricsCollectionIntervalMillis());
	}

	@Test
	void testSetEventBufferSize() {
		ObservabilityConfiguration.INSTANCE.setEventBufferSize(500);
		assertEquals(500, ObservabilityConfiguration.INSTANCE.getEventBufferSize());
	}

	@Test
	void testSetPerformanceMonitoringEnabled() {
		ObservabilityConfiguration.INSTANCE.setPerformanceMonitoringEnabled(false);
		assertFalse(ObservabilityConfiguration.INSTANCE.isPerformanceMonitoringEnabled());
		
		ObservabilityConfiguration.INSTANCE.setPerformanceMonitoringEnabled(true);
		assertTrue(ObservabilityConfiguration.INSTANCE.isPerformanceMonitoringEnabled());
	}

	@Test
	void testAutowire() {
		IConfiguration config = ObservabilityConfiguration.INSTANCE.autowire();
		assertNotNull(config);
		assertSame(ObservabilityConfiguration.INSTANCE, config);
	}

	@Test
	void testFluentInterface() {
		ObservabilityConfiguration config = ObservabilityConfiguration.INSTANCE
			.setMetricsEnabled(false)
			.setEventTrackingEnabled(false)
			.setMetricsCollectionIntervalMillis(7000)
			.setEventBufferSize(2000)
			.setPerformanceMonitoringEnabled(false);
		
		assertSame(ObservabilityConfiguration.INSTANCE, config);
		assertFalse(config.isMetricsEnabled());
		assertFalse(config.isEventTrackingEnabled());
		assertEquals(7000, config.getMetricsCollectionIntervalMillis());
		assertEquals(2000, config.getEventBufferSize());
		assertFalse(config.isPerformanceMonitoringEnabled());
	}

	@Test
	void testImplementsIConfiguration() {
		assertTrue(ObservabilityConfiguration.INSTANCE instanceof IConfiguration);
	}
}
