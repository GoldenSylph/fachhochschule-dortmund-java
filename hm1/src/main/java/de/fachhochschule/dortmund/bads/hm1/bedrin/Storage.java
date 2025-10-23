package de.fachhochschule.dortmund.bads.hm1.bedrin;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Storage extends Thread {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private List<List<StorageCell>> cells;
	private ExecutorService storageExecutor;
	// POJO of an automated storage
	// has cells that can be filled and released
	// each cell have 3 dimensions: length, width, height
	// each cell can be empty or filled
	// each cell can store several items according to their volumes
	
	@Override
	public void run() {
		LOGGER.info("Storage started...");
	}
}
