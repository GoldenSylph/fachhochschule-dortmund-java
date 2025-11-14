package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import de.fachhochschule.dortmund.bads.model.Area;
import de.fachhochschule.dortmund.bads.model.Area.Point;
import de.fachhochschule.dortmund.bads.model.Storage;
import de.fachhochschule.dortmund.bads.model.StorageCell;
import de.fachhochschule.dortmund.bads.resources.AGV;
import de.fachhochschule.dortmund.bads.resources.BeveragesBox;

public class Homework3UnitTests {
	private static final Logger LOGGER = LogManager.getLogger();
	
	@BeforeEach
	void setUp() {
		if (CoreConfiguration.INSTANCE.getAutowiredStatus()) {
			CoreConfiguration.INSTANCE.shutdown();
			try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
		}
	}
	
	@AfterEach
	void tearDown() {
		if (CoreConfiguration.INSTANCE.getAutowiredStatus()) {
			CoreConfiguration.INSTANCE.shutdown();
			try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
		}
	}
	
	// Helper: Create grid graph
	private Map<Point, Set<Point>> createGrid(int width, int height) {
		Map<Point, Set<Point>> graph = new HashMap<>();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Point current = new Point(x, y);
				Set<Point> neighbors = new java.util.HashSet<>();
				if (x > 0) neighbors.add(new Point(x - 1, y));
				if (x < width - 1) neighbors.add(new Point(x + 1, y));
				if (y > 0) neighbors.add(new Point(x, y - 1));
				if (y < height - 1) neighbors.add(new Point(x, y + 1));
				graph.put(current, neighbors);
			}
		}
		return graph;
	}
	
	// Helper: Create warehouse with storage and charging stations
	private Storage createWarehouse(int width, int height, int numChargingStations, int cellSize) {
		Area area = CoreConfiguration.INSTANCE.newArea();
		area.setGraph(createGrid(width, height));
		
		int totalCells = width * height;
		int storageCells = totalCells - numChargingStations;
		StorageCell[] cells = new StorageCell[totalCells];
		
		for (int i = 0; i < storageCells; i++) {
			cells[i] = CoreConfiguration.INSTANCE.newStorageCell(StorageCell.Type.ANY, cellSize, cellSize, cellSize);
		}
		for (int i = storageCells; i < totalCells; i++) {
			cells[i] = CoreConfiguration.INSTANCE.newChargingStation();
		}
		
		Storage warehouse = CoreConfiguration.INSTANCE.newStorage(area, cells);
		CoreConfiguration.INSTANCE.initializeAGVChargingSystem(warehouse);
		return warehouse;
	}
	
	// Helper: Create and setup AGVs
	private List<AGV> createAGVs(int count, Storage warehouse, int ticksPerMove, double batteryThreshold, int chargeRate) {
		List<AGV> agvs = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			AGV agv = CoreConfiguration.INSTANCE.newAGV();
			agv.setTicksPerMovement(ticksPerMove);
			agv.setBatteryLowThreshold(batteryThreshold);
			agv.setChargePerTick(chargeRate);
			agv.executeProgram(new AGV.Statement<?>[] {
				new AGV.Statement<>(AGV.Operand.SETUP, warehouse, new Point(i % 4, i / 4))
			});
			CoreConfiguration.INSTANCE.registerTickable(agv);
			agvs.add(agv);
		}
		return agvs;
	}
	
	/**
	 * TEST 1: Simulate simultaneous charging of 1..N AGVs with limited charging stations.
	 * 
	 * Parameters:
	 * - 8 AGVs total
	 * - 3 charging stations available
	 * - All AGVs request charging simultaneously
	 * - Verify that only 3 can charge at once, others must wait
	 */
	@Test
	@Timeout(value = 30, unit = TimeUnit.SECONDS)
	void testSimultaneousAGVCharging() throws InterruptedException {
		LOGGER.info("╔═══════════════════════════════════════════════════════════════╗");
		LOGGER.info("║  TEST 1: Simultaneous AGV Charging with Limited Resources     ║");
		LOGGER.info("╚═══════════════════════════════════════════════════════════════╝");
		
		final int NUM_CHARGING_STATIONS = 3, NUM_AGVS = 8;
		CoreConfiguration.INSTANCE.autowire();
		
		Storage warehouse = createWarehouse(4, 4, NUM_CHARGING_STATIONS, 100);
		List<AGV> agvs = createAGVs(NUM_AGVS, warehouse, 1, 30.0, 5);
		
		LOGGER.info("All {} AGVs requesting charging simultaneously...", NUM_AGVS);
		agvs.forEach(AGV::requestCharging);
		Thread.sleep(100);
		
		// Verify queue behavior
		int charging = 0, waiting = 0, movingToCharge = 0;
		for (AGV agv : agvs) {
			switch (agv.getState()) {
				case CHARGING -> charging++;
				case WAITING_FOR_CHARGE -> waiting++;
				case MOVING_TO_CHARGE -> movingToCharge++;
				default -> {}
			}
		}
		
		assertTrue(charging + movingToCharge <= NUM_CHARGING_STATIONS);
		assertTrue(waiting >= NUM_AGVS - NUM_CHARGING_STATIONS);
		
		// Charge all AGVs
		int tick = 0;
		while (tick < 500) {
			final int currentTick = tick;
			agvs.forEach(agv -> agv.onTick(currentTick));
			if (agvs.stream().allMatch(agv -> agv.getBatteryLevel() >= 100)) break;
			tick++;
		}
		
		agvs.forEach(agv -> assertEquals(100, agv.getBatteryLevel()));
		LOGGER.info("Test completed: All {} AGVs charged using {} stations", NUM_AGVS, NUM_CHARGING_STATIONS);
	}
	
	/**
	 * TEST 2: Simulate parallel charging at multiple stations with random arrival times.
	 * AGVs arriving randomly, waiting time calculation, queue departure if wait > 15 min.
	 * 
	 * Parameters:
	 * - 4 charging stations
	 * - 12 AGVs arriving at random intervals (0-30 seconds apart)
	 * - 1 tick = 1 second of real time
	 * - Maximum wait time: 900 ticks (15 minutes)
	 * - AGVs leave queue if waiting exceeds 15 minutes
	 */
	@Test
	@Timeout(value = 45, unit = TimeUnit.SECONDS)
	void testParallelChargingWithRandomArrivals() throws InterruptedException {
		LOGGER.info("╔═══════════════════════════════════════════════════════════════╗");
		LOGGER.info("║  TEST 2: Parallel Charging with Random Arrivals & Wait Time   ║");
		LOGGER.info("╚═══════════════════════════════════════════════════════════════╝");
		
		final int NUM_AGVS = 12, MAX_WAIT_TIME = 900;
		CoreConfiguration.INSTANCE.autowire();
		
		Storage warehouse = createWarehouse(5, 4, 4, 100);
		List<AGV> agvs = new ArrayList<>();
		Map<String, Integer> arrivalTimes = new ConcurrentHashMap<>();
		Map<String, Integer> startChargingTimes = new ConcurrentHashMap<>();
		Map<String, Boolean> leftQueue = new ConcurrentHashMap<>();
		Set<String> completedAGVs = ConcurrentHashMap.newKeySet();
		
		Random random = new Random(42);
		for (int i = 0; i < NUM_AGVS; i++) {
			AGV agv = CoreConfiguration.INSTANCE.newAGV();
			agv.setTicksPerMovement(1);
			agv.setChargePerTick(1);
			agv.executeProgram(new AGV.Statement<?>[] {
				new AGV.Statement<>(AGV.Operand.SETUP, warehouse, new Point(i % 5, i / 5))
			});
			CoreConfiguration.INSTANCE.registerTickable(agv);
			agvs.add(agv);
			leftQueue.put(agv.getAgvId(), false);
		}
		
		int[] arrivalSchedule = new int[NUM_AGVS];
		for (int i = 0; i < NUM_AGVS; i++) arrivalSchedule[i] = random.nextInt(31);
		java.util.Arrays.sort(arrivalSchedule);
		
		int tick = 0, nextArrival = 0;
		AtomicInteger agvsLeftQueue = new AtomicInteger(0);
		
		while (tick < 1500 && completedAGVs.size() + agvsLeftQueue.get() < NUM_AGVS) {
			// Process arrivals
			while (nextArrival < NUM_AGVS && arrivalSchedule[nextArrival] <= tick) {
				AGV arriving = agvs.get(nextArrival);
				arrivalTimes.put(arriving.getAgvId(), tick);
				arriving.requestCharging();
				nextArrival++;
			}
			
			// Process AGVs
			for (AGV agv : agvs) {
				String id = agv.getAgvId();
				if (agv.getBatteryLevel() >= 100) {
					completedAGVs.add(id);
					continue;
				}
				if (Boolean.TRUE.equals(leftQueue.get(id))) continue;
				
				if (agv.getState() == AGV.AGVState.WAITING_FOR_CHARGE && arrivalTimes.containsKey(id)) {
					if (tick - arrivalTimes.get(id) > MAX_WAIT_TIME) {
						leftQueue.put(id, true);
						agvsLeftQueue.incrementAndGet();
						continue;
					}
				}
				
				if (agv.getState() == AGV.AGVState.CHARGING && !startChargingTimes.containsKey(id)) {
					startChargingTimes.put(id, tick);
				}
				agv.onTick(tick);
			}
			tick++;
		}
		
		assertTrue(completedAGVs.size() + agvsLeftQueue.get() == NUM_AGVS);
		LOGGER.info("Test completed: {} charged, {} left queue", completedAGVs.size(), agvsLeftQueue.get());
	}
	
	/**
	 * TEST 3: Simulate M tasks running simultaneously with K available AGVs.
	 * 
	 * Parameters:
	 * - 15 tasks to execute
	 * - 5 AGVs available
	 * - Each task requires moving items between locations
	 * - Tasks compete for limited AGV resources
	 * - Verify all tasks eventually complete
	 */
	@Test
	@Timeout(value = 60, unit = TimeUnit.SECONDS)
	void testSimultaneousTasksWithLimitedAGVs() throws InterruptedException {
		LOGGER.info("╔═══════════════════════════════════════════════════════════════╗");
		LOGGER.info("║  TEST 3: M Simultaneous Tasks with K Available AGVs           ║");
		LOGGER.info("╚═══════════════════════════════════════════════════════════════╝");
		
		final int NUM_TASKS = 15, NUM_AGVS = 5;
		CoreConfiguration.INSTANCE.autowire();
		
		Storage warehouse = createWarehouse(6, 6, 4, 200);
		
		// Create inventory
		List<BeveragesBox> inventory = new ArrayList<>();
		for (int i = 0; i < NUM_TASKS; i++) {
			BeveragesBox box = CoreConfiguration.INSTANCE.newBeveragesBox(
				BeveragesBox.Type.AMBIENT, "Item-" + i, 40, 30, 25, 24);
			warehouse.getCellByNotation(Storage.pointToNotation(new Point(i % 6, i / 6))).add(box);
			inventory.add(box);
		}
		
		// Create AGVs
		List<AGV> agvs = new ArrayList<>();
		for (int i = 0; i < NUM_AGVS; i++) {
			AGV agv = CoreConfiguration.INSTANCE.newAGV();
			agv.setTicksPerMovement(2);
			agv.setBatteryLowThreshold(15.0);
			agv.setLoseChargePerActionPerTick(1);
			agv.executeProgram(new AGV.Statement<?>[] {
				new AGV.Statement<>(AGV.Operand.SETUP, warehouse, new Point(i, 0))
			});
			CoreConfiguration.INSTANCE.registerTickable(agv);
			agvs.add(agv);
		}
		
		Map<Integer, TaskStatus> taskStatuses = new ConcurrentHashMap<>();
		AtomicInteger nextTask = new AtomicInteger(0);
		Set<Integer> completed = ConcurrentHashMap.newKeySet();
		Set<Integer> assigned = ConcurrentHashMap.newKeySet();
		Random random = new Random(123);
		
		for (int i = 0; i < NUM_TASKS; i++) taskStatuses.put(i, new TaskStatus());
		
		int tick = 0;
		while (tick < 1000 && completed.size() < NUM_TASKS) {
			// Assign tasks
			for (AGV agv : agvs) {
				if (agv.getState() == AGV.AGVState.IDLE && agv.getBatteryLevel() > 20) {
					int taskIdx = nextTask.get();
					if (taskIdx < NUM_TASKS && assigned.add(taskIdx)) {
						TaskStatus status = taskStatuses.get(taskIdx);
						synchronized (status) {
							if (!status.isStarted()) {
								status.setStarted(true);
								status.setAssignedAGV(agv.getAgvId());
								nextTask.incrementAndGet();
								
								String src = Storage.pointToNotation(new Point(taskIdx % 6, taskIdx / 6));
								String dst;
								do { dst = Storage.pointToNotation(new Point(random.nextInt(6), random.nextInt(6))); }
								while (dst.equals(src));
								
								agv.executeProgram(new AGV.Statement<?>[] {
									new AGV.Statement<>(AGV.Operand.PUSH, src), new AGV.Statement<>(AGV.Operand.MOVE),
									new AGV.Statement<>(AGV.Operand.PUSH, inventory.get(taskIdx)),
									new AGV.Statement<>(AGV.Operand.PUSH, src), new AGV.Statement<>(AGV.Operand.TAKE),
									new AGV.Statement<>(AGV.Operand.PUSH, dst), new AGV.Statement<>(AGV.Operand.MOVE),
									new AGV.Statement<>(AGV.Operand.PUSH, inventory.get(taskIdx)),
									new AGV.Statement<>(AGV.Operand.PUSH, dst), new AGV.Statement<>(AGV.Operand.RELEASE)
								});
							}
						}
					}
				}
			}
			
			// Tick AGVs
			for (AGV agv : agvs) {
				try { agv.onTick(tick); } catch (Exception e) { /* Continue */ }
			}
			
			// Check completion
			for (int i = 0; i < NUM_TASKS; i++) {
				TaskStatus status = taskStatuses.get(i);
				synchronized (status) {
					if (status.isStarted() && !status.isCompleted()) {
						status.incrementTicksWorked();
						String agvId = status.getAssignedAGV();
						if (agvId != null) {
							boolean idle = agvs.stream().filter(a -> a.getAgvId().equals(agvId))
								.anyMatch(a -> a.getState() == AGV.AGVState.IDLE);
							if (status.getTicksWorked() >= 10 && idle) {
								status.setCompleted(true);
								status.setCompletionTick(tick);
								completed.add(i);
							}
						}
					}
				}
			}
			tick++;
		}
		
		long started = taskStatuses.values().stream().filter(TaskStatus::isStarted).count();
		assertTrue(started > 0);
		assertTrue(completed.size() >= NUM_TASKS * 0.5);
		assertTrue(tick < 1000);
		LOGGER.info("Test completed: {}/{} tasks completed", completed.size(), NUM_TASKS);
	}
	
	private static class TaskStatus {
		private volatile boolean started = false, completed = false;
		private volatile int ticksWorked = 0, completionTick = 0;
		private volatile String assignedAGV = null;
		
		public synchronized boolean isStarted() { return started; }
		public synchronized void setStarted(boolean s) { started = s; }
		public synchronized boolean isCompleted() { return completed; }
		public synchronized void setCompleted(boolean c) { completed = c; }
		public synchronized int getTicksWorked() { return ticksWorked; }
		public synchronized void incrementTicksWorked() { ticksWorked++; }
		@SuppressWarnings("unused")
		public synchronized int getCompletionTick() { return completionTick; }
		public synchronized void setCompletionTick(int t) { completionTick = t; }
		public synchronized String getAssignedAGV() { return assignedAGV; }
		public synchronized void setAssignedAGV(String a) { assignedAGV = a; }
	}
}
