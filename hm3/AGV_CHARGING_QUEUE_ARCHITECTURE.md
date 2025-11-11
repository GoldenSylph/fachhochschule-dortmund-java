# AGV Charging Queue Architecture - Encapsulated Design

## Overview
The AGV charging queue functionality is now **fully encapsulated within the AGV class**. Each AGV manages its own charging lifecycle, and all AGVs share a centralized charging queue system that is maintained as static members within the AGV class itself.

---

## ✅ Key Features

### 1. **Self-Managed Charging Lifecycle**
- AGVs automatically detect when battery is low (default: 20%)
- AGVs request charging autonomously
- AGVs manage their own state transitions through charging process
- No external fleet management system required

### 2. **Centralized Charging Queue**
- Shared static `ConcurrentLinkedQueue<AGV>` across all AGV instances
- Thread-safe queue management
- Fair FIFO (First-In-First-Out) queue processing
- Automatic charging station assignment

### 3. **AGV State Management**
Five distinct states:
- `IDLE` - Ready for tasks
- `BUSY` - Executing task
- `WAITING_FOR_CHARGE` - In charging queue
- `MOVING_TO_CHARGE` - Moving to assigned charging station
- `CHARGING` - Currently charging

### 4. **Battery Management**
- Configurable battery threshold (default: 20%)
- Configurable charge rate per tick (default: 10%)
- Configurable battery drain per action (default: 5%)
- Automatic charging request when battery is low

---

## 🏗️ Architecture Components

### Static Members (Shared Across All AGVs)

```java
// Charging queue shared by all AGV instances
private static final ConcurrentLinkedQueue<AGV> CHARGING_QUEUE;

// Number of available charging stations
private static final AtomicInteger AVAILABLE_CHARGING_STATIONS;

// Auto-incrementing ID counter for AGV identification
private static AtomicInteger idCounter;
```

### Instance Members (Per AGV)

```java
// AGV state and identification
private AGVState state;
private String agvId;
private boolean needsCharging;

// Battery management
private int batteryLevel;
private double batteryLowThreshold;
private int chargePerTick;
private int loseChargePerActionPerTick;
private boolean charging;

// Charging station assignment
private Point assignedChargingStation;
```

---

## 🔄 Charging Process Flow

### 1. **Low Battery Detection**
```
AGV.onTick() called by ClockingSimulation
    ↓
checkBatteryLevel()
    ↓
if (batteryLevel <= batteryLowThreshold && state == IDLE)
    ↓
requestCharging()
```

### 2. **Join Charging Queue**
```
requestCharging()
    ↓
state = WAITING_FOR_CHARGE
needsCharging = true
    ↓
CHARGING_QUEUE.add(this)
    ↓
Log: "AGV-X added to charging queue (position: Y, queue size: Z)"
```

### 3. **Process Queue (Every Tick)**
```
AGV.onTick()
    ↓
processChargingQueue()
    ↓
if (this == CHARGING_QUEUE.peek() && state == WAITING_FOR_CHARGE)
    ↓
chargingStationPoint = storage.findAvailableChargingStation()
    ↓
if (chargingStationPoint != null)
    ↓
storage.occupyChargingStation(chargingStationPoint, this)
    ↓
CHARGING_QUEUE.poll()
state = MOVING_TO_CHARGE
    ↓
Set up path to charging station
```

### 4. **Move to Charging Station**
```
state = MOVING_TO_CHARGE
    ↓
AGV follows path to assignedChargingStation
    ↓
Upon arrival at charging station
    ↓
startCharging()
    ↓
state = CHARGING
charging = true
```

### 5. **Charging**
```
onTick() while charging == true
    ↓
batteryLevel += chargePerTick
    ↓
if (batteryLevel >= 100)
    ↓
completeCharging()
```

### 6. **Complete Charging**
```
completeCharging()
    ↓
storage.releaseChargingStation(assignedChargingStation)
    ↓
charging = false
needsCharging = false
state = IDLE
assignedChargingStation = null
    ↓
AGV available for tasks
```

