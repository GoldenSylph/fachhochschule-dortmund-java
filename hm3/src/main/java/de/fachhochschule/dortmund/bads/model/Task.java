package de.fachhochschule.dortmund.bads.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.systems.Process;

/**
 * Task - Represents a unit of work to be executed by an AGV.
 * Contains multiple processes and can be prioritized.
 */
public class Task extends Thread {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final AtomicInteger TASK_ID_GENERATOR = new AtomicInteger(0);
	
	private final int id;
	private final List<Process> processes;
	private int priority;
	
	public Task() {
		this.id = TASK_ID_GENERATOR.incrementAndGet();
		this.processes = new ArrayList<>();
		this.priority = 0; // Default priority
		LOGGER.debug("Task {} created", id);
	}
	
	public Task(int priority) {
		this.id = TASK_ID_GENERATOR.incrementAndGet();
		this.processes = new ArrayList<>();
		this.priority = priority;
		LOGGER.debug("Task {} created with priority {}", id, priority);
	}
	
	@Override
	public void run() {
		LOGGER.info("Task {} started with {} processes", id, processes.size());
		
		long startTime = System.currentTimeMillis();
		int processCount = 0;
		
		for (Process process : this.processes) {
			processCount++;
			LOGGER.debug("Task {} executing process {}/{}", id, processCount, processes.size());
			
			try {
				process.processOperations();
				LOGGER.debug("Task {} completed process {}/{}", id, processCount, processes.size());
			} catch (Exception e) {
				LOGGER.error("Task {} error in process {}/{}: {}", id, processCount, processes.size(), e.getMessage(), e);
				throw e;
			}
		}
		
		long executionTime = System.currentTimeMillis() - startTime;
		LOGGER.info("Task {} completed. Executed {} processes in {}ms", id, processCount, executionTime);
	}
	
	public synchronized void addProcess(Process process) {
		this.processes.add(process);
		LOGGER.debug("Task {} added process. Total processes: {}", id, processes.size());
	}
	
	public synchronized Process getProcess(int index) {
		return this.processes.get(index);
	}
	
	public synchronized List<Process> getProcesses() {
		return Collections.unmodifiableList(this.processes);
	}
	
	public synchronized Process updateProcess(int index, Process process) {
		Process previous = this.processes.set(index, process);
		LOGGER.debug("Task {} updated process at index {}", id, index);
		return previous;
	}
	
	public synchronized Process removeProcess(int index) {
		Process removed = this.processes.remove(index);
		LOGGER.debug("Task {} removed process at index {}", id, index);
		return removed;
	}
	
	public synchronized boolean removeProcess(Process process) {
		boolean result = this.processes.remove(process);
		LOGGER.debug("Task {} removed process: {}", id, result ? "success" : "not found");
		return result;
	}
	
	public synchronized int getProcessCount() {
		return this.processes.size();
	}
	
	public int getTaskId() {
		return this.id;
	}
	
	public int getTaskPriority() {
		return this.priority;
	}
	
	public void setTaskPriority(int priority) {
		this.priority = priority;
	}
	
	@Override
	public String toString() {
		return String.format("Task[id=%d, priority=%d, processes=%d]", id, priority, processes.size());
	}
}