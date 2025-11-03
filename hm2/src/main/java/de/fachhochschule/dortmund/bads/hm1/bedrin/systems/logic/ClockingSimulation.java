package de.fachhochschule.dortmund.bads.hm1.bedrin.systems.logic;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClockingSimulation extends Thread {
	private static final Logger LOGGER = LogManager.getLogger(ClockingSimulation.class.getName());

	private final CopyOnWriteArrayList<ITickable> tickables = new CopyOnWriteArrayList<>();
	private volatile boolean running = true;
	private AtomicInteger currentTime = new AtomicInteger(0);
	private AtomicInteger delay = new AtomicInteger(1000);

	public ClockingSimulation() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("ClockingSimulation created with default delay: {}ms", delay.get());
		}
	}

	@Override
	public void run() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("ClockingSimulation started with {} registered tickables", tickables.size());
		}
		
		long simulationStartTime = System.currentTimeMillis();
		int tickCount = 0;
		
		while (running) {
			try {
				int currentTick = currentTime.incrementAndGet();
				tickCount++;
				
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Tick {}: Processing {} tickables", currentTick, tickables.size());
				}
				
				// Track tick processing performance
				long tickStartTime = System.currentTimeMillis();
				int successfulTicks = 0;
				int failedTicks = 0;
				
				for (ITickable t : tickables) {
					try {
						t.onTick(currentTick);
						successfulTicks++;
						if (LOGGER.isTraceEnabled()) {
							LOGGER.trace("Successfully processed tick for tickable: {}", t.getClass().getSimpleName());
						}
					} catch (RuntimeException ex) {
						failedTicks++;
						if (LOGGER.isWarnEnabled()) {
							LOGGER.warn("Tickable {} threw exception during tick {}: {}", 
									   t.getClass().getSimpleName(), currentTick, ex.getMessage(), ex);
						}
					}
				}
				
				long tickDuration = System.currentTimeMillis() - tickStartTime;
				
				// Log tick completion with performance metrics
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Tick {} completed in {}ms - Successful: {}, Failed: {}", 
							   currentTick, tickDuration, successfulTicks, failedTicks);
				}
				
				// Log performance warnings for slow ticks
				if (tickDuration > delay.get() / 2 && LOGGER.isWarnEnabled()) {
					LOGGER.warn("Tick {} took {}ms ({}% of delay period) - performance degradation detected", 
							   currentTick, tickDuration, (tickDuration * 100) / delay.get());
				}
				
				Thread.sleep(delay.get());
				
			} catch (InterruptedException e) {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("ClockingSimulation interrupted after {} ticks", tickCount);
				}
				Thread.currentThread().interrupt();
				break;
			}
		}
		
		long totalSimulationTime = System.currentTimeMillis() - simulationStartTime;
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("ClockingSimulation stopped after {} ticks in {}ms (avg: {:.2f}ms per tick)", 
					   tickCount, totalSimulationTime, tickCount > 0 ? (double)totalSimulationTime / tickCount : 0.0);
		}
	}

	public void toggleClocking() {
		boolean previousState = this.running;
		this.running = !this.running;
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("ClockingSimulation toggled from {} to {}", 
					   previousState ? "RUNNING" : "STOPPED", this.running ? "RUNNING" : "STOPPED");
		}
	}

	public int getCurrentTime() {
		int time = this.currentTime.get();
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Current time requested: {}", time);
		}
		return time;
	}

	public void setDelay(int delay) {
		int previousDelay = this.delay.get();
		this.delay.set(delay);
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Simulation delay changed from {}ms to {}ms", previousDelay, delay);
		}
	}

	public boolean isRunning() {
		boolean runningState = this.running;
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Running state requested: {}", runningState);
		}
		return runningState;
	}

	public void registerTickable(ITickable tickable) {
		if (tickable != null) {
			boolean added = this.tickables.addIfAbsent(tickable);
			if (added && LOGGER.isDebugEnabled()) {
				LOGGER.debug("Registered new tickable: {}. Total tickables: {}", 
						   tickable.getClass().getSimpleName(), this.tickables.size());
			} else if (!added && LOGGER.isDebugEnabled()) {
				LOGGER.debug("Tickable {} already registered, skipping", tickable.getClass().getSimpleName());
			}
		} else {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Attempted to register null tickable - ignoring");
			}
		}
	}

	public void unregisterTickable(ITickable tickable) {
		if (tickable != null) {
			boolean removed = this.tickables.remove(tickable);
			if (removed && LOGGER.isDebugEnabled()) {
				LOGGER.debug("Unregistered tickable: {}. Remaining tickables: {}", 
						   tickable.getClass().getSimpleName(), this.tickables.size());
			} else if (!removed && LOGGER.isDebugEnabled()) {
				LOGGER.debug("Tickable {} was not registered, nothing to remove", tickable.getClass().getSimpleName());
			}
		} else {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Attempted to unregister null tickable - ignoring");
			}
		}
	}

	public void clearTickables() {
		int previousCount = this.tickables.size();
		this.tickables.clear();
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Cleared all tickables - removed {} tickables", previousCount);
		}
	}

	/**
	 * Gets the current delay between ticks in milliseconds.
	 * 
	 * @return the current delay in milliseconds
	 */
	public int getDelay() {
		return this.delay.get();
	}

	/**
	 * Gets the number of currently registered tickables.
	 * 
	 * @return the number of registered tickables
	 */
	public int getTickableCount() {
		int count = this.tickables.size();
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Tickable count requested: {}", count);
		}
		return count;
	}

	/**
	 * Stops the simulation gracefully.
	 */
	public void stopSimulation() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Stopping ClockingSimulation gracefully");
		}
		this.running = false;
		this.interrupt(); // Wake up the thread if it's sleeping
	}
}