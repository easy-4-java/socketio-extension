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

import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;

/**
 * Enumerates the canonical Socket.IO cache keys and offers utility helpers to
 * assemble them into namespaced strings.
 *
 * <p>Each enum constant bundles a human-readable description together with a
 * function that produces the final cache-key string given any required
 * arguments (e.g. a session identifier or an IP address). The static helpers
 * {@link #getKeyStr(Object...)} and {@link #getThreadKeyStr(String, Object...)}
 * perform the actual string assembly, using {@link #REDIS_PREFIX} as the global
 * prefix and {@link #DELIMITER} ({@code ":"}) as the separator.</p>
 *
 * <p>Null or blank segments are skipped silently during assembly so call sites
 * can pass optional identifiers without conditionals.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see CacheKeyConstant
 */
public enum CacheKey {

    /**
     * Cache key for the list of all known Socket.IO sessions.
     */
    SOCKET_IO_SESSIONS("Socket session list", (p1) -> {
        return CacheKey.getKeyStr(CacheKeyConstant.SOCKET_IO_SESSIONS_KEY);
    }),
    /**
     * Cache key for an individual Socket.IO session; combined with the session
     * identifier passed to {@link #getKey(Object)}.
     */
    SOCKET_IO_SESSION("Socket session info", (sessionId) -> {
        return CacheKey.getKeyStr(CacheKeyConstant.SOCKET_IO_SESSION_KEY, sessionId);
    }),

    /**
     * Cache key for the region/country code resolved from an IP address.
     */
    SOCKET_IO_IP_REGION("IP-to-region code cache", (ip)->{
        return getKeyStr(CacheKeyConstant.SOCKET_IO_IP_REGION_KEY, ip);
    }),
    /**
     * Cache key for the geographic coordinate resolved from an IP address.
     */
    SOCKET_IO_IP_LOCATION("IP-to-location coordinate cache", (ip)->{
        return getKeyStr(CacheKeyConstant.SOCKET_IO_IP_LOCATION_KEY, ip);
    })
	;

    /**
     * Human-readable description of this cache key, intended for logging and
     * debugging rather than runtime dispatch.
     */
	private String desc;
    /**
     * Function that builds the final cache-key string when given the
     * context-specific identifier (or {@code null} for keys without one).
     */
    private Function<Object, String> function;

    /**
     * Creates a new enum constant.
     *
     * @param desc     a short human-readable description of the key.
     * @param function the function that produces the final cache-key string;
     *                 must accept {@code null} for keys that take no argument.
     */
    CacheKey(String desc, Function<Object, String> function) {
        this.desc = desc;
        this.function = function;
    }

    /**
     * Returns the description associated with this key.
     *
     * @return the short human-readable description; never {@code null}.
     */
	public String getDesc() {
		return desc;
	}

    /**
     * Returns the fully-qualified cache key (no extra arguments required).
     *
     * @return the assembled cache key with only {@link #REDIS_PREFIX} and the
     *         constant segment(s) included.
     */
    public String getKey() {
        return this.function.apply(null);
    }

    /**
     * Returns the fully-qualified cache key, combining the constant prefix with
     * the supplied identifier.
     *
     * @param key the context-specific identifier (e.g. session id, IP address).
     *            May be {@code null}, in which case it is skipped during
     *            assembly rather than rendered as the literal string
     *            {@code "null"}.
     * @return the assembled cache key.
     */
    public String getKey(Object key) {
        return this.function.apply(key);
    }

    /**
     * Global prefix applied to every Redis cache key produced by this class.
     */
    public static String REDIS_PREFIX = "rds";
    /**
     * Segment separator used by the cache-key assembly helpers.
     */
    public final static String DELIMITER = ":";

    /**
     * Assembles a cache key by concatenating {@link #REDIS_PREFIX} with each
     * supplied argument using {@link #DELIMITER}.
     *
     * <p>Null values and values whose {@code toString()} is blank are silently
     * skipped, so callers may pass optional identifiers without branching.</p>
     *
     * @param args the segments to concatenate after the prefix; may be empty
     *             but should not be {@code null}.
     * @return the joined cache key string, never {@code null}.
     */
    public static String getKeyStr(Object... args) {
        StringJoiner tempKey = new StringJoiner(DELIMITER);
        tempKey.add(REDIS_PREFIX);
        for (Object s : args) {
            if (Objects.isNull(s) || !StringUtils.hasText(s.toString())) {
                continue;
            }
            tempKey.add(s.toString());
        }
        return tempKey.toString();
    }

    /**
     * Assembles a thread-scoped cache key that includes the current thread id
     * between the supplied {@code prefix} and the extra arguments.
     *
     * @param prefix the leading segment (added before the thread id).
     * @param args   the additional segments; null or blank entries are skipped.
     * @return the joined, thread-scoped cache key string, never {@code null}.
     */
    public static String getThreadKeyStr(String prefix, Object... args) {

        StringJoiner tempKey = new StringJoiner(DELIMITER);
        tempKey.add(prefix);
        tempKey.add(String.valueOf(Thread.currentThread().getId()));
        for (Object s : args) {
            if (Objects.isNull(s) || !StringUtils.hasText(s.toString())) {
                continue;
            }
            tempKey.add(s.toString());
        }
        return tempKey.toString();
    }

    /**
     * Local sanity check used during development to print a sample key.
     *
     * @param args ignored; present so this class can be executed directly via
     *             {@code java CacheKey}.
     */
    public static void main(String[] args) {
        System.out.println(getKeyStr(233,""));
    }


}