---

## 🚀 Usage Example

### System Initialization

```java
// 1. Autowire core configuration
CoreConfiguration.INSTANCE.autowire();

// 2. Create storage area
Area storageArea = CoreConfiguration.INSTANCE.newArea();
// ... configure area with graph/adjacency map ...

// 3. Create storage cells including charging stations
StorageCell[] cells = new StorageCell[] {
    CoreConfiguration.INSTANCE.newStorageCell(Type.AMBIENT, 100, 100, 100),
    CoreConfiguration.INSTANCE.newStorageCell(Type.REFRIGERATED, 100, 100, 100),
    CoreConfiguration.INSTANCE.newChargingStation(),  // Charging station 1
    CoreConfiguration.INSTANCE.newChargingStation(),  // Charging station 2
    CoreConfiguration.INSTANCE.newStorageCell(Type.BULK, 100, 100, 100)
};

// 4. Create storage
Storage storage = CoreConfiguration.INSTANCE.newStorage(storageArea, cells);

// 5. Initialize AGV charging system with charging station count
CoreConfiguration.INSTANCE.initializeAGVChargingSystem(storage);
// Output: "AGV Charging System initialized with 2 charging stations from Storage"

// 6. Create AGVs
AGV agv1 = CoreConfiguration.INSTANCE.newAGV();
AGV agv2 = CoreConfiguration.INSTANCE.newAGV();
AGV agv3 = CoreConfiguration.INSTANCE.newAGV();

// 7. Register AGVs with clocking system for tick updates
CoreConfiguration.INSTANCE.registerTickable(agv1);
CoreConfiguration.INSTANCE.registerTickable(agv2);
CoreConfiguration.INSTANCE.registerTickable(agv3);

// 8. Set up AGVs with storage reference
AGV.Statement<?>[] setupProgram = new AGV.Statement<?>[] {
    new AGV.Statement<>(AGV.Operand.SETUP, storage, startingPoint)
};
agv1.executeProgram(setupProgram);
agv2.executeProgram(setupProgram);
agv3.executeProgram(setupProgram);
```

### Configure Battery Management (Optional)

```java
AGV agv = CoreConfiguration.INSTANCE.newAGV();

// Set battery low threshold to 30%
agv.setBatteryLowThreshold(30.0);

// Set faster charging (15% per tick)
agv.setChargePerTick(15);

// Set battery drain (3% per action)
agv.setLoseChargePerActionPerTick(3);
```

### Monitor Charging Queue

```java
// Get current queue size
int queueSize = AGV.getChargingQueueSize();
System.out.println("AGVs waiting for charge: " + queueSize);

// Get available charging stations
int available = AGV.getAvailableChargingStations();
System.out.println("Available charging stations: " + available);

// Check individual AGV status
AGV agv = CoreConfiguration.INSTANCE.newAGV();
System.out.println("AGV ID: " + agv.getAgvId());
System.out.println("Battery: " + agv.getBatteryLevel() + "%");
System.out.println("State: " + agv.getState());
```

---

## 🔧 API Reference

### Static Methods

| Method | Description |
|--------|-------------|
| `initializeChargingSystem(int)` | Initialize charging system with number of stations |
| `getChargingQueueSize()` | Get current number of AGVs in queue |
| `getAvailableChargingStations()` | Get configured number of charging stations |

### Instance Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `requestCharging()` | void | Request charging (adds to queue) |
| `getState()` | AGVState | Get current AGV state |
| `getAgvId()` | String | Get AGV identifier |
| `getBatteryLevel()` | int | Get current battery percentage |
| `setBatteryLowThreshold(double)` | void | Set threshold for auto-charging |
| `setChargePerTick(int)` | void | Set charge rate per tick |
| `setLoseChargePerActionPerTick(int)` | void | Set battery drain rate |

---

## 🔐 Thread Safety

