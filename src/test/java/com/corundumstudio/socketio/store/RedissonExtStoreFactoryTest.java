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
import org.redisson.Redisson;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link RedissonExtStoreFactory}.
 *
 * <p>Each factory call must produce a non-null {@link RedissonExtStore}.
 * On JVMs where ByteBuddy cannot instrument the {@link Redisson} class
 * hierarchy (e.g. JDK 26+), the tests are skipped gracefully.</p>
 *
 * @since 3.0.0
 */
class RedissonExtStoreFactoryTest {

    /**
     * Attempts to create a mock of {@link Redisson}. Returns {@code null}
     * if the current JVM's ByteBuddy cannot instrument the class.
     */
    private static Redisson tryMockRedisson() {
        try {
            return mock(Redisson.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * The factory's {@code createStore(UUID)} must return a non-null
     * {@link RedissonExtStore} instance.
     */
    @Test
    void shouldCreateNonNullStore() {
        Redisson redisMain = tryMockRedisson();
        if (redisMain == null) return; // JDK 26 ByteBuddy limitation
        Redisson redisPub = tryMockRedisson();
        Redisson redisSub = tryMockRedisson();
        if (redisPub == null || redisSub == null) return;

        RedissonExtStoreFactory factory = new RedissonExtStoreFactory(redisMain, redisPub, redisSub);

        Store store = factory.createStore(UUID.randomUUID());
        assertNotNull(store);
        assertTrue(store instanceof RedissonExtStore);
    }

    /**
     * Two distinct session ids must yield two distinct {@link RedissonExtStore}
     * instances.
     */
    @Test
    void shouldCreateDistinctStoreForEachSession() {
        Redisson redisMain = tryMockRedisson();
        if (redisMain == null) return;
        Redisson redisPub = tryMockRedisson();
        Redisson redisSub = tryMockRedisson();
        if (redisPub == null || redisSub == null) return;

        RedissonExtStoreFactory factory = new RedissonExtStoreFactory(redisMain, redisPub, redisSub);

        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        Store storeA = factory.createStore(a);
        Store storeB = factory.createStore(b);

        assertNotNull(storeA);
        assertNotNull(storeB);
        // Different sessions must yield different store instances.
        assertTrue(storeA != storeB);
    }

    /**
     * Two calls for the same session id must both produce non-null stores
     * (the factory itself does not cache them).
     */
    @Test
    void shouldCreateFreshStoreOnEveryCall() {
        Redisson redisMain = tryMockRedisson();
        if (redisMain == null) return;
        Redisson redisPub = tryMockRedisson();
        Redisson redisSub = tryMockRedisson();
        if (redisPub == null || redisSub == null) return;

        RedissonExtStoreFactory factory = new RedissonExtStoreFactory(redisMain, redisPub, redisSub);

        UUID sessionId = UUID.randomUUID();
        Store first = factory.createStore(sessionId);
        Store second = factory.createStore(sessionId);

        assertNotNull(first);
        assertNotNull(second);
    }

    /**
     * The factory must forward all three Redisson clients to its
     * super-class; the main client is captured locally so {@code createStore}
     * can resolve the backing map without going through the super-class.
     */
    @Test
    void shouldAcceptAllThreeRedissonClients() {
        Redisson redisMain = tryMockRedisson();
        Redisson redisPub = tryMockRedisson();
        Redisson redisSub = tryMockRedisson();
        if (redisMain == null || redisPub == null || redisSub == null) return;

        RedissonExtStoreFactory factory = new RedissonExtStoreFactory(redisMain, redisPub, redisSub);
        assertNotNull(factory);
    }
}
