package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fachhochschule.dortmund.bads.systems.logic.Observation;

class ObservationTest {
	
	private Observation observation;
	
	@BeforeEach
	void setUp() {
		observation = new Observation();
	}
	
	@Test
	void testObservationCreation() {
		assertNotNull(observation);
	}
	
	@Test
	void testRunSystem() {
		assertDoesNotThrow(() -> {
			Thread thread = new Thread(observation);
			thread.start();
			Thread.sleep(100);
			thread.interrupt();
		});
	}
	
	@Test
	void testMultipleObservationInstances() {
		Observation obs1 = new Observation();
		Observation obs2 = new Observation();
		
		assertNotNull(obs1);
		assertNotNull(obs2);
		assertNotSame(obs1, obs2);
	}
	
	@Test
	void testObservationThreadSafety() throws InterruptedException {
		Thread t1 = new Thread(observation);
		t1.start();
		Thread.sleep(50);
		t1.interrupt();
		t1.join(1000);
		
		assertFalse(t1.isAlive());
	}
}
