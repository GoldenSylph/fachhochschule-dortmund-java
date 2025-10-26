package de.fachhochschule.dortmund.bads.hm1.bedrin.resources;

public class Beverage extends Resource {

	@Override
	public Resource call() throws Exception {
		return this;
	}

	@Override
	public double getQuantity() {
		return 0;
	}

}
