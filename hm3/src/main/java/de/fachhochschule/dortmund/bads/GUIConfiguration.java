package de.fachhochschule.dortmund.bads;

public enum GUIConfiguration implements IConfiguration {
	INSTANCE;
	
	@Override
	public IConfiguration autowire() {
		return this;
	}
}
