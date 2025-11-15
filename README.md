# Homeworks project for Group 1 at MDT (Fachhochschule Dortmund)

## How to contribute your work?

1) Clone the repo.
2) Make a folder with your name or any name you'd like to utilize within the root directory.
3) Place there your Eclipse project.
4) Assign the package for the whole assignment solution and write it in the **My packages** section under your name. 
5) Make a screencast and place it in the `screencasts` folder, make the name so it would be recognizable that this is your work.
6) Explain in the section called **How to test my solution?** how one could execute and check the solution you wrote.
7) Profit =)

## How to test my solution?

### Oleg Bedrin

#### My packages

* Assignment 1 - package with the sources `de.fachhochschule.dortmund.bedrin.problems`
  1) Clone the repo.
  2) Open a folder inside the root that called `bedrin` in Eclipse IDE.
  3) Run the `de.fachhochschule.dortmund.bedrin.AssignmentOne` class.
* Assignment 2 - package with the sources `de.fachhochschule.dortmund.bedrin.facility`
  1) Clone the repo.
  2) Open a folder inside the root that called `bedrin` in Eclipse IDE.
  3) Run the `de.fachhochschule.dortmund.bedrin.AssignmentTwo` class.
* Assignment 3 - package with the sources `de.fachhochschule.dortmund.bedrin.inheritance`
  1) Clone the repo.
  2) Open a folder inside the root that called `bedrin` in Eclipse IDE.
  3) Run the `de.fachhochschule.dortmund.bedrin.AssignmentThree` class.
  4) The UML diagram can be found at the root under the name `bedrin_uml.png`.

### Saeid Rafiei (TEMPORARILY REMOVED FROM THE GROUP)
1) Clone the repo
2) Import rafiei in Eclipse
3) Run these classes from src/problemset/:

**For Task1:**
    `Problem1.java`
    `Problem2.java`
    `Problem3.java`
    `Problem4.java`
    
**For Task1:**
    `Task2UML.java`
	
	
4) Outputs are saved in outputs/ folder

### Rowena Pagayanan

* Home Assignment 1 - Assigned to log file management and metadata (create, delete and querying)
* Package with the sources `de.fachhochschule.dortmund.bads.hm1.rowena;`
  1) Clone the repo.
  2) Open a folder inside the root that called `hm1` in Eclipse IDE.
  3) Run the Module2.java inside `de.fachhochschule.dortmund.bads.hm1.rowena`.

### Bilyaminu Safiyanu Mohammed
* Home Assignment 1:  Archivation functionality and Junit tests
* Package with the sources `de.fachhochschule.dortmund.bads.hm1.bilyaminu;`
1) Clone the repository.
2) Open a folder inside the root directory named as `hm1` in Eclipse IDE.
3) Run the `ArchiveManager.java` and `ArchiveManagerTest.java` inside `de.fachhochschule.dortmund.bads.hm1.bilyaminu`.

### GROUP HOMEWORKS

#### Home Assignment 1 (hm1)
* **Main entry point:** `de.fachhochschule.dortmund.bads.hm1.App`
* **Main package:** `de.fachhochschule.dortmund.bads.hm1`

**Key modules and classes:**
- **Task Management System** - `de.fachhochschule.dortmund.bads.hm1.bedrin.systems.logic.TaskManagement`
- **Storage Management System** - `de.fachhochschule.dortmund.bads.hm1.bedrin.systems.logic.StorageManagement`
- **Observation System** - `de.fachhochschule.dortmund.bads.hm1.bedrin.systems.logic.Observation`
- **Log File Management** - `de.fachhochschule.dortmund.bads.hm1.rowena.LogFileManager`
- **Archive Manager** - `de.fachhochschule.dortmund.bads.hm1.bilyaminu.ArchiveManager`

**How to run:**
1. Clone the repo
2. Open the `hm1` folder in your IDE (Eclipse/IntelliJ)
3. Build the project using Maven: `mvn clean install`
4. Run the `App.java` class from `de.fachhochschule.dortmund.bads.hm1.App`
5. To run tests: `mvn test`

**Configuration classes:**
- `CoreConfiguration` - Core system configuration
- `ObservabilityConfiguration` - Logging and monitoring configuration
- `GUIConfiguration` - GUI settings

