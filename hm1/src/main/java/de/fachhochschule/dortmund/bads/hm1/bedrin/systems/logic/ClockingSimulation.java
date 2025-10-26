package de.fachhochschule.dortmund.bads.hm1.bedrin.systems.logic;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClockingSimulation extends Thread {
	private static final Logger LOGGER = LogManager.getLogger(ClockingSimulation.class.getName());
	
	private volatile boolean running = true;
	private AtomicInteger currentTime = new AtomicInteger(0);
	private AtomicInteger delay = new AtomicInteger(1000);
	
	@Override
	public void run() {
		while (running) {
			try {
				currentTime.incrementAndGet();
				LOGGER.info("Current Time: " + currentTime.get());
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
}
