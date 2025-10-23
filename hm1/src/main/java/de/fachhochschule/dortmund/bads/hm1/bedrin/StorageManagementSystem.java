package de.fachhochschule.dortmund.bads.hm1.bedrin;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StorageManagementSystem extends Thread {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private List<Storage> storages;
	// provides storage management operations for AGVs
	// fire events for the observability
	
	@Override
	public void run() {
		LOGGER.info("Storage Management System started...");
	}
}
