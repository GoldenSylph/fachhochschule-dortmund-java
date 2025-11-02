package de.fachhochschule.dortmund.bads.hm1.bedrin.resources;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.hm1.bedrin.Area.Point;
import de.fachhochschule.dortmund.bads.hm1.bedrin.Storage;
import de.fachhochschule.dortmund.bads.hm1.bedrin.StorageCell;
import de.fachhochschule.dortmund.bads.hm1.bedrin.StorageCell.Type;
import de.fachhochschule.dortmund.bads.hm1.bedrin.systems.logic.ITickable;

public class AGV extends Resource implements ITickable {
	private static final Logger LOGGER = LogManager.getLogger();

	public enum Operand {
		PUSH, STOP, MOVE, TAKE, RELEASE, SETUP, CHARGE
	}

	public static class Statement<T> {
		public final Operand operand;
		public final T[] args;

		@SuppressWarnings("unchecked")
		public Statement(Operand operand, T... args) {
			this.operand = operand;
			this.args = args;
		}
	}

	private abstract class BeveragesBoxOperation {
		protected String cellLabel;
		protected BeveragesBox box;
		protected StorageCell inventoryCell = AGV.this.inventoryCell;
		protected Point currentPosition = AGV.this.currentPosition;
		protected Storage storage = AGV.this.storage;

		public BeveragesBoxOperation(String cellLabel, BeveragesBox box) {
			this.cellLabel = cellLabel;
			this.box = box;
		}

		public abstract void execute();
	}

	private StorageCell inventoryCell = new StorageCell(Type.ANY, Integer.MAX_VALUE, Integer.MAX_VALUE,
			Integer.MAX_VALUE);

	private int batteryLevel = 100;
	private int chargePerTick = 50;
	private int loseChargePerActionPerTick = 10;
	private boolean charging;

	private int ticksPerMovement = 2; // Number of ticks required for each movement
	private int movementTickCounter = 0; // Counter to track ticks for movement timing

	private Queue<Point> endPoints = new ArrayDeque<>();
	private Queue<BeveragesBoxOperation> operationsForEndPoints = new ArrayDeque<>();
	private Stack<Object> memory = new Stack<>();

	private List<Point> optimalPath;
	private Point currentPosition;
	private Storage storage;

	private Statement<?>[] cachedProgram;

	/**
	 * MOVE point -> label of point 
	 * TAKE label of point -> resource instance inside
	 * inventory cell 
	 * RELEASE label of point -> inventory cell releases resource
	 * instance
	 */

