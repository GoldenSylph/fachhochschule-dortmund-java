package de.fachhochschule.dortmund.bads.hm1.bedrin.systems.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fachhochschule.dortmund.bads.model.Task;
import de.fachhochschule.dortmund.bads.systems.logic.TaskManagement;

public class TaskManagementTest {

    private TaskManagement taskManagement;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        taskManagement = new TaskManagement();
        task1 = new Task();
        task2 = new Task();
        task3 = new Task();
    }

    @Test
    public void createTasks_addAndGet() {
        assertEquals(0, taskManagement.getTasksCount());

        taskManagement.addTask(task1);
        taskManagement.addTask(task2);
        
        assertEquals(2, taskManagement.getTasksCount());
        assertSame(task1, taskManagement.getTask(0));
        assertSame(task2, taskManagement.getTask(1));
    }

    @Test
    public void updateTask_replacesAtIndex() {
        taskManagement.addTask(task1);
        taskManagement.addTask(task2);

        Task prev = taskManagement.updateTask(1, task3);
        
        assertSame(task2, prev);
        assertSame(task3, taskManagement.getTask(1));
        assertEquals(2, taskManagement.getTasksCount());
    }

    @Test
    void addTask_increasesCount() {
        assertEquals(0, taskManagement.getTasksCount());
        
        taskManagement.addTask(task1);
        assertEquals(1, taskManagement.getTasksCount());
        
        taskManagement.addTask(task2);
        assertEquals(2, taskManagement.getTasksCount());
    }

    @Test
    void addTask_withNullTask_addsNull() {
        taskManagement.addTask(null);
        assertEquals(1, taskManagement.getTasksCount());
        assertEquals(null, taskManagement.getTask(0));
    }

    @Test
    void getTask_withValidIndex_returnsTask() {
        taskManagement.addTask(task1);
        taskManagement.addTask(task2);
        
        assertSame(task1, taskManagement.getTask(0));
        assertSame(task2, taskManagement.getTask(1));
    }

    @Test
    void getTask_withInvalidIndex_throwsException() {
        taskManagement.addTask(task1);
        
        assertThrows(IndexOutOfBoundsException.class, () -> taskManagement.getTask(1));
        assertThrows(IndexOutOfBoundsException.class, () -> taskManagement.getTask(-1));
    }

    @Test
    void getTasks_returnsUnmodifiableList() {
        taskManagement.addTask(task1);
        taskManagement.addTask(task2);
        
        List<Task> tasks = taskManagement.getTasks();
        assertEquals(2, tasks.size());
        assertSame(task1, tasks.get(0));
        assertSame(task2, tasks.get(1));
        
        // Verify list is unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> tasks.add(task3));
    }

    @Test
    void updateTask_withValidIndex_returnsOldTask() {
        taskManagement.addTask(task1);
        taskManagement.addTask(task2);
        
        Task oldTask = taskManagement.updateTask(0, task3);
        
        assertSame(task1, oldTask);
        assertSame(task3, taskManagement.getTask(0));
        assertEquals(2, taskManagement.getTasksCount());
    }

    @Test
    void updateTask_withInvalidIndex_throwsException() {
        taskManagement.addTask(task1);
        
        assertThrows(IndexOutOfBoundsException.class, () -> taskManagement.updateTask(1, task2));
        assertThrows(IndexOutOfBoundsException.class, () -> taskManagement.updateTask(-1, task2));
    }

    @Test
    void removeTaskByIndex_withValidIndex_removesAndReturnsTask() {
        taskManagement.addTask(task1);
        taskManagement.addTask(task2);
        taskManagement.addTask(task3);
        
        Task removed = taskManagement.removeTask(1);
        
        assertSame(task2, removed);
        assertEquals(2, taskManagement.getTasksCount());
        assertSame(task1, taskManagement.getTask(0));
        assertSame(task3, taskManagement.getTask(1));
    }

    @Test
    void removeTaskByIndex_withInvalidIndex_throwsException() {
        taskManagement.addTask(task1);
        
        assertThrows(IndexOutOfBoundsException.class, () -> taskManagement.removeTask(1));
        assertThrows(IndexOutOfBoundsException.class, () -> taskManagement.removeTask(-1));
    }

    @Test
    void removeTaskByObject_existingTask_removesAndReturnsTrue() {
        taskManagement.addTask(task1);
        taskManagement.addTask(task2);
        taskManagement.addTask(task3);
        
        boolean result = taskManagement.removeTask(task2);
        
        assertTrue(result);
        assertEquals(2, taskManagement.getTasksCount());
        assertSame(task1, taskManagement.getTask(0));
        assertSame(task3, taskManagement.getTask(1));
    }

    @Test
    void removeTaskByObject_nonExistingTask_returnsFalse() {
        taskManagement.addTask(task1);
        taskManagement.addTask(task2);
        
        boolean result = taskManagement.removeTask(task3);
        
        assertFalse(result);
        assertEquals(2, taskManagement.getTasksCount());
    }

    @Test
    void removeTaskByObject_withNull_returnsFalse() {
        taskManagement.addTask(task1);
        
        boolean result = taskManagement.removeTask(null);
        
        assertFalse(result);
        assertEquals(1, taskManagement.getTasksCount());
    }

    @Test
    void removeTaskByObject_withNullInList_removesNull() {
        taskManagement.addTask(task1);
        taskManagement.addTask(null);
        taskManagement.addTask(task2);
        
        boolean result = taskManagement.removeTask(null);
        
        assertTrue(result);
        assertEquals(2, taskManagement.getTasksCount());
        assertSame(task1, taskManagement.getTask(0));
        assertSame(task2, taskManagement.getTask(1));
    }

    @Test
    void getTasksCount_emptyList_returnsZero() {
        assertEquals(0, taskManagement.getTasksCount());
    }

    @Test
    void getTasksCount_afterOperations_returnsCorrectCount() {
        taskManagement.addTask(task1);
        assertEquals(1, taskManagement.getTasksCount());
        
        taskManagement.addTask(task2);
        assertEquals(2, taskManagement.getTasksCount());
        
        taskManagement.removeTask(0);
        assertEquals(1, taskManagement.getTasksCount());
        
        taskManagement.removeTask(task2);
        assertEquals(0, taskManagement.getTasksCount());
    }

    @Test
    void threadSafety_concurrentAccess() throws InterruptedException {
        final int numThreads = 10;
        final int tasksPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        // Add tasks concurrently
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < tasksPerThread; j++) {
                        taskManagement.addTask(new Task());
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        assertEquals(numThreads * tasksPerThread, taskManagement.getTasksCount());

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    void run_startsThread() throws InterruptedException {
        Thread thread = new Thread(taskManagement);
        thread.start();
        thread.join(1000); // Wait max 1 second
        
        // Thread should complete successfully
        assertFalse(thread.isAlive());
    }

    @Test
    void addTask_multipleNulls_allowsMultipleNulls() {
        taskManagement.addTask(null);
        taskManagement.addTask(null);
        taskManagement.addTask(task1);
        
        assertEquals(3, taskManagement.getTasksCount());
        assertEquals(null, taskManagement.getTask(0));
        assertEquals(null, taskManagement.getTask(1));
        assertSame(task1, taskManagement.getTask(2));
    }

    @Test
    void updateTask_withNull_replacesWithNull() {
        taskManagement.addTask(task1);
        
        Task previous = taskManagement.updateTask(0, null);
        
        assertSame(task1, previous);
        assertEquals(null, taskManagement.getTask(0));
        assertEquals(1, taskManagement.getTasksCount());
    }

    @Test
    void removeTask_fromEmptyList_throwsException() {
        assertThrows(IndexOutOfBoundsException.class, () -> taskManagement.removeTask(0));
    }

    @Test
    void removeTaskByObject_fromEmptyList_returnsFalse() {
        boolean result = taskManagement.removeTask(task1);
        assertFalse(result);
    }

    @Test
    void getTasks_emptyList_returnsEmptyUnmodifiableList() {
        List<Task> tasks = taskManagement.getTasks();
        
        assertTrue(tasks.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> tasks.add(task1));
    }

    @Test
    void threadSafety_mixedOperations() throws InterruptedException {
        // Pre-populate with some tasks
        for (int i = 0; i < 50; i++) {
            taskManagement.addTask(new Task());
        }

        final int numThreads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        // Thread 1: Add tasks
        executor.submit(() -> {
            try {
                for (int i = 0; i < 20; i++) {
                    taskManagement.addTask(new Task());
                }
            } finally {
                latch.countDown();
            }
        });

        // Thread 2: Update tasks
        executor.submit(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    try {
                        if (taskManagement.getTasksCount() > i) {
                            taskManagement.updateTask(i, new Task());
                        }
                    } catch (IndexOutOfBoundsException e) {
                        // Expected in concurrent environment
                    }
                }
            } finally {
                latch.countDown();
            }
        });

        // Thread 3: Remove tasks by index
        executor.submit(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    try {
                        if (taskManagement.getTasksCount() > 0) {
                            taskManagement.removeTask(0);
                        }
                    } catch (IndexOutOfBoundsException e) {
                        // Expected in concurrent environment
                    }
                }
            } finally {
                latch.countDown();
            }
        });

        // Thread 4: Get tasks count
        executor.submit(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    taskManagement.getTasksCount();
                }
            } finally {
                latch.countDown();
            }
        });

        // Thread 5: Get tasks list
        executor.submit(() -> {
            try {
                for (int i = 0; i < 50; i++) {
                    taskManagement.getTasks();
                }
            } finally {
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);

        // Verify we have some tasks and no corruption
        assertTrue(taskManagement.getTasksCount() >= 0);
        assertEquals(taskManagement.getTasksCount(), taskManagement.getTasks().size());

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    }
}