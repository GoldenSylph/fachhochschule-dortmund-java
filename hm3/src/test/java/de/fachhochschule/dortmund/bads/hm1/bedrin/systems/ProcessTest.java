package de.fachhochschule.dortmund.bads.hm1.bedrin.systems;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fachhochschule.dortmund.bads.exceptions.ProcessExecutionException;
import de.fachhochschule.dortmund.bads.resources.Resource;
import de.fachhochschule.dortmund.bads.systems.Operation;
import de.fachhochschule.dortmund.bads.systems.Systems;
import de.fachhochschule.dortmund.bads.systems.Process;
import de.fachhochschule.dortmund.bads.systems.logic.ClockingSimulation;

class ProcessTest {

    private Process process;
    private ClockingSimulation mockClockingSimulation;
    
    // Mock Resource implementation for testing
    static class MockResource extends Resource {
        private final double quantity;
        private final int executionTime;
        private final boolean shouldThrowException;
        private final CountDownLatch latch;
        
        public MockResource(double quantity, int executionTime) {
            this(quantity, executionTime, false, null);
        }
        
        public MockResource(double quantity, int executionTime, boolean shouldThrowException) {
            this(quantity, executionTime, shouldThrowException, null);
        }
        
        public MockResource(double quantity, int executionTime, CountDownLatch latch) {
            this(quantity, executionTime, false, latch);
        }
        
        public MockResource(double quantity, int executionTime, boolean shouldThrowException, CountDownLatch latch) {
            this.quantity = quantity;
            this.executionTime = executionTime;
            this.shouldThrowException = shouldThrowException;
            this.latch = latch;
        }
        
        @Override
        public double getQuantity() {
            return quantity;
        }
        
        @Override
        public Resource call() throws Exception {
            if (shouldThrowException) {
                throw new RuntimeException("Mock resource exception");
            }
            
            if (executionTime > 0) {
                Thread.sleep(executionTime);
            }
            
            if (latch != null) {
                latch.countDown();
            }
            
            return this;
        }
    }
    
    // Mock Operation implementation for testing
    static class MockOperation extends Operation {
        private final int testCreationTime;
        
        public MockOperation(List<Resource> resources, int creationTime) {
            super();
            this.testCreationTime = creationTime;
            this.resources.clear();
            this.resources.addAll(resources);
        }
        
        @Override
        public int getCreationTime() {
            return testCreationTime;
        }
    }

    @BeforeEach
    void setUp() {
        process = new Process();
        
        // Set up mock clocking simulation
        mockClockingSimulation = new ClockingSimulation();
        Systems.CLOCKING.build(Systems.SystemBuilder.INSTANCE.logic(mockClockingSimulation));
    }
    
    @AfterEach
    void tearDown() {
        if (mockClockingSimulation != null && mockClockingSimulation.isRunning()) {
            mockClockingSimulation.toggleClocking();
            mockClockingSimulation.interrupt();
        }
    }

    @Test
    void testProcessCreation() {
        assertNotNull(process);
        assertEquals(0, process.getOperationsCount());
    }

    @Test
    void testAddOperation() {
        MockResource resource = new MockResource(1.0, 0);
        MockOperation operation = new MockOperation(List.of(resource), 100);
        
        assertEquals(0, process.getOperationsCount());
        
        process.addOperation(operation);
        
        assertEquals(1, process.getOperationsCount());
        assertSame(operation, process.getOperation(0));
    }

    @Test
    void testAddMultipleOperations() {
        MockResource resource1 = new MockResource(1.0, 0);
        MockResource resource2 = new MockResource(2.0, 0);
        MockOperation operation1 = new MockOperation(List.of(resource1), 100);
        MockOperation operation2 = new MockOperation(List.of(resource2), 200);
        
        process.addOperation(operation1);
        process.addOperation(operation2);
        
        assertEquals(2, process.getOperationsCount());
        assertSame(operation1, process.getOperation(0));
        assertSame(operation2, process.getOperation(1));
    }

