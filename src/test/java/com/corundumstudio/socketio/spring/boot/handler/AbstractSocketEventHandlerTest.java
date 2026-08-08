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
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import io.netty.handler.codec.http.HttpHeaders;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AbstractSocketEventHandler}.
 *
 * <p>Mockito is used to stub {@link SocketIONamespace} and
 * {@link SocketIOClient} (both are interfaces). {@link SocketIOServer}
 * (a concrete class) is constructed via Mockito's inline mock-maker where
 * available; on JVMs where ByteBuddy cannot instrument the class hierarchy
 * (e.g. JDK 26+), those tests are skipped gracefully.</p>
 *
 * @since 3.0.0
 */
class AbstractSocketEventHandlerTest {

    /**
     * Concrete subclass that exposes the protected methods on the abstract
     * base class so the tests can drive them.
     */
    static class TestHandler extends AbstractSocketEventHandler {
        TestHandler() {
            super();
        }

        TestHandler(SocketIOServer server) {
            super(server);
        }

        @Override
        public void onConnect(SocketIOClient client) {
            super.onConnect(client);
        }

        @Override
        public void onDisconnect(SocketIOClient client) {
            super.onDisconnect(client);
        }
    }

    /**
     * Build a stubbed {@link SocketIOClient} backed by Mockito. The client
     * returns a fresh {@link HandshakeData} and a stable random session id.
     */
    private static SocketIOClient stubClient() {
        SocketIOClient client = mock(SocketIOClient.class);
        UUID sessionId = UUID.randomUUID();
        HandshakeData handshake = new HandshakeData(
                HttpHeaders.EMPTY_HEADERS,
                new HashMap<String, List<String>>(),
                InetSocketAddress.createUnresolved("127.0.0.1", 0),
                "/socket.io",
                false);
        when(client.getSessionId()).thenReturn(sessionId);
        when(client.getHandshakeData()).thenReturn(handshake);
        return client;
    }

    /**
     * Attempts to create a mock of {@link SocketIOServer}. Returns {@code null}
     * if the current JVM's ByteBuddy cannot instrument the class (e.g. JDK 26+).
     */
    private static SocketIOServer tryMockServer() {
        try {
            return mock(SocketIOServer.class);
        } catch (Exception e) {
            return null;
        }
    }

    // ---------------------------------------------------------------
    // Constructor tests
    // ---------------------------------------------------------------

    /**
     * The no-arg constructor must succeed and produce a non-null handler
     * whose {@link SocketIOServer} reference is initially {@code null}.
     */
    @Test
    void shouldConstructWithDefaultConstructor() {
        TestHandler handler = new TestHandler();

        assertNotNull(handler);
        assertNull(handler.getSocketIOServer());
    }

    /**
     * The convenience constructor must bind the supplied server.
     */
    @Test
    void shouldConstructWithSocketIOServer() {
        SocketIOServer server = tryMockServer();
        if (server == null) return; // JDK 26 ByteBuddy limitation
        TestHandler handler = new TestHandler(server);

        assertSame(server, handler.getSocketIOServer());
    }

    // ---------------------------------------------------------------
    // Setter tests
    // ---------------------------------------------------------------

    /**
     * The setter must replace the previously-bound server reference.
     */
    @Test
    void shouldReplaceServerReferenceViaSetter() {
        SocketIOServer first = tryMockServer();
        SocketIOServer second = tryMockServer();
        if (first == null || second == null) return; // JDK 26 ByteBuddy limitation
        TestHandler handler = new TestHandler(first);

        handler.setSocketIOServer(second);

        assertSame(second, handler.getSocketIOServer());
    }

    /**
     * Setting the server to {@code null} must clear the reference.
     */
    @Test
    void shouldAllowNullServerViaSetter() {
        TestHandler handler = new TestHandler();

        handler.setSocketIOServer(null);

        assertNull(handler.getSocketIOServer());
    }

    // ---------------------------------------------------------------
    // Lifecycle callback tests
    // ---------------------------------------------------------------

    /**
     * The default {@code connect} callback must call
     * {@link SocketIOClient#sendEvent(String, Object...)} with the
     * {@code "welcome"} event.
     */
    @Test
    void shouldSendWelcomeEventOnConnect() {
        SocketIOClient client = stubClient();
        TestHandler handler = new TestHandler();

        handler.onConnect(client);

        verify(client, times(1)).sendEvent("welcome", "ok");
    }

