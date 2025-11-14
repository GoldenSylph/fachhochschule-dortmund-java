package de.fachhochschule.dortmund.bads.resources;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.model.Area;
import de.fachhochschule.dortmund.bads.model.StorageCell;
import de.fachhochschule.dortmund.bads.model.Area.Point;
import de.fachhochschule.dortmund.bads.systems.logic.utils.ITickable;

public class Truck extends Resource implements ITickable {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private int currentLocationIdx = 0;
	private int ticksPerPointInRoute = 1;
	private List<Point> route;
	private List<String> routePointsDescriptions = new ArrayList<>();
	private Area city;
	private StorageCell inventoryCell;
	
	private Point startPoint;
	private Point destinationPoint;
	private int ticksSinceLastMove = 0;
	private boolean moving = false;
	
	public Truck(Area city) {
		this.city = city;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Truck created with city area");
		}
	}
	
	@Override
	public Resource call() throws Exception {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Truck call() method invoked - calculating route from {} to {}", this.startPoint, this.destinationPoint);
		}
		
		// Clear previous route data
		this.routePointsDescriptions.clear();
		this.currentLocationIdx = 0;
		this.ticksSinceLastMove = 0;
		
		if (this.startPoint == null || this.destinationPoint == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Cannot calculate route - start point: {}, destination point: {}", this.startPoint, this.destinationPoint);
			}
			return this;
		}
		
		this.route = this.city.findPath(this.startPoint, this.destinationPoint);
		
		if (this.route == null || this.route.isEmpty()) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("No route found from {} to {}", this.startPoint, this.destinationPoint);
			}
			return this;
		}
		
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Route calculated with {} points from {} to {}", this.route.size(), this.startPoint, this.destinationPoint);
		}
		
		for (int i = 0; i < this.route.size(); i++) {
			Point point = this.route.get(i);
			this.routePointsDescriptions.add("City point No. "+ (i + 1) + " at (" + point.x() + "," + point.y() + ")");
		}
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Route descriptions generated for {} points", this.route.size());
		}
		
		return this;
	}

	@Override
	public double getQuantity() {
		return 1.0;
	}

	@Override
	public void onTick(int currentTick) {
		if (!moving) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Truck is paused - not moving");
			}
			return;
		}
		if (this.route != null && !this.route.isEmpty()) {
			if (this.currentLocationIdx <= this.route.size() && ticksSinceLastMove < ticksPerPointInRoute) {
				ticksSinceLastMove++;
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("Truck waiting at current location - ticks since last move: {}/{}", ticksSinceLastMove, ticksPerPointInRoute);
				}
			} else {
				currentLocationIdx++;
				ticksSinceLastMove = 0;
				if (currentLocationIdx <= this.route.size()) {
					Point currentPoint = this.route.get(currentLocationIdx - 1);
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("Truck moved to point: {} (Route progress: {}/{})", currentPoint, currentLocationIdx, this.route.size());
					}
					if (currentLocationIdx >= this.route.size()) {
						if (LOGGER.isInfoEnabled()) {
							LOGGER.info("Truck reached destination: {}", this.destinationPoint);
						}
					}
				}
			}
		} else {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Truck has no route - skipping movement");
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
		Point previousStart = this.startPoint;
		this.startPoint = startPoint;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Truck start point changed from {} to {}", previousStart, startPoint);
		}
	}

	public void setDestinationPoint(Point destinationPoint) {
		Point previousDestination = this.destinationPoint;
		this.destinationPoint = destinationPoint;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Truck destination point changed from {} to {}", previousDestination, destinationPoint);
		}
	}

	public StorageCell getInventoryCell() {
		return inventoryCell;
	}

	public void setInventoryCell(StorageCell inventoryCell) {
		StorageCell previousCell = this.inventoryCell;
		this.inventoryCell = inventoryCell;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Truck inventory cell changed from {} to {}", previousCell, inventoryCell);
		}
	}

	/**
	 * Sets the number of ticks required for each point in the route.
	 * Higher values make the truck move slower through the city.
	 * 
	 * @param ticksPerPointInRoute the number of ticks per route point (must be positive)
	 */
	public void setTicksPerPointInRoute(int ticksPerPointInRoute) {
		if (ticksPerPointInRoute <= 0) {
			throw new IllegalArgumentException("Ticks per point in route must be positive");
		}
		int previousTicks = this.ticksPerPointInRoute;
		this.ticksPerPointInRoute = ticksPerPointInRoute;
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Truck movement speed changed from {} to {} ticks per route point", previousTicks, ticksPerPointInRoute);
		}
	}

	/**
	 * Gets the current number of ticks required for each route point.
	 * 
	 * @return the number of ticks per route point
	 */
	public int getTicksPerPointInRoute() {
		return ticksPerPointInRoute;
	}

	/**
	 * Gets the current location index in the route.
	 * 
	 * @return the current location index
	 */
	public int getCurrentLocationIdx() {
		return currentLocationIdx;
	}

	/**
	 * Gets the current route.
	 * 
	 * @return the current route as a list of points
	 */
	public List<Point> getRoute() {
		return route;
	}

	/**
	 * Gets the route descriptions.
	 * 
	 * @return the route point descriptions
	 */
	public List<String> getRoutePointsDescriptions() {
		return routePointsDescriptions;
	}

	/**
	 * Checks if the truck has reached its destination.
	 * 
	 * @return true if the truck has reached the destination, false otherwise
	 */
	public boolean hasReachedDestination() {
		boolean reached = this.route != null && this.currentLocationIdx >= this.route.size();
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Truck destination check - reached: {}, currentLocationIdx: {}, route size: {}", 
						reached, currentLocationIdx, route != null ? route.size() : 0);
		}
		return reached;
	}

	public boolean isMoving() {
		return moving;
	}

	public void setMoving(boolean moving) {
		this.moving = moving;
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Truck moving state set to {}", moving);
		}
	}
}
