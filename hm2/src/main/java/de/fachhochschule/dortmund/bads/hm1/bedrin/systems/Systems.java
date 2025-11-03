package de.fachhochschule.dortmund.bads.hm1.bedrin.systems;

import de.fachhochschule.dortmund.bads.hm2.exceptions.SystemConfigurationException;

public enum Systems {
	CLOCKING,
	TASK_MANAGEMENT,
	STORAGE_MANAGEMENT,
	OBSERVATION;
	
	private Thread logic;
	
	public void build(SystemBuilder builder) {
		if (builder == null) {
			throw new SystemConfigurationException("SystemBuilder cannot be null");
		}
		if (builder.logicToInstall == null) {
			throw new SystemConfigurationException("Logic thread cannot be null for system: " + this.name());
		}
		logic = builder.logicToInstall;
	}
	
	public void start() {
		if (logic == null) {
			throw new SystemConfigurationException("Cannot start system " + this.name() + " - logic not configured");
		}
		logic.start();
	}
	
	public Thread getLogic() {
		return logic;
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