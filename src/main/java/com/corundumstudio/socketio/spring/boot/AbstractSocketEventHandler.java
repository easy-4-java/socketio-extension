/*
 * Copyright (c) 2018-present, easy-4-java (https://github.com/easy-4-java).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.corundumstudio.socketio.spring.boot.handler;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.UUID;

/**
 * Reusable base class for netty-socketio event handlers.
 *
 * <p>Subclasses only have to declare their own event methods (annotated with
 * {@code @OnEvent}, {@link OnConnect}, {@link OnDisconnect}, etc.); the base
 * class provides:</p>
 * <ul>
 *   <li>Lifecycle callbacks that log the session id and send a {@code welcome}
 *       event when a client connects.</li>
 *   <li>Convenience accessors for retrieving connected clients, broadcast
 *       operations and per-room broadcast operations from the underlying
 *       {@link SocketIOServer}.</li>
 *   <li>A standard {@link SocketIOServer} reference injectable via constructor
 *       or setter so the same handler instance can be wired up by Spring or
 *       instantiated manually.</li>
 * </ul>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see SocketIOServer
 * @see SocketIOClient
 */
@Slf4j
public abstract class AbstractSocketEventHandler {

    /**
     * The Socket.IO server that produced this handler. May be {@code null}
     * until {@link #setSocketIOServer(SocketIOServer)} or the convenience
     * constructor has been invoked.
     */
	private SocketIOServer socketIOServer;

    /**
     * Default constructor; useful when the Socket.IO server is supplied later
     * via {@link #setSocketIOServer(SocketIOServer)} or by the Spring
     * container during bean initialisation.
     */
	public AbstractSocketEventHandler() {
	}

    /**
     * Convenience constructor that wires the handler directly to a Socket.IO
     * server instance.
     *
     * @param socketIOServer the server to dispatch events to; must not be
     *                       {@code null} if any {@code getXxx} method is
     *                       going to be called from this handler.
     */
	public AbstractSocketEventHandler(SocketIOServer socketIOServer) {
		this.socketIOServer = socketIOServer;
	}

    /**
     * Default {@code connect} callback invoked by netty-socketio.
     *
     * <p>Logs the handshake data and emits a {@code welcome} event back to the
     * newly connected client. Subclasses may override this method to add
     * custom logic but should call {@code super.onConnect(client)} first if
     * they want to preserve the welcome message.</p>
     *
     * @param client the client that just established a connection; never
     *               {@code null}.
     */
	@OnConnect
	public void onConnect(SocketIOClient client) {
		log.debug("Connect OK.");
		log.debug("Session ID  : %s", client.getSessionId());
		log.debug("HttpHeaders : %s", client.getHandshakeData().getHttpHeaders());
		log.debug("UrlParams   : %s", client.getHandshakeData().getUrlParams());

		client.sendEvent("welcome", "ok");
	}

    /**
     * Default {@code disconnect} callback invoked by netty-socketio.
     *
     * <p>Logs the leaving client. Subclasses may override this method to add
     * custom cleanup logic.</p>
     *
     * @param client the client that just disconnected; never {@code null}.
     */
	@OnDisconnect
	public void onDisconnect(SocketIOClient client) {
		log.debug("Disconnect OK.");
		log.debug("Session ID  : %s", client.getSessionId());
	}

    /**
     * Returns every client currently connected to the given namespace.
     *
     * @param namespace the namespace name (use the empty string for the
     *                  default namespace); must not be {@code null}.
     * @return the live collection of clients; never {@code null}.
     */
	public Collection<SocketIOClient> getClients(String namespace) {
		return getSocketIOServer().getNamespace(namespace).getAllClients();
	}

    /**
     * Looks up a single client in the given namespace by its session id.
     *
     * @param namespace the namespace name; must not be {@code null}.
     * @param sessionId the session id; must not be {@code null}.
     * @return the matching client, or {@code null} if no client with that
     *         session id is currently connected.
     */
	public SocketIOClient getClient(String namespace, UUID sessionId) {
		return getSocketIOServer().getNamespace(namespace).getClient(sessionId);
	}

    /**
     * Returns the {@link BroadcastOperations} for an entire namespace.
     *
     * @param namespace the namespace name; must not be {@code null}.
     * @return the broadcast handle; never {@code null}.
     */
	public BroadcastOperations getBroadcastOperations(String namespace) {
		return getSocketIOServer().getNamespace(namespace).getBroadcastOperations();
	}

    /**
     * Returns the {@link BroadcastOperations} scoped to a single room inside a
     * namespace.
     *
     * @param namespace the namespace name; must not be {@code null}.
     * @param room      the room name; must not be {@code null}.
     * @return the room-scoped broadcast handle; never {@code null}.
     */
	public BroadcastOperations getBroadcastOperations(String namespace, String room) {
		return getSocketIOServer().getNamespace(namespace).getRoomOperations(room);
	}

    /**
     * Returns the Socket.IO server bound to this handler.
     *
     * @return the bound server, possibly {@code null} if none has been set.
     */
	public SocketIOServer getSocketIOServer() {
		return socketIOServer;
	}

    /**
     * Replaces the Socket.IO server bound to this handler.
     *
     * @param socketIOServer the new server; may be {@code null} to clear the
     *                       reference (not recommended for production code).
     */
	public void setSocketIOServer(SocketIOServer socketIOServer) {
		this.socketIOServer = socketIOServer;
	}

}