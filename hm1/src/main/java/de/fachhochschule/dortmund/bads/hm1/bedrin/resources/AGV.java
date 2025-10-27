package de.fachhochschule.dortmund.bads.hm1.bedrin.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.hm1.bedrin.Area.Point;
import de.fachhochschule.dortmund.bads.hm1.bedrin.interfaces.ICPU;
import de.fachhochschule.dortmund.bads.hm1.bedrin.systems.logic.Tickable;

public class AGV extends Resource implements ICPU<InputStream, OutputStream>, Tickable {
    private static final Logger LOGGER = LogManager.getLogger();
    
    public static final RuntimeException SYNTAX_EXCEPTION_PROGRAM_HAS_NOT_BEEN_STARTED = new RuntimeException(
            "Syntax Error: always start program with 0x00 byte code");

    private double batteryLoad, batteryConsumptionPerTick;
    private final int[] position = new int[2];

    // constant costs (in ticks) for take/release, configured via constructor
    private final int takeCostTicks;
    private final int releaseCostTicks;

    private boolean programRunning;
    private OutputStream outputBuffer;
    private InputStream cachedProgram;

    // movement/operation state
    private final Deque<Point> waypointQueue = new ArrayDeque<>();
    private int movementPerTick = 1; // how many waypoints to advance per tick

    // pending operations that complete after N ticks. key format: "take:<id>" |
    // "release:<id>"
    private final Map<String, Integer> pendingOperations = new HashMap<>();
    private final Set<String> heldResources = new HashSet<>();

    public AGV() {
        this(3, 3);
    }

