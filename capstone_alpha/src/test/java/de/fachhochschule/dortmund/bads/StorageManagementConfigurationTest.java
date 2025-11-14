package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StorageManagementConfigurationTest {

	@BeforeEach
	void setUp() {
		// Reset configuration to defaults before each test
		StorageManagementConfiguration.INSTANCE
			.setDefaultStorageCapacity(100)
			.setAutoCompactionEnabled(true)
			.setCompactionIntervalMillis(60000)
			.setStorageUtilizationThreshold(0.85);
	}

	@Test
	void testSingletonInstance() {
		assertNotNull(StorageManagementConfiguration.INSTANCE);
		assertSame(StorageManagementConfiguration.INSTANCE, StorageManagementConfiguration.INSTANCE);
	}

	@Test
	void testDefaultValues() {
		assertEquals(100, StorageManagementConfiguration.INSTANCE.getDefaultStorageCapacity());
		assertTrue(StorageManagementConfiguration.INSTANCE.isAutoCompactionEnabled());
		assertEquals(60000, StorageManagementConfiguration.INSTANCE.getCompactionIntervalMillis());
		assertEquals(0.85, StorageManagementConfiguration.INSTANCE.getStorageUtilizationThreshold(), 0.001);
	}

	@Test
	void testSetDefaultStorageCapacity() {
		StorageManagementConfiguration.INSTANCE.setDefaultStorageCapacity(200);
		assertEquals(200, StorageManagementConfiguration.INSTANCE.getDefaultStorageCapacity());
	}

	@Test
	void testSetAutoCompactionEnabled() {
		StorageManagementConfiguration.INSTANCE.setAutoCompactionEnabled(false);
		assertFalse(StorageManagementConfiguration.INSTANCE.isAutoCompactionEnabled());
		
		StorageManagementConfiguration.INSTANCE.setAutoCompactionEnabled(true);
		assertTrue(StorageManagementConfiguration.INSTANCE.isAutoCompactionEnabled());
	}

	@Test
	void testSetCompactionIntervalMillis() {
		StorageManagementConfiguration.INSTANCE.setCompactionIntervalMillis(30000);
		assertEquals(30000, StorageManagementConfiguration.INSTANCE.getCompactionIntervalMillis());
	}

	@Test
	void testSetStorageUtilizationThreshold() {
		StorageManagementConfiguration.INSTANCE.setStorageUtilizationThreshold(0.75);
		assertEquals(0.75, StorageManagementConfiguration.INSTANCE.getStorageUtilizationThreshold(), 0.001);
	}

	@Test
	void testAutowire() {
		IConfiguration config = StorageManagementConfiguration.INSTANCE.autowire();
		assertNotNull(config);
		assertSame(StorageManagementConfiguration.INSTANCE, config);
	}

	@Test
	void testFluentInterface() {
		StorageManagementConfiguration config = StorageManagementConfiguration.INSTANCE
			.setDefaultStorageCapacity(150)
			.setAutoCompactionEnabled(false)
			.setCompactionIntervalMillis(45000)
			.setStorageUtilizationThreshold(0.90);
		
		assertSame(StorageManagementConfiguration.INSTANCE, config);
		assertEquals(150, config.getDefaultStorageCapacity());
		assertFalse(config.isAutoCompactionEnabled());
		assertEquals(45000, config.getCompactionIntervalMillis());
		assertEquals(0.90, config.getStorageUtilizationThreshold(), 0.001);
	}

	@Test
	void testImplementsIConfiguration() {
		assertTrue(StorageManagementConfiguration.INSTANCE instanceof IConfiguration);
	}
}
