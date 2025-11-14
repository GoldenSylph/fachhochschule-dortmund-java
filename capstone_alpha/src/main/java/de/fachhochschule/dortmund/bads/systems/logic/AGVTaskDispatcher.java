package de.fachhochschule.dortmund.bads.systems.logic;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.model.Area.Point;
import de.fachhochschule.dortmund.bads.model.Storage;
import de.fachhochschule.dortmund.bads.model.Task;
import de.fachhochschule.dortmund.bads.resources.AGV;
import de.fachhochschule.dortmund.bads.resources.BeveragesBox;
import de.fachhochschule.dortmund.bads.resources.Resource;
import de.fachhochschule.dortmund.bads.systems.Operation;

/**
 * AGV Task Dispatcher - Assigns tasks to available AGVs and creates execution programs.
 * This class acts as the bridge between the Task Management system and the AGV fleet,
 * translating beverage orders into concrete AGV movement and operation instructions.
 */
public class AGVTaskDispatcher {
	private static final Logger LOGGER = LogManager.getLogger(AGVTaskDispatcher.class);

	private final List<AGV> agvFleet;
	private final Storage warehouse;
	private final ReadWriteLock lock;

	// Queue for tasks that were aborted due to low battery, waiting for reassignment
	private final Queue<Task> abortedTasks;

