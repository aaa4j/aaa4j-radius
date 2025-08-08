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

package org.aaa4j.radius.server.servers;

import org.aaa4j.radius.core.dictionary.Dictionary;
import org.aaa4j.radius.core.packet.Packet;
import org.aaa4j.radius.server.DeduplicationCache;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * A RADIUS server using TCP as the underlying transport layer.
 *
 * <p>
 * Build an instance of {@link TcpRadiusServer} by using a {@link Builder} object retrieved from {@link #newBuilder()}.
 * </p>
 */
public final class TcpRadiusServer extends AbstractRadiusServer {

    private static final String THREAD_NAME_PREFIX = "aaa4j-radius-server-tcp";

    private static final AtomicLong SERVER_ID_COUNTER = new AtomicLong(1);

    private final Supplier<DeduplicationCache> deduplicationCacheSupplier;

    private final Handler handler;

    private final Set<Socket> clientSockets = ConcurrentHashMap.newKeySet();

    private ServerSocket serverSocket;

    private TcpRadiusServer(Builder builder) {
        super(builder, String.format("%s-%d", THREAD_NAME_PREFIX, SERVER_ID_COUNTER.getAndIncrement()));

        this.deduplicationCacheSupplier = builder.deduplicationCacheSupplier == null
                ? DEFAULT_DEDUPLICATION_CACHE_SUPPLIER : builder.deduplicationCacheSupplier;
        this.handler = Objects.requireNonNull(builder.handler);
    }

    /**
     * Creates a new builder object.
     *
     * @return a new builder object
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    void close() {
        try {
            startCountDownLatch.await();
        }
        catch (InterruptedException ignored) {
            // Ignored
        }

        if (serverSocket != null) {
            try {
                serverSocket.close();
            }
            catch (IOException ignored) {
                // Ignored
            }
        }

        if (isInternalExecutor) {
            ((ExecutorService) executor).shutdown();
        }
    }

    @Override
    void listen() {
        try (ServerSocket serverSocket = new ServerSocket()) {
            this.serverSocket = serverSocket;

            serverSocket.bind(bindAddress);

            startCountDownLatch.countDown();

            while (isRunning) {
                // Block and wait for a new client
                Socket clientSocket = serverSocket.accept();

                clientSockets.add(clientSocket);

                // Handle the client
                executor.execute(() -> handleClient(clientSocket));
            }
        }
        catch (Throwable e) {
            if (isRunning) {
                try {
                    handler.handleException(e);
                }
                catch (Exception ignored) {
                    // Ignored
                }
            }
        }
        finally {
            startCountDownLatch.countDown();

            for (Socket clientSocket : clientSockets) {
                try {
                    clientSocket.close();
                }
                catch (IOException ignored) {
                    // Ignored
                }

                clientSockets.remove(clientSocket);
            }

            if (isRunning) {
                close();

                isRunning = false;
            }

            stopCountDownLatch.countDown();
        }
    }

    private void handleClient(Socket clientSocket) {
        try {
            byte[] secret = handler.handleClient((InetSocketAddress) clientSocket.getRemoteSocketAddress());

            if (secret == null) {
                // The handler doesn't want to handle requests from this client
                clientSocket.close();

                return;
            }

            DeduplicationCache deduplicationCache = deduplicationCacheSupplier.get();

            InputStream inputStream = clientSocket.getInputStream();
            byte[] buffer = new byte[MAX_PACKET_SIZE];
            int position = 0;

            readLoop: while (isRunning) {
                // Block and wait for more bytes
                int bytesRead = inputStream.read(buffer, position, buffer.length - position);

                if (bytesRead == -1) {
                    // The client gracefully closed the connection
                    clientSocket.close();

                    break readLoop;
                }

                position += bytesRead;

                while (position >= 4) {
                    // We have enough bytes to get the packet length, so let's see if we can get a packet
                    int packetLength = (short) ((buffer[2] << 8) | (buffer[3] & 0xff));

                    if (packetLength < 20 || packetLength > MAX_PACKET_SIZE) {
                        // The packet is going to be an invalid length, so close the connection
                        clientSocket.close();

                        deduplicationCache.clear();

                        break readLoop;
                    }

                    if (position >= packetLength) {
                        // We have enough bytes for a full packet
                        byte[] requestPacketBytes = new byte[packetLength];
                        System.arraycopy(buffer, 0, requestPacketBytes, 0, packetLength);

                        // Handle the request
                        executor.execute(() ->
                                handleRequest(clientSocket, deduplicationCache, secret, requestPacketBytes));

                        // Shift the bytes in the buffer
                        System.arraycopy(buffer, packetLength, buffer, 0, position - packetLength);
                        position = position - packetLength;
                    }
                    else {
                        // We need to read more bytes to get a full packet
                        break;
                    }
                }
            }
        }
        catch (Throwable e) {
            if (isRunning) {
                try {
                    handler.handleException(e);
                }
                catch (Exception ignored) {
                    // Ignored
                }
            }
        }
        finally {
            try {
                clientSocket.close();
            }
            catch (IOException ignored) {
                // Ignored
            }

            clientSockets.remove(clientSocket);

            try {
                handler.handleClientDisconnect((InetSocketAddress) clientSocket.getRemoteSocketAddress());
            }
            catch (Exception ignored) {
                // Ignored
            }
        }
    }

    private void handleRequest(Socket clientSocket, DeduplicationCache deduplicationCache, byte[] secret,
                               byte[] requestPacketBytes)
    {
        try {
            InetSocketAddress clientSocketAddress = (InetSocketAddress) clientSocket.getRemoteSocketAddress();

            // Perform deduplication and get a response from the handler
            byte[] responsePacketBytes = processRequest(clientSocketAddress, deduplicationCache, secret,
                    requestPacketBytes, (Packet packet) -> handler.handlePacket(clientSocketAddress, packet));

            if (responsePacketBytes != null) {
                synchronized (clientSocket.getOutputStream()) {
                    clientSocket.getOutputStream().write(responsePacketBytes);
                    clientSocket.getOutputStream().flush();
                }
            }
        }
        catch (Throwable e) {
            try {
                handler.handleException(e);
            }
            catch (Exception ignored) {
                // Ignored
            }

            try {
                clientSocket.close();
            }
            catch (IOException ignored) {
                // Ignore
            }
        }
    }

    /**
     * A TCP RADIUS server handler.
     */
    public interface Handler {

        /**
         * Handles a new client connection. Returns the shared secret to use for the client.
         *
         * @param clientSocketAddress the client socket address
         *
         * @return the shared secret to use for the client or {@code null} if the client should be disconnected
         */
        byte[] handleClient(InetSocketAddress clientSocketAddress);

        /**
         * Handles a client disconnection.
         *
         * @param clientSocketAddress the client socket address
         */
        default void handleClientDisconnect(InetSocketAddress clientSocketAddress) {
        }

        /**
         * Handles an incoming RADIUS request packet.
         *
         * @param clientSocketAddress the client socket address
         * @param requestPacket the request packet
         *
         * @return the response packet or {@code null} if no response should be sent back
         */
        Packet handlePacket(InetSocketAddress clientSocketAddress, Packet requestPacket);

        /**
         * Handles thrown exceptions.
         *
         * @param throwable the exception that was thrown
         */
        default void handleException(Throwable throwable) {
            throwable.printStackTrace();
        }

    }

    /**
     * Builder for {@link TcpRadiusServer}s.
     */
    public final static class Builder extends AbstractRadiusServer.Builder<TcpRadiusServer, Builder> {

        Handler handler;

        /**
         * {@inheritDoc}
         */
        public Builder bindAddress(InetSocketAddress bindAddress) {
            return super.bindAddress(bindAddress);
        }

        /**
         * {@inheritDoc}
         */
        public Builder executor(Executor executor) {
            return super.executor(executor);
        }

        /**
         * {@inheritDoc}
         */
        public Builder dictionary(Dictionary dictionary) {
            return super.dictionary(dictionary);
        }

        /**
         * {@inheritDoc}
         */
        public Builder deduplicationCacheSupplier(Supplier<DeduplicationCache> deduplicationCacheSupplier) {
            return super.deduplicationCacheSupplier(deduplicationCacheSupplier);
        }

        /**
         * Sets the server handler. Required.
         *
         * @param handler the handler to use
         *
         * @return this builder
         */
        public Builder handler(Handler handler) {
            this.handler = handler;

            return this;
        }

        /**
         * Returns a new {@link TcpRadiusServer} built using the builder's options.
         *
         * @return a new {@link TcpRadiusServer}
         */
        public TcpRadiusServer build() {
            return new TcpRadiusServer(this);
        }

    }

}
