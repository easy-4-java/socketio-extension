# socketio-extension

[English](./README.md) | [简体中文](./README.zh-CN.md)

## 目录

- [1. Project Overview](#1-project-overview)
- [2. Features & Status](#2-features--status)
- [3. Requirements & Compatibility](#3-requirements--compatibility)
- [4. Architecture & Modules](#4-architecture--modules)
- [5. Installation](#5-installation)
- [6. Quick Start](#6-quick-start)
- [7. Configuration](#7-configuration)
- [8. Core Usage / API](#8-core-usage--api)
- [9. Testing & Build](#9-testing--build)
- [10. Versioning & Branches](#10-versioning--branches)
- [11. Contributing & License](#11-contributing--license)

## 1. Project Overview

`socketio-extension` 是 Netty-SocketIO 服务端库的一组扩展与工具。它提供基于 Redisson 的分布式会话存储（多个 Socket.IO 服务实例可共享会话）、面向 Redisson 的 `Config` 基类、可复用的事件处理器基类、缓存键常量与关闭钩子——让 Socket.IO 应用无需重复实现存储层即可基于 Redis 水平扩展。

它是 Netty-SocketIO 应用的配套库——不提供完整的服务端启动器，也不提供 Spring Boot 自动配置。

典型场景：

| 场景 | 本模块提供的组件 |
|:---|:---|
| 多个服务实例共享 Socket.IO 会话 | `RedissonExtStore` + `RedissonExtStoreFactory` |
| 以常用服务器模式构建 Redisson 客户端 | `RedissonConfig`（单机、集群、主从、复制、哨兵） |
| 标准的连接/断开与广播辅助方法 | `AbstractSocketEventHandler` |
| Socket.IO 服务优雅关闭 | `SocketIOServerShutdownHook` |
| 规范的 Redis 键名 | `CacheKey` / `CacheKeyConstant` |

## 2. Features & Status

项目状态：`1.0.x.*` 预发布开发线（快照版本）；在首个正式 Release 标签之前，公开 API 仍在稳定过程中。

| 能力 | 状态 | 说明 |
|:---|:---|:---|
| Redisson 会话存储 | 稳定 | `RedissonExtStore implements Store`——基于 `RedissonClient` 的 `set` / `get` / `has` / `del` |
| 存储工厂 | 稳定 | `RedissonExtStoreFactory extends RedissonStoreFactory`——`createStore(sessionId)`；构造函数接收命令/发布/订阅三个 Redisson 客户端 |
| Redisson 配置基类 | 稳定 | `RedissonConfig extends org.redisson.config.Config`，提供集群、主从、复制、哨兵配置的便捷构造函数 |
| 事件处理器基类 | 稳定 | `AbstractSocketEventHandler`——`@OnConnect` / `@OnDisconnect` 处理器，按 namespace/room 的客户端查询与广播辅助方法 |
| 关闭钩子 | 稳定 | `SocketIOServerShutdownHook extends Thread` 在 JVM 退出时停止服务 |
| 缓存键常量 | 稳定 | `CacheKey` 枚举 + `CacheKeyConstant`（会话、IP 地域/位置键） |

## 3. Requirements & Compatibility

| 要求 | 版本 |
|:---|:---|
| JDK | 8+ |
| Maven | 3.6+ |
| Netty-SocketIO | 2.0.11 |
| Redisson | 3.36.0 |
| Spring Framework | 5.3.x（spring-context，用于事件/监听集成） |

版本线：

| 分支 | JDK | 版本模式 | 说明 |
|:---|:---|:---|:---|
| `feature/1.0.x` | 8 | `1.0.x.*` | 当前开发线；Spring 5.x 时代 |
| `feature/2.0.x` | 17 | `2.0.x.*` | 下一条版本线 |
| `feature/3.0.x` | 21 | `3.0.x.*` | 未来版本线 |

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

本工程为单 jar 模块，类位于 `com.corundumstudio.socketio.spring.boot`：

| 类 | 职责 |
|:---|:---|
| `RedissonExtStore` | 基于 Redisson 的按会话存储 |
| `RedissonExtStoreFactory` | 为会话创建 `RedissonExtStore` 实例 |
| `RedissonConfig` | 面向常用 Redisson 服务器模式的 `Config` 基类 |
| `AbstractSocketEventHandler` | Socket.IO 事件处理器基类（连接/断开/广播辅助） |
| `SocketIOServerShutdownHook` | JVM 关闭时停止 `SocketIOServer` |
| `CacheKey` / `CacheKeyConstant` | 规范的 Redis 键名 |

## 5. Installation

制品发布到 easy4j 私有仓库与 GitHub Releases，暂未发布 Maven Central。

Maven：

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>socketio-extension</artifactId>
    <version>1.0.x.20260630-SNAPSHOT</version>
</dependency>
```

Gradle：

```groovy
implementation 'io.github.easy4j:socketio-extension:1.0.x.20260630-SNAPSHOT'
```

## 6. Quick Start

使用 Redisson 会话存储启动 Netty-SocketIO 服务：

```java
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.spring.boot.RedissonConfig;
import com.corundumstudio.socketio.spring.boot.RedissonExtStoreFactory;
import org.redisson.Redisson;

// 1. Redisson 客户端（继承自 org.redisson.config.Config）
RedissonConfig redissonConfig = new RedissonConfig();
redissonConfig.useSingleServer().setAddress("redis://127.0.0.1:6379");
Redisson redisson = (Redisson) Redisson.create(redissonConfig);

// 2. 会话存储工厂（命令、发布、订阅客户端）
RedissonExtStoreFactory storeFactory = new RedissonExtStoreFactory(redisson, redisson, redisson);

// 3. Socket.IO 服务配置
Configuration socketioConfig = new Configuration();
socketioConfig.setHostname("localhost");
socketioConfig.setPort(9092);
socketioConfig.setStoreFactory(storeFactory);

// 4. 启动
SocketIOServer server = new SocketIOServer(socketioConfig);
server.addListeners(new MyEventHandler(server));
server.start();
Runtime.getRuntime().addShutdownHook(new SocketIOServerShutdownHook(server));
```

预期结果：服务在 `9092` 端口接收 Socket.IO 客户端；每个客户端会话通过 `RedissonExtStore` 写入 Redis，单实例重启后会话不丢失，也可被其他服务实例共享。

## 7. Configuration

存储使用规范的 Redis 键名（见 `CacheKey` / `CacheKeyConstant`）：

| 键常量 | 值 | 用途 |
|:---|:---|:---|
| `SOCKET_IO_SESSIONS_KEY` | `socket_io:sessions` | 会话集合键 |
| `SOCKET_IO_SESSION_KEY` | `socket_io:session` | 单会话键前缀 |
| `SOCKET_IO_IP_REGION_KEY` | `socket_io:ip:region` | 客户端 IP 地域缓存 |
| `SOCKET_IO_IP_LOCATION_KEY` | `socket_io:ip:location` | 客户端 IP 位置缓存 |

本模块不绑定 Spring 属性前缀（纯库）。

## 8. Core Usage / API

继承 `AbstractSocketEventHandler` 并添加应用事件：

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

基类内置的广播辅助方法：

```java
// 某 namespace 下的全部客户端，或其中某 room 的客户端
getClients(namespace);
getClient(namespace, sessionId);
getBroadcastOperations(namespace);
getBroadcastOperations(namespace, room);
```

## 9. Testing & Build

构建：

```bash
./mvnw clean verify
```

- 构建配置了 JaCoCo Maven 插件：覆盖率报告生成于 `target/site/jacoco/index.html`，并配置了 BUNDLE 行覆盖率 90% 的校验规则（`haltOnFailure=false`，即只报告不阻断构建）；
- 当前仓库本模块暂无单元测试，覆盖率以 JaCoCo 报告为准；
- `central` Maven Profile（`./mvnw -Pcentral deploy`）附加 GPG 签名、源码包与 Javadoc 包用于发布。

## 10. Versioning & Branches

维护三条并行版本线：

| 分支 | JDK | 版本模式 |
|:---|:---|:---|
| `feature/1.0.x` | 8 | `1.0.x.*` |
| `feature/2.0.x` | 17 | `2.0.x.*` |
| `feature/3.0.x` | 21 | `3.0.x.*` |

维护策略：`1.0.x` 为当前活跃开发线（当前快照 `1.0.x.20260630-SNAPSHOT`）；`2.0.x` 与 `3.0.x` 为面向更新 JDK 的前向移植线。快照按需构建，正式 Release 通过 GitHub Releases 分发。

## 11. Contributing & License

- Fork 仓库并提交 Pull Request；`1.0.x` 版本线保持 JDK 8 兼容；
- Bug 反馈与功能建议通过 GitHub Issues 跟踪；
- 基于 [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0) 开源。
