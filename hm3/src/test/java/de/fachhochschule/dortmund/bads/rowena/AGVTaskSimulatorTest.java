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

	// ===== Comprehensive SimulatedAGVTask Tests =====

	@Test
	@DisplayName("Test SimulatedAGVTask creation with fixed duration")
	void testSimulatedTaskCreationFixedDuration() {
		int taskId = 10;
		long submissionTime = System.currentTimeMillis();
		long duration = 250;

		SimulatedAGVTask task = new SimulatedAGVTask(taskId, submissionTime, duration);

		assertEquals(taskId, task.getTaskId());
		assertEquals(submissionTime, task.getSubmissionTime());
		assertEquals(duration, task.getExecutionDuration());
	}

	@Test
	@DisplayName("Test SimulatedAGVTask creation with variable duration")
	void testSimulatedTaskCreationVariableDuration() {
		int taskId = 20;
		long submissionTime = System.currentTimeMillis();
		long minDuration = 100;
		long maxDuration = 300;

		SimulatedAGVTask task = new SimulatedAGVTask(taskId, submissionTime, minDuration, maxDuration);

		assertEquals(taskId, task.getTaskId());
		assertEquals(submissionTime, task.getSubmissionTime());

		// Duration should be within range
		long duration = task.getExecutionDuration();
		assertTrue(duration >= minDuration && duration <= maxDuration,
				String.format("Duration %d should be between %d and %d", duration, minDuration, maxDuration));
	}

	@Test
	@DisplayName("Test SimulatedAGVTask execution via call method")
	void testSimulatedTaskExecution() throws Exception {
		long submissionTime = System.currentTimeMillis();
		long duration = 100;
		SimulatedAGVTask task = new SimulatedAGVTask(1, submissionTime, duration);

		long beforeCall = System.currentTimeMillis();
		TaskResult result = task.call();
		long afterCall = System.currentTimeMillis();

		// Verify result is not null
		assertNotNull(result);

		// Verify result contains correct task ID
		assertEquals(1, result.getTaskId());

		// Verify execution actually took approximately the expected duration
		long actualDuration = afterCall - beforeCall;
		assertTrue(actualDuration >= duration - 10 && actualDuration <= duration + 100,
				String.format("Actual duration %d should be close to expected %d", actualDuration, duration));

		// Verify timing fields
		assertTrue(result.getSubmissionTime() == submissionTime);
		assertTrue(result.getStartTime() >= submissionTime);
		assertTrue(result.getCompletionTime() >= result.getStartTime());
		assertTrue(result.getExecutionTime() >= duration - 10);
	}

	@Test
	@DisplayName("Test SimulatedAGVTask all getters")
	void testSimulatedTaskGetters() {
		int taskId = 15;
		long submissionTime = 5000;
		long duration = 200;

		SimulatedAGVTask task = new SimulatedAGVTask(taskId, submissionTime, duration);

		assertEquals(taskId, task.getTaskId());
		assertEquals(submissionTime, task.getSubmissionTime());
		assertEquals(duration, task.getExecutionDuration());
	}

	@Test
	@DisplayName("Test simulated task toString method")
	void testSimulatedTaskToString() {
		SimulatedAGVTask task = new SimulatedAGVTask(1, System.currentTimeMillis(), 500);

		String str = task.toString();
		assertNotNull(str);
		assertTrue(str.contains("SimulatedAGVTask"));
		assertTrue(str.contains("id=1"));
		assertTrue(str.contains("500"), "Should contain duration");
	}

	@Test
	@DisplayName("Test SimulatedAGVTask with zero duration")
	void testSimulatedTaskZeroDuration() throws Exception {
		SimulatedAGVTask task = new SimulatedAGVTask(1, System.currentTimeMillis(), 0);

		long beforeCall = System.currentTimeMillis();
		TaskResult result = task.call();
		long afterCall = System.currentTimeMillis();

		assertNotNull(result);
		assertEquals(1, result.getTaskId());

		// Should complete very quickly
		long actualDuration = afterCall - beforeCall;
		assertTrue(actualDuration < 50, "Zero duration task should complete quickly");
	}

	@Test
	@DisplayName("Test SimulatedAGVTask records correct AGV ID")
	void testSimulatedTaskRecordsAGVId() throws Exception {
		SimulatedAGVTask task = new SimulatedAGVTask(1, System.currentTimeMillis(), 50);

		TaskResult result = task.call();

		assertNotNull(result.getAssignedAGVId());
		assertTrue(result.getAssignedAGVId().contains("AGV") || result.getAssignedAGVId().length() > 0,
				"Should record the thread name as AGV ID");
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

	// ===== Comprehensive TaskResult Tests =====

	@Test
	@DisplayName("Test TaskResult all getters")
	void testTaskResultGetters() {
		int taskId = 42;
		long submissionTime = 5000;
		long startTime = 5100;
		long completionTime = 5600;
		String agvId = "AGV-7";

		TaskResult result = new TaskResult(taskId, submissionTime, startTime, completionTime, agvId);

		assertEquals(taskId, result.getTaskId());
		assertEquals(submissionTime, result.getSubmissionTime());
		assertEquals(startTime, result.getStartTime());
		assertEquals(completionTime, result.getCompletionTime());
		assertEquals(agvId, result.getAssignedAGVId());
	}

	@Test
	@DisplayName("Test TaskResult with zero wait time")
	void testTaskResultZeroWaitTime() {
		long time = 1000;
		TaskResult result = new TaskResult(1, time, time, time + 500, "AGV-1");

		assertEquals(0, result.getWaitTime(), "Wait time should be 0 when submission equals start time");
		assertEquals(500, result.getExecutionTime());
		assertEquals(500, result.getTotalTime());
	}

	@Test
	@DisplayName("Test TaskResult with instant execution")
	void testTaskResultInstantExecution() {
		long time = 1000;
		TaskResult result = new TaskResult(1, time, time, time, "AGV-1");

		assertEquals(0, result.getWaitTime());
		assertEquals(0, result.getExecutionTime(), "Execution time should be 0 for instant tasks");
		assertEquals(0, result.getTotalTime());
	}

	@Test
	@DisplayName("Test TaskResult timing relationships")
	void testTaskResultTimingRelationships() {
		long submissionTime = 2000;
		long startTime = 2300;
		long completionTime = 2800;

		TaskResult result = new TaskResult(1, submissionTime, startTime, completionTime, "AGV-1");

		// Verify mathematical relationships
		assertEquals(result.getTotalTime(), result.getWaitTime() + result.getExecutionTime(),
				"Total time should equal wait time plus execution time");
		assertEquals(startTime - submissionTime, result.getWaitTime());
		assertEquals(completionTime - startTime, result.getExecutionTime());
		assertEquals(completionTime - submissionTime, result.getTotalTime());
	}

	@Test
	@DisplayName("Test TaskResult with large values")
	void testTaskResultLargeValues() {
		long submissionTime = System.currentTimeMillis();
		long startTime = submissionTime + 10000;
		long completionTime = startTime + 50000;

		TaskResult result = new TaskResult(999, submissionTime, startTime, completionTime, "AGV-99");

		assertEquals(10000, result.getWaitTime());
		assertEquals(50000, result.getExecutionTime());
		assertEquals(60000, result.getTotalTime());
	}

	@Test
	@DisplayName("Test TaskResult toString contains all key information")
	void testTaskResultToStringContent() {
		TaskResult result = new TaskResult(5, 1000, 1200, 1700, "AGV-3");

		String str = result.toString();
		assertNotNull(str);
		assertTrue(str.contains("5"), "Should contain task ID");
		assertTrue(str.contains("AGV-3"), "Should contain AGV ID");
		assertTrue(str.contains("200"), "Should contain wait time");
		assertTrue(str.contains("500"), "Should contain execution time");
		assertTrue(str.contains("700"), "Should contain total time");
	}

	// ===== Scenario Tests (from AGVTaskSimulationDemo) =====

	@Test
	@DisplayName("Scenario 1: M > K (10 tasks, 3 AGVs) - demonstrates queuing and wait times")
	void testScenario1_MoreTasksThanAGVs() {
		int M = 10; // Number of tasks
		int K = 3;  // Number of AGVs

		AGVTaskSimulator scenarioSimulator = new AGVTaskSimulator(K);

		try {
			List<SimulatedAGVTask> tasks = generateTasksWithRange(M, 50, 100);
			List<TaskResult> results = scenarioSimulator.runSimulation(tasks);

			// Verify all tasks completed
			assertEquals(M, results.size());

			// With more tasks than AGVs, expect significant wait time
			long totalWaitTime = results.stream().mapToLong(TaskResult::getWaitTime).sum();
			assertTrue(totalWaitTime > 0, "Expected wait time when M > K");

			// All tasks should complete successfully
			for (TaskResult result : results) {
				assertNotNull(result);
				assertTrue(result.getExecutionTime() > 0);
				assertTrue(result.getTotalTime() >= result.getExecutionTime());
			}
		} finally {
			scenarioSimulator.shutdown();
		}
	}

	@Test
	@DisplayName("Scenario 2: M = K (5 tasks, 5 AGVs) - minimal wait time expected")
	void testScenario2_EqualTasksAndAGVs() {
		int M = 5; // Number of tasks
		int K = 5; // Number of AGVs

		AGVTaskSimulator scenarioSimulator = new AGVTaskSimulator(K);

		try {
			List<SimulatedAGVTask> tasks = generateTasksWithRange(M, 50, 100);
			List<TaskResult> results = scenarioSimulator.runSimulation(tasks);

			// Verify all tasks completed
			assertEquals(M, results.size());

			// With equal tasks and AGVs, expect minimal wait time
			// Most tasks should start immediately or very quickly
			long tasksWithLowWaitTime = results.stream()
					.filter(r -> r.getWaitTime() < 50)
					.count();
			assertTrue(tasksWithLowWaitTime >= M - 1,
					"Expected most tasks to have minimal wait time when M = K");

			// All tasks should complete successfully
			for (TaskResult result : results) {
				assertNotNull(result);
				assertTrue(result.getExecutionTime() > 0);
			}
		} finally {
			scenarioSimulator.shutdown();
		}
	}

	@Test
	@DisplayName("Scenario 3: M < K (3 tasks, 5 AGVs) - all tasks execute immediately")
	void testScenario3_FewerTasksThanAGVs() {
		int M = 3; // Number of tasks
		int K = 5; // Number of AGVs

		AGVTaskSimulator scenarioSimulator = new AGVTaskSimulator(K);

		try {
			List<SimulatedAGVTask> tasks = generateTasksWithRange(M, 50, 100);
			List<TaskResult> results = scenarioSimulator.runSimulation(tasks);

			// Verify all tasks completed
			assertEquals(M, results.size());

			// With fewer tasks than AGVs, all tasks should start immediately
			// Wait time should be very minimal (close to 0)
			for (TaskResult result : results) {
				assertNotNull(result);
				assertTrue(result.getWaitTime() < 50,
						"Expected minimal wait time when M < K");
				assertTrue(result.getExecutionTime() > 0);
			}
		} finally {
			scenarioSimulator.shutdown();
		}
	}

	@Test
	@DisplayName("Test custom scenario with variable duration tasks")
	void testCustomScenarioWithVariableDuration() {
		int numberOfTasks = 8;
		int numberOfAGVs = 4;
		long minDuration = 100;
		long maxDuration = 200;

		AGVTaskSimulator scenarioSimulator = new AGVTaskSimulator(numberOfAGVs);

		try {
			List<SimulatedAGVTask> tasks = generateTasksWithRange(numberOfTasks, minDuration, maxDuration);
			List<TaskResult> results = scenarioSimulator.runSimulation(tasks);

			// Verify all tasks completed
			assertEquals(numberOfTasks, results.size());

			// Verify execution times are within expected range
			for (TaskResult result : results) {
				assertNotNull(result);
				long execTime = result.getExecutionTime();
				assertTrue(execTime >= minDuration - 10 && execTime <= maxDuration + 50,
						String.format("Execution time %d should be within range [%d, %d]",
								execTime, minDuration, maxDuration));
			}
		} finally {
			scenarioSimulator.shutdown();
		}
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

	/**
	 * Helper method to generate a list of simulated tasks with variable duration.
	 *
	 * @param count number of tasks to generate
	 * @param minDuration minimum execution duration in milliseconds
	 * @param maxDuration maximum execution duration in milliseconds
	 * @return list of simulated tasks
	 */
	private List<SimulatedAGVTask> generateTasksWithRange(int count, long minDuration, long maxDuration) {
		List<SimulatedAGVTask> tasks = new ArrayList<>();
		long currentTime = System.currentTimeMillis();

		for (int i = 1; i <= count; i++) {
			tasks.add(new SimulatedAGVTask(i, currentTime, minDuration, maxDuration));
		}

		return tasks;
	}
}
