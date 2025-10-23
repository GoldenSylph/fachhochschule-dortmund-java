package de.fachhochschule.dortmund.bads.hm1;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.hm1.bedrin.ClockingSimulationSystem;

public class App {
	private static final Logger LOGGER = LogManager.getLogger();
	
	public static void main(String[] args) {
		LOGGER.info("Start clocking...");
		ClockingSimulationSystem simulationSystem = new ClockingSimulationSystem();
		simulationSystem.start();
		
		// start storage management system
		
		// start tasks management system
		
		// start observation system
		
		// start gui
	}
}
