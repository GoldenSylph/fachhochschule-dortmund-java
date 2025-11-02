package de.fachhochschule.dortmund.bads.hm1.bedrin.systems.logic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Observation extends Thread {
	private static final Logger LOGGER = LogManager.getLogger();
	
	@Override
	public void run() {
		LOGGER.info("Observation started...");
	}
	
	public record ObservationData(int clockingTime, String eventType, String details) {}
}