#### Home Assignment 2 (hm2)
* **Main entry point:** `de.fachhochschule.dortmund.bads.hm1.App`
* **Main package:** `de.fachhochschule.dortmund.bads.hm2`
* **Focus:** Exception handling and error management

**Key modules and classes:**
- **Exception Framework** - `de.fachhochschule.dortmund.bads.hm2.exceptions.*`
  - `InvalidNotationException` - Invalid coordinate/notation errors
  - `InvalidCoordinatesException` - Coordinate validation errors
  - `StorageCellMismatchException` - Storage cell conflicts
  - `StorageException` - General storage errors
  - `ResourceException` - Resource management errors
  - `SystemException` - System-level errors
  - `SystemConfigurationException` - Configuration errors
  - `ProcessExecutionException` - Process execution errors
- **Enhanced systems from hm1** with exception handling

**How to run:**
1. Clone the repo
2. Open the `hm2` folder in your IDE (Eclipse/IntelliJ)
3. Build the project using Maven: `mvn clean install`
4. Run the `App.java` class from `de.fachhochschule.dortmund.bads.hm1.App`
5. To run tests: `mvn test`

#### Home Assignment 3 (hm3)
* **Main entry point:** `de.fachhochschule.dortmund.bads.App`
* **Main package:** `de.fachhochschule.dortmund.bads`
* **Focus:** Refactored architecture with improved structure

**Key modules and classes:**
- **Model Package** - `de.fachhochschule.dortmund.bads.model.*`
  - `Storage` - Storage management model
  - `StorageCell` - Individual storage cell
  - `Area` - Area management
  - `Task` - Task representation
- **Systems Package** - `de.fachhochschule.dortmund.bads.systems.*`
  - `TaskManagement` - Task management system
  - `StorageManagement` - Storage operations
  - `Observation` - System monitoring
  - `ArchiveManager` - Archive management
  - `LogFileManager` - Log file operations
  - `ClockingSimulation` - Time simulation
- **Resources Package** - `de.fachhochschule.dortmund.bads.resources.*`
  - `AGV` - Automated Guided Vehicle
  - `Truck` - Truck resource
  - `BeveragesBox` - Beverages box resource
- **Exceptions Package** - `de.fachhochschule.dortmund.bads.exceptions.*`

**How to run:**
1. Clone the repo
2. Open the `hm3` folder in your IDE (Eclipse/IntelliJ)
3. Build the project using Maven: `mvn clean install`
4. Run the `App.java` class from `de.fachhochschule.dortmund.bads.App`
5. To run tests: `mvn test`

**Configuration classes:**
- `CoreConfiguration` - Core system configuration
- `ObservabilityConfiguration` - Logging and monitoring configuration
- `GUIConfiguration` - GUI settings

**Unit Tests:**
HM3 includes comprehensive unit and integration tests covering all major components:

**⭐ HOMEWORK 3 ASSIGNMENT TESTS:**
- `Homework3UnitTests` - **Official Homework 3 test suite (381 lines)**
  - **Test 1:** Simultaneous AGV Charging with Limited Resources
    - Tests 8 AGVs competing for 3 charging stations
    - Validates queue behavior and resource contention
    - Ensures all AGVs eventually charge to 100%
  - **Test 2:** Parallel Charging with Random Arrivals & Wait Time
    - Tests 12 AGVs arriving at random intervals (0-30 seconds)
    - Implements 15-minute maximum wait time (900 ticks)
    - AGVs leave queue if wait exceeds threshold
    - Validates realistic charging station management
  - **Test 3:** M Simultaneous Tasks with K Available AGVs
    - Tests 15 concurrent tasks with only 5 AGVs
    - Tasks compete for limited AGV resources
    - Validates task assignment and completion
    - Tests resource allocation under contention

*Unit Tests (de.fachhochschule.dortmund.bads):*
- `AppTest` - Application entry point tests
- `AreaTest` - Area functionality tests
- `StorageTest` - Storage operations tests
- `StorageCellTest` - Storage cell tests
- `TaskTest` - Task management tests
- `AGVTest` - AGV resource tests
- `TruckTest` - Truck resource tests
- `BeveragesBoxTest` - Beverages box tests
- `ResourceTest` - Generic resource tests
- `ProcessTest` - Process execution tests
- `OperationTest` - Operation tests
- `SystemsTest` - Systems functionality tests
- `ClockingSimulationTest` - Time simulation tests

