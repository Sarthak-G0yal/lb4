# Java Layer 4 Load Balancer — Core Architecture Design

# Package Structure

```text
com.yourname.loadbalancer
│
├── config
│   ├── ConfigLoader
│   ├── AppConfig
│   ├── ServerConfig
│   ├── BackendConfig
│   └── LoadBalancerConfig
│
├── core
│   ├── LoadBalancerServer
│   ├── EventLoop
│   ├── Session
│   ├── SessionManager
│   ├── Backend
│   ├── BackendRegistry
│   ├── ClientInfo
│   └── SessionCloser
│
├── balancing
│   ├── LoadBalancingStrategy
│   └── IPHashStrategy
│
├── network
│   ├── AcceptHandler
│   ├── ReadHandler
│   ├── ConnectionHandler
│   └── BufferManager
│
├── health
│   └── PassiveHealthTracker
│
├── metrics
│   ├── MetricsRegistry
│   └── MetricsPrinter
│
├── logging
│   └── LoggerFactory
│
├── util
│   ├── HashUtils
│   ├── SocketUtils
│   └── BufferUtils
│
└── Main
```

---

# Architectural Philosophy

The project follows a deliberately simple architecture focused on learning:

* single-threaded event loop
* non-blocking sockets
* explicit state ownership
* minimal hidden state
* brutal connection teardown
* deterministic routing
* reusable memory
* passive failure handling

The project prioritizes:

* clarity
* observability
* networking fundamentals
* TCP mechanics
* selector lifecycle understanding

The project intentionally avoids:

* multi-threaded selector models
* advanced backpressure systems
* partial-write queues
* TLS
* HTTP parsing
* UDP
* retries
* distributed systems complexity
* production-grade optimizations

---

# Core Event Flow

```text
load configuration
↓
initialize backend registry
↓
initialize selector
↓
bind server socket
↓
register OP_ACCEPT
↓
selector loop starts
↓
accept client connection
↓
create Session
↓
select backend using IP hash
↓
open backend connection
↓
register client/backend channels
↓
read bytes from one side
↓
forward bytes to other side
↓
detect EOF or IOException
↓
brutal teardown
↓
remove session
↓
continue selector loop
```

---

# Core Design Decisions

| Area             | Decision                         |
| ---------------- | -------------------------------- |
| IO Model         | Java NIO                         |
| Threading Model  | Single-threaded                  |
| Selector Model   | Single Global Selector           |
| Buffer Strategy  | Single reusable DirectByteBuffer |
| Routing Strategy | IP Hash                          |
| Failure Strategy | Brutal Teardown                  |
| Health Handling  | Passive detection                |
| Config System    | YAML + Jackson                   |
| Build Tool       | Maven                            |
| Metrics          | Simple counters                  |
| Logging          | Structured logs                  |
| Session Tracking | SessionManager                   |
| Backend Storage  | BackendRegistry                  |

---

# Core Classes

# Main

## Responsibility

Application entry point.

## Responsibilities

* load YAML config
* initialize components
* start load balancer server

## Example

```java
public class Main {
    public static void main(String[] args);
}
```

---

# LoadBalancerServer

## Responsibility

Top-level orchestrator.

## Responsibilities

* initialize selector
* initialize event loop
* initialize registries
* start server

## Fields

```java
class LoadBalancerServer {

    private final AppConfig config;

    private final BackendRegistry backendRegistry;

    private final SessionManager sessionManager;

    private final EventLoop eventLoop;
}
```

---

# EventLoop

## Responsibility

Owns selector lifecycle and event dispatching.

## Responsibilities

* selector loop
* process SelectionKeys
* dispatch events
* register channels
* manage selector wakeups

## Fields

```java
class EventLoop {

    private final Selector selector;

    private final BackendRegistry backendRegistry;

    private final SessionManager sessionManager;

    private final BufferManager bufferManager;
}
```

## Functions

```java
void run();

void processSelectedKeys();

void handleAccept(SelectionKey key);

void handleRead(SelectionKey key);

void registerChannel(SocketChannel channel);

void closeSession(Session session);
```

---

# Session

## Responsibility

Represents one proxied TCP connection pair.

## Responsibilities

* store paired channels
* store SelectionKeys
* store backend ownership
* track session state

## Fields

```java
class Session {

    long sessionId;

    SocketChannel clientChannel;

    SocketChannel backendChannel;

    SelectionKey clientKey;

    SelectionKey backendKey;

    Backend backend;

    boolean closed;
}
```

---

# SessionManager

## Responsibility

Tracks all active sessions.

## Responsibilities

* register sessions
* remove sessions
* session lookup
* active session metrics

## Fields

```java
class SessionManager {

    private final Map<Long, Session> sessions;
}
```

## Functions

```java
void register(Session session);

void remove(Session session);

Collection<Session> getAllSessions();

int activeSessionCount();
```

---

# Backend

## Responsibility

Represents a backend server target.

## Responsibilities

* backend metadata
* backend state tracking

## Fields

```java
class Backend {

    String id;

    String host;

    int port;

    BackendState state;

    long failedConnections;

    long successfulConnections;

    long activeSessions;
}
```

---

# BackendState

## Responsibility

Represents backend health state.

## Enum

```java
enum BackendState {
    HEALTHY,
    UNHEALTHY
}
```

---

# BackendRegistry

## Responsibility

Central backend state manager.

## Responsibilities