	/**
	 * Create a new AGV Task Dispatcher
	 *
	 * @param agvFleet the fleet of AGVs available for task assignment
	 * @param warehouse the warehouse storage system
	 */
	public AGVTaskDispatcher(List<AGV> agvFleet, Storage warehouse) {
		if (agvFleet == null || agvFleet.isEmpty()) {
			throw new IllegalArgumentException("AGV fleet cannot be null or empty");
		}
		if (warehouse == null) {
			throw new IllegalArgumentException("Warehouse cannot be null");
		}

		this.agvFleet = agvFleet;
		this.warehouse = warehouse;
		this.lock = new ReentrantReadWriteLock();
		this.abortedTasks = new ConcurrentLinkedQueue<>();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("AGVTaskDispatcher initialized with {} AGVs", agvFleet.size());
		}
	}

	/**
	 * Assign a task to an available AGV. Creates a program for the AGV to pick up
	 * a beverage box from storage and deliver it to the loading dock.
	 *
	 * @param task the task to be executed
	 * @param box the beverage box to be transported
	 * @return true if task was successfully assigned, false otherwise
	 */
	public boolean assignTaskToAGV(Task task, BeveragesBox box) {
		if (task == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Cannot assign null task");
			}
			return false;
		}

		if (box == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Cannot assign task {} with null beverage box", task.getTaskId());
			}
			return false;
		}

		lock.writeLock().lock();
		try {
			// Find an available AGV
			AGV availableAGV = findIdleAGV();

			if (availableAGV == null) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("No available AGV for task {} - all AGVs busy", task.getTaskId());
				}
				return false;
			}

			// Determine source cell based on beverage type
			String sourceCell = determineSourceCell(box.getType());

			// Determine destination cell (loading dock)
			String destinationCell = "6D"; // Loading dock position

			// Create AGV program to fulfill the task
			AGV.Statement<?>[] program = createTaskProgram(box, sourceCell, destinationCell);

			// Link task to AGV before execution (for abortion/reassignment tracking)
			availableAGV.setCurrentTask(task);

			// Execute the program on the selected AGV
			availableAGV.executeProgram(program);

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Assigned task {} to {} - Moving {} from {} to {}",
					task.getTaskId(), availableAGV.getAgvId(),
					box.getBeverageName(), sourceCell, destinationCell);
			}

			return true;

		} catch (Exception e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Failed to assign task {} to AGV: {}", task.getTaskId(), e.getMessage(), e);
			}
			return false;
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Find an idle AGV from the fleet.
	 *
	 * @return an idle AGV, or null if none available
	 */
	private AGV findIdleAGV() {
		for (AGV agv : agvFleet) {
			AGV.AGVState state = agv.getState();
			if (state == AGV.AGVState.IDLE) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Found idle AGV: {} (battery: {}%)", agv.getAgvId(), agv.getBatteryLevel());
				}
				return agv;
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("No idle AGVs found. Current states:");
			for (AGV agv : agvFleet) {
				LOGGER.debug("  {} - State: {}, Battery: {}%",
					agv.getAgvId(), agv.getState(), agv.getBatteryLevel());
			}
		}

		return null;
	}

	/**
	 * Determine which storage cell to pick up a beverage from based on its type.
	 *
	 * @param boxType the type of beverage box
	 * @return the cell notation (e.g., "1A", "4A", "7A")
	 */
	private String determineSourceCell(BeveragesBox.Type boxType) {
		return switch (boxType) {
			case AMBIENT -> "1A";        // Ambient storage area
			case REFRIGERATED -> "4A";   // Refrigerated storage area
			case BULK -> "7A";           // Bulk storage area
		};
	}

	/**
	 * Create an AGV program to transport a beverage box from source to destination.
	 *
	 *
	 * @param box the beverage box to transport
	 * @param sourceCell the source cell notation
	 * @param destinationCell the destination cell notation
	 * @return an array of AGV statements forming the program
	 */
	private AGV.Statement<?>[] createTaskProgram(BeveragesBox box, String sourceCell, String destinationCell) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Creating AGV program: {} → {} → {}",
				sourceCell, destinationCell, box.getBeverageName());
		}

		return new AGV.Statement<?>[] {
			// Move to source cell (pickup location)
			new AGV.Statement<>(AGV.Operand.PUSH, sourceCell),
			new AGV.Statement<>(AGV.Operand.MOVE),

			// TODO: Add TAKE operation here when inventory system is synchronized
			// new AGV.Statement<>(AGV.Operand.PUSH, box),
			// new AGV.Statement<>(AGV.Operand.PUSH, sourceCell),
			// new AGV.Statement<>(AGV.Operand.TAKE),

			// Move to destination cell (loading dock)
			new AGV.Statement<>(AGV.Operand.PUSH, destinationCell),
			new AGV.Statement<>(AGV.Operand.MOVE)

			// TODO: Add RELEASE operation here when inventory system is synchronized
			// new AGV.Statement<>(AGV.Operand.PUSH, box),
			// new AGV.Statement<>(AGV.Operand.PUSH, destinationCell),
			// new AGV.Statement<>(AGV.Operand.RELEASE)
		};
	}

	/**
	 * Get the number of AGVs in the fleet.
	 *
	 * @return the fleet size
	 */
	public int getFleetSize() {
		return agvFleet.size();
	}

	/**
	 * Get the number of currently idle AGVs.
	 *
	 * @return number of idle AGVs
	 */
	public int getIdleAGVCount() {
		lock.readLock().lock();
		try {
			return (int) agvFleet.stream()
				.filter(agv -> agv.getState() == AGV.AGVState.IDLE)
				.count();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Get statistics about the AGV fleet.
	 *
	 * @return formatted string with fleet statistics
	 */
	public String getFleetStatistics() {
		lock.readLock().lock();
		try {
			int total = agvFleet.size();
			int idle = 0;
			int busy = 0;
			int charging = 0;
			int waitingForCharge = 0;
			int movingToCharge = 0;

			for (AGV agv : agvFleet) {
				switch (agv.getState()) {
					case IDLE -> idle++;
					case BUSY -> busy++;
					case CHARGING -> charging++;
					case WAITING_FOR_CHARGE -> waitingForCharge++;
					case MOVING_TO_CHARGE -> movingToCharge++;
				}
			}

			return String.format(
				"AGV Fleet: Total=%d, Idle=%d, Busy=%d, Charging=%d, Waiting=%d, MovingToCharge=%d",
				total, idle, busy, charging, waitingForCharge, movingToCharge
			);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Called when an AGV aborts a task due to low battery.
	 * Adds the task to the reassignment queue.
	 *
	 * @param task the task that was aborted
	 * @param agv the AGV that aborted the task
	 */
	public void onTaskAborted(Task task, AGV agv) {
		if (task == null) {
			return;
		}

		if (LOGGER.isWarnEnabled()) {
			LOGGER.warn("Task T-{} aborted by {} (battery: {}%), queuing for reassignment",
				task.getTaskId(), agv.getAgvId(), agv.getBatteryLevel());
		}

		abortedTasks.add(task);
	}

	/**
	 * Attempt to reassign aborted tasks to available AGVs.
	 * This should be called periodically (e.g., from a monitoring thread or tick system).
	 */
	public void reassignAbortedTasks() {
		if (abortedTasks.isEmpty()) {
			return;
		}

		lock.writeLock().lock();
		try {
			// Process all aborted tasks in the queue
			while (!abortedTasks.isEmpty()) {
				Task task = abortedTasks.peek();  // Peek first, don't remove yet

				// Find an idle AGV
				AGV availableAGV = findIdleAGV();
				if (availableAGV == null) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("No available AGV for reassignment, {} tasks waiting",
							abortedTasks.size());
					}
					break;  // No AGVs available, stop processing
				}

				// Remove task from queue now that we have an AGV
				abortedTasks.poll();

				// Extract beverage box from task (same as original assignment)
				BeveragesBox box = extractBeverageBoxFromTask(task);
				if (box != null) {
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("Reassigning Task T-{} to {}", task.getTaskId(), availableAGV.getAgvId());
					}

					// Assign task to the available AGV
					assignTaskToAGV(task, box);
				} else {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("Could not extract beverage box from Task T-{}, cannot reassign",
							task.getTaskId());
					}
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Extract the BeveragesBox from a task for reassignment.
	 *
	 * @param task the task to extract from
	 * @return the beverage box, or null if not found
	 */
	private BeveragesBox extractBeverageBoxFromTask(Task task) {
		if (task == null || task.getProcessCount() == 0) {
			return null;
		}

		// Navigate through task -> process -> operation -> resource
		for (int i = 0; i < task.getProcessCount(); i++) {
			de.fachhochschule.dortmund.bads.systems.Process process = task.getProcess(i);
			if (process != null) {
				for (int j = 0; j < process.getOperationsCount(); j++) {
					Operation operation = process.getOperation(j);
					if (operation != null) {
						for (int k = 0; k < operation.getResourcesCount(); k++) {
							Resource resource = operation.getResource(k);
							if (resource instanceof BeveragesBox) {
								return (BeveragesBox) resource;
							}
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * Get the number of tasks waiting for reassignment.
	 *
	 * @return number of aborted tasks in queue
	 */
	public int getAbortedTaskCount() {
		return abortedTasks.size();
	}
}
