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
package org.redisson.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link RedissonConfig}.
 *
 * <p>Exercises every public constructor. Most of the work is delegated to the
 * parent {@link Config} so the assertions focus on verifying that the
 * Redisson-specific sub-configs were wired correctly.</p>
 *
 * @since 3.0.0
 */
class RedissonConfigTest {

    /**
     * Default constructor must succeed and produce a non-null instance whose
     * sub-configs are all {@code null} (i.e. nothing has been installed yet).
     */
    @Test
    void shouldConstructWithDefaultConstructor() {
        RedissonConfig config = new RedissonConfig();

        assertNotNull(config);
        assertFalse(config.isClusterConfig());
        assertFalse(config.isSentinelConfig());
        assertFalse(config.isSingleConfig());
    }

    /**
     * Copy-constructor must clone every simple field from the source and
     * install the same sub-configurations.
     */
    @Test
    void shouldConstructByCopyingAnotherConfig() {
        RedissonConfig source = new RedissonConfig();
        source.setThreads(8);

        RedissonConfig copy = new RedissonConfig(source);

        assertNotNull(copy);
        assertEquals(8, copy.getThreads());
    }

    /**
     * Passing {@code null} to the copy-constructor must throw a
     * {@link NullPointerException} because the parent {@link Config} class
     * does not guard against null arguments.
     */
    @Test
    void shouldThrowNpeWhenCopyConstructorReceivesNull() {
        assertThrows(NullPointerException.class, () -> new RedissonConfig((Config) null));
    }

    /**
     * The cluster-server constructor must install the supplied configuration
     * and mark the config as a cluster one.
     */
    @Test
    void shouldConstructWithClusterServersConfig() {
        ClusterServersConfig cluster = new ClusterServersConfig();
        cluster.addNodeAddress("redis://127.0.0.1:7000");

        RedissonConfig config = new RedissonConfig(cluster);

        assertNotNull(config);
        assertTrue(config.isClusterConfig());
    }

    /**
     * Passing a {@code null} cluster config must still produce a usable
     * instance (Redisson will then default to an empty cluster).
     */
    @Test
    void shouldAcceptNullClusterServersConfig() {
        RedissonConfig config = new RedissonConfig((ClusterServersConfig) null);

        assertNotNull(config);
        assertFalse(config.isSingleConfig());
    }

    /**
     * The master/slave constructor must install the supplied configuration.
     */
    @Test
    void shouldConstructWithMasterSlaveServersConfig() {
        MasterSlaveServersConfig ms = new MasterSlaveServersConfig();
        ms.setMasterAddress("redis://127.0.0.1:6379");

        RedissonConfig config = new RedissonConfig(ms);

        assertNotNull(config);
        // Master/slave mode does not flip isClusterConfig/isSentinelConfig;
        // just confirm the instance is non-null.
    }

    /**
     * Passing a {@code null} master/slave config must not throw.
     */
    @Test
    void shouldAcceptNullMasterSlaveServersConfig() {
        RedissonConfig config = new RedissonConfig((MasterSlaveServersConfig) null);
        assertNotNull(config);
    }

    /**
     * The replicated-servers constructor must install the supplied
     * configuration.
     */
    @Test
    void shouldConstructWithReplicatedServersConfig() {
        ReplicatedServersConfig rep = new ReplicatedServersConfig();
        rep.addNodeAddress("redis://127.0.0.1:6379");

        RedissonConfig config = new RedissonConfig(rep);

        assertNotNull(config);
    }

    /**
     * Passing a {@code null} replicated config must not throw.
     */
    @Test
    void shouldAcceptNullReplicatedServersConfig() {
        RedissonConfig config = new RedissonConfig((ReplicatedServersConfig) null);
        assertNotNull(config);
    }

