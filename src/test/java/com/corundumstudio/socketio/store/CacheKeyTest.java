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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link CacheKey}.
 *
 * <p>Exercises every enum constant's {@code getKey()} / {@code getKey(Object)}
 * variants as well as the static {@code getKeyStr(...)} and
 * {@code getThreadKeyStr(...)} helpers, including the null/blank skipping
 * behaviour.</p>
 *
 * @since 3.0.0
 */
class CacheKeyTest {

    /**
     * Sanity-check that all four enum constants exist; guards against
     * accidental removal.
     */
    @Test
    void shouldExposeAllExpectedEnumConstants() {
        assertNotNull(CacheKey.valueOf("SOCKET_IO_SESSIONS"));
        assertNotNull(CacheKey.valueOf("SOCKET_IO_SESSION"));
        assertNotNull(CacheKey.valueOf("SOCKET_IO_IP_REGION"));
        assertNotNull(CacheKey.valueOf("SOCKET_IO_IP_LOCATION"));
    }

    /**
     * {@link CacheKey#SOCKET_IO_SESSIONS} takes no argument and returns the
     * namespaced prefix only.
     */
    @Test
    void shouldReturnSessionListKeyWithoutArguments() {
        String key = CacheKey.SOCKET_IO_SESSIONS.getKey();
        assertEquals("rds:socket_io:sessions", key);
    }

    /**
     * {@link CacheKey#SOCKET_IO_SESSION} combines its constant segment with
     * the supplied session id.
     */
    @Test
    void shouldReturnSessionKeyWithId() {
        String key = CacheKey.SOCKET_IO_SESSION.getKey("abc-123");
        assertEquals("rds:socket_io:session:abc-123", key);
    }

    /**
     * {@link CacheKey#SOCKET_IO_IP_REGION} combines its constant segment with
     * the supplied IP address.
     */
    @Test
    void shouldReturnIpRegionKeyWithIp() {
        String key = CacheKey.SOCKET_IO_IP_REGION.getKey("10.0.0.1");
        assertEquals("rds:socket_io:ip:region:10.0.0.1", key);
    }

    /**
     * {@link CacheKey#SOCKET_IO_IP_LOCATION} combines its constant segment with
     * the supplied IP address.
     */
    @Test
    void shouldReturnIpLocationKeyWithIp() {
        String key = CacheKey.SOCKET_IO_IP_LOCATION.getKey("192.168.1.1");
        assertEquals("rds:socket_io:ip:location:192.168.1.1", key);
    }

    /**
     * Passing a null identifier must not raise; the assembly helper skips
     * null segments.
     */
    @Test
    void shouldHandleNullArgumentWithoutFailing() {
        String key = CacheKey.SOCKET_IO_SESSION.getKey(null);
        assertEquals("rds:socket_io:session", key);
    }

    /**
     * Every enum constant must expose a non-null, non-blank description.
     */
    @Test
    void shouldExposeNonBlankDescriptionForEveryConstant() {
        for (CacheKey key : CacheKey.values()) {
            assertNotNull(key.getDesc());
            assertTrue(key.getDesc().length() > 0,
                    "description for " + key + " must not be blank");
        }
    }

    /**
     * Static helper {@link CacheKey#getKeyStr(Object...)} joins the global
     * prefix with all non-null/non-blank arguments.
     */
    @Test
    void shouldAssembleKeyFromArguments() {
        assertEquals("rds:foo:bar", CacheKey.getKeyStr("foo", "bar"));
    }

    /**
     * Blank and null arguments are silently skipped.
     */
    @Test
    void shouldSkipNullAndBlankArguments() {
        assertEquals("rds:foo:bar", CacheKey.getKeyStr("foo", null, "", "bar"));
        assertEquals("rds:foo", CacheKey.getKeyStr("foo", null, ""));
    }

    /**
     * No-argument variant yields the prefix alone.
     */
    @Test
    void shouldAssemblePrefixOnlyWhenNoArgumentsProvided() {
        assertEquals("rds", CacheKey.getKeyStr());
    }

    /**
     * {@link CacheKey#getThreadKeyStr(String, Object...)} injects the current
     * thread id between the supplied prefix and the additional arguments.
     */
    @Test
    void shouldAssembleThreadScopedKey() {
        String key = CacheKey.getThreadKeyStr("ns", "alpha", "beta");
        long expectedThreadId = Thread.currentThread().getId();
        assertEquals("ns:" + expectedThreadId + ":alpha:beta", key);
    }

    /**
     * Thread-scoped helper also skips null and blank segments.
     */
    @Test
    void shouldSkipNullAndBlankSegmentsInThreadScopedKey() {
        String key = CacheKey.getThreadKeyStr("ns", null, "", "alpha");
        long expectedThreadId = Thread.currentThread().getId();
        assertEquals("ns:" + expectedThreadId + ":alpha", key);
    }

    /**
     * The constant {@link CacheKey#REDIS_PREFIX} must be non-null, and the
     * delimiter must be the colon character.
     */
    @Test
    void shouldExposeExpectedPrefixAndDelimiter() {
        assertNotNull(CacheKey.REDIS_PREFIX);
        assertEquals(":", CacheKey.DELIMITER);
        assertEquals("rds", CacheKey.REDIS_PREFIX);
    }

    /**
     * The {@code main} method exists so the class can be executed directly
     * and prints a sample key. Invoke it and assert no exception escapes.
     */
    @Test
    void shouldRunMainWithoutThrowing() {
        // The main method prints to stdout; we just need it not to throw.
        CacheKey.main(new String[]{"ignored"});
    }

    /**
     * Every assembled key should start with {@link CacheKey#REDIS_PREFIX}.
     */
    @Test
    void shouldPrefixAllAssembledKeysWithRedisPrefix() {
        for (CacheKey key : CacheKey.values()) {
            String assembled = key.getKey();
            assertTrue(assembled.startsWith(CacheKey.REDIS_PREFIX + CacheKey.DELIMITER),
                    assembled + " must start with the redis prefix");
            assertFalse(assembled.endsWith(":"),
                    assembled + " must not end with a trailing separator");
        }
    }
}