    public AGV(int takeCostTicks, int releaseCostTicks) {
        this.takeCostTicks = Math.max(0, takeCostTicks);
        this.releaseCostTicks = Math.max(0, releaseCostTicks);
        this.outputBuffer = new ByteArrayOutputStream();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("AGV created with takeCostTicks={} and releaseCostTicks={}", this.takeCostTicks, this.releaseCostTicks);
        }
    }

    // optional external configuration
    public void setMovementPerTick(int movementPerTick) {
        int old = this.movementPerTick;
        this.movementPerTick = Math.max(1, movementPerTick);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("movementPerTick updated from {} to {}", old, this.movementPerTick);
        }
    }

    public void setBatteryConsumptionPerTick(double value) {
        double old = this.batteryConsumptionPerTick;
        this.batteryConsumptionPerTick = Math.max(0.0, value);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("batteryConsumptionPerTick updated from {} to {}", old, this.batteryConsumptionPerTick);
        }
    }

    // helper APIs to avoid crafting bytecode
    public void enqueueWaypoint(int x, int y) {
        this.waypointQueue.addLast(new Point(x, y));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Waypoint enqueued: ({}, {}), queue size now {}", x, y, this.waypointQueue.size());
        }
    }

    public void scheduleTake(int resourceId) {
        this.pendingOperations.put("take:" + resourceId, Integer.valueOf(this.takeCostTicks));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Scheduled take for resource {} with cost {} ticks", resourceId, this.takeCostTicks);
        }
    }

    public void scheduleRelease(int resourceId) {
        this.pendingOperations.put("release:" + resourceId, Integer.valueOf(this.releaseCostTicks));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Scheduled release for resource {} with cost {} ticks", resourceId, this.releaseCostTicks);
        }
    }

    @Override
    public OutputStream getOutput() {
        ByteArrayOutputStream snapshot = new ByteArrayOutputStream();
        try {
            snapshot.write(((ByteArrayOutputStream) this.outputBuffer).toByteArray());
            int size = snapshot.size();
            ((ByteArrayOutputStream) this.outputBuffer).reset();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Output buffer drained: {} bytes returned", size);
            }
        } catch (IOException ioException) {
            LOGGER.error("Failed to read AGV output buffer", ioException);
            throw new RuntimeException(ioException);
        }
        return snapshot;
    }

    // compact reader over program bytes
    private static final class ProgramReader {
        private final byte[] data;
        private int positionPointer;

        ProgramReader(byte[] inputData) {
            this.data = inputData;
            this.positionPointer = 0;
        }

        boolean hasNext() {
            return this.positionPointer < this.data.length;
        }

        int readUnsignedByte() {
            return this.data[this.positionPointer++] & 0xFF;
        }

        byte[] readBytes(int length) {
            byte[] result = Arrays.copyOfRange(this.data, this.positionPointer, this.positionPointer + length);
            this.positionPointer += length;
            return result;
        }

        int readInt() {
            return ByteBuffer.wrap(readBytes(Integer.BYTES)).getInt();
        }
    }

    @Override
    public void executeProgram(InputStream programStream) {
        byte[] programBytes;
        try (programStream) {
            ByteArrayOutputStream outputCollector = new ByteArrayOutputStream();
            byte[] temporaryBuffer = new byte[1024];
            int bytesReadCount;
            while ((bytesReadCount = programStream.read(temporaryBuffer)) != -1) {
                outputCollector.write(temporaryBuffer, 0, bytesReadCount);
            }
            programBytes = outputCollector.toByteArray();
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Executing program stream ({} bytes)", programBytes.length);
            }
        } catch (IOException ioException) {
            LOGGER.error("Failed to read program stream", ioException);
            throw new RuntimeException(ioException);
        }

        ProgramReader reader = new ProgramReader(programBytes);

        // Opcodes supported:
        // 0x00 - start program (no tick advancement)
        // 0xFF - end program
        // 0x14 - enqueue waypoint: args int x (4 bytes), int y (4 bytes)
        // 0x20 - take resource: args 1 byte resource id (ticks are constant)
        // 0x21 - release resource: args 1 byte resource id (ticks are constant)

        do {
            if (!reader.hasNext())
                break;
            int opcode = reader.readUnsignedByte();
            switch (opcode) {
            case 0x00:
                if (!this.programRunning) {
                    this.programRunning = true;
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Program started");
                    }
                } else if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Program start opcode received while already running");
                }
                break;
            case 0xFF:
                if (!this.programRunning)
                    throw SYNTAX_EXCEPTION_PROGRAM_HAS_NOT_BEEN_STARTED;
                this.programRunning = false;
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Program ended");
                }
                break;
            case 0x14:
                ensureRunning();
                int waypointX = reader.readInt();
                int waypointY = reader.readInt();
                this.waypointQueue.addLast(new Point(waypointX, waypointY));
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Opcode 0x14: waypoint enqueued ({}, {})", waypointX, waypointY);
                }
                break;
            case 0x20:
                ensureRunning();
                int resourceIdByteTake = reader.readUnsignedByte();
                this.pendingOperations.put("take:" + resourceIdByteTake, Integer.valueOf(this.takeCostTicks));
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Opcode 0x20: scheduled take for resource {} ({} ticks)", resourceIdByteTake, this.takeCostTicks);
                }
                break;
            case 0x21:
                ensureRunning();
                int resourceIdByteRelease = reader.readUnsignedByte();
                this.pendingOperations.put("release:" + resourceIdByteRelease, Integer.valueOf(this.releaseCostTicks));
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Opcode 0x21: scheduled release for resource {} ({} ticks)", resourceIdByteRelease, this.releaseCostTicks);
                }
                break;
            default:
                LOGGER.error("Syntax Error: unknown byte code {}", opcode);
                throw new RuntimeException("Syntax Error: unknown byte code " + opcode);
            }
        } while (reader.hasNext());
    }

    // perform a single tick: drain battery, advance waypoints, progress operations
    private void performTick() {
        double before = this.batteryLoad;
        this.batteryLoad = Math.max(0.0, this.batteryLoad - this.batteryConsumptionPerTick);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Tick: battery {} -> {} (consumptionPerTick={})", before, this.batteryLoad, this.batteryConsumptionPerTick);
        }
        int moved = 0;
        int fromX = this.position[0];
        int fromY = this.position[1];
        for (int step = 0; step < this.movementPerTick && !this.waypointQueue.isEmpty(); step++) {
            Point next = this.waypointQueue.pollFirst();
            this.position[0] = next.x();
            this.position[1] = next.y();
            moved++;
        }
        if (moved > 0 && LOGGER.isDebugEnabled()) {
            LOGGER.debug("Tick: moved {} step(s) from ({}, {}) to ({}, {}), remaining waypoints {}", moved, fromX, fromY, this.position[0], this.position[1], this.waypointQueue.size());
        }
        List<String> completedKeys = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : this.pendingOperations.entrySet()) {
            int remaining = entry.getValue().intValue() - 1;
            if (remaining <= 0) {
                completedKeys.add(entry.getKey());
            } else {
                entry.setValue(Integer.valueOf(remaining));
            }
        }
        for (String key : completedKeys) {
            this.pendingOperations.remove(key);
            if (key.startsWith("take:")) {
                String resourceId = key.substring(5);
                this.heldResources.add(resourceId);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Tick: take completed for resource {}", resourceId);
                }
            } else if (key.startsWith("release:")) {
                String resourceId = key.substring(8);
                this.heldResources.remove(resourceId);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Tick: release completed for resource {}", resourceId);
                }
            }
        }
        if (!completedKeys.isEmpty() && LOGGER.isTraceEnabled()) {
            LOGGER.trace("Tick: operations completed {}", completedKeys);
        }
    }

    private void ensureRunning() {
        if (!this.programRunning) {
            LOGGER.error("Program opcode encountered before start (0x00)");
            throw SYNTAX_EXCEPTION_PROGRAM_HAS_NOT_BEEN_STARTED;
        }
    }

    @Override
    public Resource call() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("AGV call(): executing cached program");
        }
        executeProgram(this.cachedProgram);
        return this;
    }

    @Override
    public void cacheProgram(InputStream program) {
        this.cachedProgram = program;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Program cached: {}", program != null ? "non-null" : "null");
        }
    }

    @Override
    public void onTick() {
        if (this.programRunning) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("onTick(): programRunning=true");
            }
            performTick();
        } else if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("onTick(): programRunning=false, nothing to do");
        }
    }

    @Override
    public double getQuantity() {
        return 1.0;
    }
}
