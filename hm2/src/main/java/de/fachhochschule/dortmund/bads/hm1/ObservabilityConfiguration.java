package de.fachhochschule.dortmund.bads.hm1;

public enum ObservabilityConfiguration implements IConfiguration {
	INSTANCE;
	
	@Override
	public IConfiguration autowire() {
		return this;
	}
}
