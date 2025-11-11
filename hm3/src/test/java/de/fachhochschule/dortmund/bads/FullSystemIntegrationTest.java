package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import de.fachhochschule.dortmund.bads.model.Area;
import de.fachhochschule.dortmund.bads.model.Area.Point;
import de.fachhochschule.dortmund.bads.model.Storage;
import de.fachhochschule.dortmund.bads.model.StorageCell;
import de.fachhochschule.dortmund.bads.model.StorageCell.Type;
import de.fachhochschule.dortmund.bads.model.Task;
import de.fachhochschule.dortmund.bads.resources.AGV;
import de.fachhochschule.dortmund.bads.resources.AGV.*;
import de.fachhochschule.dortmund.bads.resources.BeveragesBox;
import de.fachhochschule.dortmund.bads.resources.Truck;
import de.fachhochschule.dortmund.bads.systems.Operation;

class FullSystemIntegrationTest {
	private static final Logger LOGGER = LogManager.getLogger(FullSystemIntegrationTest.class);
	private CoreConfiguration coreConfig;
	private Storage warehouseA, warehouseB;
	private List<AGV> agvsA = new ArrayList<>(), agvsB = new ArrayList<>();
	private List<Truck> trucks = new ArrayList<>();
	private List<BeveragesBox> boxes = new ArrayList<>();

	@BeforeEach
	void setUp() {
		coreConfig = CoreConfiguration.INSTANCE;
		/*
		 * WAREHOUSE A LAYOUT (6x4 grid = 24 cells):
		 * ┌────┬────┬────┬────┬────┬────┐
		 * │ 0A │ 1A │ 2A │ 3A │ 4A │ 5A │  Row A (y=0)
		 * │    │    │    │    │    │ CH │  Cell 5 = Charging Station
		 * ├────┼────┼────┼────┼────┼────┤
		 * │ 0B │ 1B │ 2B │ 3B │ 4B │ 5B │  Row B (y=1)
		 * │    │ AM │ RE │ BU │    │    │  1=AMBIENT, 2=REFRIGERATED, 3=BULK
		 * ├────┼────┼────┼────┼────┼────┤
		 * │ 0C │ 1C │ 2C │ 3C │ 4C │ 5C │  Row C (y=2)
		 * │    │ AM │ RE │ BU │    │    │
		 * ├────┼────┼────┼────┼────┼────┤
		 * │ 0D │ 1D │ 2D │ 3D │ 4D │ 5D │  Row D (y=3)
		 * │    │ AM │ RE │ BU │    │ CH │  Cell 11 = Charging Station
		 * └────┴────┴────┴────┴────┴────┘
		 * START: (0,0) = Cell 0A - Entry/Exit Point
		 * 3 AGVs, 2 Charging Stations at cells 5 & 11
		 */
		warehouseA = setupWarehouse(6, 4, 24, 3, new int[] { 5, 11 }, agvsA);
		
		/*
		 * WAREHOUSE B LAYOUT (4x3 grid = 12 cells):
		 * ┌────┬────┬────┬────┐
		 * │ 0A │ 1A │ 2A │ 3A │  Row A (y=0)
		 * │    │ AM │ RE │ CH │  Cell 3 = Charging Station
		 * ├────┼────┼────┼────┤
		 * │ 0B │ 1B │ 2B │ 3B │  Row B (y=1)
		 * │    │ AM │ RE │ BU │  1=AMBIENT, 2=REFRIGERATED, 3=BULK
		 * ├────┼────┼────┼────┤
		 * │ 0C │ 1C │ 2C │ 3C │  Row C (y=2)
		 * │    │ AM │ RE │ BU │
		 * └────┴────┴────┴────┘
		 * START: (0,0) = Cell 0A - Entry/Exit Point
		 * 2 AGVs, 1 Charging Station at cell 3
		 */
		warehouseB = setupWarehouse(4, 3, 12, 2, new int[] { 3 }, agvsB);
		
		setupTrucks(3);
		setupBeverageBoxes();
		AGVManagementConfiguration.INSTANCE.setNumberOfAGVs(5).setBatteryLowThreshold(0.20)
				.setAutoChargingEnabled(true);
		TaskManagementConfiguration.INSTANCE.setMaxConcurrentTasks(10).setTaskPrioritizationEnabled(true);
		StorageManagementConfiguration.INSTANCE.setDefaultStorageCapacity(100).setAutoCompactionEnabled(true);
	}

