package de.fachhochschule.dortmund.bads.hm1.bedrin.resources;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.hm1.bedrin.Area;
import de.fachhochschule.dortmund.bads.hm1.bedrin.Area.Point;
import de.fachhochschule.dortmund.bads.hm1.bedrin.StorageCell;
import de.fachhochschule.dortmund.bads.hm1.bedrin.systems.logic.ITickable;

public class Truck extends Resource implements ITickable {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private int currentLocationIdx = 0;
	private int ticksPerPointInRoute = 1;
	private List<Point> route;
	private List<String> routePointsDescriptions;
	private Area city;
	private StorageCell inventoryCell;
	
	private Point startPoint;
	private Point destinationPoint;
	private int ticksSinceLastMove = 0;
	
	public Truck(Area city) {
		this.city = city;
	}
	
	@Override
	public Resource call() throws Exception {
		this.route = this.city.findPath(this.startPoint, this.destinationPoint);
		for (int i = 0; i < this.route.size(); i++) {
			Point point = this.route.get(i);
			this.routePointsDescriptions.add("City point № "+ (i + 1) + "at (" + point.x() + "," + point.y() + ")");
		}
		return this;
	}

	@Override
	public double getQuantity() {
		return 1.0;
	}

	@Override
	public void onTick(int currentTick) {
		if (this.route != null && !this.route.isEmpty()) {
			if (this.currentLocationIdx <= this.route.size() && ticksSinceLastMove < ticksPerPointInRoute) {
				ticksSinceLastMove++;
			} else {
				currentLocationIdx++;
				ticksSinceLastMove = 0;
				LOGGER.info("Truck moved to point: " + this.route.get(currentLocationIdx - 1) + " (Route progress: " + currentLocationIdx + "/" + this.route.size() + ")");
			}
		}
	}

	public Point getStartPoint() {
		return startPoint;
	}

	public Point getDestinationPoint() {
		return destinationPoint;
	}

	public void setStartPoint(Point startPoint) {
		this.startPoint = startPoint;
	}

	public void setDestinationPoint(Point destinationPoint) {
		this.destinationPoint = destinationPoint;
	}

	public StorageCell getInventoryCell() {
		return inventoryCell;
	}

	public void setInventoryCell(StorageCell inventoryCell) {
		this.inventoryCell = inventoryCell;
	}

}