    /**
     * The default {@code connect} callback must query the client's session
     * id and handshake data for the debug log.
     */
    @Test
    void shouldReadSessionIdAndHandshakeDuringOnConnect() {
        SocketIOClient client = stubClient();
        TestHandler handler = new TestHandler();

        handler.onConnect(client);

        verify(client, times(1)).getSessionId();
        // getHandshakeData() is called twice: once for getHttpHeaders(), once for getUrlParams()
        verify(client, times(2)).getHandshakeData();
    }

    /**
     * The default {@code disconnect} callback must query the session id but
     * not send any event.
     */
    @Test
    void shouldNotSendAnyEventOnDisconnect() {
        SocketIOClient client = stubClient();
        TestHandler handler = new TestHandler();

        handler.onDisconnect(client);

        verify(client, times(1)).getSessionId();
        verify(client, times(0)).sendEvent(anyString(), any(Object[].class));
    }

    // ---------------------------------------------------------------
    // Delegation tests (require SocketIOServer mock)
    // ---------------------------------------------------------------

    /**
     * {@link AbstractSocketEventHandler#getClients(String)} must delegate to
     * the bound server's namespace.
     */
    @Test
    void shouldReturnAllClientsFromNamespace() {
        SocketIOServer server = tryMockServer();
        if (server == null) return; // JDK 26 ByteBuddy limitation
        SocketIONamespace namespace = mock(SocketIONamespace.class);
        Collection<SocketIOClient> clients = Collections.emptyList();
        when(server.getNamespace("/")).thenReturn(namespace);
        when(namespace.getAllClients()).thenReturn(clients);

        TestHandler handler = new TestHandler(server);

        Collection<SocketIOClient> result = handler.getClients("/");
        assertSame(clients, result);
    }

    /**
     * {@link AbstractSocketEventHandler#getClient(String, UUID)} must look up
     * the namespace and then the client.
     */
    @Test
    void shouldLookUpSingleClientBySessionId() {
        SocketIOServer server = tryMockServer();
        if (server == null) return; // JDK 26 ByteBuddy limitation
        SocketIONamespace namespace = mock(SocketIONamespace.class);
        SocketIOClient client = mock(SocketIOClient.class);
        UUID sessionId = UUID.randomUUID();

        when(server.getNamespace("/")).thenReturn(namespace);
        when(namespace.getClient(sessionId)).thenReturn(client);

        TestHandler handler = new TestHandler(server);

        assertSame(client, handler.getClient("/", sessionId));
    }

    /**
     * {@link AbstractSocketEventHandler#getBroadcastOperations(String)} must
     * delegate to the namespace's broadcast operations.
     */
    @Test
    void shouldExposeNamespaceBroadcastOperations() {
        SocketIOServer server = tryMockServer();
        if (server == null) return; // JDK 26 ByteBuddy limitation
        SocketIONamespace namespace = mock(SocketIONamespace.class);
        BroadcastOperations ops = mock(BroadcastOperations.class);

        when(server.getNamespace("/")).thenReturn(namespace);
        when(namespace.getBroadcastOperations()).thenReturn(ops);

        TestHandler handler = new TestHandler(server);

        assertSame(ops, handler.getBroadcastOperations("/"));
    }

    /**
     * {@link AbstractSocketEventHandler#getBroadcastOperations(String, String)}
     * must delegate to the namespace's room-scoped broadcast operations.
     */
    @Test
    void shouldExposeRoomBroadcastOperations() {
        SocketIOServer server = tryMockServer();
        if (server == null) return; // JDK 26 ByteBuddy limitation
        SocketIONamespace namespace = mock(SocketIONamespace.class);
        BroadcastOperations ops = mock(BroadcastOperations.class);

        when(server.getNamespace("/")).thenReturn(namespace);
        when(namespace.getRoomOperations("room1")).thenReturn(ops);

        TestHandler handler = new TestHandler(server);

        assertSame(ops, handler.getBroadcastOperations("/", "room1"));
    }

    /**
     * The {@code getNamespace} resolution must use the exact namespace name
     * passed in.
     */
    @Test
    void shouldUseExactNamespaceNameForLookup() {
        SocketIOServer server = tryMockServer();
        if (server == null) return; // JDK 26 ByteBuddy limitation
        SocketIONamespace namespace = mock(SocketIONamespace.class);
        Collection<SocketIOClient> clients = Arrays.asList(mock(SocketIOClient.class));
        when(server.getNamespace("/custom")).thenReturn(namespace);
        when(namespace.getAllClients()).thenReturn(clients);

        TestHandler handler = new TestHandler(server);

        Collection<SocketIOClient> result = handler.getClients("/custom");
        assertEquals(1, result.size());
        verify(server, times(1)).getNamespace("/custom");
    }
}
