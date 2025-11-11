package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fachhochschule.dortmund.bads.model.Area;
import de.fachhochschule.dortmund.bads.model.Storage;
import de.fachhochschule.dortmund.bads.model.StorageCell;
import de.fachhochschule.dortmund.bads.model.Area.Point;
import de.fachhochschule.dortmund.bads.model.StorageCell.Type;
import de.fachhochschule.dortmund.bads.resources.AGV;
import de.fachhochschule.dortmund.bads.resources.BeveragesBox;
import de.fachhochschule.dortmund.bads.resources.Resource;
import de.fachhochschule.dortmund.bads.resources.AGV.Operand;
import de.fachhochschule.dortmund.bads.resources.AGV.Statement;

class AGVTest {

    private AGV agv;
    private Storage storage;
    private Area area;

    @BeforeEach
    void setUp() {
        // Use factory method to create AGV
        agv = CoreConfiguration.INSTANCE.newAGV();
        
        // Use factory method to create Area
        area = CoreConfiguration.INSTANCE.newArea();
        Map<Point, Set<Point>> adjacencyMap = new HashMap<>();
        
        Point p00 = new Point(0, 0);
        Point p10 = new Point(1, 0);
        Point p20 = new Point(2, 0);
        Point p01 = new Point(0, 1);
        Point p11 = new Point(1, 1);
        
        adjacencyMap.put(p00, Set.of(p10, p01));
        adjacencyMap.put(p10, Set.of(p00, p20, p11));
        adjacencyMap.put(p20, Set.of(p10));
        adjacencyMap.put(p01, Set.of(p00, p11));
        adjacencyMap.put(p11, Set.of(p10, p01));
        
        area.setGraph(adjacencyMap);
        
        // Use factory methods to create storage cells
        StorageCell[] cells = new StorageCell[5];
        cells[0] = CoreConfiguration.INSTANCE.newStorageCell(Type.ANY, 10, 10, 10); // p00
        cells[1] = CoreConfiguration.INSTANCE.newStorageCell(Type.ANY, 10, 10, 10); // p10
        cells[2] = CoreConfiguration.INSTANCE.newChargingStation(); // p20
        cells[3] = CoreConfiguration.INSTANCE.newStorageCell(Type.ANY, 10, 10, 10); // p01
        cells[4] = CoreConfiguration.INSTANCE.newStorageCell(Type.ANY, 10, 10, 10); // p11
        
        // Use factory method to create Storage
        storage = CoreConfiguration.INSTANCE.newStorage(area, cells);
    }

    @Test
    void testInitialState() {
        assertEquals(1.0, agv.getQuantity());
        assertEquals(1, agv.getTicksPerMovement());
    }

    @Test
    void testSetTicksPerMovement() {
        agv.setTicksPerMovement(3);
        assertEquals(3, agv.getTicksPerMovement());
        
        agv.setTicksPerMovement(1);
        assertEquals(1, agv.getTicksPerMovement());
    }