The charging queue system is designed to be thread-safe:

1. **ConcurrentLinkedQueue** - Lock-free thread-safe queue
2. **AtomicInteger** - Thread-safe counter for charging stations
3. **Synchronized methods** - `requestCharging()`, `startCharging()`, `completeCharging()`
4. **Volatile state** - `isOccupied` flag in StorageCell

---

## 📊 Benefits of Encapsulation

### Before (Distributed Architecture)
- ❌ Charging logic scattered across multiple classes
- ❌ External systems managed AGV charging
- ❌ Complex inter-system communication required
- ❌ Difficult to track charging queue state
- ❌ AGVs passive in charging process

### After (Encapsulated Architecture)
- ✅ All charging logic within AGV class
- ✅ AGVs self-manage their charging lifecycle
- ✅ Single source of truth for charging queue
- ✅ Easy to monitor queue state via static methods
- ✅ AGVs actively participate in charging process
- ✅ Reduced coupling between systems
- ✅ Easier testing and maintenance

---

## 🎯 Design Patterns Used

1. **Singleton Pattern** - Static charging queue shared across instances
2. **State Pattern** - AGVState enum for state management
3. **Observer Pattern** - ITickable interface for tick-based updates
4. **Queue Pattern** - FIFO charging queue
5. **Factory Pattern** - CoreConfiguration creates AGV instances

---

## 📝 Important Notes

1. **Initialization Required**: Must call `initializeAGVChargingSystem(storage)` after creating Storage
2. **Storage Reference**: Each AGV needs Storage reference via SETUP operation
3. **Tick Registration**: AGVs must be registered with ClockingSimulation via `registerTickable()`
4. **Queue Fairness**: First come, first served - no priority queue
5. **Automatic Process**: Charging happens automatically, no manual intervention needed

---

## 🐛 Troubleshooting

### AGVs not charging
- **Check**: Is `initializeAGVChargingSystem()` called?
- **Check**: Do AGVs have Storage reference (SETUP operation)?
- **Check**: Are AGVs registered with ClockingSimulation?
- **Check**: Are there charging stations in Storage?

### Queue not processing
- **Check**: Are ticks being generated by ClockingSimulation?
- **Check**: Is Storage reference valid in AGV?
- **Check**: Are charging stations available (not all occupied)?

### Battery draining too fast
- **Adjust**: `setLoseChargePerActionPerTick()` to lower value
- **Adjust**: `setBatteryLowThreshold()` to higher value
- **Add**: More charging stations to reduce queue wait time

---

## 🔄 Migration from External Management

If migrating from external AGV fleet management:

1. Remove external charging queue/semaphore code
2. Remove AGV charging request handlers from fleet management
3. Call `AGV.initializeChargingSystem()` during initialization
4. Register AGVs with ClockingSimulation
5. Remove manual charging state management
6. AGVs now handle everything automatically!

---

## 📈 Performance Considerations

- **Queue Processing**: O(1) per tick per AGV (only front of queue processed)
- **State Checks**: O(1) synchronized methods
- **Memory**: Minimal - static queue shared across all AGVs
- **Thread Contention**: Low - lock-free queue operations
- **Scalability**: Handles hundreds of AGVs efficiently

---

## 🚦 Logging

The system provides comprehensive logging:

- **INFO**: Charging queue additions, station assignments, charging start/complete
- **DEBUG**: Battery level updates, state transitions
- **WARN**: Low battery warnings, charging process issues
- **ERROR**: Charging failures, invalid operations

Example log output:
```
INFO  - AGV-1 battery low (18%), requesting charging
INFO  - AGV-1 added to charging queue (position: 1, queue size: 1)
INFO  - AGV-1 assigned charging station at 3C, moving to charge
INFO  - AGV-1 started charging at station 3C, battery: 18%
DEBUG - AGV-1 charging: battery level now 28%
INFO  - AGV-1 completed charging, released station 3C, battery: 100%
```
