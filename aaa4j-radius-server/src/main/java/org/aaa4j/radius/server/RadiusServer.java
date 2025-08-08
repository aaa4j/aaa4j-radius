/*
 * Copyright 2020 The AAA4J-RADIUS Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.aaa4j.radius.server;

/**
 * A RADIUS server accepts RADIUS requests and replies with RADIUS responses.
 */
public interface RadiusServer {

    /**
     * Starts the server. Blocks until the server has started or has failed to start. The server may only be started
     * once and may not be started after it has been stopped.
     *
     * @throws InterruptedException if the thread is interrupted while waiting for the server to start
     */
    void start() throws InterruptedException;

    /**
     * Stops the server. Calling this method on a stopped server is permitted and has no effect.
     *
     * @throws InterruptedException if the thread is interrupted while waiting for the server to stop
     *
     */
    void stop() throws InterruptedException;

    /**
     * Returns whether the server is running (i.e., is listening for incoming RADIUS packets).
     *
     * @return {@code true} if the server is running
     */
    boolean isRunning();

}
