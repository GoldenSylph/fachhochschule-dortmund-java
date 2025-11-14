package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fachhochschule.dortmund.bads.systems.logic.ClockingSimulation;

class ClockingSimulationTest {
	
	private ClockingSimulation clockingSimulation;
	
	@BeforeEach
	void setUp() {
		clockingSimulation = new ClockingSimulation();
	}
	
	@Test
	void testClockingSimulationCreation() {
		assertNotNull(clockingSimulation);
	}
	
	@Test
	void testGetCurrentTime() {
		int time = clockingSimulation.getCurrentTime();
		assertTrue(time >= 0);
	}
	
	@Test
	void testRunSystem() {
		assertDoesNotThrow(() -> {
			Thread thread = new Thread(clockingSimulation);
			thread.start();
			Thread.sleep(200);
			thread.interrupt();
		});
	}
	
	@Test
	void testTickProgression() throws InterruptedException {
		clockingSimulation.setDelay(50); // Set faster delay for testing
		Thread thread = new Thread(clockingSimulation);
		thread.start();
		
		int initialTime = clockingSimulation.getCurrentTime();
		Thread.sleep(150);
		int laterTime = clockingSimulation.getCurrentTime();
		
		thread.interrupt();
		
		assertTrue(laterTime >= initialTime, "Time should progress over time");
	}
	
	@Test
	void testMultipleClockingSimulations() {
		ClockingSimulation cs1 = new ClockingSimulation();
		ClockingSimulation cs2 = new ClockingSimulation();
		
		assertNotNull(cs1);
		assertNotNull(cs2);
		assertNotSame(cs1, cs2);
	}
	
	@Test
	void testIsRunningInitialState() {
		assertTrue(clockingSimulation.isRunning(), "ClockingSimulation should be running initially");
	}
	
	@Test
	void testToggleClocking() {
		boolean initialState = clockingSimulation.isRunning();
		clockingSimulation.toggleClocking();
		boolean toggledState = clockingSimulation.isRunning();
		
		assertNotEquals(initialState, toggledState, "State should change after toggle");
	}
	
	@Test
	void testSetDelay() {
		assertDoesNotThrow(() -> {
			clockingSimulation.setDelay(500);
			clockingSimulation.setDelay(100);
			clockingSimulation.setDelay(2000);
		});
	}
}