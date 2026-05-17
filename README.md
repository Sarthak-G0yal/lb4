# Java Layer 4 Load Balancer (Learning Project)

This project is a learning-focused Java NIO Layer 4 load balancer. Phases 1-7 cover project setup, YAML configuration, config validation, the initial selector-based event loop, session tracking, backend connection pairing, bidirectional byte forwarding, IP-hash selection, and passive failure handling.

## Phase 1 Status

- Maven project and Java 21 configuration
- YAML config mapped to POJOs (Jackson YAML)
- Config validation with a simple loader
- TCP echo backend for local testing
- Docker Compose for running multiple backends

## Phase 2 Status

- Single-threaded selector event loop
- Server socket accept handling (OP_ACCEPT)
- Client socket registration (OP_READ)
- Connection and read event logging

## Phase 3 Status

- Session model with client channel tracking
- SelectionKey attachment to sessions
- Session lifecycle tracking and cleanup

## Phase 4 Status

- Backend registry and selection
- Non-blocking backend connections (OP_CONNECT)
- Client-backend pairing per session

## Phase 5 Status

- Bidirectional byte forwarding
- Single reusable direct buffer
- EOF handling and brutal teardown on errors

## Phase 6 Status

- IP-hash backend selection by client IP
- Deterministic backend mapping per client
- Configurable algorithm: `ip_hash` or `round_robin`

## Phase 7 Status

- Passive backend health tracking on IO failures
- Temporary unhealthy marking with cooldown
- Backend selection skips unhealthy targets

## Requirements

- Java 21
- Maven 3.9+
- Docker + Docker Compose (optional for backends)

## Project Layout

- src/main/java/org/lb4/loadbalancer/config: config POJOs + loader
- src/main/java/org/lb4/loadbalancer/core: selector event loop
- src/main/java/org/lb4/loadbalancer/tools: TCP echo server
- src/main/resources/config.yaml: sample config
- Dockerfile, compose.yaml: backend test setup

## Build

```bash
mvn -q -DskipTests package
```

## Validate Config Loading

```bash
mvn -q -DskipTests exec:java \
  -Dexec.mainClass=org.lb4.loadbalancer.Main \
  -Dexec.args="src/main/resources/config.yaml"
```

## Run Backends (Docker)

```bash
docker compose -f compose.yaml up --build
```

## Run Backends (Local Terminals)

```bash
java -cp target/java-lb4-1.0-SNAPSHOT.jar org.lb4.loadbalancer.tools.TcpEchoServer 9001 backend-1
java -cp target/java-lb4-1.0-SNAPSHOT.jar org.lb4.loadbalancer.tools.TcpEchoServer 9002 backend-2
java -cp target/java-lb4-1.0-SNAPSHOT.jar org.lb4.loadbalancer.tools.TcpEchoServer 9003 backend-3
```

## Run Load Balancer (Phase 2-7)

```bash
mvn -q -DskipTests exec:java \
  -Dexec.mainClass=org.lb4.loadbalancer.Main \
  -Dexec.args="src/main/resources/config.yaml"
```

## End-to-End Validation (Phase 5)

1. Start the backends (Docker or local terminals).
2. Start the load balancer.
3. Send traffic through the load balancer and verify the echo response.

```bash
printf "hello phase5\n" | nc 127.0.0.1 8080
```

You should see the same text echoed back. The load balancer logs should show accept, connect, read, and forward events.

## Next Phase

Phase 8 adds logging and observability.
