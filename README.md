# socketio-extension

![Java](https://img.shields.io/badge/Java-8-orange) ![License](https://img.shields.io/badge/License-Apache%202.0-blue)

[1. Project Overview](#1-project-overview) | [2. Features & Status](#2-features--status) | [3. Requirements & Compatibility](#3-requirements--compatibility) | [4. Architecture & Modules](#4-architecture--modules) | [5. Installation](#5-installation) | [6. Quick Start](#6-quick-start) | [7. Configuration](#7-configuration) | [8. Core Usage / API](#8-core-usage--api) | [9. Testing & Build](#9-testing--build) | [10. Versioning & Branches](#10-versioning--branches) | [11. Contributing & License](#11-contributing--license)

## 1. Project Overview

`socketio-extension` is a set of extensions and utilities for the Netty-SocketIO server library. It adds a Redisson-backed distributed session store (so multiple Socket.IO server instances can share sessions), a Redisson-friendly `Config` base, a reusable event handler base class, cache key constants and a shutdown hook — letting a Socket.IO application scale horizontally on Redis without re-implementing the store plumbing.

It is a companion library for applications already using Netty-SocketIO — it does not ship a full server bootstrap or Spring Boot auto-configuration.

Typical scenarios:

| Scenario | What this module contributes |
|:---|:---|
| Share Socket.IO sessions across server instances | `RedissonExtStore` + `RedissonExtStoreFactory` |
| Build a Redisson client with common server modes | `RedissonConfig` (single, cluster, master/slave, replicated, sentinel) |
| Standard connect/disconnect + broadcast helpers | `AbstractSocketEventHandler` |
| Clean shutdown of the Socket.IO server | `SocketIOServerShutdownHook` |
| Well-known Redis key names | `CacheKey` / `CacheKeyConstant` |

## 2. Features & Status

Project status: pre-release development line (`1.0.x.*` snapshots); public API is still stabilizing until the first tagged release.

| Capability | Status | Notes |
|:---|:---|:---|
| Redisson session store | Stable | `RedissonExtStore implements Store` — `set` / `get` / `has` / `del` backed by a `RedissonClient` |
| Store factory | Stable | `RedissonExtStoreFactory extends RedissonStoreFactory` — `createStore(sessionId)`; constructor takes publish/subscribe/command Redisson clients |
| Redisson config base | Stable | `RedissonConfig extends org.redisson.config.Config` with convenience constructors for cluster, master/slave, replicated and sentinel configs |
| Event handler base class | Stable | `AbstractSocketEventHandler` — `@OnConnect` / `@OnDisconnect` handlers, client lookup and broadcast helpers per namespace/room |
| Shutdown hook | Stable | `SocketIOServerShutdownHook extends Thread` stops the server on JVM shutdown |
| Cache key constants | Stable | `CacheKey` enum + `CacheKeyConstant` (session, IP region/location keys) |

## 3. Requirements & Compatibility

| Requirement | Version |
|:---|:---|
| JDK | 8+ |
| Maven | 3.6+ |
| Netty-SocketIO | 2.0.11 |
| Redisson | 3.36.0 |
| Spring Framework | 5.3.x (spring-context, for event/listener integration) |

Version lines:

| Branch | JDK | Version pattern | Notes |
|:---|:---|:---|:---|
| `feature/1.0.x` | 8 | `1.0.x.*` | Current line; Spring 5.x era |
| `feature/2.0.x` | 17 | `2.0.x.*` | Next line |
| `feature/3.0.x` | 21 | `3.0.x.*` | Future line |

## 4. Architecture & Modules

```
Socket.IO clients
        |
        v
Netty-SocketIO Server (Configuration)
        |
        v
+------------------------------------+
| RedissonExtStoreFactory            |
|  -> RedissonExtStore (per session) |
|     set / get / has / del          |
+------------------------------------+
        |
        v
Redisson (Redis) — shared sessions across instances
```

The project is a single jar module. Classes live under `com.corundumstudio.socketio.spring.boot`:

| Class | Responsibility |
|:---|:---|
| `RedissonExtStore` | Per-session store backed by Redisson |
| `RedissonExtStoreFactory` | Creates `RedissonExtStore` instances for sessions |
| `RedissonConfig` | `Config` base pre-built for common Redisson server modes |
| `AbstractSocketEventHandler` | Base class for Socket.IO event handlers (connect/disconnect/broadcast helpers) |
| `SocketIOServerShutdownHook` | Stops the `SocketIOServer` on JVM shutdown |
| `CacheKey` / `CacheKeyConstant` | Well-known Redis key names |

## 5. Installation

Artifacts are published to the easy4j private repository and GitHub Releases; the project is not yet on Maven Central.

Maven:

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>socketio-extension</artifactId>
    <version>1.0.x.20260630-SNAPSHOT</version>
</dependency>
```

Gradle:

```groovy
implementation 'io.github.easy4j:socketio-extension:1.0.x.20260630-SNAPSHOT'
```

## 6. Quick Start

Start a Netty-SocketIO server with a Redisson-backed session store:

```java
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.spring.boot.RedissonConfig;
import com.corundumstudio.socketio.spring.boot.RedissonExtStoreFactory;
import org.redisson.Redisson;

// 1. Redisson client (extends org.redisson.config.Config)
RedissonConfig redissonConfig = new RedissonConfig();
redissonConfig.useSingleServer().setAddress("redis://127.0.0.1:6379");
Redisson redisson = (Redisson) Redisson.create(redissonConfig);

// 2. Session store factory (command, publish, subscribe clients)
RedissonExtStoreFactory storeFactory = new RedissonExtStoreFactory(redisson, redisson, redisson);

// 3. Socket.IO server configuration
Configuration socketioConfig = new Configuration();
socketioConfig.setHostname("localhost");
socketioConfig.setPort(9092);
socketioConfig.setStoreFactory(storeFactory);

// 4. Start
SocketIOServer server = new SocketIOServer(socketioConfig);
server.addListeners(new MyEventHandler(server));
server.start();
Runtime.getRuntime().addShutdownHook(new SocketIOServerShutdownHook(server));
```

Expected result: the server accepts Socket.IO clients on port `9092`; every client session is stored in Redis through `RedissonExtStore`, so sessions survive per-instance restarts and can be shared by other server instances.

## 7. Configuration

The store uses well-known Redis keys (see `CacheKey` / `CacheKeyConstant`):

| Key constant | Value | Purpose |
|:---|:---|:---|
| `SOCKET_IO_SESSIONS_KEY` | `socket_io:sessions` | Session collection key |
| `SOCKET_IO_SESSION_KEY` | `socket_io:session` | Per-session key prefix |
| `SOCKET_IO_IP_REGION_KEY` | `socket_io:ip:region` | Client IP region cache |
| `SOCKET_IO_IP_LOCATION_KEY` | `socket_io:ip:location` | Client IP location cache |

No Spring property prefix is bound by this module (plain library).

## 8. Core Usage / API

Extend `AbstractSocketEventHandler` and add application events:

```java
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.corundumstudio.socketio.spring.boot.AbstractSocketEventHandler;

public class MyEventHandler extends AbstractSocketEventHandler {

    public MyEventHandler(SocketIOServer server) {
        super(server);
    }

    @OnEvent("chat")
    public void onChat(SocketIOClient client, String message) {
        client.sendEvent("chat", "echo: " + message);
    }
}
```

Broadcast helpers are inherited from the base class:

```java
// all clients of a namespace, or of a room within a namespace
getClients(namespace);
getClient(namespace, sessionId);
getBroadcastOperations(namespace);
getBroadcastOperations(namespace, room);
```

## 9. Testing & Build

Build:

```bash
./mvnw clean verify
```

- The build is configured with the JaCoCo Maven plugin: a coverage report is generated at `target/site/jacoco/index.html` and a rule checks the bundle line coverage against a 90% minimum (`haltOnFailure=false`, so the check reports but does not fail the build).
- The repository currently ships no unit tests for this module; coverage is tracked via the JaCoCo report.
- The `central` Maven profile (`./mvnw -Pcentral deploy`) attaches GPG signatures, sources and Javadoc jars for publishing.

## 10. Versioning & Branches

Three parallel version lines are maintained:

| Branch | JDK | Version pattern |
|:---|:---|:---|
| `feature/1.0.x` | 8 | `1.0.x.*` |
| `feature/2.0.x` | 17 | `2.0.x.*` |
| `feature/3.0.x` | 21 | `3.0.x.*` |

Maintenance policy: the `1.0.x` line is the actively developed line (current snapshot `1.0.x.20260630-SNAPSHOT`); `2.0.x` and `3.0.x` are forward porting lines targeting newer JDKs. Snapshots are built on demand; tagged releases are distributed via GitHub Releases.

## 11. Contributing & License

- Fork the repository and open a pull request; keep the `1.0.x` line compatible with JDK 8.
- Bug reports and feature requests are tracked via GitHub Issues.
- Licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
