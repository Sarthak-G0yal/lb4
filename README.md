# Java Layer 4 Load Balancer (Learning Project)

This is a learning-focused Java NIO TCP Layer 4 load balancer. It accepts client TCP connections, opens a matching backend TCP connection, and forwards raw bytes in both directions. The design is intentionally simple and explicit to make non-blocking I/O mechanics, selector flow, and session lifecycle easy to study.

## Features

- Single-threaded selector event loop
- Full proxy architecture (client and backend sockets are separate)
- Bidirectional byte forwarding using a reusable direct buffer
- Deterministic backend selection: `ip_hash` or `round_robin`
- Passive backend health handling with temporary unhealthy cooldown
- Structured event logs
- Basic metrics counters with periodic printing
- Graceful shutdown via JVM shutdown hook
- Docker Compose environment for load balancer and backend cluster

## Architecture Summary

- One global `Selector` handles all `OP_ACCEPT`, `OP_CONNECT`, and `OP_READ` events
- Each TCP client is paired with a backend in a `Session`
- Any I/O error or EOF triggers a brutal teardown of both sides
- Backends are selected by the configured algorithm and skipped if unhealthy
- A shutdown hook stops the event loop and closes channels cleanly

## Requirements

- Java 21
- Maven 3.9+
- Docker + Docker Compose (optional for backend testing)

## Project Layout

- [src/main/java/org/lb4/loadbalancer/config](src/main/java/org/lb4/loadbalancer/config) configuration POJOs + loader
- [src/main/java/org/lb4/loadbalancer/core](src/main/java/org/lb4/loadbalancer/core) core event loop and session logic
- [src/main/java/org/lb4/loadbalancer/tools](src/main/java/org/lb4/loadbalancer/tools) test echo server
- [src/main/resources/config.yaml](src/main/resources/config.yaml) sample config
- [Dockerfile](Dockerfile) and [compose.yaml](compose.yaml) backend test setup

## Configuration

The load balancer reads a YAML config file using Jackson. The key fields are:

```yaml
server:
  listen_ip: 0.0.0.0
  listen_port: 8080

load_balancing:
  algorithm: ip_hash   # or round_robin

backends:
  - id: backend-1
    host: backend1
    port: 9001
  - id: backend-2
    host: backend2
    port: 9002
  - id: backend-3
    host: backend3
    port: 9003
```

For local (non-Docker) runs, point backends to localhost:

```yaml
backends:
  - id: backend-1
    host: 127.0.0.1
    port: 9001
  - id: backend-2
    host: 127.0.0.1
    port: 9002
  - id: backend-3
    host: 127.0.0.1
    port: 9003
```

## Build

```bash
mvn -q -DskipTests package
```

Docker runtime dependencies:

```bash
mvn -q -DskipTests dependency:copy-dependencies -DoutputDirectory=target/dependency
```

## Run

### Docker (load balancer + backends)

```bash
docker compose -f compose.yaml up --build
```

### Local terminals

Ensure the config uses localhost backends, then start backends and the load balancer.

```bash
java -cp target/java-lb4-1.0-SNAPSHOT.jar org.lb4.loadbalancer.tools.TcpEchoServer 9001 backend-1
java -cp target/java-lb4-1.0-SNAPSHOT.jar org.lb4.loadbalancer.tools.TcpEchoServer 9002 backend-2
java -cp target/java-lb4-1.0-SNAPSHOT.jar org.lb4.loadbalancer.tools.TcpEchoServer 9003 backend-3
```

Start the load balancer:

```bash
mvn -q -DskipTests exec:java \
  -Dexec.mainClass=org.lb4.loadbalancer.Main \
  -Dexec.args="src/main/resources/config.yaml"
```

## End-to-End Validation

Send a message through the load balancer and verify the echo:

```bash
printf "hello lb\n" | nc 127.0.0.1 8080
```

You should see the same text returned. The console logs will show session accept, backend selection, and forward events. Every ~10 seconds, a metrics line is printed with counts for sessions, bytes, and backend failures.

## Shutdown

Press Ctrl+C to stop the load balancer. A shutdown hook closes all channels, stops metrics printing, and exits cleanly.

## Observability Output

Example structured logs:

```
ts=... event=listen ip=0.0.0.0 port=8080
ts=... event=session_accept sessionId=1 client=/127.0.0.1:12345
ts=... event=backend_selected sessionId=1 clientIp=127.0.0.1 backend=Backend{...}
ts=... event=forward sessionId=1 from=client bytes=12
```

Example metrics line:

```
metrics ts=... totalSessions=1 activeSessions=1 failedSessions=0 bytesForwarded=12 backendFailures=0
```

