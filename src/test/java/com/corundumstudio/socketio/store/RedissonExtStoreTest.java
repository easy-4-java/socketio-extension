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
package com.corundumstudio.socketio.store;

import org.junit.jupiter.api.Test;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RedissonExtStore}.
 *
 * <p>Uses a JDK {@link Proxy} to create a lightweight {@link RMap}
 * implementation that delegates to a plain {@link HashMap}, since
 * ByteBuddy cannot instrument the RMap interface hierarchy on JDK 26+.
 * The {@link RedissonClient} (an interface) is mocked via Mockito's
 * JDK-Proxy-based mock maker.</p>
 *
 * @since 3.0.0
 */
class RedissonExtStoreTest {

    /**
     * Creates a JDK dynamic proxy that implements {@link RMap} by delegating
     * {@link Map} operations to the supplied backing map. Non-Map methods
     * return sensible defaults (false/0/null) so the proxy can satisfy the
     * full RMap contract without a real Redisson connection.
     *
     * @param delegate the real map to delegate to; must not be {@code null}.
     * @return a proxy implementing {@code RMap}; never {@code null}.
     */
    @SuppressWarnings("unchecked")
    private static <K, V> RMap<K, V> proxyRMap(Map<K, V> delegate) {
        return (RMap<K, V>) Proxy.newProxyInstance(
                RMap.class.getClassLoader(),
                new Class<?>[]{RMap.class},
                (proxy, method, args) -> {
                    try {
                        Method delegateMethod = delegate.getClass()
                                .getMethod(method.getName(), method.getParameterTypes());
                        return delegateMethod.invoke(delegate, args);
                    } catch (NoSuchMethodException e) {
                        Class<?> returnType = method.getReturnType();
                        if (returnType == boolean.class) return false;
                        if (returnType == int.class) return 0;
                        if (returnType == long.class) return 0L;
                        return null;
                    }
                });
    }

    /**
     * Helper that wires a Mockito {@link RedissonClient} to a proxied
     * {@link RMap} backed by the supplied {@link HashMap}, and returns the
     * constructed {@link RedissonExtStore}.
     */
    private static RedissonExtStore newStoreWithMap(UUID sessionId,
                                                     Map<String, Object> backing,
                                                     RedissonClient client) {
        String expectedKey = CacheKey.SOCKET_IO_SESSION.getKey(sessionId);
        RMap<String, Object> rmap = proxyRMap(backing);
        when(client.<String, Object>getMap(eq(expectedKey))).thenReturn(rmap);
        return new RedissonExtStore(sessionId, client);
    }

    /**
     * After construction the store must request the map keyed by the
     * session-id-aware cache key from the supplied Redisson client.
     */
    @Test
    void shouldAskRedissonForSessionScopedMap() {
        RedissonClient client = mock(RedissonClient.class);
        UUID sessionId = UUID.randomUUID();
        Map<String, Object> backing = new HashMap<>();

        newStoreWithMap(sessionId, backing, client);

        verify(client, times(1)).getMap(CacheKey.SOCKET_IO_SESSION.getKey(sessionId));
    }

    /**
     * {@link RedissonExtStore#set(String, Object)} must forward to the
     * backing map's {@code put}.
     */
    @Test
    void shouldStoreValueUnderKey() {
        RedissonClient client = mock(RedissonClient.class);
        Map<String, Object> backing = new HashMap<>();
        RedissonExtStore store = newStoreWithMap(UUID.randomUUID(), backing, client);

        store.set("user", "alice");

        assertEquals("alice", backing.get("user"));
    }

    /**
     * {@link RedissonExtStore#set(String, Object)} should overwrite any
     * pre-existing value.
     */
    @Test
    void shouldOverwriteExistingValue() {
        RedissonClient client = mock(RedissonClient.class);
        Map<String, Object> backing = new HashMap<>();
        RedissonExtStore store = newStoreWithMap(UUID.randomUUID(), backing, client);

        store.set("user", "alice");
        store.set("user", "bob");

        assertEquals("bob", backing.get("user"));
    }

    /**
     * {@link RedissonExtStore#get(String)} must return the stored value
     * and {@code null} for unknown keys.
     */
    @Test
    void shouldReturnStoredValueOrNullForUnknownKey() {
        RedissonClient client = mock(RedissonClient.class);
        Map<String, Object> backing = new HashMap<>();
        RedissonExtStore store = newStoreWithMap(UUID.randomUUID(), backing, client);

        store.set("k", 42);

        Integer v = store.get("k");
        assertEquals(Integer.valueOf(42), v);

        assertNull(store.get("missing"));
    }

    /**
     * {@link RedissonExtStore#has(String)} must reflect the presence of an
     * entry in the backing map.
     */
    @Test
    void shouldReportHasCorrectly() {
        RedissonClient client = mock(RedissonClient.class);
        Map<String, Object> backing = new HashMap<>();
        RedissonExtStore store = newStoreWithMap(UUID.randomUUID(), backing, client);

        assertFalse(store.has("k"));

        store.set("k", "value");

        assertTrue(store.has("k"));
    }

    /**
     * {@link RedissonExtStore#del(String)} must remove the entry from the
     * backing map and is safe to call for non-existent keys.
     */
    @Test
    void shouldDeleteKey() {
        RedissonClient client = mock(RedissonClient.class);
        Map<String, Object> backing = new HashMap<>();
        RedissonExtStore store = newStoreWithMap(UUID.randomUUID(), backing, client);

        store.set("k", "v");
        store.del("k");

        assertFalse(store.has("k"));
        // deleting a missing key must not throw.
        store.del("missing");
        assertFalse(store.has("missing"));
    }

    /**
     * Multiple keys can coexist independently.
     */
    @Test
    void shouldSupportMultipleKeys() {
        RedissonClient client = mock(RedissonClient.class);
        Map<String, Object> backing = new HashMap<>();
        RedissonExtStore store = newStoreWithMap(UUID.randomUUID(), backing, client);

        store.set("a", 1);
        store.set("b", 2);
        store.set("c", 3);

        assertEquals(Integer.valueOf(1), store.get("a"));
        assertEquals(Integer.valueOf(2), store.get("b"));
        assertEquals(Integer.valueOf(3), store.get("c"));
        assertTrue(store.has("a"));
        assertTrue(store.has("b"));
        assertTrue(store.has("c"));
    }
}
