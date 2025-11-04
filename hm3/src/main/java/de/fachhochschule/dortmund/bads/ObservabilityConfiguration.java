package de.fachhochschule.dortmund.bads;

public enum ObservabilityConfiguration implements IConfiguration {
	INSTANCE;
	
	@Override
	public IConfiguration autowire() {
		return this;
	}
}