*System Management Tests:*
- `TaskManagementTest` - Task management system tests
- `StorageManagementTest` - Storage management tests
- `ObservationTest` - Observation system tests
- `ArchiveManagerTest` - Archive management tests

*Configuration Tests:*
- `CoreConfigurationTest` - Core configuration tests
- `ObservabilityConfigurationTest` - Observability config tests
- `GUIConfigurationTest` - GUI configuration tests
- `TaskManagementConfigurationTest` - Task management config tests
- `StorageManagementConfigurationTest` - Storage management config tests
- `AGVManagementConfigurationTest` - AGV management config tests

*Integration Tests:*
- `FullSystemIntegrationTest` - Complete system integration testing (340 lines)
- `ParallelSystemsIntegrationTest` - Parallel systems and concurrency testing (660 lines)

*Utility Tests:*
- `SearchResultTest` - Search result functionality tests
- `ArchiveCommandTest` - Archive command tests

**To run specific test suites:**
- All tests: `mvn test`
- **Homework 3 official tests:** `mvn test -Dtest=Homework3UnitTests`
- Specific test class: `mvn test -Dtest=FullSystemIntegrationTest`
- Integration tests only: `mvn test -Dtest=*IntegrationTest`
- Single test method: `mvn test -Dtest=Homework3UnitTests#testSimultaneousAGVCharging`

# Capstone Project - BADS - Beverages Automated Distribution System

## Overview

BADS (Beverage Automated Distribution System) is a **concurrent warehouse simulation system** for automated beverage logistics. The project demonstrates enterprise-level software engineering concepts including concurrent programming, dependency injection, observer patterns, and real-time simulation.

The system is architected in two main components:
* **Core Library** - Manages concurrent systems across multiple warehouses, AGVs, cities, and trucks
* **GUI Application** - Real-time visualization of 1 warehouse with 3 AGVs operating on 3 bay loading docks for truck loading operations

### Key Features

- ✅ **Autonomous AGV Fleet** with self-managed charging queue system
- ✅ **7-Type Storage Management** (Ambient, Refrigerated, Bulk, Charging Station, Loading, Corridor, Any)
- ✅ **Priority-Based Task Scheduling** with concurrent queue processing
- ✅ **Real-Time Tick-Based Simulation** coordinating all system components
- ✅ **Graph-Based Pathfinding** using Dijkstra's algorithm for AGV navigation
- ✅ **Stack Machine Architecture** for programmable AGV behavior
- ✅ **Dependency Injection** via singleton enum configuration pattern
- ✅ **Comprehensive Exception Handling** with custom exception hierarchy
- ✅ **Log4j2 Integration** for observability and debugging

## Architecture Documentation

Comprehensive architecture documentation is available in:
- **[SYSTEM_OVERVIEW.md](capstone_alpha/SYSTEM_OVERVIEW.md)** - Detailed technical documentation covering:
  - System purpose and thread model
  - Core packages and design patterns
  - AGV stack machine architecture
  - Graph-based spatial model with pathfinding
  - Configuration layer and dependency injection
  - Execution flow and real-world examples

- **[AGV_CHARGING_QUEUE_ARCHITECTURE.md](capstone_alpha/AGV_CHARGING_QUEUE_ARCHITECTURE.md)** - AGV charging system design

### UML Class Diagrams

The `capstone_umls/` folder contains detailed UML class diagrams for all major subsystems:

| Diagram | Coverage | Description |
|---------|----------|-------------|
| **configuration_classes.png** | Configuration Layer | Dependency injection containers, factory patterns, system builders |
| **models_classes.png** | Domain Model | Area (graph/pathfinding), Storage, StorageCell, Task entities |
| **resources_classes.png** | Active Resources | AGV (stack machine), Truck, BeveragesBox resource classes |
| **systems_classes.png** | System Coordination | TaskManagement, StorageManagement, ClockingSimulation, Observation |
| **exceptions_classes.png** | Exception Hierarchy | Custom exception framework for error handling |
| **gui_classes.png** | Main GUI | MainFrame, panels, tick-based rendering |
| **gui_agv_classes.png** | AGV Visualization | AGV status panels, battery indicators, state displays |
| **gui_inventory_classes.png** | Inventory UI | Storage cell views, box management panels |
| **unit_tests_classes.png** | Test Suite | Test classes for all major components |