	@AfterEach
	void tearDown() {
		agvsA.clear();
		agvsB.clear();
		trucks.clear();
		boxes.clear();
		if (coreConfig.getAutowiredStatus()) {
			try {
				coreConfig.shutdown();
				Thread.sleep(200);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private Area createGridArea(int width, int height) {
		Area area = coreConfig.newArea();
		Map<Point, Set<Point>> graph = new HashMap<>();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Point p = new Point(x, y);
				Set<Point> neighbors = new HashSet<>();
				if (x > 0)
					neighbors.add(new Point(x - 1, y));
				if (x < width - 1)
					neighbors.add(new Point(x + 1, y));
				if (y > 0)
					neighbors.add(new Point(x, y - 1));
				if (y < height - 1)
					neighbors.add(new Point(x, y + 1));
				graph.put(p, neighbors);
			}
		}
		area.setGraph(graph);
		area.setStart(0, 0);
		return area;
	}

	private Storage setupWarehouse(int w, int h, int cells, int agvs, int[] charging, List<AGV> agvList) {
		Area area = createGridArea(w, h);
		StorageCell[] cellArray = new StorageCell[cells];
		cellArray[0] = coreConfig.newStorageCell(Type.ANY, 100, 100, 100);
		for (int i = 1; i < cells; i++) {
			if (Arrays.binarySearch(charging, i) >= 0)
				cellArray[i] = coreConfig.newChargingStation();
			else
				cellArray[i] = coreConfig.newStorageCell(
						i % 5 == 1 ? Type.AMBIENT : i % 5 == 2 ? Type.REFRIGERATED : i % 5 == 3 ? Type.BULK : Type.ANY,
						i % 5 == 3 ? 80 : i % 5 == 0 ? 10 : 50, i % 5 == 3 ? 80 : i % 5 == 0 ? 10 : 50,
						i % 5 == 3 ? 80 : i % 5 == 0 ? 10 : 50);
		}
		Storage storage = coreConfig.newStorage(area, cellArray);
		coreConfig.initializeAGVChargingSystem(storage);
		for (int i = 0; i < agvs; i++) {
			AGV agv = coreConfig.newAGV();
			agv.setBatteryLowThreshold(20.0);
			agv.setChargePerTick(10);
			agv.setLoseChargePerActionPerTick(5);
			agv.executeProgram(new Statement<?>[] { new Statement<>(Operand.SETUP, storage, new Point(0, 0)) });
			agvList.add(agv);
		}
		return storage;
	}

	private void setupTrucks(int count) {
		Area cityArea = createGridArea(5, 5);
		cityArea.setStart(2, 2);
		for (int i = 0; i < count; i++) {
			Truck truck = coreConfig.newTruck(cityArea);
			truck.setInventoryCell(coreConfig.newStorageCell(Type.ANY, 200, 200, 200));
			trucks.add(truck);
		}
	}

	private void setupBeverageBoxes() {
		boxes.add(coreConfig.newBeveragesBox(BeveragesBox.Type.AMBIENT, "Water", 10, 10, 10, 24));
		boxes.add(coreConfig.newBeveragesBox(BeveragesBox.Type.AMBIENT, "Juice", 10, 10, 10, 24));
		boxes.add(coreConfig.newBeveragesBox(BeveragesBox.Type.AMBIENT, "Soda", 10, 10, 10, 24));
		boxes.add(coreConfig.newBeveragesBox(BeveragesBox.Type.REFRIGERATED, "Milk", 10, 10, 10, 12));
		boxes.add(coreConfig.newBeveragesBox(BeveragesBox.Type.REFRIGERATED, "Yogurt", 10, 10, 10, 12));
		boxes.add(coreConfig.newBeveragesBox(BeveragesBox.Type.BULK, "Beer", 20, 20, 20, 6));
		boxes.add(coreConfig.newBeveragesBox(BeveragesBox.Type.BULK, "Water Cooler", 20, 20, 20, 6));
	}

	@Test
	@Timeout(60)
	void testFullSystemIntegration() {
		if (!coreConfig.getAutowiredStatus())
			coreConfig.autowire();
		assertTrue(coreConfig.getAutowiredStatus());
		assertNotNull(coreConfig.getClockingSystem());
		assertNotNull(coreConfig.getTaskManagementSystem());

		int initialTaskCount = coreConfig.getTaskManagementSystem().getTasksCount();

		// Truck tasks
		Task[] truckTasks = {
				createTruckTask(trucks.get(0), List.of(boxes.get(0), boxes.get(1), boxes.get(3)), new Point(2, 2),
						new Point(0, 0), 10),
				createTruckTask(trucks.get(1), List.of(boxes.get(2), boxes.get(4)), new Point(2, 2), new Point(4, 4),
						10),
				createTruckTask(trucks.get(2), List.of(boxes.get(5), boxes.get(6)), new Point(2, 2), new Point(0, 0),
						9) };
		for (Task t : truckTasks)
			coreConfig.getTaskManagementSystem().addTask(t);
		executeTasks(Arrays.asList(truckTasks));
		simulateTicks(trucks, 10, 50);

		// AGV tasks
		Task[] agvTasks = { createAGVTask(agvsA.get(0), warehouseA, boxes.get(0), "1A", "2A", 7),
				createAGVTask(agvsA.get(1), warehouseA, boxes.get(1), "1A", "3A", 7),
				createAGVTask(agvsA.get(2), warehouseA, boxes.get(5), "1A", "1C", 6),
				createAGVTask(agvsB.get(0), warehouseB, boxes.get(2), "1A", "2A", 7),
				createAGVTask(agvsB.get(1), warehouseB, boxes.get(4), "1A", "3A", 8) };
		coreConfig.getTaskManagementSystem().addTasks(Arrays.asList(agvTasks));
		executeTasks(Arrays.asList(agvTasks));

		// Simulate operations
		for (int tick = 1; tick <= 50; tick++) {
			final int currentTick = tick;
			agvsA.forEach(agv -> agv.onTick(currentTick));
			agvsB.forEach(agv -> agv.onTick(currentTick));
			sleep(50);
		}

		// Test charging
		AGV testAGV = agvsA.get(0);
		testAGV.setBatteryLowThreshold(50.0);
		for (int i = 0; i < 30; i++)
			testAGV.onTick(50 + i + 1);

		// Verify
		verifyAGVs(agvsA);
		verifyAGVs(agvsB);
		assertEquals(2, warehouseA.getChargingStationCount());
		assertEquals(1, warehouseB.getChargingStationCount());
		assertEquals(initialTaskCount + 8, coreConfig.getTaskManagementSystem().getAllTasks().size());
	}

	@Test
	@Timeout(30)
	void testChargingQueueManagement() {
		coreConfig.initializeAGVChargingSystem(warehouseA);
		agvsA.forEach(agv -> agv.setBatteryLowThreshold(80.0));
		simulateTicks(agvsA, 30, 50);
		long charged = agvsA.stream()
				.filter(agv -> agv.getState() == AGV.AGVState.CHARGING || agv.getBatteryLevel() > 80).count();
		LOGGER.info("Charging test: {}/{} AGVs charged", charged, agvsA.size());
	}

	@Test
	void testConfiguration() {
		// Warehouses
		assertNotNull(warehouseA);
		assertEquals(2, warehouseA.getChargingStationCount());
		assertEquals(24, warehouseA.AREA.getAdjacencyMap().size());
		assertEquals(3, agvsA.size());
		assertNotNull(warehouseB);
		assertEquals(1, warehouseB.getChargingStationCount());
		assertEquals(12, warehouseB.AREA.getAdjacencyMap().size());
		assertEquals(2, agvsB.size());
		verifyAGVs(agvsA);
		verifyAGVs(agvsB);

		// Beverages
		assertEquals(7, boxes.size());
		assertEquals(BeveragesBox.Type.AMBIENT, boxes.get(0).getType());
		assertEquals("Water", boxes.get(0).getBeverageName());
		assertEquals(BeveragesBox.Type.BULK, boxes.get(5).getType());
	}

	@Test
	void testTaskManagement() {
		if (!coreConfig.getAutowiredStatus())
			coreConfig.autowire();
		int initial = coreConfig.getTaskManagementSystem().getTasksCount();
		Task[] tasks = { coreConfig.newTask(10), coreConfig.newTask(5), coreConfig.newTask(1) };
		for (Task t : tasks)
			coreConfig.getTaskManagementSystem().addTask(t);
		assertEquals(initial + 3, coreConfig.getTaskManagementSystem().getAllTasks().size());
		List<Task> sorted = coreConfig.getTaskManagementSystem().getTasksByPriority();
		assertTrue(sorted.indexOf(tasks[0]) < sorted.indexOf(tasks[1]));
		assertTrue(sorted.indexOf(tasks[1]) < sorted.indexOf(tasks[2]));
		coreConfig.shutdown();
	}

	private Task createTruckTask(Truck truck, List<BeveragesBox> cargo, Point start, Point dest, int priority) {
		Task task = coreConfig.newTask(priority);
		Operation loadOp = new Operation(), routeOp = new Operation(), unloadOp = new Operation();
		loadOp.addResource(truck);
		cargo.forEach(box -> {
			truck.getInventoryCell().add(box);
			loadOp.addResource(box);
		});
		truck.setStartPoint(start);
		truck.setDestinationPoint(dest);
		routeOp.addResource(truck);
		unloadOp.addResource(truck);
		cargo.forEach(unloadOp::addResource);
		task.addProcess(coreConfig.newProcess(List.of(loadOp, routeOp, unloadOp)));
		return task;
	}

	private Task createAGVTask(AGV agv, Storage storage, BeveragesBox box, String src, String tgt, int priority) {
		Task task = coreConfig.newTask(priority);
		StorageCell source = storage.getCellByNotation(src);
		if (source != null)
			source.add(box);
		Operation pickupOp = new Operation(), deliveryOp = new Operation();
		pickupOp.addResource(agv);
		pickupOp.addResource(box);
		deliveryOp.addResource(agv);
		deliveryOp.addResource(box);
		task.addProcess(coreConfig.newProcess(List.of(pickupOp, deliveryOp)));
		agv.cacheProgram(new Statement<?>[] { new Statement<>(Operand.SETUP, storage, Storage.notationToPoint(src)),
				new Statement<>(Operand.PUSH, box), new Statement<>(Operand.PUSH, src), new Statement<>(Operand.TAKE),
				new Statement<>(Operand.PUSH, tgt), new Statement<>(Operand.MOVE), new Statement<>(Operand.PUSH, box),
				new Statement<>(Operand.PUSH, tgt), new Statement<>(Operand.RELEASE) });
		try {
			agv.call();
		} catch (Exception e) {
			LOGGER.error("AGV program failed", e);
		}
		return task;
	}

	private void executeTasks(List<Task> tasks) {
		tasks.forEach(task -> new Thread(task::run, "Task-" + task.getTaskId()) {
			{
				setDaemon(true);
				start();
			}
		});
	}

	private <T> void simulateTicks(List<T> entities, int ticks, long sleepMs) {
		for (int tick = 1; tick <= ticks; tick++) {
			final int currentTick = tick;
			for (T entity : entities) {
				if (entity instanceof Truck)
					((Truck) entity).onTick(currentTick);
				else if (entity instanceof AGV)
					((AGV) entity).onTick(currentTick);
			}
			sleep(sleepMs);
		}
	}

	private void verifyAGVs(List<AGV> agvs) {
		agvs.forEach(agv -> {
			assertNotNull(agv.getAgvId());
			assertTrue(agv.getBatteryLevel() >= 0 && agv.getBatteryLevel() <= 100);
			assertNotNull(agv.getState());
		});
	}

	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}