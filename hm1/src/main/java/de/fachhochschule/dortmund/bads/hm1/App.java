package de.fachhochschule.dortmund.bads.hm1;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {
	private static final Logger LOGGER = LogManager.getLogger();
	
	public static void main(String[] args) {
		LOGGER.info("Start clocking...");
		
		// start clocking system
		// start tasks management system
		// start storage management system
		CoreConfiguration.INSTANCE.autowire();
		
		// start observation system
		
		// start gui
		
		// make wait all threads to finish GUI thread
	}
}
