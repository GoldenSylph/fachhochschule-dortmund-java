package de.fachhochschule.dortmund.bads.hm1.bedrin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.hm1.bedrin.systems.Process;

public class Task extends Thread {
	private static final Logger LOGGER = LogManager.getLogger();
	
	// A list of processes that make up the task
	private final List<Process> processes;
	
	public Task() {
		this.processes = new ArrayList<>();
	}
	
	@Override
	public void run() {
		LOGGER.info("Task started...");
		for (Process process : this.processes) {
			process.processOperations();
		}
	}
	
	// CRUD operations for processes within this task
	public synchronized void addProcess(Process process) {
		this.processes.add(process);
		LOGGER.debug("Added process: {}. Total processes: {}", process, this.processes.size());
	}
	
	public synchronized Process getProcess(int index) {
		return this.processes.get(index);
	}
	
	public synchronized List<Process> getProcesses() {
		return Collections.unmodifiableList(this.processes);
	}
	
	public synchronized Process updateProcess(int index, Process process) {
		Process previous = this.processes.set(index, process);
		LOGGER.debug("Updated process at index {}: previous={}, current={}", index, previous, process);
		return previous;
	}
	
	public synchronized Process removeProcess(int index) {
		return this.processes.remove(index);
	}
	
	public synchronized boolean removeProcess(Process process) {
		return this.processes.remove(process);
	}
	
	public synchronized int getProcessesCount() {
		return this.processes.size();
	}
}