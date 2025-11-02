package de.fachhochschule.dortmund.bads.hm1.bedrin.resources;

import java.util.concurrent.Callable;

public abstract class Resource implements Callable<Resource> {
	public boolean isFungible() {
		return Double.compare(1.0, getQuantity()) == 1;
	};

	public abstract double getQuantity();
}
