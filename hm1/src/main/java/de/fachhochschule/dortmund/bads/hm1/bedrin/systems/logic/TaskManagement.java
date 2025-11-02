package de.fachhochschule.dortmund.bads.hm1.bedrin.systems.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.hm1.bedrin.Task;

public class TaskManagement extends Thread {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private final List<Task> tasks;
	
	public TaskManagement() {
		this.tasks = new ArrayList<>();
	}
	
	@Override
	public void run() {
		LOGGER.info("Task Management System started...");
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