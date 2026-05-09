# Java Layer 4 Load Balancer (Learning Project)

This project is a learning-focused Java NIO Layer 4 load balancer. Phase 1 covers project setup, YAML configuration, config validation, and local backend test servers.

## Phase 1 Status

- Maven project and Java 21 configuration
- YAML config mapped to POJOs (Jackson YAML)
- Config validation with a simple loader
- TCP echo backend for local testing
- Docker Compose for running multiple backends

## Requirements

- Java 21
- Maven 3.9+
- Docker + Docker Compose (optional for backends)

## Project Layout

- src/main/java/org/lb4/loadbalancer/config: config POJOs + loader
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

## Next Phase

Phase 2 builds the single-threaded selector event loop and accepts client connections.