* store backends
* backend lookup
* backend health tracking
* provide backend selection

## Fields

```java
class BackendRegistry {

    private final List<Backend> backends;

    private final LoadBalancingStrategy strategy;
}
```

## Functions

```java
Backend selectBackend(ClientInfo client);

void markBackendHealthy(Backend backend);

void markBackendUnhealthy(Backend backend);

List<Backend> getHealthyBackends();

Optional<Backend> getBackendById(String id);
```

---

# ClientInfo

## Responsibility

Represents lightweight client metadata.

## Fields

```java
class ClientInfo {

    String ip;

    int port;
}
```

---

# LoadBalancingStrategy

## Responsibility

Backend selection abstraction.

## Interface

```java
interface LoadBalancingStrategy {

    Backend selectBackend(
        ClientInfo client,
        List<Backend> backends
    );
}
```

---

# IPHashStrategy

## Responsibility

Deterministic backend routing.

## Responsibilities

* hash client IP
* map client to backend

## Example

```java
class IPHashStrategy implements LoadBalancingStrategy
```

---

# AcceptHandler

## Responsibility

Handle OP_ACCEPT events.

## Responsibilities

* accept new clients
* configure non-blocking mode
* create sessions
* initialize backend connections

## Functions

```java
void handleAccept(SelectionKey key);
```

---

# ReadHandler

## Responsibility

Handle OP_READ events.

## Responsibilities

* read bytes
* forward bytes
* detect EOF
* detect IOException

## Functions

```java
void handleRead(SelectionKey key);
```

---

# ConnectionHandler

## Responsibility

Manage backend connections.

## Responsibilities

* open backend connections
* pair channels
* initialize session routing

## Functions

```java
SocketChannel connectBackend(Backend backend);
```

---

# BufferManager

## Responsibility

Owns reusable DirectByteBuffer.

## Responsibilities

* allocate DirectByteBuffer
* provide reusable buffer

## Fields

```java
class BufferManager {

    private final ByteBuffer buffer;
}
```

## Functions

```java
ByteBuffer getBuffer();
```

---

# SessionCloser

## Responsibility

Centralized brutal teardown logic.

## Responsibilities

* close channels
* cancel SelectionKeys
* cleanup sessions

## Functions

```java
static void brutalClose(Session session);
```

---

# PassiveHealthTracker

## Responsibility

Track backend failures passively.

## Responsibilities

* mark failures from IOExceptions
* detect unhealthy backends

## Functions

```java
void markFailure(Backend backend);

void markSuccess(Backend backend);
```

---

# MetricsRegistry

## Responsibility

Stores runtime metrics.

## Responsibilities

* track counters
* expose runtime state

## Fields

```java
class MetricsRegistry {

    long totalSessions;

    long activeSessions;

    long failedSessions;

    long bytesForwarded;

    long backendFailures;
}
```

---

# MetricsPrinter

## Responsibility

Print periodic metrics.

## Functions

```java
void printMetrics();
```

---

# LoggerFactory

## Responsibility

Centralized logger configuration.

## Responsibilities

* structured logging
* logger creation

---

# Utility Classes

# HashUtils

## Responsibilities

* IP hashing
* deterministic hashing helpers

---

# SocketUtils

## Responsibilities

* safe socket cleanup
* socket helper functions

---

# BufferUtils

## Responsibilities

* ByteBuffer helpers
* buffer reset/flip utilities

---

# YAML Configuration Structure

```yaml
server:
  listen_ip: 0.0.0.0
  listen_port: 8080

load_balancing:
  algorithm: ip_hash

backends:
  - id: backend-1
    host: 127.0.0.1
    port: 9001

  - id: backend-2
    host: 127.0.0.1
    port: 9002

logging:
  level: INFO
```

---

# Configuration POJOs

# AppConfig

```java
class AppConfig {

    ServerConfig server;

    LoadBalancerConfig loadBalancing;

    List<BackendConfig> backends;
}
```

---

# ServerConfig

```java
class ServerConfig {

    String listenIp;

    int listenPort;
}
```

---

# BackendConfig

```java
class BackendConfig {

    String id;

    String host;

    int port;
}
```

---

# LoadBalancerConfig

```java
class LoadBalancerConfig {

    String algorithm;
}
```

---

# Failure Philosophy

The system follows a strict fail-fast strategy:

```text
if any read/write/connect operation fails:
    immediately close client channel
    immediately close backend channel
    cancel SelectionKeys
    remove session
```

No retries.
No connection recovery.
No partial state preservation.

This keeps lifecycle handling simple and explicit.

---

# Buffer Philosophy

The system uses:

* one reusable DirectByteBuffer
* explicit flip/clear operations
* no buffer pools
* no per-session buffers

The purpose is:

* learning ByteBuffer mechanics
* reducing allocation complexity
* keeping memory ownership explicit

---

# Observability Philosophy

The system prioritizes:

* visible runtime behavior
* explicit logs
* simple counters

Metrics include:

* active sessions
* total sessions
* failed sessions
* bytes forwarded
* backend failures

Structured logs should include:

* session id
* backend id
* client IP
* event type
* timestamp

---

# Testing Strategy

Testing includes:

* local echo servers
* Dockerized backends
* connection bursts
* backend crashes
* long-lived TCP sessions
* EOF handling
* abrupt disconnects
* invalid backend routing

Tools:

* Docker
* Docker Compose
* custom TCP clients
* wrk
* iperf