    @Test
    void testSetTicksPerMovementInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> agv.setTicksPerMovement(0));
        assertThrows(IllegalArgumentException.class, () -> agv.setTicksPerMovement(-1));
    }

    @Test
    void testCacheProgram() {
        Statement<?>[] program = {
            new Statement<>(Operand.SETUP, storage, new Point(0, 0)),
            new Statement<>(Operand.PUSH, "2A"),
            new Statement<>(Operand.MOVE)
        };
        
        agv.cacheProgram(program);
        
        // Should not throw exception when calling
        assertDoesNotThrow(() -> agv.call());
    }

    @Test
    void testSetupOperation() {
        Point startPosition = new Point(0, 0);
        Statement<?>[] program = {
            new Statement<>(Operand.SETUP, storage, startPosition)
        };
        
        agv.executeProgram(program);
        
        // Setup should initialize storage and current position
        // We can't directly access private fields, but we can test behavior
        assertDoesNotThrow(() -> agv.executeProgram(program));
    }

    @Test
    void testPushOperation() {
        Statement<?>[] program = {
            new Statement<>(Operand.PUSH, "test_value"),
            new Statement<>(Operand.PUSH, new Point(1, 1))
        };
        
        assertDoesNotThrow(() -> agv.executeProgram(program));
    }

    @Test
    void testStopOperation() {
        Statement<?>[] program = {
            new Statement<>(Operand.PUSH, "test_value"),
            new Statement<>(Operand.STOP),
            new Statement<>(Operand.PUSH, "should_not_execute")
        };
        
        assertDoesNotThrow(() -> agv.executeProgram(program));
    }

    @Test
    void testMoveOperation() {
        Point startPosition = new Point(0, 0);
        Statement<?>[] program = {
            new Statement<>(Operand.SETUP, storage, startPosition),
            new Statement<>(Operand.PUSH, "2A"), // Point(1, 0)
            new Statement<>(Operand.MOVE)
        };
        
        assertDoesNotThrow(() -> agv.executeProgram(program));
    }

    @Test
    void testChargeOperation() {
        Point startPosition = new Point(0, 0);
        Statement<?>[] program = {
            new Statement<>(Operand.SETUP, storage, startPosition),
            new Statement<>(Operand.PUSH, "3A"), // Point(2, 0) - charging station
            new Statement<>(Operand.CHARGE)
        };
        
        assertDoesNotThrow(() -> agv.executeProgram(program));
    }

    @Test
    void testTakeAndReleaseOperations() {
        Point startPosition = new Point(0, 0);
        BeveragesBox box = new BeveragesBox(BeveragesBox.Type.AMBIENT, "TestBeverage", 5, 5, 5, 10);
        
        Statement<?>[] program = {
            new Statement<>(Operand.SETUP, storage, startPosition),
            new Statement<>(Operand.PUSH, box),
            new Statement<>(Operand.PUSH, "1A"), // Point(0, 0)
            new Statement<>(Operand.TAKE),
            new Statement<>(Operand.PUSH, box),
            new Statement<>(Operand.PUSH, "1A"), // Point(0, 0)
            new Statement<>(Operand.RELEASE)
        };
        
        assertDoesNotThrow(() -> agv.executeProgram(program));
    }

    @Test
    void testOnTickBatteryManagement() {
        // Test initial battery level and charging behavior
        agv.setTicksPerMovement(1);
        
        // Simulate battery depletion scenario
        Point startPosition = new Point(0, 0);
        Statement<?>[] setupProgram = {
            new Statement<>(Operand.SETUP, storage, startPosition)
        };
        agv.executeProgram(setupProgram);
        
        // Test multiple ticks to see battery behavior
        for (int i = 0; i < 5; i++) {
            final int tick = i;
            assertDoesNotThrow(() -> agv.onTick(tick));
        }
    }

    @Test
    void testOnTickMovementWithDifferentSpeeds() {
        Point startPosition = new Point(0, 0);
        Statement<?>[] program = {
            new Statement<>(Operand.SETUP, storage, startPosition),
            new Statement<>(Operand.PUSH, "2A"), // Point(1, 0)
            new Statement<>(Operand.MOVE)
        };
        
        agv.executeProgram(program);
        
        // Test with ticksPerMovement = 1 (default)
        agv.setTicksPerMovement(1);
        assertDoesNotThrow(() -> agv.onTick(1));
        
        // Test with ticksPerMovement = 3
        agv.setTicksPerMovement(3);
        assertDoesNotThrow(() -> agv.onTick(1));
        assertDoesNotThrow(() -> agv.onTick(2));
        assertDoesNotThrow(() -> agv.onTick(3));
    }

    @Test
    void testOnTickChargingBehavior() {
        Point startPosition = new Point(2, 0); // Charging station position
        Statement<?>[] program = {
            new Statement<>(Operand.SETUP, storage, startPosition),
            new Statement<>(Operand.PUSH, "3A"), // Point(2, 0) - charging station
            new Statement<>(Operand.CHARGE)
        };
        
        agv.executeProgram(program);
        
        // Execute the charge operation by moving to the charging station and triggering operation
        agv.onTick(1); // This should trigger pathfinding to charging station
        
        // Simulate multiple ticks to test charging behavior
        for (int i = 2; i < 10; i++) {
            final int tick = i;
            assertDoesNotThrow(() -> agv.onTick(tick));
        }
    }

    @Test
    void testOnTickPathfinding() {
        Point startPosition = new Point(0, 0);
        Statement<?>[] program = {
            new Statement<>(Operand.SETUP, storage, startPosition),
            new Statement<>(Operand.PUSH, "3A"), // Point(2, 0)
            new Statement<>(Operand.MOVE)
        };
        
        agv.executeProgram(program);
        
        // Test pathfinding behavior - should calculate path from (0,0) to (2,0)
        assertDoesNotThrow(() -> agv.onTick(1));
        
        // Continue ticking to simulate movement along path
        for (int i = 2; i < 10; i++) {
            final int tick = i;
            assertDoesNotThrow(() -> agv.onTick(tick));
        }
    }

    @Test
    void testOnTickWithNoPath() {
        Point startPosition = new Point(0, 0);
        Statement<?>[] program = {
            new Statement<>(Operand.SETUP, storage, startPosition)
        };
        
        agv.executeProgram(program);
        
        // Test tick with no destinations - should not cause errors
        assertDoesNotThrow(() -> agv.onTick(1));
        assertDoesNotThrow(() -> agv.onTick(2));
    }

    @Test
    void testComplexMovementSequence() {
        Point startPosition = new Point(0, 0);
        Statement<?>[] program = {
            new Statement<>(Operand.SETUP, storage, startPosition),
            new Statement<>(Operand.PUSH, "2A"), // Point(1, 0)
            new Statement<>(Operand.MOVE),
            new Statement<>(Operand.PUSH, "3A"), // Point(2, 0)
            new Statement<>(Operand.MOVE),
            new Statement<>(Operand.PUSH, "1B"), // Point(0, 1)
            new Statement<>(Operand.MOVE)
        };
        
        agv.executeProgram(program);
        
        // Simulate multiple ticks to execute the movement sequence
        for (int i = 1; i <= 20; i++) {
            final int tick = i;
            assertDoesNotThrow(() -> agv.onTick(tick));
        }
    }

    @Test
    void testMovementSpeedVariations() {
        Point startPosition = new Point(0, 0);
        Statement<?>[] program = {
            new Statement<>(Operand.SETUP, storage, startPosition),
            new Statement<>(Operand.PUSH, "3A"), // Point(2, 0)
            new Statement<>(Operand.MOVE)
        };
        
        agv.executeProgram(program);
        
        // Test with different movement speeds
        int[] speeds = {1, 2, 3, 5};
        
        for (int speed : speeds) {
            AGV testAgv = new AGV();
            testAgv.setTicksPerMovement(speed);
            testAgv.executeProgram(program);
            
            // Simulate movement with this speed
            for (int tick = 1; tick <= speed * 5; tick++) {
                final int currentTick = tick;
                assertDoesNotThrow(() -> testAgv.onTick(currentTick));
            }
        }
    }

    @Test
    void testResourceCallMethod() {
        Statement<?>[] program = {
            new Statement<>(Operand.SETUP, storage, new Point(0, 0))
        };
        
        agv.cacheProgram(program);
        
        Resource result = assertDoesNotThrow(() -> agv.call());
        assertSame(agv, result);
    }

    @Test
    void testOperationExecutionErrors() {
        Point startPosition = new Point(0, 0);
        BeveragesBox box = new BeveragesBox(BeveragesBox.Type.AMBIENT, "TestBeverage", 5, 5, 5, 10);
        
        // Test TAKE operation with invalid position
        Statement<?>[] program = {
            new Statement<>(Operand.SETUP, storage, startPosition),
            new Statement<>(Operand.PUSH, box),
            new Statement<>(Operand.PUSH, "2A"), // Point(1, 0) - different from current position
            new Statement<>(Operand.TAKE)
        };
        
        agv.executeProgram(program);
        
        // Move to destination and try to execute operation
        // This should handle the error gracefully
        assertDoesNotThrow(() -> {
            for (int i = 1; i <= 10; i++) {
                agv.onTick(i);
            }
        });
    }

    @Test
    void testBatteryDepletionHandling() {
        Point startPosition = new Point(0, 0);
        Statement<?>[] program = {
            new Statement<>(Operand.SETUP, storage, startPosition),
            new Statement<>(Operand.PUSH, "3A"), // Point(2, 0)
            new Statement<>(Operand.MOVE)
        };
        
        agv.executeProgram(program);
        
        // Simulate many ticks to potentially deplete battery
        for (int i = 1; i <= 50; i++) {
            final int tick = i;
            assertDoesNotThrow(() -> agv.onTick(tick));
        }
    }

    @Test
    void testTicksPerMovementBehavior() {
        Point startPosition = new Point(0, 0);
        Statement<?>[] program = {
            new Statement<>(Operand.SETUP, storage, startPosition),
            new Statement<>(Operand.PUSH, "2A"), // Point(1, 0)
            new Statement<>(Operand.MOVE)
        };
        
        agv.setTicksPerMovement(3);
        agv.executeProgram(program);
        
        // With ticksPerMovement = 3, movement should only happen every 3 ticks
        agv.onTick(1); // Should start pathfinding
        agv.onTick(2); // Should increment counter but not move
        agv.onTick(3); // Should increment counter but not move
        agv.onTick(4); // Should move (counter reaches 3)
        
        // All ticks should complete without errors
        assertDoesNotThrow(() -> {
            for (int i = 1; i <= 10; i++) {
                agv.onTick(i);
            }
        });
    }

    @Test
    void testMultipleOperationsSequence() {
        Point startPosition = new Point(0, 0);
        BeveragesBox box1 = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Beverage1", 5, 5, 5, 10);
        BeveragesBox box2 = new BeveragesBox(BeveragesBox.Type.REFRIGERATED, "Beverage2", 3, 3, 3, 5);
        
        Statement<?>[] program = {
            new Statement<>(Operand.SETUP, storage, startPosition),
            // Move to position and take first box
            new Statement<>(Operand.PUSH, box1),
            new Statement<>(Operand.PUSH, "1A"),
            new Statement<>(Operand.TAKE),
            // Move to another position
            new Statement<>(Operand.PUSH, "2A"),
            new Statement<>(Operand.MOVE),
            // Take second box
            new Statement<>(Operand.PUSH, box2),
            new Statement<>(Operand.PUSH, "2A"),
            new Statement<>(Operand.TAKE),
            // Move to charging station
            new Statement<>(Operand.PUSH, "3A"),
            new Statement<>(Operand.MOVE),
            new Statement<>(Operand.PUSH, "3A"),
            new Statement<>(Operand.CHARGE)
        };
        
        agv.executeProgram(program);
        
        // Execute the complex sequence
        for (int i = 1; i <= 30; i++) {
            final int tick = i;
            assertDoesNotThrow(() -> agv.onTick(tick));
        }
    }

    @Test
    void testEdgeCaseEmptyProgram() {
        Statement<?>[] emptyProgram = {};
        assertDoesNotThrow(() -> agv.executeProgram(emptyProgram));
        assertDoesNotThrow(() -> agv.onTick(1));
    }

    @Test
    void testEdgeCaseNullProgram() {
        // Test behavior with null cached program
        agv.cacheProgram(null);
        assertDoesNotThrow(() -> agv.call());
    }
}
