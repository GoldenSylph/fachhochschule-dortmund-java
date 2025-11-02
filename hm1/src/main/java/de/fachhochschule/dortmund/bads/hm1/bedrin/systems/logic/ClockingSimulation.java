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

	@Override
	public void run() {
		while (running) {
			try {
				currentTime.incrementAndGet();
				LOGGER.info("Current Time: " + currentTime.get());
				for (ITickable t : tickables) {
					try {
						t.onTick(this.currentTime.get());
					} catch (RuntimeException ex) {
						LOGGER.warn("Tickable threw: ", ex);
					}
				}
				Thread.sleep(delay.get());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
	}

	public void toggleClocking() {
		this.running = !this.running;
	}

	public int getCurrentTime() {
		return this.currentTime.get();
	}

	public void setDelay(int delay) {
		this.delay.set(delay);
	}

	public boolean isRunning() {
		return this.running;
	}

	public void registerTickable(ITickable tickable) {
		if (tickable != null)
			this.tickables.addIfAbsent(tickable);
	}

	public void unregisterTickable(ITickable tickable) {
		if (tickable != null)
			this.tickables.remove(tickable);
	}

	public void clearTickables() {
		this.tickables.clear();
	}
}