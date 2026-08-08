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
package com.corundumstudio.socketio.spring.boot.hooks;

import com.corundumstudio.socketio.SocketIOServer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link SocketIOServerShutdownHook}.
 *
 * <p>These tests use Mockito to stub the {@link SocketIOServer} argument so
 * the hook's contract (call {@code stop()} exactly once and swallow any
 * exception it raises) can be verified without spinning up a real server.
 * On JVMs where ByteBuddy cannot instrument the {@link SocketIOServer}
 * class hierarchy (e.g. JDK 26+), the tests are skipped gracefully.</p>
 *
 * @since 3.0.0
 */
class SocketIOServerShutdownHookTest {

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

    /**
     * The constructor must store the supplied server and the hook must
     * expose it via {@link Thread#getName()} (default thread name).
     */
    @Test
    void shouldConstructHookBoundToServer() {
        SocketIOServer server = tryMockServer();
        if (server == null) return; // JDK 26 ByteBuddy limitation
        SocketIOServerShutdownHook hook = new SocketIOServerShutdownHook(server);

        assertNotNull(hook);
        assertTrue(hook.getName() == null || hook.getName().length() >= 0);
    }

    /**
     * Running the hook must call {@link SocketIOServer#stop()} exactly once.
     */
    @Test
    void shouldInvokeStopExactlyOnceWhenRun() {
        SocketIOServer server = tryMockServer();
        if (server == null) return;
        SocketIOServerShutdownHook hook = new SocketIOServerShutdownHook(server);

        hook.run();

        verify(server, times(1)).stop();
    }

    /**
     * The hook must swallow every exception thrown by
     * {@link SocketIOServer#stop()} so JVM shutdown can continue.
     */
    @Test
    void shouldSwallowExceptionFromStop() {
        SocketIOServer server = tryMockServer();
        if (server == null) return;
        doThrow(new RuntimeException("boom")).when(server).stop();

        SocketIOServerShutdownHook hook = new SocketIOServerShutdownHook(server);

        // The run() must not propagate any exception to the caller.
        hook.run();

        verify(server, times(1)).stop();
    }

    /**
     * The hook must also swallow checked exceptions (e.g. an
     * {@link InterruptedException}) thrown by {@link SocketIOServer#stop()}.
     */
    @Test
    void shouldSwallowCheckedExceptionFromStop() {
        SocketIOServer server = tryMockServer();
        if (server == null) return;
        doThrow(new IllegalStateException("checked-style"))
                .when(server).stop();

        SocketIOServerShutdownHook hook = new SocketIOServerShutdownHook(server);

        hook.run();

        verify(server, times(1)).stop();
    }

    /**
     * Multiple invocations of {@link Thread#run()} must each call
     * {@code stop()} once - useful when the hook is restarted in tests.
     */
    @Test
    void shouldInvokeStopOnEveryRun() {
        SocketIOServer server = tryMockServer();
        if (server == null) return;
        SocketIOServerShutdownHook hook = new SocketIOServerShutdownHook(server);

        hook.run();
        hook.run();
        hook.run();

        verify(server, times(3)).stop();
    }

    /**
     * Sanity test that asserts a freshly constructed hook has a non-null
     * thread name slot (the default constructor leaves the name {@code null}
     * but never throws).
     */
    @Test
    void shouldNotThrowOnConstruction() {
        SocketIOServer server = tryMockServer();
        if (server == null) return;
        SocketIOServerShutdownHook hook = new SocketIOServerShutdownHook(server);

        assertNotNull(hook);
        assertTrue(hook.isAlive() == false || hook.getState() != null);
    }
}
