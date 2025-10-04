package de.fachhochschule.dortmund.bedrin.inheritance.new_resources.base;

import de.fachhochschule.dortmund.bedrin.inheritance.Resource;

public abstract class NonHumanResource extends Resource {
	
	private boolean fungible;
	
	public NonHumanResource(double quantity, boolean fungible) {
		super(quantity);
		this.fungible = fungible;
	}

	@Override
	public boolean isFungible() {
		return fungible;
	}
	
}
