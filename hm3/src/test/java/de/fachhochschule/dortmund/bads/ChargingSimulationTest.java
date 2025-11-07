package de.fachhochschule.dortmund.bads.hm3.bilyaminu

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test that simulates charging M devices with K charging stations.
 * It asserts that at no time more than K devices are charging simultaneously.
 *
 * Notes for unit tests: times are scaled down (ms instead of minutes) so test runs quickly.
 */
public class ChargingSimulationTest {

    @Test
    void testMaxConcurrentChargingDoesNotExceedStations() throws InterruptedException {
        final int K = 3;       // number of charging stations (simulate N)
        final int M = 20;      // number of devices to charge
        final int maxChargeMillis = 200; // simulated charge time (ms)
        final int arrivalWindowMillis = 100; // devices arrive randomly in this window

        Semaphore stations = new Semaphore(K);
        AtomicInteger currentlyCharging = new AtomicInteger(0);
        List<Integer> violations = new CopyOnWriteArrayList<>(); // record any overflows

        ExecutorService exec = Executors.newFixedThreadPool(Math.min(M, 10));
        CountDownLatch done = new CountDownLatch(M);
        Random rand = new Random(12345);

        for (int i = 0; i < M; i++) {
            final int deviceId = i;
            exec.submit(() -> {
                try {
                    // random arrival
                    Thread.sleep(rand.nextInt(arrivalWindowMillis));

                    long attempt = System.currentTimeMillis();
                    stations.acquire(); // waits for a free station
                    try {
                        int current = currentlyCharging.incrementAndGet();

                        // check invariant: current <= K
                        if (current > K) {
                            violations.add(current); // record violation (should not happen)
                        }

                        // simulate charging
                        Thread.sleep(rand.nextInt(maxChargeMillis) + 20);

                    } finally {
                        currentlyCharging.decrementAndGet();
                        stations.release();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        // wait for completion with reasonable timeout
        boolean finished = done.await(10, TimeUnit.SECONDS);
        exec.shutdownNow();

        assertTrue(finished, "Simulation did not finish in time");
        // no violations expected
        assertEquals(0, violations.size(), "No more than K devices should ever be charging simultaneously");
    }

    @Test
    void testWaitingTimeThresholdExample() throws InterruptedException {
        // Example of measuring waiting time and asserting if waiting > threshold (scaled)
        final int K = 2;
        final int M = 6;
        final long thresholdMillis = 150; // corresponds to 15 minutes scaled down

        Semaphore stations = new Semaphore(K);
        ExecutorService exec = Executors.newFixedThreadPool(M);
        CountDownLatch finished = new CountDownLatch(M);
        List<Long> waitTimes = new CopyOnWriteArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < M; i++) {
            exec.submit(() -> {
                try {
                    long arrive = System.currentTimeMillis();
                    // try to acquire and measure waiting time
                    stations.acquire();
                    long acquired = System.currentTimeMillis();
                    waitTimes.add(acquired - arrive);

                    // hold station for a bit
                    Thread.sleep(120 + rand.nextInt(50));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    stations.release();
                    finished.countDown();
                }
            });
        }

        boolean ok = finished.await(10, TimeUnit.SECONDS);
        exec.shutdownNow();
        assertTrue(ok, "Tasks did not complete in time");

        // check if any waiting time exceeded threshold
        boolean exceeded = waitTimes.stream().anyMatch(t -> t > thresholdMillis);
        // This test merely demonstrates how to detect exceedances — assert that at least one or none depending on scenario.
        // Here we assert that at least one wait time did exceed (since M>K)
        assertTrue(exceeded, "At least one device should have waited > threshold in this scenario");
    }
}
