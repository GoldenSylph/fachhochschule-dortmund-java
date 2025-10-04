package de.fachhochschule.dortmund.bedrin.inheritance.new_resources.base;

import de.fachhochschule.dortmund.bedrin.inheritance.Resource;

public abstract class HumanResource extends Resource {

	private String name;
	
	public HumanResource(double quantity, String name) {
		super(quantity);
		this.name = name;
	}

	@Override
	public boolean isFungible() {
		return false;
	}
	
	public String getName() {
		return name;
	}

}
