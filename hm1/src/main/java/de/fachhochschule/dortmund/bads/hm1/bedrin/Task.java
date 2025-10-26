package de.fachhochschule.dortmund.bads.hm1.bedrin;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Task extends Thread {
	private static final Logger LOGGER = LogManager.getLogger();
	
	// A list of processes that make up the task
	private List<Process> processes;
	
	@Override
	public void run() {
		LOGGER.info("Task started...");
	}
}
