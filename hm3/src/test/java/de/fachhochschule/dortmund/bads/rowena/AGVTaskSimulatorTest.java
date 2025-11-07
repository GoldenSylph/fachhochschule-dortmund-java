package de.fachhochschule.dortmund.bads.rowena;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the AGVTaskSimulator.
 */
public class AGVTaskSimulatorTest {

	private AGVTaskSimulator simulator;

	@BeforeEach
	void setUp() {
		// Create a simulator with 3 AGVs for most tests
		simulator = new AGVTaskSimulator(3);
	}

	@AfterEach
	void tearDown() {
		if (simulator != null) {
			simulator.shutdown();
		}
	}

	@Test
	@DisplayName("Test simulator creation with valid number of AGVs")
	void testSimulatorCreation() {
		AGVTaskSimulator sim = new AGVTaskSimulator(5);
		assertNotNull(sim);
		assertEquals(5, sim.getNumberOfAGVs());
		sim.shutdown();
	}

	@Test
	@DisplayName("Test simulator creation with invalid number of AGVs")
	void testSimulatorCreationInvalid() {
		assertThrows(IllegalArgumentException.class, () -> new AGVTaskSimulator(0));
		assertThrows(IllegalArgumentException.class, () -> new AGVTaskSimulator(-1));
	}

	@Test
	@DisplayName("Test running simulation with null tasks")
	void testRunSimulationWithNullTasks() {
		assertThrows(IllegalArgumentException.class, () -> simulator.runSimulation(null));
	}

	@Test
	@DisplayName("Test running simulation with empty task list")
	void testRunSimulationWithEmptyTasks() {
		List<SimulatedAGVTask> emptyTasks = new ArrayList<>();
		assertThrows(IllegalArgumentException.class, () -> simulator.runSimulation(emptyTasks));
	}

	@Test
	@DisplayName("Test simulation with M < K (fewer tasks than AGVs)")
	void testSimulationWithFewerTasksThanAGVs() {
		// M = 2, K = 3
		List<SimulatedAGVTask> tasks = generateTasks(2, 100);

		List<TaskResult> results = simulator.runSimulation(tasks);

		assertEquals(2, results.size());
		// All tasks should complete
		for (TaskResult result : results) {
			assertNotNull(result);
			assertTrue(result.getWaitTime() >= 0);
			assertTrue(result.getExecutionTime() > 0);
		}
	}

	@Test
	@DisplayName("Test simulation with M = K (equal tasks and AGVs)")
	void testSimulationWithEqualTasksAndAGVs() {
		// M = 3, K = 3
		List<SimulatedAGVTask> tasks = generateTasks(3, 100);

		List<TaskResult> results = simulator.runSimulation(tasks);

		assertEquals(3, results.size());
		for (TaskResult result : results) {
			assertNotNull(result);
			assertTrue(result.getWaitTime() >= 0);
			assertTrue(result.getExecutionTime() > 0);
		}
	}

	@Test
	@DisplayName("Test simulation with M > K (more tasks than AGVs)")
	void testSimulationWithMoreTasksThanAGVs() {
		// M = 10, K = 3
		List<SimulatedAGVTask> tasks = generateTasks(10, 100);

		List<TaskResult> results = simulator.runSimulation(tasks);

		assertEquals(10, results.size());

		// Some tasks should have wait times since we have more tasks than AGVs
		long totalWaitTime = results.stream().mapToLong(TaskResult::getWaitTime).sum();
		// With 10 tasks and 3 AGVs, we expect some waiting
		// At least the 4th task onwards should have some wait time
		assertTrue(totalWaitTime > 0, "Expected some wait time when M > K");

		// All tasks should complete
		for (TaskResult result : results) {
			assertNotNull(result);
			assertTrue(result.getExecutionTime() > 0);
		}
	}

	@Test
	@DisplayName("Test task result timing consistency")
	void testTaskResultTimingConsistency() {
		List<SimulatedAGVTask> tasks = generateTasks(5, 100);

		List<TaskResult> results = simulator.runSimulation(tasks);

		for (TaskResult result : results) {
			// Verify timing relationships
			assertTrue(result.getSubmissionTime() <= result.getStartTime(),
					"Submission time should be before or equal to start time");
			assertTrue(result.getStartTime() <= result.getCompletionTime(),
					"Start time should be before or equal to completion time");
			assertTrue(result.getTotalTime() == result.getWaitTime() + result.getExecutionTime(),
					"Total time should equal wait time plus execution time");
		}
	}

	@Test
	@DisplayName("Test concurrent task execution")
	void testConcurrentTaskExecution() {
		// Create 6 tasks with 200ms each for 3 AGVs
		List<SimulatedAGVTask> tasks = generateTasks(6, 200);

		long startTime = System.currentTimeMillis();
		List<TaskResult> results = simulator.runSimulation(tasks);
		long endTime = System.currentTimeMillis();
		long actualTime = endTime - startTime;

		assertEquals(6, results.size());

		// With 6 tasks of 200ms each and 3 AGVs running concurrently:
		// Expected time: ~400ms (2 batches of 3 tasks)
		// Allow some overhead for thread scheduling
		assertTrue(actualTime < 600, "Simulation should benefit from concurrency");
	}

	@Test
	@DisplayName("Test simulator shutdown")
	void testSimulatorShutdown() {
		List<SimulatedAGVTask> tasks = generateTasks(3, 50);
		simulator.runSimulation(tasks);

		// Shutdown should complete without errors
		simulator.shutdown();
		// Calling shutdown multiple times should be safe
		simulator.shutdown();
	}

	@Test
	@DisplayName("Test task result toString method")
	void testTaskResultToString() {
		long currentTime = System.currentTimeMillis();
		TaskResult result = new TaskResult(1, currentTime, currentTime + 100,
				currentTime + 600, "AGV-1");

		String str = result.toString();
		assertNotNull(str);
		assertTrue(str.contains("TaskResult"));
		assertTrue(str.contains("id=1"));
		assertTrue(str.contains("AGV-1"));
	}

	@Test
	@DisplayName("Test simulated task toString method")
	void testSimulatedTaskToString() {
		SimulatedAGVTask task = new SimulatedAGVTask(1, System.currentTimeMillis(), 500);

		String str = task.toString();
		assertNotNull(str);
		assertTrue(str.contains("SimulatedAGVTask"));
		assertTrue(str.contains("id=1"));
	}

	@Test
	@DisplayName("Test task result calculations")
	void testTaskResultCalculations() {
		long submissionTime = 1000;
		long startTime = 1100;
		long completionTime = 1600;

		TaskResult result = new TaskResult(1, submissionTime, startTime, completionTime, "AGV-1");

		assertEquals(100, result.getWaitTime());
		assertEquals(500, result.getExecutionTime());
		assertEquals(600, result.getTotalTime());
	}

	/**
	 * Helper method to generate a list of simulated tasks with fixed duration.
	 *
	 * @param count number of tasks to generate
	 * @param duration execution duration in milliseconds
	 * @return list of simulated tasks
	 */
	private List<SimulatedAGVTask> generateTasks(int count, long duration) {
		List<SimulatedAGVTask> tasks = new ArrayList<>();
		long currentTime = System.currentTimeMillis();

		for (int i = 1; i <= count; i++) {
			tasks.add(new SimulatedAGVTask(i, currentTime, duration));
		}

		return tasks;
	}
}
