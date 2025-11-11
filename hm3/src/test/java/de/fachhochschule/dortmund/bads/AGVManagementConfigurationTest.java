package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AGVManagementConfigurationTest {

	@BeforeEach
	void setUp() {
		// Reset configuration to defaults before each test
		AGVManagementConfiguration.INSTANCE
			.setNumberOfAGVs(5)
			.setChargeDurationMillis(5000)
			.setMaxWaitForChargingMillis(10000)
			.setBatteryLowThreshold(0.20)
			.setAutoChargingEnabled(true);
	}

	@Test
	void testSingletonInstance() {
		assertNotNull(AGVManagementConfiguration.INSTANCE);
		assertSame(AGVManagementConfiguration.INSTANCE, AGVManagementConfiguration.INSTANCE);
	}

	@Test
	void testDefaultValues() {
		assertEquals(5, AGVManagementConfiguration.INSTANCE.getNumberOfAGVs());
		assertEquals(5000, AGVManagementConfiguration.INSTANCE.getChargeDurationMillis());
		assertEquals(10000, AGVManagementConfiguration.INSTANCE.getMaxWaitForChargingMillis());
		assertEquals(0.20, AGVManagementConfiguration.INSTANCE.getBatteryLowThreshold(), 0.001);
		assertTrue(AGVManagementConfiguration.INSTANCE.isAutoChargingEnabled());
	}

	@Test
	void testSetNumberOfAGVs() {
		AGVManagementConfiguration.INSTANCE.setNumberOfAGVs(10);
		assertEquals(10, AGVManagementConfiguration.INSTANCE.getNumberOfAGVs());
	}

	@Test
	void testSetChargeDurationMillis() {
		AGVManagementConfiguration.INSTANCE.setChargeDurationMillis(3000);
		assertEquals(3000, AGVManagementConfiguration.INSTANCE.getChargeDurationMillis());
	}

	@Test
	void testSetMaxWaitForChargingMillis() {
		AGVManagementConfiguration.INSTANCE.setMaxWaitForChargingMillis(15000);
		assertEquals(15000, AGVManagementConfiguration.INSTANCE.getMaxWaitForChargingMillis());
	}

	@Test
	void testSetBatteryLowThreshold() {
		AGVManagementConfiguration.INSTANCE.setBatteryLowThreshold(0.30);
		assertEquals(0.30, AGVManagementConfiguration.INSTANCE.getBatteryLowThreshold(), 0.001);
	}

	@Test
	void testSetEnableAutoCharging() {
		AGVManagementConfiguration.INSTANCE.setAutoChargingEnabled(false);
		assertFalse(AGVManagementConfiguration.INSTANCE.isAutoChargingEnabled());
		
		AGVManagementConfiguration.INSTANCE.setAutoChargingEnabled(true);
		assertTrue(AGVManagementConfiguration.INSTANCE.isAutoChargingEnabled());
	}

	@Test
	void testAutowire() {
		IConfiguration config = AGVManagementConfiguration.INSTANCE.autowire();
		assertNotNull(config);
		assertSame(AGVManagementConfiguration.INSTANCE, config);
	}

	@Test
	void testFluentInterface() {
		AGVManagementConfiguration config = AGVManagementConfiguration.INSTANCE
			.setNumberOfAGVs(8)
			.setChargeDurationMillis(4000)
			.setMaxWaitForChargingMillis(12000)
			.setBatteryLowThreshold(0.25)
			.setAutoChargingEnabled(false);
		
		assertSame(AGVManagementConfiguration.INSTANCE, config);
		assertEquals(8, config.getNumberOfAGVs());
		assertEquals(4000, config.getChargeDurationMillis());
		assertEquals(12000, config.getMaxWaitForChargingMillis());
		assertEquals(0.25, config.getBatteryLowThreshold(), 0.001);
		assertFalse(config.isAutoChargingEnabled());
	}

	@Test
	void testImplementsIConfiguration() {
		assertTrue(AGVManagementConfiguration.INSTANCE instanceof IConfiguration);
	}
}