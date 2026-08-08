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

/**
 * Constants for Socket.IO related cache key segments.
 *
 * <p>Centralises the literal segments that {@link CacheKey} uses to build full
 * namespaced cache keys (typically &quot;socket_io:...&quot;). Keeping them in a
 * dedicated abstract class avoids string duplication and makes it trivial to
 * rename or translate them without touching every call site.</p>
 *
 * <p>All constants are intentionally declared as {@code public static final String}
 * so they may be used inline (e.g. as Redis prefix tokens) without
 * instantiation. The class is {@code abstract} and has no public constructor
 * because it is a pure constant holder.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see CacheKey
 */
public abstract class CacheKeyConstant {

    /**
     * Cache-key segment for the global Socket.IO session list (the set of all
     * currently connected sessions).
     */
    public final static String SOCKET_IO_SESSIONS_KEY = "socket_io:sessions";

    /**
     * Cache-key segment for a single Socket.IO session record, normally combined
     * with a session identifier suffix.
     */
    public final static String SOCKET_IO_SESSION_KEY = "socket_io:session";

    /**
     * Cache-key segment for the IP-to-region mapping (e.g. country / province
     * codes derived from a client IP address).
     */
    public final static String SOCKET_IO_IP_REGION_KEY = "socket_io:ip:region";

    /**
     * Cache-key segment for the IP-to-geographic-location mapping (lat/lon or
     * similar coordinate information).
     */
    public final static String SOCKET_IO_IP_LOCATION_KEY = "socket_io:ip:location";

}