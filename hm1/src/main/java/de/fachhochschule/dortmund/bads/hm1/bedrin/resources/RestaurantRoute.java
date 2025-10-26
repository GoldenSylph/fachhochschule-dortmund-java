package de.fachhochschule.dortmund.bads.hm1.bedrin.resources;

import java.util.List;

import de.fachhochschule.dortmund.bads.hm1.bedrin.Area.Point;

public class RestaurantRoute extends Resource {

	private Truck truck;
	private List<Point> route;
	private boolean isCompleted;
	
	@Override
	public Resource call() throws Exception {
		return this;
	}

	@Override
	public double getQuantity() {
		return 1.0;
	}

}