	public void executeProgram(Statement<?>[] program) {
		for (Statement<?> statement : program) {
			switch (statement.operand) {
			case STOP -> {
				memory.clear();
				endPoints.clear();
				operationsForEndPoints.clear();
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("AGV stopping execution and clearing memory.");
				}
				return;
			}
			case SETUP -> {
				storage = (Storage) statement.args[0];
				currentPosition = (Point) statement.args[1];
			}
			case PUSH -> {
				memory.push(statement.args[0]);
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("AGV pushed to memory: " + statement.args[0]);
				}
			}
			case MOVE -> {
				String pointLabel = (String) memory.pop();
				Point destination = Storage.notationToPoint(pointLabel);
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("AGV set to moving to point: " + destination);
				}
				endPoints.add(destination);
			}
			case CHARGE -> {
				String pointLabel = (String) memory.pop();
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("AGV will be charged at: " + pointLabel);
				}
				if (storage.getCellByNotation(pointLabel).TYPE == Type.CHARGING_STATION) {
					operationsForEndPoints.add(new BeveragesBoxOperation(pointLabel, null) {
						@Override
						public void execute() {
							if (LOGGER.isInfoEnabled()) {
								LOGGER.info("AGV charging at station: " + this.cellLabel);
							}
							AGV.this.charging = true;
						}
					});
				}
			}
			case RELEASE -> {
				String cellLabel = (String) memory.pop();
				BeveragesBox box = (BeveragesBox) memory.pop();
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("AGV releasing resource to cell: " + cellLabel);
				}
				this.operationsForEndPoints.add(new BeveragesBoxOperation(cellLabel, box) {
					@Override
					public void execute() {
						if (LOGGER.isInfoEnabled()) {
							LOGGER.info("AGV taking resource from cell: " + cellLabel);
						}
						if (cellLabel.equals(Storage.pointToNotation(currentPosition))) {
							if (!storage.getCellByNotation(cellLabel).add(box)) {
								throw new IllegalStateException(
										"Failed to take resource: insufficient space in inventory cell.");
							} else {
								this.inventoryCell.remove(box);
							}
						} else {
							throw new IllegalStateException("AGV not at the specified cell to take resource.");
						}
					}
				});
			}
			case TAKE -> {
				String cellLabel = (String) memory.pop();
				BeveragesBox box = (BeveragesBox) memory.pop();
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("AGV taking resource from cell: " + cellLabel);
				}
				this.operationsForEndPoints.add(new BeveragesBoxOperation(cellLabel, box) {
					@Override
					public void execute() {
						if (LOGGER.isInfoEnabled()) {
							LOGGER.info("AGV taking resource from cell: " + this.cellLabel);
						}
						if (this.cellLabel.equals(Storage.pointToNotation(this.currentPosition))) {
							if (!this.storage.getCellByNotation(this.cellLabel).remove(this.box)) {
								throw new IllegalStateException(
										"Failed to take resource: resource not found in storage cell.");
							} else {
								this.inventoryCell.add(this.box);
							}
						} else {
							throw new IllegalStateException("AGV not at the specified cell to take resource.");
						}
					}
				});
			}
			}
		}
	}

	public void cacheProgram(Statement<?>[] program) {
		this.cachedProgram = program;
	}

	/**
	 * Sets the number of ticks required for each movement. Higher values make the
	 * AGV move slower.
	 * 
	 * @param ticksPerMovement the number of ticks per movement (must be positive)
	 */
	public void setTicksPerMovement(int ticksPerMovement) {
		if (ticksPerMovement <= 0) {
			throw new IllegalArgumentException("Ticks per movement must be positive");
		}
		this.ticksPerMovement = ticksPerMovement;
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("AGV movement speed changed: {} ticks per movement", ticksPerMovement);
		}
	}

	/**
	 * Gets the current number of ticks required for each movement.
	 * 
	 * @return the number of ticks per movement
	 */
	public int getTicksPerMovement() {
		return ticksPerMovement;
	}

	@Override
	public Resource call() {
		if (this.cachedProgram != null) {
			executeProgram(this.cachedProgram);
		}
		return this;
	}

	@Override
	public double getQuantity() {
		return 1.0;
	}

	@Override
	public void onTick(int currentTick) {
		// Handle battery management
		if (charging) {
			batteryLevel = Math.min(100, batteryLevel + chargePerTick);
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("AGV charging: battery level now " + batteryLevel + "%");
			}

			// Stop charging when fully charged
			if (batteryLevel >= 100) {
				charging = false;
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("AGV fully charged, stopping charge");
				}
			}
			return; // Don't do anything else while charging
		}

		// Check if we have enough battery to continue
		if (batteryLevel <= 0) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("AGV battery depleted, cannot perform actions");
			}
			return;
		}

		// If we have a path to follow, move along it
		if (optimalPath != null && !optimalPath.isEmpty()) {
			// Increment movement tick counter
			movementTickCounter++;

			// Only move when we've reached the required number of ticks
			if (movementTickCounter >= ticksPerMovement) {
				// Reset counter and move to next point in path
				movementTickCounter = 0;
				currentPosition = optimalPath.remove(0);
				batteryLevel = Math.max(0, batteryLevel - loseChargePerActionPerTick);
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("AGV moved to position: " + currentPosition + ", battery: " + batteryLevel + "%");
				}

				// If we've reached the end of the current path
				if (optimalPath.isEmpty()) {
					optimalPath = null;

					// Execute any pending operation at this destination
					if (!operationsForEndPoints.isEmpty()) {
						BeveragesBoxOperation operation = operationsForEndPoints.poll();
						try {
							operation.execute();
							batteryLevel = Math.max(0, batteryLevel - loseChargePerActionPerTick);
							if (LOGGER.isInfoEnabled()) {
								LOGGER.info("AGV executed operation at position: " + currentPosition + ", battery: "
										+ batteryLevel + "%");
							}
						} catch (Exception e) {
							LOGGER.error("Failed to execute operation: " + e.getMessage(), e);
						}
					}
				}
			}
		} else if (!endPoints.isEmpty() && storage != null) {
			// If we don't have a current path but have destinations to visit
			
			Point destination = endPoints.poll();

			// Calculate optimal path to destination
			optimalPath = storage.AREA.findPath(currentPosition, destination);

			if (optimalPath != null && !optimalPath.isEmpty()) {
				// Remove the first point if it's our current position
				if (!optimalPath.isEmpty() && optimalPath.get(0).equals(currentPosition)) {
					optimalPath.remove(0);
				}
				// Reset movement counter when starting a new path
				movementTickCounter = 0;
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("AGV calculated path to destination: " + destination + ", path length: "
							+ (optimalPath != null ? optimalPath.size() : 0));
				}
			} else {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("No path found to destination: " + destination);
				}
				// Remove the corresponding operation since we can't reach the destination
				if (!operationsForEndPoints.isEmpty()) {
					operationsForEndPoints.poll();
				}
			}
		}
	}
}