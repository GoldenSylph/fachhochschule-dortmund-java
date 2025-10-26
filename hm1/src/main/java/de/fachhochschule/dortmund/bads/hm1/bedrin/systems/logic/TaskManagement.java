package de.fachhochschule.dortmund.bads.hm1.bedrin.systems.logic;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.hm1.bedrin.Task;

public class TaskManagement extends Thread {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private List<Task> tasks;
	// management of tasks which are processes
	// fire events for the observability
	
	@Override
	public void run() {
		LOGGER.info("Task Management System started...");
	}
}
