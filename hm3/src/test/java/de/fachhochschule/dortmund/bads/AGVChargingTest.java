package de.fachhochschule.dortmund.bads.hm3.bilyaminu;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for AGV charging simulation.
 *
 * Times are scaled to milliseconds so tests run fast. In production mapping, 15 minutes -> e.g. 15000 ms.
 */
public class AGVChargingTest {

    @Test
    void testNoMoreThanKAGVsChargingAtOnce() throws Exception {
        int K = 3;               // charging stations
        int M = 20;              // AGVs arriving
        long chargeMillis = 200; // simulated charge time
        long maxWaitMillis = 500; // wait before leaving

        AGVChargingManager manager = new AGVChargingManager(K);

        List<AGV> agvs = new ArrayList<>();
        for (int i = 0; i < M; i++) agvs.add(new AGV("AGV-" + i));

        ExecutorService exec = Executors.newFixedThreadPool(Math.min(M, 8));

        AGVChargingManager.SimulationResult result = manager.simulateCharging(agvs, exec, chargeMillis, maxWaitMillis);

        exec.shutdownNow();

        // ensure simulation finished
        assertTrue(result.finished, "Simulation should finish in allotted time");

        // observed concurrency must not exceed K
        assertTrue(result.maxConcurrentChargingObserved <= K,
                "Max concurrent charging observed (" + result.maxConcurrentChargingObserved + ") must be <= K (" + K + ")");

        // optionally print logs for debugging (uncomment while debugging)
        // result.logs.forEach(System.out::println);
    }

    @Test
    void testAGVsLeaveWhenWaitingTooLong() throws Exception {
        int K = 1;                // only one station to force waiting
        int M = 6;
        long chargeMillis = 400;   // long enough to cause queueing
        long maxWaitMillis = 150;  // threshold (scaled) -> AGV will leave if wait > this

        AGVChargingManager manager = new AGVChargingManager(K);

        List<AGV> agvs = new ArrayList<>();
        for (int i = 0; i < M; i++) agvs.add(new AGV("AGV-" + i));

        ExecutorService exec = Executors.newFixedThreadPool(Math.min(M, 6));
        AGVChargingManager.SimulationResult result = manager.simulateCharging(agvs, exec, chargeMillis, maxWaitMillis);
        exec.shutdownNow();

        assertTrue(result.finished, "Simulation should finish");

        // since M>K and chargeMillis is larger than maxWaitMillis, some AGVs should leave
        assertTrue(result.leftQueue > 0, "Some AGVs should have left the queue due to waiting > threshold");
    }
}
