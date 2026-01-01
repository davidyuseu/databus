# DataBus Wiki (A Stream Processing System Architecture)

## Overview

<img width="2716" height="1271" alt="图片 1" src="https://github.com/user-attachments/assets/341ce362-6654-41d3-9f85-32a60220e60f" />

**DataBus** is a **co-located edge server, stream–batch unified data processing system** designed for **edge computing and real-time stream processing scenarios**.

The system is built around the core goals of **agile construction, low latency, high throughput, and strong evolvability**, and is suitable for UAV edge gateways, industrial IoT aggregation nodes, real-time monitoring, and time-series data processing.

The core philosophy of DataBus can be summarized in four aspects:

1. **Agile drag-and-drop visual DAG construction of data pipelines;**
2. **Lock-free, loosely coupled serial components with runtime hot-plugging;**
3. **Efficient zero-copy data transfer between operators;**
4. **In-band control signal mechanism for unified data and control flow.**

## Preview Version
To facilitate quick evaluation, we provide a precompiled preview version of the system：
- Platform: Windows x64
[Download Preview Build (for Evaluation)](https://github.com/davidyuseu/databus/releases/tag/artifact-preview)

The following sections elaborate on these four aspects.

---

## 1\. Drag-and-Drop Visual DAG Construction of Data Pipelines

<img width="1807" height="494" alt="图片 3" src="https://github.com/user-attachments/assets/6f57242d-04b9-4edd-a6c5-2b3d6f1c1965" />

### 1.1 Visual Pipeline Modeling for Engineering Productivity

DataBus adopts a DAG (Directed Acyclic Graph)–based approach for data pipeline modeling.

Each operator (Operator / Processor) is represented as a node in the graph, and data or control flows are represented as directed edges.

Within the visual editor, users can construct pipelines via **drag-and-drop**, including:

- Creating data sources, processing operators, and sink nodes;
- Adjusting the connectivity between operators;
- Quickly inserting, removing, or replacing operators;
- Versioning and rolling back pipeline configurations.

This approach significantly lowers the barrier to building stream processing systems and enables a clear separation between **pipeline construction** and **business logic implementation**.

### 1.2 Mapping Between DAG and Runtime Execution Model

<img width="1893" height="821" alt="图片 4" src="https://github.com/user-attachments/assets/309e24bd-9d1e-4375-82bc-a36adeade03b" />

The visual DAG is not merely a presentation artifact; it directly corresponds to the runtime execution model:

- Each DAG node maps to a runtime Processor instance;
- Each DAG edge maps to a message channel;
- The topological order of the DAG determines data propagation and execution dependencies.

When the DAG is modified, the system can safely reflect these changes in the running execution graph while preserving data consistency, laying the foundation for **hot-plugging and online evolution**.

---

## 2\. Lock-Free, Loosely Coupled Serial Components and Hot Plugging

<img width="1893" height="665" alt="图片 6" src="https://github.com/user-attachments/assets/8b7dadfd-d0bb-4c9f-b6ca-c231b7c7ea05" />

<img width="1280" height="1011" alt="图片 7" src="https://github.com/user-attachments/assets/3ff5448b-a5de-46f8-a08e-4660b8717f9a" />

### 2.1 Serial Component Model

Each Processor in DataBus is designed as a **serial execution component**:

- Every Processor owns its own execution context (strand / executor);
- Tasks within a Processor are executed strictly serially;
- Processors interact exclusively through message passing.

This design yields several benefits:

- Eliminates the need for complex locks inside operators;
- Provides natural thread safety between components;
- Allows operator logic to be written in a sequential style, simplifying development and debugging.

### 2.2 Lock-Free Message Channels and Loose Coupling

Operators are connected via **lock-free message channels**:

- Upstream operators enqueue messages into downstream execution queues;
- Downstream operators process messages within their own execution contexts;
- No mutable state is shared across operators.

This “message-as-boundary” design ensures **true loose coupling**, avoiding common pitfalls of shared-memory concurrency such as contention, deadlocks, and priority inversion.

### 2.3 Runtime Hot Plugging and Online Evolution

Based on serial components and lock-free channels, DataBus supports **runtime hot plugging** of operators:

- New operators can be added while the system is running;
- Existing operators can be removed from the pipeline;
- Operators can be replaced with updated implementations.

During hot-plugging:

- In-flight data continues to be processed along the original paths;
- New data is automatically routed through the updated pipeline;
- No global stop or system restart is required.

This capability makes DataBus particularly suitable for **continuously evolving engineering environments**, such as algorithm upgrades, rule updates, and online debugging.

---

## 3\. Efficient Zero-Copy Data Transfer Between Operators

<img width="2316" height="596" alt="image" src="https://github.com/user-attachments/assets/cd35a587-bb96-48ea-b3bb-766e178b4620" />

3.1 Shared Buffers and Shallow-Copy Semantics

DataBus employs a unified shared buffer abstraction (Shared Buffer) for data transfer between operators:

- Operators pass lightweight descriptors referencing the same buffer;
- Descriptor copying involves only pointers, lengths, and reference counters;
- The underlying data payload is not copied in most execution paths.

This approach significantly reduces:

- Memory bandwidth consumption;
- CPU overhead due to copying;
- Tail-latency amplification caused by large payloads.

### 3.2 Read/Write Layering and Explicit Copy Boundaries

To balance safety and performance, DataBus introduces a **read/write layering model**:

- **Read Layer:**
  - Operators only read data;
  - Buffers can be safely shared;
  - No deep copy is triggered.

- **Write Layer:**
  - Operators modify data;
  - At an explicit “write handoff point,” the system checks whether the buffer is shared;
  - A deep copy is triggered only when necessary.


This **explicit copy boundary** design allows precise control over when and where copying occurs, and is more predictable than traditional Copy-on-Write (COW) mechanisms.

### 3.3 Optimization for High Throughput and Low Tail Latency

The zero-copy design is especially critical in scenarios involving:

- Medium to large payloads (images, video frames, batch records);
- Deep or highly concurrent operator chains;
- Systems sensitive to P99/P999 latency.

In practical evaluations, the zero-copy mechanism significantly reduces CPU utilization and memory copy amplification, preserving processing headroom under heavy load.

---

## 4\. In-Band Control Signal Mechanism

<img width="2268" height="1472" alt="image" src="https://github.com/user-attachments/assets/0cfb4283-df33-4e5c-bf88-80401354aab5" />

### 4.1 Unified Modeling of Data Flow and Control Flow

DataBus models **control signals** and regular data messages uniformly as events:

- Data Events: carry business data;
- Signal Events: carry control and coordination information.

Both types of events traverse the same message channels; the difference lies only in the event type.

### 4.2 Examples of Control Signals

In-band control signals can be used to implement:

- Pipeline start, stop, and pause;
- Window closing and watermark advancement;
- Configuration updates and state refresh;
- Metrics and monitoring triggers;
- Operator lifecycle management.

Because control signals propagate along the same paths as data, the system naturally preserves **temporal consistency between data and control**.

### 4.3 Default Handling and Extensibility

For control signals not explicitly handled by an operator, the system provides reasonable default strategies, such as:

- Transparent pass-through;
- Safe dropping with warnings for directed signals.

Developers can define specialized handling logic for specific signals without modifying the framework core.