    @Test
    void testGetOperationOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            process.getOperation(0);
        });
        
        MockResource resource = new MockResource(1.0, 0);
        MockOperation operation = new MockOperation(List.of(resource), 100);
        process.addOperation(operation);
        
        assertThrows(IndexOutOfBoundsException.class, () -> {
            process.getOperation(1);
        });
    }

    @Test
    void testProcessDurationWithNoOperations() {
        // Start the clocking simulation to get a current time
        mockClockingSimulation.toggleClocking();
        
        // With no operations, duration cannot be calculated
        assertThrows(ProcessExecutionException.class, () -> {
        	process.processDuration();
        });
    }

    @Test
    void testProcessDurationWithSingleOperation() {
        // Start the clocking simulation
        mockClockingSimulation.toggleClocking();
        
        // Wait a bit to advance the time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        int currentTime = mockClockingSimulation.getCurrentTime();
        MockResource resource = new MockResource(1.0, 0);
        MockOperation operation = new MockOperation(List.of(resource), currentTime - 50);
        
        process.addOperation(operation);
        
        double duration = process.processDuration();
        assertTrue(duration >= 50.0, "Duration should be at least 50 time units");
    }

    @Test
    void testProcessDurationWithMultipleOperations() {
        // Start the clocking simulation
        mockClockingSimulation.toggleClocking();
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        int currentTime = mockClockingSimulation.getCurrentTime();
        
        MockResource resource1 = new MockResource(1.0, 0);
        MockResource resource2 = new MockResource(2.0, 0);
        MockOperation operation1 = new MockOperation(List.of(resource1), currentTime - 30); // newer
        MockOperation operation2 = new MockOperation(List.of(resource2), currentTime - 80); // older
        
        process.addOperation(operation1);
        process.addOperation(operation2);
        
        double duration = process.processDuration();
        // Duration should be based on the oldest operation (80 time units ago)
        assertTrue(duration >= 80.0, "Duration should be based on oldest operation");
    }

    @Test
    void testProcessOperationsWithNoOperations() {
    	assertThrows(ProcessExecutionException.class, () -> {
			process.processOperations();
		});
    }

    @Test
    void testProcessOperationsWithSingleResource() throws InterruptedException, ExecutionException {
        MockResource resource = new MockResource(1.0, 50);
        MockOperation operation = new MockOperation(List.of(resource), 100);
        
        process.addOperation(operation);
        
        List<Future<Resource>> futures = process.processOperations();
        
        assertNotNull(futures);
        assertEquals(1, futures.size());
        
        // Wait for completion and verify result
        Resource result = futures.get(0).get();
        assertSame(resource, result);
        assertEquals(1.0, result.getQuantity());
    }

    @Test
    void testProcessOperationsWithMultipleResources() throws InterruptedException, ExecutionException {
        CountDownLatch latch = new CountDownLatch(3);
        
        MockResource resource1 = new MockResource(1.0, 50, latch);
        MockResource resource2 = new MockResource(2.0, 30, latch);
        MockResource resource3 = new MockResource(3.0, 40, latch);
        
        MockOperation operation1 = new MockOperation(List.of(resource1, resource2), 100);
        MockOperation operation2 = new MockOperation(List.of(resource3), 200);
        
        process.addOperation(operation1);
        process.addOperation(operation2);
        
        List<Future<Resource>> futures = process.processOperations();
        
        assertNotNull(futures);
        assertEquals(3, futures.size());
        
        // Wait for all tasks to complete
        assertTrue(latch.await(2, TimeUnit.SECONDS), "All resources should complete within timeout");
        
        // Verify all futures are done
        for (Future<Resource> future : futures) {
            assertTrue(future.isDone());
            assertNotNull(future.get());
        }
    }

    @Test
    void testProcessOperationsWithException() throws InterruptedException {
        MockResource normalResource = new MockResource(1.0, 10);
        MockResource exceptionResource = new MockResource(2.0, 10, true);
        
        MockOperation operation = new MockOperation(List.of(normalResource, exceptionResource), 100);
        process.addOperation(operation);
        
        List<Future<Resource>> futures = process.processOperations();
        
        assertNotNull(futures);
        assertEquals(2, futures.size());
        
        // The normal resource should complete successfully
        assertDoesNotThrow(() -> {
            Resource result = futures.get(0).get();
            assertSame(normalResource, result);
        });
        
        // The exception resource should throw an exception
        assertThrows(ExecutionException.class, () -> {
            futures.get(1).get();
        });
    }

    @Test
    void testProcessOperationsConcurrency() throws InterruptedException {
        int resourceCount = 10;
        CountDownLatch latch = new CountDownLatch(resourceCount);
        
        // Create multiple resources with slight delays
        for (int i = 0; i < resourceCount; i++) {
            MockResource resource = new MockResource(i, 20, latch);
            MockOperation operation = new MockOperation(List.of(resource), 100 + i);
            process.addOperation(operation);
        }
        
        long startTime = System.currentTimeMillis();
        List<Future<Resource>> futures = process.processOperations();
        
        // Wait for all to complete
        assertTrue(latch.await(2, TimeUnit.SECONDS), "All resources should complete concurrently");
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        // If running concurrently, total time should be much less than sequential execution
        // Sequential would be 20ms * 10 = 200ms+ overhead
        // Concurrent should be around 20ms + overhead
        assertTrue(executionTime < 150, "Concurrent execution should be faster than sequential");
        
        assertEquals(resourceCount, futures.size());
        for (Future<Resource> future : futures) {
            assertTrue(future.isDone());
        }
    }

    @Test
    void testProcessOperationsInterruption() throws InterruptedException {
        // Create a resource that will run for a long time
        MockResource longRunningResource = new MockResource(1.0, 2000);
        MockOperation operation = new MockOperation(List.of(longRunningResource), 100);
        
        process.addOperation(operation);
        
        // Start processing in a separate thread
        Thread processThread = new Thread(() -> {
            process.processOperations();
        });
        
        processThread.start();
        
        // Wait a bit then interrupt
        Thread.sleep(100);
        processThread.interrupt();
        
        // Wait for the thread to finish
        processThread.join(1000);
        
        // Verify the thread terminated
        assertFalse(processThread.isAlive(), "Process thread should have terminated after interruption");
    }

    @Test
    void testProcessOperationsTimeout() {
        // This test verifies that the executor service properly shuts down
        // even if some tasks take longer than expected
        
        MockResource slowResource = new MockResource(1.0, 100);
        MockOperation operation = new MockOperation(List.of(slowResource), 100);
        
        process.addOperation(operation);
        
        long startTime = System.currentTimeMillis();
        List<Future<Resource>> futures = process.processOperations();
        long endTime = System.currentTimeMillis();
        
        assertNotNull(futures);
        assertEquals(1, futures.size());
        
        // The method should return after the executor shutdown timeout
        // which is 5000ms (TIMEOUT_SHUTDOWN) plus some overhead
        long executionTime = endTime - startTime;
        assertTrue(executionTime < 6000, "Process should complete within reasonable time");
    }
}