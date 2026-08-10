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

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;

import java.util.UUID;

/**
 * Factory that produces {@link RedissonExtStore} instances, one per session,
 * using a single shared {@link RedissonClient}.
 *
 * <p>The class extends Redisson's own {@code RedissonStoreFactory} so it remains
 * drop-in compatible with netty-socketio's pluggable store infrastructure, but
 * it deliberately overrides {@link #createStore(UUID)} to return the richer
 * {@link RedissonExtStore} variant. The Pub/Sub {@code Redisson} clients are
 * forwarded to the super-class unchanged because the extension only needs the
 * main data-plane client.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see RedissonExtStore
 */
public class RedissonExtStoreFactory extends RedissonStoreFactory {

    /**
     * The data-plane Redisson client used to build every store. Cached
     * locally so {@link #createStore(UUID)} does not have to reach into the
     * super-class for it on every call.
     */
    private final RedissonClient redisClient;

    /**
     * Creates a new factory. All three Redisson clients are forwarded to the
     * super-class so the original Pub/Sub wiring is preserved; the main client
     * is also captured locally for {@link #createStore(UUID)}.
     *
     * @param redisClient the main Redisson client used for data operations.
     * @param redisPub    the Redisson client used for publish-side pub/sub.
     * @param redisSub    the Redisson client used for subscribe-side pub/sub.
     */
    public RedissonExtStoreFactory(Redisson redisClient, Redisson redisPub, Redisson redisSub) {
        super(redisClient, redisPub, redisSub);
        this.redisClient = redisClient;
    }

    /**
     * Builds a new {@link RedissonExtStore} for the supplied session id.
     *
     * @param sessionId the Socket.IO session id the new store will be scoped to.
     * @return a ready-to-use {@link RedissonExtStore}; never {@code null}.
     */
    @Override
    public Store createStore(UUID sessionId) {
        return new RedissonExtStore(sessionId, redisClient);
    }


}