## Technical Highlights

### Concurrent Architecture
The system runs **4 core threads** plus the GUI Event Dispatch Thread:
- **CLOCKING** - Tick-based time coordinator (continuous loop)
- **TASK_MANAGEMENT** - Task queue maintenance (periodic 1s cycles)
- **STORAGE_MANAGEMENT** - Warehouse operations (event-driven)
- **OBSERVATION** - Metrics & monitoring (continuous)
- **GUI EDT** - UI rendering & events (event-driven)

All threads synchronize via the `ITickable` interface for coordinated updates.

### AGV Stack Machine
AGVs implement a **7-instruction stack-based virtual machine** for programmable behavior:
- `SETUP` - Initialize AGV with storage and position
- `PUSH` - Push values onto execution stack
- `MOVE` - Navigate to destination (pops cell label)
- `TAKE` - Pick up box (pops box & label)
- `RELEASE` - Drop off box (pops box & label)
- `CHARGE` - Charge at station (pops station label)
- `STOP` - Emergency halt and clear queues

### Graph-Based Navigation
Warehouse and city layouts use **undirected graph representation** with Dijkstra's shortest path algorithm:
- **Warehouse Area**: 7×5 grid (35 nodes, ~70 edges)
- **City Area**: 10×10 grid (100 nodes, ~180 edges)
- Edge weights: Euclidean distance for natural pathfinding
- Average computation: <1ms for warehouse, <2ms for city

### Dependency Injection Pattern
Singleton enum-based configuration layer with fluent builder APIs:
```java
CoreConfiguration.INSTANCE.autowire();  // Initializes all subsystems
GUIConfiguration.INSTANCE
    .setWarehouseData(warehouse, null, agvFleet, trucks)
    .autowire();  // Launches GUI with backend connections
```

## How to Run

1. Clone the repo
2. Open the `capstone_alpha` folder in your IDE (Eclipse/IntelliJ)
3. Build the project using Maven: `mvn clean install`
4. Run the `App.java` class from `de.fachhochschule.dortmund.bads.App`
5. To run tests: `mvn test`

## Project Structure

```
capstone_alpha/
├── src/main/java/de/fachhochschule/dortmund/bads/
│   ├── App.java                          # Entry point
│   ├── configuration/                    # DI layer
│   │   ├── CoreConfiguration.java        # Central coordinator
│   │   ├── AGVManagementConfiguration.java
│   │   ├── TaskManagementConfiguration.java
│   │   └── ...
│   ├── model/                            # Domain model
│   │   ├── Area.java                     # Graph & pathfinding
│   │   ├── Storage.java                  # Warehouse
│   │   ├── StorageCell.java              # Storage units
│   │   └── Task.java                     # Work units
│   ├── resources/                        # Active resources
│   │   ├── AGV.java                      # Stack machine AGV
│   │   ├── Truck.java                    # Delivery trucks
│   │   └── BeveragesBox.java             # Cargo
│   ├── systems/                          # System coordination
│   │   ├── TaskManagement.java           # Task scheduling
│   │   ├── StorageManagement.java        # Warehouse ops
│   │   ├── ClockingSimulation.java       # Time coordinator
│   │   └── Observation.java              # Monitoring
│   ├── exceptions/                       # Exception hierarchy
│   └── gui/                              # Swing UI
│       ├── MainFrame.java                # Main window
│       ├── agv/                          # AGV panels
│       └── inventory/                    # Storage panels
├── SYSTEM_OVERVIEW.md                    # Architecture docs
├── AGV_CHARGING_QUEUE_ARCHITECTURE.md    # Charging system docs
└── pom.xml                               # Maven configuration

capstone_umls/                            # UML class diagrams
├── configuration_classes.png
├── models_classes.png
├── resources_classes.png
├── systems_classes.png
├── exceptions_classes.png
├── gui_classes.png
├── gui_agv_classes.png
├── gui_inventory_classes.png
└── unit_tests_classes.png
```


