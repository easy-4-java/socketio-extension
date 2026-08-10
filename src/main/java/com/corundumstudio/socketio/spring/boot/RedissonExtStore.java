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

import org.redisson.api.RedissonClient;

import java.util.Map;
import java.util.UUID;

/**
 * {@link Store} backed by a single Redisson {@link RMap} scoped to one
 * Socket.IO session.
 *
 * <p>For a given session id the store asks the supplied {@link RedissonClient}
 * for the distributed map keyed by
 * {@link CacheKey#SOCKET_IO_SESSION} and forwards all {@link Store} operations
 * to that map. Because the same Redisson map is shared across JVMs, multiple
 * application instances may observe consistent session state.</p>
 *
 * <p>All operations are thin delegations: they neither copy values nor enforce
 * type safety. Callers are responsible for using a consistent value type per
 * key.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see Store
 * @see RedissonExtStoreFactory
 */
public class RedissonExtStore implements Store{

    /**
     * The Redisson map that physically stores the session attributes. The map
     * is obtained once at construction time and reused for every operation.
     */
    private final Map<String, Object> map;

    /**
     * Creates a new store bound to the Redisson map that corresponds to the
     * given session id.
     *
     * @param sessionId the Socket.IO session id; embedded into the cache key
     *                  via {@link CacheKey#SOCKET_IO_SESSION}.
     * @param redisson  the Redisson client used to obtain the backing map;
     *                  must not be {@code null}.
     */
    public RedissonExtStore(UUID sessionId, RedissonClient redisson) {
        this.map = redisson.getMap(CacheKey.SOCKET_IO_SESSION.getKey(sessionId));
    }

    /**
     * Stores {@code value} under {@code key}, overwriting any previous entry.
     *
     * @param key   the attribute key; may be {@code null} although Redisson
     *              will propagate the {@link NullPointerException} in that case.
     * @param value the value to associate with {@code key}; may be any
     *              Redisson-serialisable object.
     */
    @Override
    public void set(String key, Object value) {
        map.put(key, value);
    }

    /**
     * Returns the value previously stored under {@code key}, or {@code null}
     * if no such entry exists.
     *
     * @param <T>  the expected value type.
     * @param key  the attribute key to look up.
     * @return the stored value, cast to {@code T}; may be {@code null}.
     */
    @Override
    public <T> T get(String key) {
        return (T) map.get(key);
    }

    /**
     * Indicates whether an entry exists for the supplied key.
     *
     * @param key the attribute key to check.
     * @return {@code true} if a value is present, {@code false} otherwise.
     */
    @Override
    public boolean has(String key) {
        return map.containsKey(key);
    }

    /**
     * Removes the entry stored under {@code key}, if any.
     *
     * @param key the attribute key to delete.
     */
    @Override
    public void del(String key) {
        map.remove(key);
    }


}