package de.fachhochschule.dortmund.bads.hm3.bilyaminu;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages AGV charging simulation using a Semaphore to represent K charging stations.
 *
 * Behavior:
 * - AGVs arrive (submitted as tasks). Each AGV will try to acquire a charging slot.
 * - If it cannot acquire within the threshold wait time, the AGV "leaves the queue".
 * - If it acquires a slot, it charges for the configured chargeDurationMillis and then releases the slot.
 */
public class AGVChargingManager {

    private final Semaphore stations;
    private final int stationCount;

    public AGVChargingManager(int stationCount) {
        if (stationCount <= 0) throw new IllegalArgumentException("stationCount must be > 0");
        this.stationCount = stationCount;
        this.stations = new Semaphore(stationCount);
    }

    /**
     * Run a simulation: submit AGVs to charging with the provided executor.
     *
     * @param agvs list of AGVs to simulate
     * @param executor executor to run tasks
     * @param chargeDurationMillis how long each AGV holds a station (simulated)
     * @param maxWaitMillis maximum waiting time before an AGV leaves the queue
     * @return result object describing counts
     * @throws InterruptedException if interrupted while waiting for completion
     */
    public SimulationResult simulateCharging(List<AGV> agvs,
                                             ExecutorService executor,
                                             long chargeDurationMillis,
                                             long maxWaitMillis) throws InterruptedException {

        final int M = agvs.size();
        CountDownLatch done = new CountDownLatch(M);
        AtomicInteger currentlyCharging = new AtomicInteger(0);
        AtomicInteger maxConcurrentObserved = new AtomicInteger(0);
        AtomicInteger leftQueue = new AtomicInteger(0);
        // queue is only for observation/debugging; semaphore controls actual access
        Queue<String> log = new ConcurrentLinkedQueue<>();

        for (AGV agv : agvs) {
            executor.submit(() -> {
                try {
                    long arrive = System.currentTimeMillis();
                    log.add(agv + " arrived at " + arrive);

                    // try to acquire within maxWaitMillis
                    boolean acquired = stations.tryAcquire(maxWaitMillis, TimeUnit.MILLISECONDS);
                    long acquiredAt = acquired ? System.currentTimeMillis() : -1;

                    if (!acquired) {
                        // left the queue
                        leftQueue.incrementAndGet();
                        log.add(agv + " left after waiting " + (System.currentTimeMillis() - arrive) + "ms");
                        return;
                    }

                    try {
                        int current = currentlyCharging.incrementAndGet();
                        // record max observed concurrency
                        maxConcurrentObserved.updateAndGet(prev -> Math.max(prev, current));
                        log.add(agv + " started charging at " + acquiredAt + " (currentCharging=" + current + ")");
                        // simulate charging
                        Thread.sleep(chargeDurationMillis);
                        log.add(agv + " finished charging");
                    } finally {
                        currentlyCharging.decrementAndGet();
                        stations.release();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.add(agv + " was interrupted");
                } finally {
                    done.countDown();
                }
            });
        }

        // wait for tasks to finish (with timeout safeguard)
        boolean finished = done.await(Math.max(10, M * Math.max(1, chargeDurationMillis / 100)) + 5000, TimeUnit.MILLISECONDS);

        return new SimulationResult(M, stationCount, leftQueue.get(), maxConcurrentObserved.get(), log, finished);
    }

    public static class SimulationResult {
        public final int submitted;
        public final int stationCount;
        public final int leftQueue;
        public final int maxConcurrentChargingObserved;
        public final Queue<String> logs;
        public final boolean finished;

        public SimulationResult(int submitted, int stationCount, int leftQueue, int maxConcurrentChargingObserved, Queue<String> logs, boolean finished) {
            this.submitted = submitted;
            this.stationCount = stationCount;
            this.leftQueue = leftQueue;
            this.maxConcurrentChargingObserved = maxConcurrentChargingObserved;
            this.logs = logs;
            this.finished = finished;
        }
    }
}
