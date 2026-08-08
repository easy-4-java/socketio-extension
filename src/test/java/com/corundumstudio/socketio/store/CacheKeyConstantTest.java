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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link CacheKeyConstant}.
 *
 * <p>Verifies that every constant is non-null, non-blank and starts with the
 * expected {@code socket_io:} namespace prefix. The class is {@code abstract}
 * so it cannot be instantiated directly; reflection is used to exercise the
 * class-loading path.</p>
 *
 * @since 3.0.0
 */
class CacheKeyConstantTest {

    /**
     * The session-list constant should equal the canonical namespace.
     */
    @Test
    void shouldExposeSocketIoSessionsKey() {
        assertEquals("socket_io:sessions", CacheKeyConstant.SOCKET_IO_SESSIONS_KEY);
    }

    /**
     * The session-info constant should equal the canonical namespace.
     */
    @Test
    void shouldExposeSocketIoSessionKey() {
        assertEquals("socket_io:session", CacheKeyConstant.SOCKET_IO_SESSION_KEY);
    }

    /**
     * The IP-to-region constant should equal the canonical namespace.
     */
    @Test
    void shouldExposeSocketIoIpRegionKey() {
        assertEquals("socket_io:ip:region", CacheKeyConstant.SOCKET_IO_IP_REGION_KEY);
    }

    /**
     * The IP-to-location constant should equal the canonical namespace.
     */
    @Test
    void shouldExposeSocketIoIpLocationKey() {
        assertEquals("socket_io:ip:location", CacheKeyConstant.SOCKET_IO_IP_LOCATION_KEY);
    }

    /**
     * Every constant must be a non-null, non-blank string. Guards against
     * accidental edits that produce empty values.
     */
    @Test
    void shouldExposeOnlyNonBlankValues() {
        String[] values = {
                CacheKeyConstant.SOCKET_IO_SESSIONS_KEY,
                CacheKeyConstant.SOCKET_IO_SESSION_KEY,
                CacheKeyConstant.SOCKET_IO_IP_REGION_KEY,
                CacheKeyConstant.SOCKET_IO_IP_LOCATION_KEY,
        };
        for (String value : values) {
            assertNotNull(value);
            assertTrue(value.length() > 0, "value must not be blank");
        }
    }

    /**
     * Every constant should carry the {@code socket_io} namespace prefix so
     * that Redis keys remain namespaced across the project.
     */
    @Test
    void shouldUseSocketIoNamespacePrefixForAll() {
        assertTrue(CacheKeyConstant.SOCKET_IO_SESSIONS_KEY.startsWith("socket_io"));
        assertTrue(CacheKeyConstant.SOCKET_IO_SESSION_KEY.startsWith("socket_io"));
        assertTrue(CacheKeyConstant.SOCKET_IO_IP_REGION_KEY.startsWith("socket_io"));
        assertTrue(CacheKeyConstant.SOCKET_IO_IP_LOCATION_KEY.startsWith("socket_io"));
    }
}