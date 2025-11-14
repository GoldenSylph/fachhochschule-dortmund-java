package de.fachhochschule.dortmund.bads.resources;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.model.Storage;
import de.fachhochschule.dortmund.bads.model.StorageCell;
import de.fachhochschule.dortmund.bads.model.Area.Point;
import de.fachhochschule.dortmund.bads.model.StorageCell.Type;
import de.fachhochschule.dortmund.bads.systems.logic.utils.ITickable;

public class AGV extends Resource implements ITickable {
	private static final Logger LOGGER = LogManager.getLogger();
	
	// Shared charging queue across all AGV instances
	private static final ConcurrentLinkedQueue<AGV> CHARGING_QUEUE = new ConcurrentLinkedQueue<>();
	private static final AtomicInteger AVAILABLE_CHARGING_STATIONS = new AtomicInteger(0);
	
	public enum AGVState {
		IDLE, BUSY, WAITING_FOR_CHARGE, CHARGING, MOVING_TO_CHARGE
	}

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
	private int chargePerTick = 10;
	private int loseChargePerActionPerTick = 5;
	private double batteryLowThreshold = 20.0; // Percentage
	private boolean charging;
	private AGVState state = AGVState.IDLE;
	private Point assignedChargingStation;
	private boolean needsCharging = false;
	private String agvId;
	private static AtomicInteger idCounter = new AtomicInteger(0);

	// Track current task being executed for abortion/reassignment
	private de.fachhochschule.dortmund.bads.model.Task currentTask = null;

	private int ticksPerMovement = 1;
	private int movementTickCounter = 0;

	private Queue<Point> endPoints = new ArrayDeque<>();
	private Queue<BeveragesBoxOperation> operationsForEndPoints = new ArrayDeque<>();
	private Stack<Object> memory = new Stack<>();

	private List<Point> optimalPath;
	private Point currentPosition;
	private Storage storage;

	private Statement<?>[] cachedProgram;
	
