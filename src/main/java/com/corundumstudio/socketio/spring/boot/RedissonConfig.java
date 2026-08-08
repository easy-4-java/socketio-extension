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

/**
 * Convenience subclass of Redisson's {@link Config} that exposes ergonomic
 * constructors for every supported server topology.
 *
 * <p>The class is a thin wrapper: each constructor delegates to the matching
 * {@code useClusterServers(...)}, {@code useMasterSlaveServers(...)},
 * {@code useReplicatedServers(...)}, {@code useSentinelServers(...)} or
 * {@code useSingleServer(...)} entry point of the parent class, or simply
 * copies an existing {@link Config} via the copy-constructor. The all-in-one
 * constructor accepts every sub-configuration at once and installs them via
 * the corresponding {@code setXxxConfig} method.</p>
 *
 * <p>Spring configuration code can therefore instantiate {@code RedissonConfig}
 * with whichever deployment topology is required without having to know the
 * internal setter names of {@link Config}.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see Config
 */
public class RedissonConfig extends Config {

    /**
     * Default no-argument constructor; produces an empty {@link Config} ready
     * to be populated via the {@code useXxxServers} builders.
     */
	public RedissonConfig() {
		super();
	}

    /**
     * Copy-constructor; clones every field of the supplied {@link Config}.
     *
     * @param oldConf the configuration to copy; may be {@code null} in which
     *                case the result behaves like the no-arg constructor.
     */
	public RedissonConfig(Config oldConf) {
		super(oldConf);
	}

    /**
     * Creates a config wired to a Redis cluster by calling
     * {@link Config#useClusterServers(ClusterServersConfig)}.
     *
     * @param clusterServersConfig the cluster topology to install; may be
     *                             {@code null}, in which case the cluster
     *                             builder is initialised empty.
     */
	public RedissonConfig(ClusterServersConfig clusterServersConfig) {
		super();
		useClusterServers(clusterServersConfig);
	}

    /**
     * Creates a config wired to a master/slave Redis topology via
     * {@link Config#useMasterSlaveServers(MasterSlaveServersConfig)}.
     *
     * @param masterSlaveServersConfig the master/slave topology to install;
     *                                 may be {@code null}.
     */
	public RedissonConfig(MasterSlaveServersConfig masterSlaveServersConfig) {
		super();
		useMasterSlaveServers(masterSlaveServersConfig);
	}

    /**
     * Creates a config wired to a replicated (AWS ElastiCache style) Redis
     * topology via {@link Config#useReplicatedServers(ReplicatedServersConfig)}.
     *
     * @param replicatedServersConfig the replicated topology to install; may
     *                                be {@code null}.
     */
	public RedissonConfig(ReplicatedServersConfig replicatedServersConfig) {
		super();
		useReplicatedServers(replicatedServersConfig);
	}

    /**
     * Creates a config wired to a Redis Sentinel topology via
     * {@link Config#useSentinelServers(SentinelServersConfig)}.
     *
     * @param sentinelServersConfig the sentinel topology to install; may be
     *                              {@code null}.
     */
	public RedissonConfig(SentinelServersConfig sentinelServersConfig) {
		super();
		useSentinelServers(sentinelServersConfig);
	}

    /**
     * Creates a config wired to a single-node Redis server via
     * {@link Config#useSingleServer(SingleServerConfig)}.
     *
     * @param singleServerConfig the single-server topology to install; may be
     *                           {@code null}.
     */
	public RedissonConfig(SingleServerConfig singleServerConfig) {
		super();
		useSingleServer(singleServerConfig);
	}

    /**
     * Creates a config that pre-installs every supported topology at once,
     * allowing the caller to decide at runtime which one to activate.
     *
     * @param clusterServersConfig      optional cluster topology.
     * @param masterSlaveServersConfig  optional master/slave topology.
     * @param replicatedServersConfig   optional replicated topology.
     * @param sentinelServersConfig     optional sentinel topology.
     * @param singleServerConfig        optional single-server topology.
     */
	public RedissonConfig(ClusterServersConfig clusterServersConfig,
			MasterSlaveServersConfig masterSlaveServersConfig,
			ReplicatedServersConfig replicatedServersConfig,
			SentinelServersConfig sentinelServersConfig,
			SingleServerConfig singleServerConfig) {
		super();
		setClusterServersConfig(clusterServersConfig);
		setMasterSlaveServersConfig(masterSlaveServersConfig);
		setReplicatedServersConfig(replicatedServersConfig);
		setSentinelServersConfig(sentinelServersConfig);
		setSingleServerConfig(singleServerConfig);
	}


}