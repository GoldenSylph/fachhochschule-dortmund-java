package de.fachhochschule.dortmund.bedrin.inheritance.new_resources.base;

public abstract class HardwareResource extends NonHumanResource {

	private String hardwareType;
	
	public HardwareResource(double quantity, String hardwareType) {
		super(quantity, true);
		this.hardwareType = hardwareType;
	}

	public String getHardwareType() {
		return hardwareType;
	}
}
