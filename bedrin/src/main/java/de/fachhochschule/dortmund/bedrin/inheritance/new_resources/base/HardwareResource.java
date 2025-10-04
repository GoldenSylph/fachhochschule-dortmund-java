package de.fachhochschule.dortmund.bedrin.inheritance.new_resources.base;

import de.fachhochschule.dortmund.bedrin.inheritance.Resource;

public abstract class HardwareResource extends NonHumanResource {

	private String hardwareType;
	
	public HardwareResource(double quantity, String hardwareType) {
		super(quantity, true);
		this.hardwareType = hardwareType;
	}

	public String getHardwareType() {
		return hardwareType;
	}

	@Override
	public <T extends Resource> Resource interchange(T intoWhat) {
		throw new UnsupportedOperationException("Cannot interchange hardware yet.");
	}

}
