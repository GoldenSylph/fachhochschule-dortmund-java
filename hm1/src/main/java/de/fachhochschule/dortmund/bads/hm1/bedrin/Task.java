package de.fachhochschule.dortmund.bads.hm1.bedrin;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Task extends Thread {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private List<Process> processes;
	// POJO class for representing a Task
	// contains processes of operations:
	// storage manipulation processes
		// fill storage cell process
		// release storage cell process
		// load soft for AGVs
	// logistics processes
		// Restaurant discovery process
		// Sending to Restaurant process
		// Load to Trucks processes from one big storage
	
	@Override
	public void run() {
		LOGGER.info("Task started...");
	}
}
