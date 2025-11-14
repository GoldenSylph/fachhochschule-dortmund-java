package de.fachhochschule.dortmund.bads.systems;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.exceptions.SystemConfigurationException;

/**
 * Systems enum - defines all core systems in the application.
 * Each system runs in its own thread and can interact with others.
 */
public enum Systems {
	CLOCKING,           // Central timing system
	TASK_MANAGEMENT,    // Task lifecycle management
	STORAGE_MANAGEMENT, // Storage operations
	OBSERVATION;        // System monitoring
	
	private static final Logger LOGGER = LogManager.getLogger();
	private Thread logic;
	
	public void build(SystemBuilder builder) {
		if (builder == null) {
			throw new SystemConfigurationException("SystemBuilder cannot be null");
		}
		if (builder.logicToInstall == null) {
			throw new SystemConfigurationException("Logic thread cannot be null for system: " + this.name());
		}
		logic = builder.logicToInstall;
		LOGGER.debug("System {} built with logic: {}", this.name(), logic.getClass().getSimpleName());
	}
	
	public void start() {
		if (logic == null) {
			throw new SystemConfigurationException("Cannot start system " + this.name() + " - logic not configured");
		}
		logic.start();
		LOGGER.info("System {} started", this.name());
	}
	
	public void stop() {
		if (logic == null) {
			LOGGER.warn("Cannot stop system {} - logic not configured", this.name());
			return;
		}
		if (logic.isAlive()) {
			logic.interrupt();
			try {
				logic.join(5000); // Wait up to 5 seconds
				if (logic.isAlive()) {
					LOGGER.warn("System {} did not stop gracefully", this.name());
				} else {
					LOGGER.info("System {} stopped", this.name());
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOGGER.error("Interrupted while stopping system {}", this.name());
			}
		}
	}
	
	public Thread getLogic() {
		return logic;
	}
	
	public boolean isRunning() {
		return logic != null && logic.isAlive();
	}
	
	public static enum SystemBuilder {
		INSTANCE;
		
		private Thread logicToInstall;
		private Systems systemToSetup;

		public SystemBuilder system(Systems system) {
			if (system == null) {
				throw new SystemConfigurationException("System cannot be null");
			}
			this.systemToSetup = system;
			return this;
		}
		
		public SystemBuilder logic(Thread logic) {
			if (logic == null) {
				throw new SystemConfigurationException("Logic thread cannot be null");
			}
			this.logicToInstall = logic;
			return this;
		}
		
		public SystemBuilder build() {
			if (systemToSetup == null) {
				throw new SystemConfigurationException("No system specified for building");
			}
			systemToSetup.build(this);
			return this;
		}
		
		public SystemBuilder buildAndStart() {
			if (systemToSetup == null) {
				throw new SystemConfigurationException("No system specified for building and starting");
			}
			systemToSetup.build(this);
			systemToSetup.start();
			return this;
		}
	}
}