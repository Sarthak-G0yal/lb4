# ~~Phase 1 — Project Foundation~~

## Goals

* Create the base project structure
* Set up build system and dependencies
* Define the configuration model
* Prepare local backend test environment

## What We Are Building

* Maven-based Java project
* YAML configuration loading
* Basic configuration POJOs
* Local TCP backend servers for testing

## What We Are Using

* Java 21+
* Maven
* Java NIO
* Jackson YAML
* Docker + Docker Compose

## Tasks

* Initialize Maven project
* Add Jackson YAML dependency
* Create config classes:

    * `ServerConfig`
    * `BackendConfig`
    * `LoadBalancerConfig`
* Implement YAML loader
* Validate configuration parsing
* Create simple TCP echo server
* Create multiple local backend instances
* Create Docker Compose setup for backends

## Focus Areas

* Project structure
* Dependency management
* YAML → POJO mapping
* Backend test setup

---

# ~~Phase 2 — Single Selector Event Loop~~

## Goals

* Build the core non-blocking event loop
* Accept TCP connections
* Understand selector-driven architecture

## What We Are Building

* Single-threaded event loop
* Non-blocking TCP server
* Socket registration system

## What We Are Using

* `Selector`
* `SelectionKey`
* `ServerSocketChannel`
* `SocketChannel`
* Non-blocking mode

## Tasks

* Open selector
* Open server socket
* Configure non-blocking mode
* Register `OP_ACCEPT`
* Implement selector loop
* Handle accept events
* Register accepted sockets
* Print connection events

## Focus Areas

* Event-driven architecture
* Selector lifecycle
* Readiness events
* Non-blocking sockets

---

# ~~Phase 3 — Session Architecture~~

## Goals

* Represent paired client/backend connections
* Build internal connection state model

## What We Are Building

* Session abstraction
* Client ↔ backend connection pairing
* SelectionKey attachments

## What We Are Using

* Custom `Session` class
* SelectionKey attachment system

## Tasks

* Design `Session` object
* Store:

    * client channel
    * backend channel
    * session state
* Attach session to SelectionKeys
* Link client and backend keys
* Track session lifecycle
* Implement session cleanup logic

## Focus Areas

* Connection state management
* Session ownership
* Key attachment model
* Explicit lifecycle tracking

---

# ~~Phase 4 — Backend Connection Logic~~

## Goals

* Connect accepted clients to backend servers
* Establish full proxy behavior

## What We Are Building

* Backend selection
* Outbound backend connections
* Full TCP proxy setup

## What We Are Using

* Non-blocking backend SocketChannels

## Tasks

* Select backend server
* Open backend connection
* Register backend channel
* Pair backend with client
* Handle backend connection success/failure
* Implement brutal teardown policy

## Focus Areas

* Full proxy architecture
* Dual-channel session handling
* Failure handling
* TCP connection orchestration

---

# ~~Phase 5 — TCP Byte Forwarding~~

## Goals

* Forward bytes bidirectionally
* Build raw TCP data pump

## What We Are Building

* Read/write forwarding loop
* Reusable Direct ByteBuffer flow

## What We Are Using

* Single reusable Direct ByteBuffer
* Non-blocking reads/writes

## Tasks

* Allocate Direct ByteBuffer
* Read client data
* Write to backend
* Read backend data
* Write to client
* Detect EOF
* Close failed sessions
* Handle IOException teardown

## Focus Areas

* Raw byte forwarding
* Non-blocking read/write behavior
* Buffer flipping/clearing
* TCP stream mechanics

---

# ~~Phase 6 — Load Balancing Logic~~

## Goals

* Add deterministic backend selection
* Implement sticky routing behavior

## What We Are Building

* IP hashing load balancing

## What We Are Using

* Client IP hashing
* Backend mapping logic

## Tasks

* Extract client IP
* Generate stable hash
* Map hash to backend list
* Ensure deterministic backend assignment
* Validate sticky session behavior

## Focus Areas

* Layer 4 routing
* Deterministic balancing
* Sticky sessions
* Stateless scheduling

---

# ~~Phase 7 — Passive Failure Handling~~

## Goals

* Detect failed backends passively
* React to runtime socket failures

## What We Are Building

* Passive backend failure detection
* Failure-aware session teardown

## What We Are Using

* IOException-based detection
* Connection refusal detection

## Tasks

* Detect connect failures
* Detect mid-stream disconnects
* Detect EOF
* Mark backend unhealthy temporarily
* Skip failed backends
* Retry healthy alternatives

## Focus Areas

