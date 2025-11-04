package de.fachhochschule.dortmund.bads.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.systems.Process;

public class Task extends Thread {
	private static final Logger LOGGER = LogManager.getLogger();
	
	// A list of processes that make up the task
	private final List<Process> processes;
	
	public Task() {
		this.processes = new ArrayList<>();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Task created with empty process list");
		}
	}
	
	@Override
	public void run() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Task started with {} processes to execute", this.processes.size());
		}
		
		long startTime = System.currentTimeMillis();
		int processCount = 0;
		
		for (Process process : this.processes) {
			processCount++;
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Executing process {}/{}: {}", processCount, this.processes.size(), process);
			}
			
			try {
				process.processOperations();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Successfully completed process {}/{}", processCount, this.processes.size());
				}
			} catch (Exception e) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Error executing process {}/{}: {}", processCount, this.processes.size(), e.getMessage(), e);
				}
				// Re-throw the exception to maintain original behavior
				throw e;
			}
		}
		
		long executionTime = System.currentTimeMillis() - startTime;
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Task completed. Executed {} processes in {}ms", processCount, executionTime);
		}
	}
	
	// CRUD operations for processes within this task
	public synchronized void addProcess(Process process) {
		this.processes.add(process);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Added process: {}. Total processes: {}", process, this.processes.size());
		}
	}
	
	public synchronized Process getProcess(int index) {
		return this.processes.get(index);
	}
	
	public synchronized List<Process> getProcesses() {
		return Collections.unmodifiableList(this.processes);
	}
	
	public synchronized Process updateProcess(int index, Process process) {
		Process previous = this.processes.set(index, process);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Updated process at index {}: previous={}, current={}", index, previous, process);
		}
		return previous;
	}
	
	public synchronized Process removeProcess(int index) {
		Process removed = this.processes.remove(index);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Removed process at index {}: {}. Remaining processes: {}", index, removed, this.processes.size());
		}
		return removed;
	}
	
	public synchronized boolean removeProcess(Process process) {
		boolean removed = this.processes.remove(process);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Attempted to remove process {}: success={}. Remaining processes: {}", process, removed, this.processes.size());
		}
		return removed;
	}
	
	public synchronized int getProcessesCount() {
		int count = this.processes.size();
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Process count requested: {}", count);
		}
		return count;
	}
}