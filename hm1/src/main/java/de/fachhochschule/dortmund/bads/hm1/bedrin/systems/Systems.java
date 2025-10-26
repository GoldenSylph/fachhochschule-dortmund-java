package de.fachhochschule.dortmund.bads.hm1.bedrin.systems;

public enum Systems {
	CLOCKING,
	TASK_MANAGEMENT,
	STORAGE_MANAGEMENT,
	OBSERVATION;
	
	private Thread logic;
	
	public void build(SystemBuilder builder) {
		logic = builder.logicToInstall;
	}
	
	public void start() {
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
			this.systemToSetup = system;
			return this;
		}
		
		public SystemBuilder logic(Thread logic) {
			this.logicToInstall = logic;
			return this;
		}
		
		public SystemBuilder build() {
			systemToSetup.build(this);
			return this;
		}
		
		public SystemBuilder buildAndStart() {
			systemToSetup.build(this);
			systemToSetup.start();
			return this;
		}
	}
}