	public AGV() {
		this.agvId = "AGV-" + idCounter.incrementAndGet();
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Created new AGV with ID: {}", agvId);
		}
	}
	
	/**
	 * Initialize the charging queue system with the number of available charging stations.
	 * This should be called once during system initialization.
	 */
	public static void initializeChargingSystem(int numberOfChargingStations) {
		AVAILABLE_CHARGING_STATIONS.set(numberOfChargingStations);
		CHARGING_QUEUE.clear();
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("AGV Charging System initialized with {} charging stations", numberOfChargingStations);
		}
	}
	
	/**
	 * Get the current charging queue size.
	 */
	public static int getChargingQueueSize() {
		return CHARGING_QUEUE.size();
	}
	
	/**
	 * Get the number of available charging stations.
	 */
	public static int getAvailableChargingStations() {
		return AVAILABLE_CHARGING_STATIONS.get();
	}
	
	/**
	 * Request charging for this AGV. Adds to queue if no stations available.
	 */
	public synchronized void requestCharging() {
		if (state == AGVState.WAITING_FOR_CHARGE || state == AGVState.CHARGING || state == AGVState.MOVING_TO_CHARGE) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("{} already in charging process (state: {})", agvId, state);
			}
			return;
		}
		
		needsCharging = true;
		state = AGVState.WAITING_FOR_CHARGE;
		
		if (!CHARGING_QUEUE.contains(this)) {
			CHARGING_QUEUE.add(this);
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("{} added to charging queue (position: {}, queue size: {})", 
					agvId, CHARGING_QUEUE.size(), CHARGING_QUEUE.size());
			}
		}
	}
	
	/**
	 * Process the charging queue - attempt to assign charging stations to waiting AGVs.
	 */
	private void processChargingQueue() {
		if (storage == null) {
			return;
		}
		
		// Only process if we're at the front of the queue and waiting
		if (state != AGVState.WAITING_FOR_CHARGE) {
			return;
		}
		
		AGV firstInQueue = CHARGING_QUEUE.peek();
		if (firstInQueue != this) {
			return; // Not our turn yet
		}
		
		// Try to find an available charging station
		Point chargingStationPoint = storage.findAvailableChargingStation();
		
		if (chargingStationPoint != null) {
			// Occupy the station
			if (storage.occupyChargingStation(chargingStationPoint, this)) {
				assignedChargingStation = chargingStationPoint;
				CHARGING_QUEUE.poll(); // Remove from queue
				state = AGVState.MOVING_TO_CHARGE;
				
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("{} assigned charging station at {}, moving to charge", 
						agvId, Storage.pointToNotation(chargingStationPoint));
				}
				
				// Set up movement to charging station
				endPoints.clear();
				operationsForEndPoints.clear();
				endPoints.add(chargingStationPoint);
				operationsForEndPoints.add(new BeveragesBoxOperation(
					Storage.pointToNotation(chargingStationPoint), null) {
					@Override
					public void execute() {
						startCharging();
					}
				});
			}
		}
	}
	
	/**
	 * Start the charging process.
	 */
	private synchronized void startCharging() {
		if (state == AGVState.MOVING_TO_CHARGE) {
			charging = true;
			state = AGVState.CHARGING;
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("{} started charging at station {}, battery: {}%", 
					agvId, Storage.pointToNotation(assignedChargingStation), batteryLevel);
			}
		}
	}
	
	/**
	 * Complete charging and release the station.
	 */
	private synchronized void completeCharging() {
		if (assignedChargingStation != null && storage != null) {
			storage.releaseChargingStation(assignedChargingStation);
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("{} completed charging, released station {}, battery: {}%",
					agvId, Storage.pointToNotation(assignedChargingStation), batteryLevel);
			}
			assignedChargingStation = null;
		}

		charging = false;
		needsCharging = false;
		state = AGVState.IDLE;

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("{} charging complete, returning to IDLE state", agvId);
		}
	}

	/**
	 * Abort current task due to low battery and notify dispatcher for reassignment.
	 */
	private synchronized void abortCurrentTask() {
		if (currentTask == null) {
			return;
		}

		if (LOGGER.isWarnEnabled()) {
			LOGGER.warn("{} aborting Task T-{} due to low battery ({}%)",
				agvId, currentTask.getTaskId(), batteryLevel);
		}

		// Clear all pending operations and path
		memory.clear();
		endPoints.clear();
		operationsForEndPoints.clear();
		optimalPath = null;

		// Notify dispatcher to reassign task
		de.fachhochschule.dortmund.bads.systems.logic.AGVTaskDispatcher dispatcher =
			de.fachhochschule.dortmund.bads.CoreConfiguration.INSTANCE.getAGVTaskDispatcher();
		if (dispatcher != null) {
			dispatcher.onTaskAborted(currentTask, this);
		}

		currentTask = null;
		state = AGVState.IDLE;  // Set to IDLE so charging can proceed
	}

	/**
	 * Check if battery is low and automatically request charging if needed.
	 * Now monitors battery during all states except CHARGING.
	 */
	private void checkBatteryLevel() {
		// Skip if already charging or needs charging already queued
		if (needsCharging || state == AGVState.CHARGING) {
			return;
		}

		// Check battery level during any non-charging state
		if (batteryLevel <= batteryLowThreshold) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("{} battery critical ({}%) during state {}, initiating charging procedure",
					agvId, batteryLevel, state);
			}

			// If currently executing a task, abort it first
			if (currentTask != null && state == AGVState.BUSY) {
				abortCurrentTask();
			}

			// Request charging
			requestCharging();
		}
	}
	
	/**
	 * Get the current AGV state.
	 */
	public AGVState getState() {
		return state;
	}
	
	/**
	 * Get the AGV ID.
	 */
	public String getAgvId() {
		return agvId;
	}
	
	/**
	 * Get the current battery level.
	 */
	public int getBatteryLevel() {
		return batteryLevel;
	}

	/**
	 * Get the current task being executed by this AGV.
	 */
	public de.fachhochschule.dortmund.bads.model.Task getCurrentTask() {
		return currentTask;
	}

	/**
	 * Set the current task being executed by this AGV.
	 */
	public void setCurrentTask(de.fachhochschule.dortmund.bads.model.Task task) {
		this.currentTask = task;
	}

	/**
	 * Set the battery low threshold percentage.
	 */
	public void setBatteryLowThreshold(double threshold) {
		if (threshold < 0 || threshold > 100) {
			throw new IllegalArgumentException("Threshold must be between 0 and 100");
		}
		this.batteryLowThreshold = threshold;
	}
	
	/**
	 * Set the charge rate per tick.
	 */
	public void setChargePerTick(int chargePerTick) {
		if (chargePerTick <= 0) {
			throw new IllegalArgumentException("Charge per tick must be positive");
		}
		this.chargePerTick = chargePerTick;
	}
	
	/**
	 * Set the battery drain per action per tick.
	 */
	public void setLoseChargePerActionPerTick(int loseChargePerActionPerTick) {
		if (loseChargePerActionPerTick < 0) {
			throw new IllegalArgumentException("Charge loss must be non-negative");
		}
		this.loseChargePerActionPerTick = loseChargePerActionPerTick;
	}

	/**
	 * Get the current position of the AGV in the warehouse.
	 * @return the current position as a Point, or null if not yet positioned
	 */
	public Point getCurrentPosition() {
		return currentPosition;
	}

	/**
	 * Get the AGV's inventory cell containing cargo.
	 * @return the inventory cell storing carried items
	 */
	public StorageCell getInventoryCell() {
		return inventoryCell;
	}

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
		// Check battery level and request charging if needed
		checkBatteryLevel();
		
		// Process charging queue
		processChargingQueue();
		
		// Handle battery management
		if (charging) {
			batteryLevel = Math.min(100, batteryLevel + chargePerTick);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("{} charging: battery level now {}%", agvId, batteryLevel);
			}

			// Stop charging when fully charged
			if (batteryLevel >= 100) {
				completeCharging();
			}
			return; // Don't do anything else while charging
		}

		// Check if we have enough battery to continue
		// EXCEPTION: Allow movement to charging station even at 0% (emergency reserve power)
		if (batteryLevel <= 0 && state != AGVState.MOVING_TO_CHARGE) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("{} battery depleted, cannot perform actions", agvId);
			}

			// Abort current task if executing one
			if (currentTask != null && state == AGVState.BUSY) {
				abortCurrentTask();
			}

			if (!needsCharging) {
				requestCharging();
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

				// Don't drain battery when moving to charging station with emergency reserve
				if (state != AGVState.MOVING_TO_CHARGE || batteryLevel > 0) {
					batteryLevel = Math.max(0, batteryLevel - loseChargePerActionPerTick);
				}

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("{} moved to position: {}, battery: {}%", agvId, currentPosition, batteryLevel);
				}

				// If we've reached the end of the current path
				if (optimalPath.isEmpty()) {
					optimalPath = null;

					// Execute any pending operation at this destination
					if (!operationsForEndPoints.isEmpty()) {
						BeveragesBoxOperation operation = operationsForEndPoints.poll();
						try {
							operation.execute();

							// Don't drain battery for startCharging operation
							if (state != AGVState.CHARGING) {
								batteryLevel = Math.max(0, batteryLevel - loseChargePerActionPerTick);
							}

							if (LOGGER.isInfoEnabled()) {
								LOGGER.info("{} executed operation at position: {}, battery: {}%",
									agvId, currentPosition, batteryLevel);
							}
						} catch (Exception e) {
							LOGGER.error("{} failed to execute operation: {}", agvId, e.getMessage(), e);
						}
					}
				}
			}
		} else if (!endPoints.isEmpty() && storage != null) {
			// If we don't have a current path but have destinations to visit
			// Don't change state if already MOVING_TO_CHARGE (for charging flow)
			if (state != AGVState.MOVING_TO_CHARGE) {
				state = AGVState.BUSY;  // Mark as BUSY when starting new movement
			}

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
					LOGGER.info("{} calculated path to destination: {}, path length: {}",
						agvId, destination, (optimalPath != null ? optimalPath.size() : 0));
				}
			} else {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("{} no path found to destination: {}", agvId, destination);
				}
				// Remove the corresponding operation since we can't reach the destination
				if (!operationsForEndPoints.isEmpty()) {
					operationsForEndPoints.poll();
				}
			}
		} else if (endPoints.isEmpty() && state == AGVState.BUSY) {
			// All destinations reached, return to IDLE
			state = AGVState.IDLE;
			currentTask = null;  // Clear current task on completion
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("{} completed all movements, returning to IDLE state", agvId);
			}
		}
	}
}