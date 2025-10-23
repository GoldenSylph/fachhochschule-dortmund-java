package de.fachhochschule.dortmund.bads.hm1.bedrin;

import java.util.concurrent.Callable;

public abstract class Resource implements Callable<Resource> {
	public boolean isFungible() {
		return getQuantity() > 1.0;
	};

	public abstract double getQuantity();
}
