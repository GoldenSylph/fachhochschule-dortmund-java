package de.fachhochschule.dortmund.bads.hm1.bedrin.resources;

import de.fachhochschule.dortmund.bads.hm1.bedrin.Area.Point;

public class Truck extends Resource {
	private Point currentLocation;
	private int ticksPerPointInRoute;
	private TruckRoute currentRoute;
	
	@Override
	public Resource call() throws Exception {
		return this;
	}

	@Override
	public double getQuantity() {
		return 1.0;
	}

}
