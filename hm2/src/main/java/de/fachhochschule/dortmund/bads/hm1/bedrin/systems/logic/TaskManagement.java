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
		LOGGER.debug("Added task: {}. Total tasks: {}", task, this.tasks.size());
	}
	
	public synchronized Task getTask(int index) {
		return this.tasks.get(index);
	}
	
	public synchronized List<Task> getTasks() {
		return Collections.unmodifiableList(this.tasks);
	}
	
	public synchronized Task updateTask(int index, Task task) {
		Task previous = this.tasks.set(index, task);
		LOGGER.debug("Updated task at index {}: previous={}, current={}", index, previous, task);
		return previous;
	}
	
	public synchronized Task removeTask(int index) {
		return this.tasks.remove(index);
	}
	
	public synchronized boolean removeTask(Task task) {
		return this.tasks.remove(task);
	}
	
	public synchronized int getTasksCount() {
		return this.tasks.size();
	}
}