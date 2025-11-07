package de.fachhochschule.dortmund.bads.rowena;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Demonstration class for the AGV task simulation.
 * Shows various scenarios of M tasks competing for K AGVs.
 */
public class AGVTaskSimulationDemo {
	private static final Logger LOGGER = LogManager.getLogger();

	public static void main(String[] args) {
		LOGGER.info("===================================");
		LOGGER.info("AGV Task Simulation Demonstration");
		LOGGER.info("===================================");
		LOGGER.info("");

		// Run multiple scenarios
		runScenario1();
		LOGGER.info("");
		LOGGER.info("");

		runScenario2();
		LOGGER.info("");
		LOGGER.info("");

		runScenario3();
		LOGGER.info("");
		LOGGER.info("");

		LOGGER.info("===================================");
		LOGGER.info("All Demonstrations Complete");
		LOGGER.info("===================================");
	}

	/**
	 * Scenario 1: More tasks than AGVs (M > K)
	 * 10 tasks competing for 3 AGVs - demonstrates queuing and wait times
	 */
	private static void runScenario1() {
		LOGGER.info("--- Scenario 1: M > K (10 tasks, 3 AGVs) ---");
		LOGGER.info("Expected behavior: Tasks will queue and wait for AGVs");
		LOGGER.info("");

		int M = 10; // Number of tasks
		int K = 3;  // Number of AGVs

		AGVTaskSimulator simulator = new AGVTaskSimulator(K);

		try {
			List<SimulatedAGVTask> tasks = generateTasks(M, 500, 1000);
			simulator.runSimulation(tasks);
		} finally {
			simulator.shutdown();
		}
	}

	/**
	 * Scenario 2: Equal tasks and AGVs (M = K)
	 * 5 tasks for 5 AGVs - minimal wait time expected
	 */
	private static void runScenario2() {
		LOGGER.info("--- Scenario 2: M = K (5 tasks, 5 AGVs) ---");
		LOGGER.info("Expected behavior: All tasks start immediately, minimal wait time");
		LOGGER.info("");

		int M = 5; // Number of tasks
		int K = 5; // Number of AGVs

		AGVTaskSimulator simulator = new AGVTaskSimulator(K);

		try {
			List<SimulatedAGVTask> tasks = generateTasks(M, 500, 1000);
			simulator.runSimulation(tasks);
		} finally {
			simulator.shutdown();
		}
	}

	/**
	 * Scenario 3: Fewer tasks than AGVs (M < K)
	 * 3 tasks for 5 AGVs - all tasks execute immediately, some AGVs remain idle
	 */
	private static void runScenario3() {
		LOGGER.info("--- Scenario 3: M < K (3 tasks, 5 AGVs) ---");
		LOGGER.info("Expected behavior: All tasks execute immediately, low utilization");
		LOGGER.info("");

		int M = 3; // Number of tasks
		int K = 5; // Number of AGVs

		AGVTaskSimulator simulator = new AGVTaskSimulator(K);

		try {
			List<SimulatedAGVTask> tasks = generateTasks(M, 500, 1000);
			simulator.runSimulation(tasks);
		} finally {
			simulator.shutdown();
		}
	}

	/**
	 * Generates a list of simulated AGV tasks with random execution durations.
	 *
	 * @param count number of tasks to generate
	 * @param minDuration minimum execution duration in milliseconds
	 * @param maxDuration maximum execution duration in milliseconds
	 * @return list of simulated tasks
	 */
	private static List<SimulatedAGVTask> generateTasks(int count, long minDuration, long maxDuration) {
		List<SimulatedAGVTask> tasks = new ArrayList<>();
		long currentTime = System.currentTimeMillis();

		for (int i = 1; i <= count; i++) {
			SimulatedAGVTask task = new SimulatedAGVTask(i, currentTime, minDuration, maxDuration);
			tasks.add(task);
		}

		return tasks;
	}

	/**
	 * Custom scenario with configurable parameters.
	 * Can be used for testing specific configurations.
	 *
	 * @param numberOfTasks M - number of tasks
	 * @param numberOfAGVs K - number of AGVs
	 * @param minDuration minimum task duration in milliseconds
	 * @param maxDuration maximum task duration in milliseconds
	 */
	public static void runCustomScenario(int numberOfTasks, int numberOfAGVs,
										  long minDuration, long maxDuration) {
		LOGGER.info("--- Custom Scenario: {} tasks, {} AGVs ---", numberOfTasks, numberOfAGVs);

		AGVTaskSimulator simulator = new AGVTaskSimulator(numberOfAGVs);

		try {
			List<SimulatedAGVTask> tasks = generateTasks(numberOfTasks, minDuration, maxDuration);
			simulator.runSimulation(tasks);
		} finally {
			simulator.shutdown();
		}
	}
}
