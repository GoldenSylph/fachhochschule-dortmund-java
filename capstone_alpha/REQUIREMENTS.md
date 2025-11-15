# BADS - System Requirements Specification
## Beverage Automated Distribution System

**Project:** Capstone Alpha  
**Version:** 1.0  
**Date:** November 15, 2025  
**Institution:** Fachhochschule Dortmund

---

## 📋 Table of Contents
1. [Functional Requirements](#functional-requirements)
2. [Non-Functional Requirements](#non-functional-requirements)
3. [Constraints](#constraints)
4. [Domain Description](#domain-description)

---

## 🎯 Functional Requirements

### FR1 - Warehouse Storage Management

**FR1.1** The system shall support 7 distinct storage cell types:
- **AMBIENT** - Standard temperature storage
- **REFRIGERATED** - Temperature-controlled storage
- **BULK** - Large-volume storage
- **CHARGING_STATION** - AGV charging locations
- **LOADING** - Truck loading/unloading docks
- **CORRIDOR** - Navigation pathways
- **ANY** - Multi-purpose storage

**FR1.2** The system shall track beverage box placement, retrieval, and inventory levels in real-time.

**FR1.3** The system shall validate storage type compatibility before allowing box placement operations.

**FR1.4** The system shall maintain capacity constraints for each storage cell based on dimensions (width × depth × height).

**FR1.5** The system shall support storage cell occupation and release operations with state tracking.

---

### FR2 - AGV Fleet Operations

**FR2.1** The system shall manage autonomous AGV navigation using Dijkstra's graph-based pathfinding algorithm.

**FR2.2** The system shall execute programmable AGV instructions via a 7-instruction stack machine architecture:
- `SETUP` - Initialize AGV with storage and position
- `PUSH` - Push values onto execution stack
- `MOVE` - Navigate to destination
- `TAKE` - Pick up beverage box
- `RELEASE` - Drop off beverage box
- `CHARGE` - Charge at charging station
- `STOP` - Emergency halt

**FR2.3** The system shall track AGV battery levels with configurable drain rate per action (default: 5% per move).

**FR2.4** The system shall trigger automatic charging requests when battery drops below configurable threshold (default: 20%).

**FR2.5** The system shall implement FIFO (First-In-First-Out) charging queue for managing limited charging station resources.

**FR2.6** The system shall support 5 distinct AGV states:
- `IDLE` - Ready for task assignment
- `BUSY` - Executing task
- `WAITING_FOR_CHARGE` - In charging queue
- `MOVING_TO_CHARGE` - Navigating to charging station
- `CHARGING` - Currently charging

**FR2.7** The system shall charge AGV batteries at configurable rate per tick (default: 10% per tick).

**FR2.8** The system shall automatically release charging stations when AGV reaches 100% battery.

---

### FR3 - Task Scheduling & Execution

**FR3.1** The system shall maintain priority-based task queues for concurrent task processing.

**FR3.2** The system shall assign tasks to available AGVs based on resource availability and battery levels.

**FR3.3** The system shall support multi-step task workflows including:
- Storage cell pickup operations
- Pathfinding and navigation
- Delivery to target locations
- Truck loading operations

**FR3.4** The system shall handle task abortion and reassignment mechanisms on failure conditions.

**FR3.5** The system shall execute tasks as separate threads with process sequencing.

**FR3.6** The system shall track task completion status and execution duration.

---

### FR4 - Truck Fleet Management

**FR4.1** The system shall coordinate truck loading operations at designated loading dock cells.

**FR4.2** The system shall navigate trucks through 10×10 city grid using shortest path algorithms.

**FR4.3** The system shall track truck cargo capacity and current load status.

**FR4.4** The system shall support truck delivery schedules and route planning.

**FR4.5** The system shall manage truck resource allocation for loading dock operations.

---

### FR5 - Real-Time Simulation

**FR5.1** The system shall execute tick-based time coordination at configurable intervals (default: 1000ms per tick).

**FR5.2** The system shall synchronize all components (AGVs, trucks, management systems) via `ITickable` interface on each tick.

**FR5.3** The system shall support pause/resume simulation controls with volatile state management.

**FR5.4** The system shall maintain simulation tick counter for temporal tracking.

**FR5.5** The system shall notify all registered tickable components on each simulation tick.

---

### FR6 - GUI Visualization

**FR6.1** The system shall display real-time warehouse status with up to 3 AGVs and 3 loading bays.

**FR6.2** The system shall show AGV battery levels with visual indicators (progress bars, percentage display).

**FR6.3** The system shall visualize AGV states with color-coded status indicators:
- Green - IDLE
- Blue - BUSY
- Yellow - WAITING_FOR_CHARGE
- Orange - MOVING_TO_CHARGE
- Red - CHARGING

**FR6.4** The system shall visualize storage cell occupancy and inventory levels in real-time.

**FR6.5** The system shall provide interactive controls for simulation management (start, pause, resume, stop).

**FR6.6** The system shall update GUI components on each simulation tick via Swing EDT.

**FR6.7** The system shall display AGV current positions on warehouse grid visualization.

**FR6.8** The system shall show truck loading progress and cargo status.

---

## ⚡ Non-Functional Requirements

### NFR1 - Performance

**NFR1.1** Pathfinding operations shall compute optimal routes in:
- <1ms for warehouse area (7×5 grid, 35 nodes)
- <2ms for city area (10×10 grid, 100 nodes)

**NFR1.2** The system shall handle 15+ concurrent tasks with 5 AGVs without performance degradation.

**NFR1.3** Tick processing shall complete within 50% of tick interval (e.g., 500ms for 1000ms ticks) to prevent backlog.

**NFR1.4** GUI rendering updates shall execute within <100ms of backend state changes.

**NFR1.5** Memory footprint for graph storage shall not exceed:
- 5KB for warehouse area
- 15KB for city area

---

### NFR2 - Concurrency

**NFR2.1** The system shall utilize 4 dedicated system threads:
- **CLOCKING** - Tick-based time coordinator (continuous loop)
- **TASK_MANAGEMENT** - Task queue maintenance (periodic 1s cycles)
- **STORAGE_MANAGEMENT** - Warehouse operations (event-driven)
- **OBSERVATION** - Metrics & monitoring (continuous)

**NFR2.2** The system shall ensure thread-safe operations using appropriate synchronization primitives:
- `ConcurrentLinkedQueue` for charging queue
- `CopyOnWriteArrayList` for tickable registration
- `AtomicInteger` for charging station availability
- `volatile` flags for state management

**NFR2.3** The system shall prevent deadlocks and race conditions in shared resource access.

**NFR2.4** The system shall use `ExecutorService` with cached thread pools for parallel resource execution.

**NFR2.5** GUI operations shall execute on Swing Event Dispatch Thread (EDT) to prevent UI freezing.

---

### NFR3 - Scalability

**NFR3.1** The system shall support configurable number of AGVs (tested range: 3-12 AGVs).

**NFR3.2** The system shall handle variable charging station counts (tested range: 2-5 stations).

**NFR3.3** The system shall support warehouse grid size modifications through configuration without code changes.

**NFR3.4** The system shall scale to multiple warehouses in library mode (non-GUI).

**NFR3.5** Task queue shall handle unbounded task submissions with graceful degradation.

---

### NFR4 - Reliability

**NFR4.1** The system shall handle AGV battery depletion gracefully with automatic charging initiation.

**NFR4.2** The system shall implement comprehensive exception handling with custom exception hierarchy:
- `InvalidNotationException`
- `InvalidCoordinatesException`
- `StorageCellMismatchException`
- `StorageException`
- `ResourceException`
- `SystemException`
- `ProcessExecutionException`

**NFR4.3** The system shall recover from task failures through reassignment mechanisms.

**NFR4.4** The system shall validate all input coordinates and cell notations before operations.

**NFR4.5** The system shall maintain data consistency during concurrent access to shared resources.

**NFR4.6** The system shall log all critical errors with stack traces for debugging.

---

### NFR5 - Maintainability

**NFR5.1** The system shall follow dependency injection pattern via singleton enum configurations for loose coupling.

**NFR5.2** The system shall provide comprehensive logging via Log4j2 with configurable log levels.

**NFR5.3** The system shall include UML class diagrams for all 9 major subsystems:
- Configuration layer
- Domain models
- Resources
- Systems
- Exceptions
- GUI components (main, AGV, inventory)
- Unit tests

**NFR5.4** The system shall achieve minimum 80% test coverage with unit and integration tests.

**NFR5.5** The system shall follow builder pattern for complex object construction.

**NFR5.6** Code shall adhere to Java naming conventions and include JavaDoc for public APIs.

---

### NFR6 - Usability

**NFR6.1** GUI shall update in real-time with <100ms latency from backend state changes.

**NFR6.2** The system shall provide clear visual indicators for all AGV states and battery levels.

**NFR6.3** Logging shall use appropriate levels for different message types:
- `DEBUG` - Detailed diagnostic information
- `INFO` - General informational messages
- `WARN` - Warning conditions
- `ERROR` - Error events with recovery

**NFR6.4** Storage cell notation shall use human-readable format (e.g., "1A", "6D") for operations.

**NFR6.5** GUI controls shall provide immediate visual feedback on user interactions.

---

### NFR7 - Extensibility

**NFR7.1** The system shall support adding new storage cell types through enum extension without core logic changes.

**NFR7.2** The system shall allow new AGV instructions via stack machine `Operand` enum extension.

**NFR7.3** Configuration parameters shall be modifiable without recompilation (via configuration classes).

**NFR7.4** The system shall support custom pathfinding algorithms through `Area` class extension.

**NFR7.5** New system threads shall be addable through `Systems` enum registration.

---

## 🔒 Constraints

### C1 - Technology Stack

**C1.1** The system shall be implemented in **Java 25** or compatible JVM version.

**C1.2** The system shall use **Maven** for build management and dependency resolution.

**C1.3** The system shall use **Log4j2** for logging infrastructure.

**C1.4** The system shall use **JUnit 5** for unit and integration testing.

**C1.5** The system shall use **Swing** framework for GUI implementation.

**C1.6** The system shall not require external databases or persistence layers.

---

### C2 - Resource Limitations

**C2.1** Maximum **3 charging stations** per warehouse (configurable via `AGVManagementConfiguration`).

**C2.2** Fixed **7×5 warehouse grid** (35 storage cells total).

**C2.3** Fixed **10×10 city grid** (100 navigation points for truck routing).

**C2.4** GUI mode limited to **1 warehouse visualization** with **3 AGVs** and **3 loading bays**.

**C2.5** AGV battery capacity fixed at **100%** with discrete percentage tracking.

---

### C3 - Business Rules

**C3.1** AGVs **must** initiate charging when battery drops below 20% threshold (configurable).

**C3.2** Charging stations can only serve **one AGV at a time** (exclusive resource).

**C3.3** Tasks **require** available AGV assignment before execution can begin.

**C3.4** Storage cells can only contain **type-compatible** beverage boxes (type validation enforced).

**C3.5** AGVs in charging queue **cannot** be assigned new tasks until charging completes.

**C3.6** Pathfinding **must** use valid graph edges only (no diagonal movement through obstacles).

**C3.7** AGV programs **must** start with `SETUP` operation to initialize storage reference.

**C3.8** Charging stations are **immutable resources** (cannot be moved or removed during simulation).

---

## 📊 Domain Description

### Overview
BADS (Beverage Automated Distribution System) is a **concurrent warehouse simulation system** for automated beverage logistics, coordinating autonomous AGV fleets, multi-type storage facilities, and city-wide truck distribution networks.

### Key Domain Entities

**1. Warehouse Operations**
- 7×5 grid storage facility with 7 specialized cell types managing beverage inventory through graph-based pathfinding navigation
- Graph-based spatial model using Dijkstra's algorithm for optimal route calculation
- Storage capacity management with type compatibility enforcement

**2. Autonomous AGV Fleet**
- Battery-constrained autonomous vehicles with self-managed charging queues executing programmable stack machine instructions for box movement
- 5-state lifecycle management (IDLE, BUSY, WAITING_FOR_CHARGE, MOVING_TO_CHARGE, CHARGING)
- Shared charging station resource pool with FIFO queue coordination

**3. Task Coordination**
- Priority-based concurrent task scheduling system coordinating multi-step operations from storage pickup through truck loading
- Thread-based task execution with process sequencing and resource allocation
- Dynamic task assignment based on AGV availability and battery levels

**4. City Distribution**
- Truck fleet navigating 10×10 city grids coordinated by real-time tick-based simulation with 4 parallel system threads
- Loading dock coordination for warehouse-to-truck transfer operations
- Delivery route optimization using shortest path algorithms

### Core Operations Flow
```
Task Creation → Task Queue → AGV Assignment → Pathfinding → 
Box Pickup → Navigation → Box Delivery → Truck Loading → 
City Distribution → Completion
```

### Domain Constraints
- **Limited Charging Stations** - Requires queue-based resource allocation and prioritization
- **Battery Capacity** - Governs AGV operational range and task assignment feasibility
- **Storage Type Compatibility** - Determines valid box placement and retrieval locations
- **Graph-Based Navigation** - Ensures collision-free pathfinding within warehouse topology
- **Concurrent Resource Access** - Requires thread-safe coordination across multiple systems

---

## 📝 Appendix

### Requirement Traceability Matrix

| Requirement ID | Priority | Verification Method | Test Coverage |
|----------------|----------|---------------------|---------------|
| FR1.1-FR1.5 | HIGH | Unit Tests | `StorageTest`, `StorageCellTest` |
| FR2.1-FR2.8 | CRITICAL | Unit + Integration Tests | `AGVTest`, `ProcessTest` |
| FR3.1-FR3.6 | HIGH | Unit + Integration Tests | `TaskManagementTest`, `ParallelSystemsIntegrationTest` |
| FR4.1-FR4.5 | MEDIUM | Unit Tests | `TruckTest` |
| FR5.1-FR5.5 | CRITICAL | Unit Tests | `ClockingSimulationTest` |
| FR6.1-FR6.8 | MEDIUM | Manual GUI Testing | Visual inspection |
| NFR1.1-NFR1.5 | HIGH | Performance Tests | `AreaTest` pathfinding benchmarks |
| NFR2.1-NFR2.5 | CRITICAL | Integration Tests | `ParallelSystemsIntegrationTest` |
| NFR4.1-NFR4.6 | HIGH | Exception Tests | Custom exception test cases |

### Configuration Parameters

| Parameter | Default Value | Configurable Via | Valid Range |
|-----------|---------------|------------------|-------------|
| Tick Interval | 1000ms | `ClockingSimulation` | 100ms - 5000ms |
| Battery Threshold | 20% | `AGVManagementConfiguration` | 10% - 50% |
| Charge Rate | 10%/tick | `AGVManagementConfiguration` | 5% - 20% |
| Battery Drain | 5%/move | AGV constructor | 1% - 10% |
| Charging Stations | 3 | `AGVManagementConfiguration` | 1 - 5 |
| Warehouse Grid | 7×5 | `App.java` createGrid | 5×5 - 10×10 |
| City Grid | 10×10 | `App.java` createGrid | 5×5 - 20×20 |

---

**Document Version Control:**
- v1.0 - Initial requirements specification (2025-11-15)
