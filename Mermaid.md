Paste the following diagrams directly into the Mermaid Live Editor.

---

# 1. Overall System Architecture

```mermaid
flowchart TD

    Client1[Client 1]
    Client2[Client 2]
    Client3[Client 3]

    subgraph LoadBalancer["Java Layer 4 Load Balancer"]
        Listener[ServerSocketChannel Listener]

        Selector[Single Global Selector Event Loop]

        SessionManager[Session Manager]

        IPHash[IP Hash Load Balancer]

        Buffer[Reusable Direct ByteBuffer]

        Logger[Logging + Metrics]

        Failure[Passive Failure Detection]

        Config[YAML Config Loader]

        Session1[Session Object]
    end

    subgraph Backends["Backend TCP Servers"]
        Backend1[Backend Server 1]
        Backend2[Backend Server 2]
        Backend3[Backend Server 3]
    end

    Client1 --> Listener
    Client2 --> Listener
    Client3 --> Listener

    Listener --> Selector

    Selector --> SessionManager
    Selector --> IPHash
    Selector --> Buffer
    Selector --> Logger
    Selector --> Failure

    Config --> Selector

    SessionManager --> Session1

    IPHash --> Backend1
    IPHash --> Backend2
    IPHash --> Backend3
```

---

# 2. Internal Load Balancer Architecture

```mermaid
flowchart LR

    subgraph EventLoop["Single Threaded Event Loop"]
        Selector[Selector]

        Accept[OP_ACCEPT]

        ReadClient[OP_READ Client]

        ReadBackend[OP_READ Backend]

        WriteClient[Write To Client]

        WriteBackend[Write To Backend]

        Teardown[Brutal Teardown]
    end

    subgraph Session["Session Object"]
        ClientSocket[Client SocketChannel]

        BackendSocket[Backend SocketChannel]

        ClientKey[Client SelectionKey]

        BackendKey[Backend SelectionKey]
    end

    Selector --> Accept

    Selector --> ReadClient
    Selector --> ReadBackend

    ReadClient --> WriteBackend

    ReadBackend --> WriteClient

    ReadClient --> Teardown
    ReadBackend --> Teardown

    Accept --> Session

    Session --> ClientSocket
    Session --> BackendSocket
    Session --> ClientKey
    Session --> BackendKey
```

---

# 3. TCP Connection Lifecycle

```mermaid
stateDiagram-v2

    [*] --> ClientConnected

    ClientConnected --> BackendSelected

    BackendSelected --> BackendConnected

    BackendConnected --> ActiveProxying

    ActiveProxying --> ForwardingClientData

    ActiveProxying --> ForwardingBackendData

    ForwardingClientData --> ActiveProxying
    ForwardingBackendData --> ActiveProxying

    ActiveProxying --> IOError

    ActiveProxying --> EOFDetected

    IOError --> BrutalTeardown

    EOFDetected --> BrutalTeardown

    BrutalTeardown --> ConnectionClosed

    ConnectionClosed --> [*]
```

---

# 4. Selector Event Flow

```mermaid
sequenceDiagram

    participant Client
    participant LB as Load Balancer
    participant Selector
    participant Backend

    Client->>LB: TCP Connect

    LB->>Selector: Register OP_ACCEPT

    Selector->>LB: Accept Event

    LB->>Backend: Open Backend Connection

    Backend-->>LB: Connected

    LB->>Selector: Register OP_READ

    Client->>LB: Send Bytes

    LB->>Backend: Forward Bytes

    Backend->>LB: Response Bytes

    LB->>Client: Forward Response

    Backend-->>LB: Disconnect / Error

    LB->>Client: Close Connection

    LB->>Backend: Close Connection
```

---

# 5. IP Hash Backend Selection

```mermaid
flowchart TD

    ClientIP[Client IP]

    Hash[Hash Function]

    Modulo[Modulo Backend Count]

    BackendList[Backend List]

    Backend1[Backend 1]
    Backend2[Backend 2]
    Backend3[Backend 3]

    ClientIP --> Hash

    Hash --> Modulo

    Modulo --> BackendList

    BackendList --> Backend1
    BackendList --> Backend2
    BackendList --> Backend3
```

---

# 6. YAML Configuration Mapping

```mermaid
flowchart LR

    YAML[application.yaml]

    Jackson[Jackson YAML Mapper]

    ConfigPOJO[Config POJO]

    ServerConfig[Server Config]

    BackendConfig[Backend Config]

    LBConfig[Load Balancer Config]

    YAML --> Jackson

    Jackson --> ConfigPOJO

    ConfigPOJO --> ServerConfig
    ConfigPOJO --> BackendConfig
    ConfigPOJO --> LBConfig
```

---

# 7. Passive Failure Detection

```mermaid
flowchart TD

    Session[Active Session]

    ReadWrite[Read / Write Operation]

    IOError[IOException]

    EOF[EOF Detected]

    MarkFailed[Mark Backend Failed]

    CloseClient[Close Client Socket]

    CloseBackend[Close Backend Socket]

    Session --> ReadWrite

    ReadWrite --> IOError
    ReadWrite --> EOF

    IOError --> MarkFailed
    EOF --> MarkFailed

    MarkFailed --> CloseClient

    MarkFailed --> CloseBackend
```

---

# 8. Docker Testing Topology

```mermaid
flowchart LR

    Client[Test Client]

    subgraph DockerNetwork["Docker Network"]
        LB[Java Load Balancer]

        B1[Backend Container 1]

        B2[Backend Container 2]

        B3[Backend Container 3]
    end

    Client --> LB

    LB --> B1
    LB --> B2
    LB --> B3
```

Useful Mermaid references:

* [Official Mermaid Docs](https://mermaid.ai/docs/mermaid-oss/intro/index.html)
* [Mermaid Live Editor](https://mermaid.live)
* [Mermaid Editor with Templates](https://mermaideditor.app/)
