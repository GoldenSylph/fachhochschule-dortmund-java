package de.fachhochschule.dortmund.bads.hm1.bedrin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TaskManagementSystem extends Thread {
	private static final Logger LOGGER = LogManager.getLogger();
	
	// management of tasks which are processes
	// fire events for the observability
	
	@Override
	public void run() {
		LOGGER.info("Task Management System started...");
	}
}
