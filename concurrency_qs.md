# HM3

## Comparison of common concurrency models (Pros & Cons)

### 1) Threads + Locks (low-level)
**Description:** Each concurrent entity is a thread. Synchronization by locks (`synchronized`, `ReentrantLock`).

**Pros**

- Full control over execution / scheduling.
- Simple mental model for small programs.

**Cons**

- Error-prone: deadlocks, race conditions.
- Hard to scale and reason about for many concurrent entities.
- Manual resource management (threads are expensive).

### 2) ExecutorService (thread pool)
**Description:** High-level abstraction where tasks (`Runnable`/`Callable`) are submitted to pools.

**Pros**

- Reuses threads (better performance).
- Easier resource control via pool size and queueing policies.
- Simpler to cancel or shut down cleanly.

**Cons**

- Requires careful choice of pool size and queue policy for best throughput.
- Task submission can create backpressure if tasks are produced faster than consumed.

### 3) Fork/Join (work-stealing)
**Description:** Designed for divide-and-conquer parallelism (e.g., `ForkJoinPool`).

**Pros**

- Good for parallel recursive algorithms.
- Work-stealing improves CPU utilization for many small tasks.

**Cons**

- Best for CPU-bound tasks; less suitable for blocking I/O.
- Harder mental model for debugging.

### 4) Actor Model (e.g., Akka)
**Description:** Each actor has its own mailbox, exchanges messages; no shared mutable state.

**Pros**

- Avoids locks — reduces concurrency bugs.
- Naturally models distributed or message-driven systems.

**Cons**

- Requires learning the actor framework.
- Message ordering and delivery semantics must be understood.

### 5) Reactive / Non-blocking (e.g., CompletableFuture, Reactor)
**Description:** Event-driven, asynchronous style using callbacks, futures, or reactive streams.

**Pros**

- Very efficient for I/O-bound workloads (small thread count, high concurrency).
- Backpressure support in reactive streams.

**Cons**

- Can be harder to reason about (callback chains, error propagation).
- Stack traces and debugging can be less straightforward.

---

## Concurrency vs Parallelism

- **Concurrency** — about dealing with *many things at once*. It’s a design concept: multiple tasks *make progress* in overlapping time periods. They may be interleaved on a single CPU (time-slicing). Example: a single-threaded event loop (Node.js) handles multiple clients by switching context.
- **Parallelism** — about doing *many things at the same time* using multiple processors/cores. Example: using multiple threads on a multi-core CPU to compute parts of a large matrix multiply simultaneously.
- **Key difference:** Concurrency is a property of the program structure (logical simultaneity); parallelism is about physical simultaneous execution.

---

## Blocking concurrency algorithms vs Non-blocking concurrency algorithms

### Blocking concurrency
**Definition:** Threads wait (block) while resources are unavailable (e.g., using `wait()`, `synchronized`, blocking queue `take()`).
**Examples:** `synchronized`, blocking queues (`ArrayBlockingQueue.take()`), `Thread.sleep()`.

**Pros**

- Simple to implement and reason about for many cases.
- Easy control-flow: code waits until condition is met.

**Cons**

- Threads tied up while blocked — expensive at scale.
- Risk of deadlocks and priority inversion.

### Non-blocking concurrency
**Definition:** Threads do not block waiting for resources — use atomic operations / lock-free algorithms (e.g., `AtomicInteger.compareAndSet`) or asynchronous callbacks/futures.
**Examples:** `AtomicReference`, CAS loops, `CompletableFuture`, Reactive streams.

**Pros**

- Better scalability and throughput under high-concurrency loads.
- Avoids many deadlock scenarios.

**Cons**

- More complex algorithms and reasoning.
- May require retry loops and careful fairness considerations.

**When to use which:**
- Use blocking algorithms for small scale or when programmer simplicity matters.
- Use non-blocking/async approaches for high-concurrency, I/O-bound systems or latency-sensitive services
