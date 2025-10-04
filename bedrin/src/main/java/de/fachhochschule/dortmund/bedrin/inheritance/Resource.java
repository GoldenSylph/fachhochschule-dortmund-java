package de.fachhochschule.dortmund.bedrin.inheritance;

import java.util.concurrent.Callable;

public abstract class Resource implements Callable<Resource> {
	private double quantity;

	public Resource(double quantity) {
		this.quantity = quantity;
	}

	public abstract boolean isFungible();
	
	public double getQuantity() {
		return quantity;
	}
}