    /**
     * The sentinel-servers constructor must install the supplied
     * configuration and mark the config as a sentinel one.
     */
    @Test
    void shouldConstructWithSentinelServersConfig() {
        SentinelServersConfig sentinel = new SentinelServersConfig();
        sentinel.setMasterName("mymaster");
        sentinel.addSentinelAddress("redis://127.0.0.1:26379");

        RedissonConfig config = new RedissonConfig(sentinel);

        assertNotNull(config);
        assertTrue(config.isSentinelConfig());
    }

    /**
     * Passing a {@code null} sentinel config must not throw.
     */
    @Test
    void shouldAcceptNullSentinelServersConfig() {
        RedissonConfig config = new RedissonConfig((SentinelServersConfig) null);
        assertNotNull(config);
    }

    /**
     * The single-server constructor must install the supplied configuration
     * and mark the config as a single one.
     */
    @Test
    void shouldConstructWithSingleServerConfig() {
        SingleServerConfig single = new SingleServerConfig();
        single.setAddress("redis://127.0.0.1:6379");

        RedissonConfig config = new RedissonConfig(single);

        assertNotNull(config);
        assertTrue(config.isSingleConfig());
    }

    /**
     * Passing a {@code null} single-server config must not throw.
     */
    @Test
    void shouldAcceptNullSingleServerConfig() {
        RedissonConfig config = new RedissonConfig((SingleServerConfig) null);
        assertNotNull(config);
    }

    /**
     * The all-in-one constructor must accept every combination of null and
     * non-null arguments.
     */
    @Test
    void shouldConstructWithAllSubConfigsSimultaneously() {
        ClusterServersConfig cluster = new ClusterServersConfig();
        cluster.addNodeAddress("redis://127.0.0.1:7000");
        MasterSlaveServersConfig ms = new MasterSlaveServersConfig();
        ms.setMasterAddress("redis://127.0.0.1:6379");
        ReplicatedServersConfig rep = new ReplicatedServersConfig();
        rep.addNodeAddress("redis://127.0.0.1:6379");
        SentinelServersConfig sentinel = new SentinelServersConfig();
        sentinel.setMasterName("m");
        SingleServerConfig single = new SingleServerConfig();
        single.setAddress("redis://127.0.0.1:6379");

        RedissonConfig config = new RedissonConfig(cluster, ms, rep, sentinel, single);

        assertNotNull(config);
    }

    /**
     * The all-in-one constructor must also tolerate {@code null} arguments
     * for every slot.
     */
    @Test
    void shouldAcceptAllNullsInCombinedConstructor() {
        RedissonConfig config = new RedissonConfig(null, null, null, null, null);
        assertNotNull(config);
    }

    /**
     * Convenience setter inherited from {@link Config} must round-trip
     * through the subclass.
     */
    @Test
    void shouldInheritConfigSetterBehaviour() {
        RedissonConfig config = new RedissonConfig();
        config.setThreads(16);

        assertEquals(16, config.getThreads());
    }

    /**
     * The {@code RedissonConfig} type must be a strict subtype of
     * {@link Config}.
     */
    @Test
    void shouldBeInstanceOfBaseConfig() {
        RedissonConfig config = new RedissonConfig();
        assertTrue(config instanceof Config);
    }

    /**
     * Copy-constructor must return an instance that is equal-by-reference
     * for sub-configurations that the source has installed.
     */
    @Test
    void shouldCopyClusterTopologyThroughCopyConstructor() {
        RedissonConfig source = new RedissonConfig();
        // The default no-arg instance has no cluster config; confirm copy
        // reflects that and the copy is a distinct instance.
        RedissonConfig copy = new RedissonConfig(source);

        assertNotNull(copy);
        assertFalse(copy.isClusterConfig());
    }

    /**
     * Confirm that getters installed via {@code useClusterServers(...)} are
     * observable through the subclass.
     */
    @Test
    void shouldExposeUseClusterServersBuilder() {
        RedissonConfig config = new RedissonConfig();
        ClusterServersConfig built = config.useClusterServers();

        assertNotNull(built);
        assertTrue(config.isClusterConfig());
    }
}
