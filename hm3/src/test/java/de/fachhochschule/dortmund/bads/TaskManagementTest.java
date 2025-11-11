package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fachhochschule.dortmund.bads.model.Task;
import de.fachhochschule.dortmund.bads.systems.Operation;
import de.fachhochschule.dortmund.bads.systems.Process;
import de.fachhochschule.dortmund.bads.systems.logic.TaskManagement;

class TaskManagementTest {
	
	private TaskManagement taskManagement;
	private Task task1;
	private Task task2;
	private Task task3;
	
	@BeforeEach
	void setUp() throws InterruptedException {
		// Initialize CoreConfiguration to make Systems available
		CoreConfiguration.INSTANCE.autowire();
		Thread.sleep(100); // Give systems time to start
		
		taskManagement = new TaskManagement();
		
		// Create tasks with different priorities
		task1 = new Task(10); // High priority
		task2 = new Task(5);  // Medium priority
		task3 = new Task(1);  // Low priority
		
		// Add processes to tasks
		Process process = new Process();
		process.addOperation(new Operation());
		
		task1.addProcess(process);
		task2.addProcess(process);
		task3.addProcess(process);
	}
	
	@AfterEach
	void tearDown() {
		// Shutdown CoreConfiguration after each test
		if (CoreConfiguration.INSTANCE.getAutowiredStatus()) {
			CoreConfiguration.INSTANCE.shutdown();
		}
	}
	
	@Test
	void testTaskManagementCreation() {
		assertNotNull(taskManagement);
	}
	
	@Test
	void testAddTask() {
		taskManagement.addTask(task1);
		assertEquals(1, taskManagement.getTasksCount());
	}
	
	@Test
	void testAddMultipleTasks() {
		taskManagement.addTask(task1);
		taskManagement.addTask(task2);
		taskManagement.addTask(task3);
		
		assertEquals(3, taskManagement.getTasksCount());
	}
	
	@Test
	void testGetTasksCount() {
		assertEquals(0, taskManagement.getTasksCount());
		
		taskManagement.addTask(task1);
		assertEquals(1, taskManagement.getTasksCount());
		
		taskManagement.addTask(task2);
		assertEquals(2, taskManagement.getTasksCount());
	}
	
	@Test
	void testGetAllTasks() {
		taskManagement.addTask(task1);
		taskManagement.addTask(task2);
		
		List<Task> tasks = taskManagement.getAllTasks();
		assertNotNull(tasks);
		assertEquals(2, tasks.size());
	}
	
	@Test
	void testGetTasksByPriority() {
		taskManagement.addTask(task2); // Medium
		taskManagement.addTask(task1); // High
		taskManagement.addTask(task3); // Low
		
		List<Task> sortedTasks = taskManagement.getTasksByPriority();
		assertNotNull(sortedTasks);
		assertEquals(3, sortedTasks.size());
		
		// Should be sorted by priority (highest first)
		assertEquals(10, sortedTasks.get(0).getTaskPriority());
		assertEquals(5, sortedTasks.get(1).getTaskPriority());
		assertEquals(1, sortedTasks.get(2).getTaskPriority());
	}
	
	@Test
	void testEmptyTaskManagement() {
		assertEquals(0, taskManagement.getTasksCount());
		assertTrue(taskManagement.getAllTasks().isEmpty());
	}
	
	@Test
	void testRunSystem() {
		taskManagement.addTask(task1);
		
		// Should not throw exception
		assertDoesNotThrow(() -> {
			Thread thread = new Thread(taskManagement);
			thread.start();
			Thread.sleep(100);
			thread.interrupt();
		});
	}
	
	@Test
	void testConcurrentTaskAddition() throws InterruptedException {
		// Test thread safety
		Thread t1 = new Thread(() -> {
			for (int i = 0; i < 10; i++) {
				taskManagement.addTask(new Task(i));
			}
		});
		
		Thread t2 = new Thread(() -> {
			for (int i = 10; i < 20; i++) {
				taskManagement.addTask(new Task(i));
			}
		});
		
		t1.start();
		t2.start();
		t1.join();
		t2.join();
		
		assertEquals(20, taskManagement.getTasksCount());
	}
}