package de.fachhochschule.dortmund.bads.hm1.bedrin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import de.fachhochschule.dortmund.bads.hm1.bedrin.systems.Process;

public class TaskCrudTest {

    @Test
    public void createProcesses_addAndGet() {
        Task task = new Task();
        assertEquals(0, task.getProcessesCount());

        Process p1 = new Process();
        Process p2 = new Process();
        task.addProcess(p1);
        task.addProcess(p2);

        assertEquals(2, task.getProcessesCount());
        assertSame(p1, task.getProcess(0));
        assertSame(p2, task.getProcess(1));
    }

    @Test
    public void updateProcess_replacesAtIndex() {
        Task task = new Task();
        Process p1 = new Process();
        Process p2 = new Process();
        Process p3 = new Process();
        task.addProcess(p1);
        task.addProcess(p2);

        Process previous = task.updateProcess(1, p3);
        assertSame(p2, previous);
        assertSame(p3, task.getProcess(1));
        assertEquals(2, task.getProcessesCount());
    }
}