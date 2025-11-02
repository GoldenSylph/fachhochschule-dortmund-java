package de.fachhochschule.dortmund.bads.hm1.bedrin.systems.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import de.fachhochschule.dortmund.bads.hm1.bedrin.Task;

public class TaskManagementCrudTest {

    @Test
    public void createTasks_addAndGet() {
        TaskManagement tm = new TaskManagement();
        assertEquals(0, tm.getTasksCount());

        Task t1 = new Task();
        Task t2 = new Task();
        tm.addTask(t1);
        tm.addTask(t2);
        assertEquals(2, tm.getTasksCount());
        assertSame(t1, tm.getTask(0));
        assertSame(t2, tm.getTask(1));
    }

    @Test
    public void updateTask_replacesAtIndex() {
        TaskManagement tm = new TaskManagement();
        Task t1 = new Task();
        Task t2 = new Task();
        Task t3 = new Task();
        tm.addTask(t1);
        tm.addTask(t2);

        Task prev = tm.updateTask(1, t3);
        assertSame(t2, prev);
        assertSame(t3, tm.getTask(1));
        assertEquals(2, tm.getTasksCount());
    }
}