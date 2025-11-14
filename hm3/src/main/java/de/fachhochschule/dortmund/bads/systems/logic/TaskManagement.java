package de.fachhochschule.dortmund.bads.systems.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.TaskManagementConfiguration;
import de.fachhochschule.dortmund.bads.model.Task;
import de.fachhochschule.dortmund.bads.systems.logic.utils.ITickable;

/**
 * Task Management System - Manages task lifecycle with CRUD operations and prioritization.
 * This system is responsible for:
 * - Creating, Reading, Updating, and Deleting tasks
 * - Maintaining task priority queue
 * - Providing sorted and filtered task lists
 * - Task status tracking
 */
public class TaskManagement extends Thread implements ITickable {
	private static final Logger LOGGER = LogManager.getLogger(TaskManagement.class.getName());
	
	private final List<Task> allTasks;
	private final PriorityQueue<Task> priorityQueue;
	private final ReadWriteLock lock;
	private volatile boolean running = true;
	
	public TaskManagement() {
		super("TaskManagement-Thread");
		this.allTasks = new ArrayList<>();
		this.lock = new ReentrantReadWriteLock();
		
		// Priority queue: higher priority values come first
		this.priorityQueue = new PriorityQueue<>(
			Comparator.comparingInt(Task::getTaskPriority).reversed()
		);
		
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("TaskManagement initialized with priority-based queue");
		}
	}
	
	@Override
	public void run() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Task Management System started");
		}
		
		long systemStartTime = System.currentTimeMillis();
		int cyclesExecuted = 0;
		
		try {
			while (running && !Thread.currentThread().isInterrupted()) {
				long cycleStartTime = System.currentTimeMillis();
				
				// Periodic maintenance: rebuild priority queue if needed
				maintainPriorityQueue();
				
				long cycleDuration = System.currentTimeMillis() - cycleStartTime;
				cyclesExecuted++;
				
				if (cycleDuration > 100 && LOGGER.isWarnEnabled()) {
					LOGGER.warn("Task management cycle {} took {}ms - performance degradation detected", 
							cyclesExecuted, cycleDuration);
				}
				
				Thread.sleep(1000); // Maintenance cycle every second
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Task Management System interrupted after {} cycles", cyclesExecuted);
			}
		}
		
		long totalRuntime = System.currentTimeMillis() - systemStartTime;
		if (LOGGER.isInfoEnabled()) {
			double avgTime = cyclesExecuted > 0 ? (double)totalRuntime / cyclesExecuted : 0.0;
			LOGGER.info("Task Management System stopped after {} cycles in {}ms (avg: {:.2f}ms per cycle)", 
					cyclesExecuted, totalRuntime, String.format("%.2f", avgTime));
		}
	}
	
	@Override
	public void onTick(int currentTick) {
		// Log status periodically
		if (currentTick % 10 == 0) {
			lock.readLock().lock();
			try {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Tick {} - Task Status - Total: {}, Priority Queue: {}", 
							currentTick, allTasks.size(), priorityQueue.size());
				}
			} finally {
				lock.readLock().unlock();
			}
		}
		
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("TaskManagement tick {} processed", currentTick);
		}
	}
	
	/**
	 * Maintain the priority queue - rebuild if tasks have been modified
	 */
	private void maintainPriorityQueue() {
		if (TaskManagementConfiguration.INSTANCE.isTaskPrioritizationEnabled()) {
			lock.writeLock().lock();
			try {
				// Rebuild priority queue to ensure it's consistent with current task priorities
				priorityQueue.clear();
				priorityQueue.addAll(allTasks);
				
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("Priority queue rebuilt with {} tasks", priorityQueue.size());
				}
			} finally {
				lock.writeLock().unlock();
			}
		}
	}
	
	// ==================== CREATE Operations ====================
	
	/**
	 * Add a new task to the system.
	 * @param task the task to add
	 * @return true if task was added successfully
	 */
	public boolean addTask(Task task) {
		if (task == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Attempted to add null task - ignoring");
			}
			return false;
		}
		
		lock.writeLock().lock();
		try {
			boolean added = allTasks.add(task);
			if (added && TaskManagementConfiguration.INSTANCE.isTaskPrioritizationEnabled()) {
				priorityQueue.offer(task);
			}
			
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Added task: {} - Total tasks: {}", task, allTasks.size());
			}
			return added;
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * Add multiple tasks at once.
	 * @param tasks the tasks to add
	 * @return number of tasks successfully added
	 */
	public int addTasks(List<Task> tasks) {
		if (tasks == null || tasks.isEmpty()) {
			return 0;
		}
		
		lock.writeLock().lock();
		try {
			int addedCount = 0;
			for (Task task : tasks) {
				if (task != null) {
					allTasks.add(task);
					if (TaskManagementConfiguration.INSTANCE.isTaskPrioritizationEnabled()) {
						priorityQueue.offer(task);
					}
					addedCount++;
				}
			}
			
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Added {} tasks in batch - Total tasks: {}", addedCount, allTasks.size());
			}
			return addedCount;
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	// ==================== READ Operations ====================
	
	/**
	 * Get a task by its ID.
	 * @param taskId the task ID
	 * @return the task, or null if not found
	 */
	public Task getTaskById(int taskId) {
		lock.readLock().lock();
		try {
			return allTasks.stream()
					.filter(t -> t.getTaskId() == taskId)
					.findFirst()
					.orElse(null);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * Get a task by index in the list.
	 * @param index the index
	 * @return the task at the specified index
	 */
	public Task getTask(int index) {
		lock.readLock().lock();
		try {
			if (index < 0 || index >= allTasks.size()) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Invalid task index: {} (size: {})", index, allTasks.size());
				}
				return null;
			}
			return allTasks.get(index);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * Get all tasks (unmodifiable list).
	 * @return unmodifiable list of all tasks
	 */
	public List<Task> getAllTasks() {
		lock.readLock().lock();
		try {
			return Collections.unmodifiableList(new ArrayList<>(allTasks));
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * Get tasks sorted by priority (highest first).
	 * @return list of tasks sorted by priority
	 */
	public List<Task> getTasksByPriority() {
		lock.readLock().lock();
		try {
			return allTasks.stream()
					.sorted(Comparator.comparingInt(Task::getTaskPriority).reversed())
					.collect(Collectors.toList());
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * Get the highest priority task (peek from priority queue).
	 * @return the highest priority task, or null if no tasks
	 */
	public Task getHighestPriorityTask() {
		lock.readLock().lock();
		try {
			return priorityQueue.peek();
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * Get tasks with a specific priority level.
	 * @param priority the priority level to filter by
	 * @return list of tasks with the specified priority
	 */
	public List<Task> getTasksByPriorityLevel(int priority) {
		lock.readLock().lock();
		try {
			return allTasks.stream()
					.filter(t -> t.getTaskPriority() == priority)
					.collect(Collectors.toList());
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * Get tasks with priority greater than or equal to specified level.
	 * @param minPriority minimum priority level
	 * @return list of tasks with priority >= minPriority
	 */
	public List<Task> getTasksWithMinPriority(int minPriority) {
		lock.readLock().lock();
		try {
			return allTasks.stream()
					.filter(t -> t.getTaskPriority() >= minPriority)
					.sorted(Comparator.comparingInt(Task::getTaskPriority).reversed())
					.collect(Collectors.toList());
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * Get the total count of tasks.
	 * @return number of tasks
	 */
	public int getTasksCount() {
		lock.readLock().lock();
		try {
			return allTasks.size();
		} finally {
			lock.readLock().unlock();
		}
	}
	
	// ==================== UPDATE Operations ====================
	
	/**
	 * Update a task's priority.
	 * @param taskId the task ID
	 * @param newPriority the new priority value
	 * @return true if task was found and updated
	 */
	public boolean updateTaskPriority(int taskId, int newPriority) {
		lock.writeLock().lock();
		try {
			Task task = allTasks.stream()
					.filter(t -> t.getTaskId() == taskId)
					.findFirst()
					.orElse(null);
			
			if (task != null) {
				int oldPriority = task.getTaskPriority();
				task.setTaskPriority(newPriority);
				
				// Rebuild priority queue to reflect the change
				if (TaskManagementConfiguration.INSTANCE.isTaskPrioritizationEnabled()) {
					priorityQueue.remove(task);
					priorityQueue.offer(task);
				}
				
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Updated task {} priority: {} -> {}", taskId, oldPriority, newPriority);
				}
				return true;
			}
			
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Task {} not found for priority update", taskId);
			}
			return false;
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * Replace a task at a specific index.
	 * @param index the index
	 * @param newTask the new task
	 * @return the previous task, or null if index invalid
	 */
	public Task updateTask(int index, Task newTask) {
		if (newTask == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Attempted to update with null task - ignoring");
			}
			return null;
		}
		
		lock.writeLock().lock();
		try {
			if (index < 0 || index >= allTasks.size()) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Invalid task index for update: {} (size: {})", index, allTasks.size());
				}
				return null;
			}
			
			Task oldTask = allTasks.set(index, newTask);
			
			// Update priority queue
			if (TaskManagementConfiguration.INSTANCE.isTaskPrioritizationEnabled()) {
				priorityQueue.remove(oldTask);
				priorityQueue.offer(newTask);
			}
			
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Updated task at index {}: {} -> {}", index, oldTask, newTask);
			}
			return oldTask;
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	// ==================== DELETE Operations ====================
	
	/**
	 * Remove a task by its ID.
	 * @param taskId the task ID
	 * @return the removed task, or null if not found
	 */
	public Task removeTaskById(int taskId) {
		lock.writeLock().lock();
		try {
			Task task = allTasks.stream()
					.filter(t -> t.getTaskId() == taskId)
					.findFirst()
					.orElse(null);
			
			if (task != null) {
				allTasks.remove(task);
				priorityQueue.remove(task);
				
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Removed task by ID: {} - Remaining tasks: {}", taskId, allTasks.size());
				}
				return task;
			}
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Task {} not found for removal", taskId);
			}
			return null;
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * Remove a task by index.
	 * @param index the index
	 * @return the removed task, or null if index invalid
	 */
	public Task removeTask(int index) {
		lock.writeLock().lock();
		try {
			if (index < 0 || index >= allTasks.size()) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Invalid task index for removal: {} (size: {})", index, allTasks.size());
				}
				return null;
			}
			
			Task removed = allTasks.remove(index);
			priorityQueue.remove(removed);
			
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Removed task at index {}: {} - Remaining: {}", index, removed, allTasks.size());
			}
			return removed;
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * Remove a specific task instance.
	 * @param task the task to remove
	 * @return true if task was found and removed
	 */
	public boolean removeTask(Task task) {
		if (task == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Attempted to remove null task - ignoring");
			}
			return false;
		}
		
		lock.writeLock().lock();
		try {
			boolean removed = allTasks.remove(task);
			if (removed) {
				priorityQueue.remove(task);
				
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Removed task: {} - Remaining: {}", task, allTasks.size());
				}
			} else {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Task {} not found for removal", task);
				}
			}
			return removed;
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * Remove all tasks with a specific priority level.
	 * @param priority the priority level
	 * @return number of tasks removed
	 */
	public int removeTasksByPriority(int priority) {
		lock.writeLock().lock();
		try {
			List<Task> tasksToRemove = allTasks.stream()
					.filter(t -> t.getTaskPriority() == priority)
					.collect(Collectors.toList());
			
			int removedCount = 0;
			for (Task task : tasksToRemove) {
				if (allTasks.remove(task)) {
					priorityQueue.remove(task);
					removedCount++;
				}
			}
			
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Removed {} tasks with priority {} - Remaining: {}", 
						removedCount, priority, allTasks.size());
			}
			return removedCount;
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * Remove all tasks.
	 * @return number of tasks removed
	 */
	public int clearAllTasks() {
		lock.writeLock().lock();
		try {
			int count = allTasks.size();
			allTasks.clear();
			priorityQueue.clear();
			
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Cleared all tasks - {} tasks removed", count);
			}
			return count;
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	// ==================== Utility Methods ====================
	
	/**
	 * Check if a task exists by ID.
	 * @param taskId the task ID
	 * @return true if task exists
	 */
	public boolean taskExists(int taskId) {
		lock.readLock().lock();
		try {
			return allTasks.stream().anyMatch(t -> t.getTaskId() == taskId);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * Get statistics about the task management system.
	 * @return formatted string with statistics
	 */
	public String getStatistics() {
		lock.readLock().lock();
		try {
			int totalTasks = allTasks.size();
			int queueSize = priorityQueue.size();
			
			if (totalTasks == 0) {
				return "TaskManagement Statistics: No tasks";
			}
			
			int maxPriority = allTasks.stream()
					.mapToInt(Task::getTaskPriority)
					.max()
					.orElse(0);
			int minPriority = allTasks.stream()
					.mapToInt(Task::getTaskPriority)
					.min()
					.orElse(0);
			double avgPriority = allTasks.stream()
					.mapToInt(Task::getTaskPriority)
					.average()
					.orElse(0.0);
			
			return String.format(
				"TaskManagement Statistics: Total=%d, Queue=%d, Priority[Min=%d, Max=%d, Avg=%.2f]",
				totalTasks, queueSize, minPriority, maxPriority, avgPriority
			);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * Stop the task management system gracefully.
	 */
	public void stopSystem() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Stopping Task Management System gracefully - {}", getStatistics());
		}
		running = false;
		interrupt();
	}
}