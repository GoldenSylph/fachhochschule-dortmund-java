package de.fachhochschule.dortmund.bads.hm1.bedrin.systems.logic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.hm1.bedrin.systems.Systems;

public class Observation extends Thread {
	private static final Logger LOGGER = LogManager.getLogger();
	
	@Override
	public void run() {
		LOGGER.info("Observation started...");
		
		try {
			while (!Thread.currentThread().isInterrupted() && isClockingAlive()) {
				// Keep the thread alive and perform periodic checks
				// You can add any periodic observation tasks here
				
				// Sleep for a short period to avoid busy waiting
				Thread.sleep(100); // 100ms sleep
			}
		} catch (InterruptedException e) {
			// Thread was interrupted, restore the interrupt flag
			Thread.currentThread().interrupt();
			LOGGER.info("Observation System interrupted, shutting down...");
		}
		
		if (!isClockingAlive()) {
			LOGGER.info("CLOCKING system is no longer alive, Observation System shutting down...");
		}
		
		LOGGER.info("Observation System stopped.");
	}
	
	private boolean isClockingAlive() {
		Thread clockingThread = Systems.CLOCKING.getLogic();
		return clockingThread != null && clockingThread.isAlive();
	}
	
	public record ObservationData(int clockingTime, String eventType, String details) {}
}