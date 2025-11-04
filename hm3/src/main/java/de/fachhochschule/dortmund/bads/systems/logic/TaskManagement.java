package de.fachhochschule.dortmund.bads.systems.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.model.Task;
import de.fachhochschule.dortmund.bads.systems.Systems;

public class TaskManagement extends Thread {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private final List<Task> tasks;
	
	public TaskManagement() {
		this.tasks = new ArrayList<>();
	}
	
	@Override
	public void run() {
		LOGGER.info("Task Management System started...");
		
		try {
			while (!Thread.currentThread().isInterrupted() && isClockingAlive()) {
				// Keep the thread alive and perform periodic checks
				// You can add any periodic maintenance tasks here
				
				// Sleep for a short period to avoid busy waiting
				Thread.sleep(100); // 100ms sleep
			}
		} catch (InterruptedException e) {
			// Thread was interrupted, restore the interrupt flag
			Thread.currentThread().interrupt();
			LOGGER.info("Task Management System interrupted, shutting down...");
		}
		
		if (!isClockingAlive()) {
			LOGGER.info("CLOCKING system is no longer alive, Task Management System shutting down...");
		}
		
		LOGGER.info("Task Management System stopped.");
	}
	
	private boolean isClockingAlive() {
		Thread clockingThread = Systems.CLOCKING.getLogic();
		return clockingThread != null && clockingThread.isAlive();
	}
	
	public synchronized void addTask(Task task) {
		this.tasks.add(task);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Added task: {}. Total tasks: {}", task, this.tasks.size());
		}
	}
	
	public synchronized Task getTask(int index) {
		return this.tasks.get(index);
	}
	
	public synchronized List<Task> getTasks() {
		return Collections.unmodifiableList(this.tasks);
	}
	
	public synchronized Task updateTask(int index, Task task) {
		Task previous = this.tasks.set(index, task);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Updated task at index {}: previous={}, current={}", index, previous, task);
		}
		return previous;
	}
	
	public synchronized Task removeTask(int index) {
		Task removed = this.tasks.remove(index);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Removed task at index {}: {}", index, removed);
		}
		return removed;
	}
	
	public synchronized boolean removeTask(Task task) {
		boolean result = this.tasks.remove(task);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Removed task {}: {}", task, result ? "success" : "not found");
		}
		return result;
	}
	
	public synchronized int getTasksCount() {
		return this.tasks.size();
	}
}