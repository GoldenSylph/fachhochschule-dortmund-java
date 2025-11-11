package de.fachhochschule.dortmund.bads.systems.logic;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.ObservabilityConfiguration;
import de.fachhochschule.dortmund.bads.systems.logic.utils.ITickable;

/**
 * Observation System - Monitors system metrics and events.
 * Integrated with ClockingSimulation for timing.
 */
public class Observation extends Thread implements ITickable {
	private static final Logger LOGGER = LogManager.getLogger(Observation.class.getName());
	
	private final ConcurrentLinkedQueue<SystemEvent> eventBuffer;
	private final AtomicLong eventsCollected;
	private volatile boolean running = true;
	
	public Observation() {
		super("Observation-Thread");
		this.eventBuffer = new ConcurrentLinkedQueue<>();
		this.eventsCollected = new AtomicLong(0);
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Observation System initialized with empty event buffer");
		}
	}
	
	@Override
	public void run() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Observation System started");
		}
		
		ObservabilityConfiguration config = ObservabilityConfiguration.INSTANCE;
		long systemStartTime = System.currentTimeMillis();
		int collectionCycles = 0;
		
		try {
			while (running && !Thread.currentThread().isInterrupted()) {
				// Collect metrics if enabled
				if (config.isMetricsEnabled()) {
					long cycleStartTime = System.currentTimeMillis();
					collectMetrics();
					long cycleDuration = System.currentTimeMillis() - cycleStartTime;
					collectionCycles++;
					
					if (cycleDuration > config.getMetricsCollectionIntervalMillis() / 2 && LOGGER.isWarnEnabled()) {
						LOGGER.warn("Metrics collection cycle {} took {}ms ({}% of interval) - performance degradation detected", 
								collectionCycles, cycleDuration, 
								(cycleDuration * 100) / config.getMetricsCollectionIntervalMillis());
					}
				}
				
				// Manage event buffer size
				int eventsRemoved = manageEventBuffer(config.getEventBufferSize());
				if (eventsRemoved > 0 && LOGGER.isDebugEnabled()) {
					LOGGER.debug("Pruned {} old events from buffer (new size: {})", eventsRemoved, eventBuffer.size());
				}
				
				Thread.sleep(config.getMetricsCollectionIntervalMillis());
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Observation System interrupted after {} collection cycles", collectionCycles);
			}
		}
		
		long totalRuntime = System.currentTimeMillis() - systemStartTime;
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Observation System stopped after {} collection cycles in {}ms. Total events collected: {} (avg: {:.2f}ms per cycle)", 
					collectionCycles, totalRuntime, eventsCollected.get(),
					collectionCycles > 0 ? (double)totalRuntime / collectionCycles : 0.0);
		}
	}
	
	@Override
	public void onTick(int currentTick) {
		// Record tick event
		if (currentTick % 50 == 0) {
			recordEvent("TICK", "System tick: " + currentTick);
			
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Tick {} - Event buffer: {} events, Total collected: {}", 
						currentTick, eventBuffer.size(), eventsCollected.get());
			}
		}
		
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Observation tick {} processed", currentTick);
		}
	}
	
	private void collectMetrics() {
		// Collect basic system metrics
		long activeThreads = Thread.getAllStackTraces().keySet().stream()
				.filter(Thread::isAlive)
				.count();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Metrics collected - Active threads: {}, Event buffer: {}, Total events: {}", 
					activeThreads, eventBuffer.size(), eventsCollected.get());
		}
		
		// Record metrics as event
		recordEvent("METRICS", String.format("Active threads: %d, Events: %d", activeThreads, eventBuffer.size()));
	}
	
	private int manageEventBuffer(int maxSize) {
		int eventsRemoved = 0;
		while (eventBuffer.size() > maxSize) {
			eventBuffer.poll();
			eventsRemoved++;
		}
		
		if (eventsRemoved > 0 && LOGGER.isTraceEnabled()) {
			LOGGER.trace("Removed {} events to maintain buffer size limit of {}", eventsRemoved, maxSize);
		}
		
		return eventsRemoved;
	}
	
	/**
	 * Record a system event.
	 */
	public void recordEvent(String eventType, String details) {
		if (eventType == null || details == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Attempted to record event with null type or details - ignoring");
			}
			return;
		}
		
		ObservabilityConfiguration config = ObservabilityConfiguration.INSTANCE;
		
		if (config.isEventTrackingEnabled()) {
			SystemEvent event = new SystemEvent(System.currentTimeMillis(), eventType, details);
			eventBuffer.offer(event);
			long totalEvents = eventsCollected.incrementAndGet();
			
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Event recorded (total: {}): {}", totalEvents, event);
			}
		} else if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Event tracking disabled - event not recorded: {} - {}", eventType, details);
		}
	}
	
	public ConcurrentLinkedQueue<SystemEvent> getEventBuffer() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Event buffer requested - returning {} events", eventBuffer.size());
		}
		return new ConcurrentLinkedQueue<>(eventBuffer);
	}
	
	public long getTotalEventsCollected() {
		long total = eventsCollected.get();
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Total events collected requested: {}", total);
		}
		return total;
	}
	
	public void stopSystem() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Stopping Observation System gracefully (buffered events: {}, total collected: {})", 
					eventBuffer.size(), eventsCollected.get());
		}
		running = false;
		interrupt();
	}
	
	/**
	 * System event record.
	 */
	public record SystemEvent(long timestamp, String eventType, String details) {
		@Override
		public String toString() {
			return String.format("[%d] %s: %s", timestamp, eventType, details);
		}
	}
}