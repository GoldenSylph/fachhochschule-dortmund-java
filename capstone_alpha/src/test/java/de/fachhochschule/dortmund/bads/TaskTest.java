package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fachhochschule.dortmund.bads.model.Task;
import de.fachhochschule.dortmund.bads.systems.Operation;
import de.fachhochschule.dortmund.bads.systems.Process;

class TaskTest {
	
	private Task task;
	private Process process1;
	private Process process2;
	
	@BeforeEach
	void setUp() {
		task = new Task(5); // Priority 5
		
		// Create processes with operations
		process1 = new Process();
		process1.addOperation(new Operation());
		
		process2 = new Process();
		process2.addOperation(new Operation());
	}
	
	@Test
	void testTaskCreation() {
		assertNotNull(task);
		assertEquals(5, task.getTaskPriority());
	}
	
	@Test
	void testTaskWithDifferentPriorities() {
		Task highPriorityTask = new Task(10);
		Task lowPriorityTask = new Task(1);
		
		assertEquals(10, highPriorityTask.getTaskPriority());
		assertEquals(1, lowPriorityTask.getTaskPriority());
	}
	
	@Test
	void testAddProcess() {
		task.addProcess(process1);
		
		List<Process> processes = task.getProcesses();
		assertNotNull(processes);
		assertEquals(1, processes.size());
		assertTrue(processes.contains(process1));
	}
	
	@Test
	void testAddMultipleProcesses() {
		task.addProcess(process1);
		task.addProcess(process2);
		
		assertEquals(2, task.getProcesses().size());
	}
	
	@Test
	void testGetProcesses() {
		task.addProcess(process1);
		task.addProcess(process2);
		
		List<Process> processes = task.getProcesses();
		assertNotNull(processes);
		assertEquals(2, processes.size());
		assertTrue(processes.contains(process1));
		assertTrue(processes.contains(process2));
	}
	
	@Test
	void testGetProcess() {
		task.addProcess(process1);
		task.addProcess(process2);
		
		assertEquals(process1, task.getProcess(0));
		assertEquals(process2, task.getProcess(1));
	}
	
	@Test
	void testTaskRun() {
		task.addProcess(process1);
		
		// Should not throw exception
		assertDoesNotThrow(() -> task.run());
	}
	
	@Test
	void testEmptyTask() {
		Task emptyTask = new Task(5);
		assertTrue(emptyTask.getProcesses().isEmpty());
		assertEquals(0, emptyTask.getProcessCount());
	}
	
	@Test
	void testPriorityZero() {
		Task zeroTask = new Task(0);
		assertEquals(0, zeroTask.getTaskPriority());
	}
	
	@Test
	void testNegativePriority() {
		Task negativeTask = new Task(-5);
		assertEquals(-5, negativeTask.getTaskPriority());
	}
	
	@Test
	void testSetTaskPriority() {
		task.setTaskPriority(10);
		assertEquals(10, task.getTaskPriority());
		
		task.setTaskPriority(0);
		assertEquals(0, task.getTaskPriority());
	}
	
	@Test
	void testGetProcessCount() {
		assertEquals(0, task.getProcessCount());
		
		task.addProcess(process1);
		assertEquals(1, task.getProcessCount());
		
		task.addProcess(process2);
		assertEquals(2, task.getProcessCount());
	}
	
	@Test
	void testRemoveProcess() {
		task.addProcess(process1);
		task.addProcess(process2);
		
		assertTrue(task.removeProcess(process1));
		assertEquals(1, task.getProcessCount());
		assertFalse(task.removeProcess(process1)); // Already removed
	}
	
	@Test
	void testRemoveProcessByIndex() {
		task.addProcess(process1);
		task.addProcess(process2);
		
		Process removed = task.removeProcess(0);
		assertEquals(process1, removed);
		assertEquals(1, task.getProcessCount());
	}
	
	@Test
	void testUpdateProcess() {
		task.addProcess(process1);
		
		Process newProcess = new Process();
		newProcess.addOperation(new Operation());
		
		Process previous = task.updateProcess(0, newProcess);
		assertEquals(process1, previous);
		assertEquals(newProcess, task.getProcess(0));
	}
	
	@Test
	void testGetTaskId() {
		int id = task.getTaskId();
		assertTrue(id > 0);
		
		Task anotherTask = new Task(5);
		assertNotEquals(task.getTaskId(), anotherTask.getTaskId());
	}
	
	@Test
	void testTaskToString() {
		String str = task.toString();
		assertNotNull(str);
		assertTrue(str.contains("Task"));
		assertTrue(str.contains("priority"));
	}
	
	@Test
	void testTaskAsThread() {
		task.addProcess(process1);
		
		// Task extends Thread, so it should be runnable
		assertDoesNotThrow(() -> {
			task.start();
			task.join(1000);
		});
	}
	
	@Test
	void testDefaultConstructor() {
		Task defaultTask = new Task();
		assertEquals(0, defaultTask.getTaskPriority());
		assertTrue(defaultTask.getProcessCount() == 0);
	}
}