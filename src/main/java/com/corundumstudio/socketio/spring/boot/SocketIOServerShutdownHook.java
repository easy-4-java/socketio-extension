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
package com.corundumstudio.socketio.spring.boot.hooks;

import com.corundumstudio.socketio.SocketIOServer;

/**
 * JVM shutdown hook that gracefully stops a {@link SocketIOServer} when the
 * enclosing process is terminating.
 *
 * <p>The hook is intentionally permissive: it swallows any exception thrown by
 * {@link SocketIOServer#stop()} so a failing teardown cannot prevent the JVM
 * from exiting cleanly. It is designed to be registered with
 * {@link Runtime#addShutdownHook(Thread)} from Spring lifecycle code or an
 * equivalent bootstrapper.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see SocketIOServer#stop()
 */
public class SocketIOServerShutdownHook extends Thread {

    /**
     * The Socket.IO server instance that will be stopped when this hook runs.
     */
	private SocketIOServer server;

    /**
     * Creates a new shutdown hook bound to the supplied server.
     *
     * @param server the Socket.IO server to stop; must not be {@code null} at
     *               the time {@link #run()} is invoked.
     */
	public SocketIOServerShutdownHook(SocketIOServer server) {
		this.server = server;
	}

    /**
     * Stops the bound {@link SocketIOServer}. Any exception raised by the
     * server's {@code stop()} method is intentionally suppressed so that a
     * failing teardown never blocks JVM shutdown.
     */
	@Override
	public void run() {
		try {
			server.stop();
		} catch (Exception e) {
		}
	}

}