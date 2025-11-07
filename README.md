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
1) Clone the repo.
2) Open a folder inside the root that called `hm1` in Eclipse IDE.
3) Run the ArchiveManager.java and ArchiveManagerTest.java inside `de.fachhochschule.dortmund.bads.hm1.bilyaminu`.

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


