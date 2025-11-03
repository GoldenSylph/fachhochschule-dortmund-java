package de.fachhochschule.dortmund.bads.hm1.bedrin.systems.logic;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.hm1.bedrin.Storage;
import de.fachhochschule.dortmund.bads.hm1.bedrin.systems.Systems;

public class StorageManagement extends Thread {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private List<Storage> storages;
	// provides storage management operations for AGVs
	// fire events for the observability
	
	@Override
	public void run() {
		LOGGER.info("Storage Management System started...");
		
		try {
			while (!Thread.currentThread().isInterrupted() && isClockingAlive()) {
				// Keep the thread alive and perform periodic checks
				// You can add any periodic maintenance tasks here
				
				// Sleep for a short period to avoid busy waiting
				Thread.sleep(100); // 100ms sleep
			}
		} catch (InterruptedException e) {
			// Thread was interrupted, restore the interrupt flag
			Thread.currentThread().interrupt();
			LOGGER.info("Storage Management System interrupted, shutting down...");
		}
		
		if (!isClockingAlive()) {
			LOGGER.info("CLOCKING system is no longer alive, Storage Management System shutting down...");
		}
		
		LOGGER.info("Storage Management System stopped.");
	}
	
	private boolean isClockingAlive() {
		Thread clockingThread = Systems.CLOCKING.getLogic();
		return clockingThread != null && clockingThread.isAlive();
	}
}