* Passive health handling
* Runtime failure recovery
* Session failure behavior
* Backend availability tracking

---

# ~~Phase 8 — Logging and Observability~~

## Goals

* Make system behavior visible
* Track runtime state

## What We Are Building

* Structured logs
* Metrics counters

## What We Are Using

* Java logging
* Atomic counters

## Tasks

* Log:

    * new connections
    * backend selection
    * disconnects
    * failures
    * bytes transferred
* Add counters:

    * active sessions
    * total sessions
    * failed sessions
    * backend failures
* Add periodic metrics printing

## Focus Areas

* Debugging visibility
* Runtime introspection
* Event tracing
* Basic observability

---

# Phase 9 — Graceful Shutdown and Cleanup

## Goals

* Shut down cleanly
* Avoid leaked sockets/resources

## What We Are Building

* Controlled shutdown system
* Resource cleanup logic

## What We Are Using

* JVM shutdown hooks
* Explicit channel cleanup

## Tasks

* Add shutdown hook
* Stop accepting new connections
* Close all channels
* Cancel SelectionKeys
* Close selector
* Flush logs/metrics
* Verify no resource leaks

## Focus Areas

* Resource lifecycle
* Cleanup correctness
* Controlled termination
* Selector shutdown behavior

---

# Phase 10 — Dockerized Testing Environment

## Goals

* Test realistic networking behavior
* Simulate backend clusters

## What We Are Building

* Containerized backend environment
* Repeatable integration testing

## What We Are Using

* Docker
* Docker Compose

## Tasks

* Create backend containers
* Configure multiple backend instances
* Run LB in Docker
* Test backend failures
* Test connection routing
* Test sticky behavior
* Test teardown handling

## Focus Areas

* Integration testing
* Container networking
* Multi-backend behavior
* Realistic deployment simulation

---

# Phase 11 — Basic Security and Limits

## Goals

* Add simple overload protections
* Learn basic defensive behavior

## What We Are Building

* Connection limits
* Basic abuse controls

## What We Are Using

* Per-IP counters
* Session caps
* Timeouts

## Tasks

* Add max connection limits
* Add per-IP connection tracking
* Add idle connection timeout
* Reject excessive clients
* Close stalled sessions

## Focus Areas

* Resource protection
* Overload behavior
* Defensive limits
* Session exhaustion handling

---

# Phase 12 — Rate Limiting

## Goals

* Control client request pressure
* Learn traffic throttling concepts

## What We Are Building

* Token bucket rate limiter

## What We Are Using

* Token bucket algorithm
* Per-IP tracking

## Tasks

* Implement token bucket
* Associate bucket with client IP
* Refill tokens periodically
* Reject over-limit clients
* Log throttling events

## Focus Areas

* Traffic shaping
* Burst handling
* Request throttling
* Basic anti-abuse design

---

# Phase 13 — Active Health Checks

## Goals

* Move beyond passive failure detection
* Learn active backend probing

## What We Are Building

* TCP health-check engine

## What We Are Using

* Scheduled health-check loop
* TCP connect checks

## Tasks

* Add periodic backend checks
* Track:

    * healthy state
    * failure counts
    * recovery counts
* Re-enable recovered backends
* Avoid unhealthy routing targets

## Focus Areas

* Active health monitoring
* Backend recovery
* Failure thresholds
* Service availability tracking

---

# Phase 14 — Benchmarking and Stress Testing

## Goals

* Measure proxy behavior under load
* Observe system bottlenecks

## What We Are Building

* Benchmark suite
* Stress-testing environment

## What We Are Using

* wrk
* iperf
* Custom TCP clients

## Tasks

* Measure throughput
* Measure concurrent sessions
* Measure failure handling
* Test backend crashes
* Test connection bursts
* Test long-lived sessions
* Analyze CPU/memory usage

## Focus Areas

* Performance observation
* Failure behavior
* Event-loop bottlenecks
* Selector scalability

---

# Phase 15 — Documentation and Architecture Explanation

## Goals

* Explain design clearly
* Make project interview-ready

## What We Are Building

* Technical documentation
* Architecture explanation

## What We Are Using

* Markdown
* Architecture diagrams

## Tasks

* Explain:

    * selector loop
    * session model
    * IP hashing
    * brutal teardown
    * Direct ByteBuffer usage
    * passive health detection
* Add architecture diagrams
* Add flow diagrams
* Add benchmark results
* Add known limitations
* Add future improvements section

## Focus Areas

* Systems design communication
* Technical clarity
* Resume/interview presentation
* Engineering tradeoff explanation
