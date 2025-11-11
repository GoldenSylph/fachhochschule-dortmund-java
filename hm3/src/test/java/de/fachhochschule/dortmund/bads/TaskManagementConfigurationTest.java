package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskManagementConfigurationTest {

	@BeforeEach
	void setUp() {
		// Reset configuration to defaults before each test
		TaskManagementConfiguration.INSTANCE
			.setMaxConcurrentTasks(10)
			.setTaskTimeoutMillis(30000)
			.setTaskPrioritizationEnabled(true);
	}

	@Test
	void testSingletonInstance() {
		assertNotNull(TaskManagementConfiguration.INSTANCE);
		assertSame(TaskManagementConfiguration.INSTANCE, TaskManagementConfiguration.INSTANCE);
	}

	@Test
	void testDefaultValues() {
		assertEquals(10, TaskManagementConfiguration.INSTANCE.getMaxConcurrentTasks());
		assertEquals(30000, TaskManagementConfiguration.INSTANCE.getTaskTimeoutMillis());
		assertTrue(TaskManagementConfiguration.INSTANCE.isTaskPrioritizationEnabled());
	}

	@Test
	void testSetMaxConcurrentTasks() {
		TaskManagementConfiguration.INSTANCE.setMaxConcurrentTasks(20);
		assertEquals(20, TaskManagementConfiguration.INSTANCE.getMaxConcurrentTasks());
	}

	@Test
	void testSetTaskTimeoutMillis() {
		TaskManagementConfiguration.INSTANCE.setTaskTimeoutMillis(60000);
		assertEquals(60000, TaskManagementConfiguration.INSTANCE.getTaskTimeoutMillis());
	}

	@Test
	void testSetTaskPrioritizationEnabled() {
		TaskManagementConfiguration.INSTANCE.setTaskPrioritizationEnabled(false);
		assertFalse(TaskManagementConfiguration.INSTANCE.isTaskPrioritizationEnabled());
		
		TaskManagementConfiguration.INSTANCE.setTaskPrioritizationEnabled(true);
		assertTrue(TaskManagementConfiguration.INSTANCE.isTaskPrioritizationEnabled());
	}

	@Test
	void testAutowire() {
		IConfiguration config = TaskManagementConfiguration.INSTANCE.autowire();
		assertNotNull(config);
		assertSame(TaskManagementConfiguration.INSTANCE, config);
	}

	@Test
	void testFluentInterface() {
		TaskManagementConfiguration config = TaskManagementConfiguration.INSTANCE
			.setMaxConcurrentTasks(15)
			.setTaskTimeoutMillis(45000)
			.setTaskPrioritizationEnabled(false);
		
		assertSame(TaskManagementConfiguration.INSTANCE, config);
		assertEquals(15, config.getMaxConcurrentTasks());
		assertEquals(45000, config.getTaskTimeoutMillis());
		assertFalse(config.isTaskPrioritizationEnabled());
	}

	@Test
	void testImplementsIConfiguration() {
		assertTrue(TaskManagementConfiguration.INSTANCE instanceof IConfiguration);
	}
}
