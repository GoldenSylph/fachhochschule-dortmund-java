package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import de.fachhochschule.dortmund.bads.systems.Systems;

class SystemsTest {
	
	@Test
	void testSystemsEnum() {
		assertNotNull(Systems.CLOCKING);
		assertNotNull(Systems.TASK_MANAGEMENT);
		assertNotNull(Systems.STORAGE_MANAGEMENT);
		assertNotNull(Systems.OBSERVATION);
	}
	
	@Test
	void testAllSystemsExist() {
		Systems[] systems = Systems.values();
		assertEquals(4, systems.length);
	}
	
	@Test
	void testSystemByName() {
		assertEquals(Systems.CLOCKING, Systems.valueOf("CLOCKING"));
		assertEquals(Systems.TASK_MANAGEMENT, Systems.valueOf("TASK_MANAGEMENT"));
		assertEquals(Systems.STORAGE_MANAGEMENT, Systems.valueOf("STORAGE_MANAGEMENT"));
		assertEquals(Systems.OBSERVATION, Systems.valueOf("OBSERVATION"));
	}
	
	@Test
	void testSystemsRunningStatus() {
		// Before autowire, systems should not be running
		assertFalse(Systems.CLOCKING.isRunning());
		assertFalse(Systems.TASK_MANAGEMENT.isRunning());
		assertFalse(Systems.STORAGE_MANAGEMENT.isRunning());
		assertFalse(Systems.OBSERVATION.isRunning());
	}
	
	@Test
	void testSystemsAfterAutowire() throws InterruptedException {
		CoreConfiguration.INSTANCE.autowire();
		
		// Give systems time to start
		Thread.sleep(100);
		
		assertTrue(Systems.CLOCKING.isRunning());
		assertTrue(Systems.TASK_MANAGEMENT.isRunning());
		assertTrue(Systems.STORAGE_MANAGEMENT.isRunning());
		assertTrue(Systems.OBSERVATION.isRunning());
		
		// Cleanup
		CoreConfiguration.INSTANCE.shutdown();
		Thread.sleep(600);
	}
}
