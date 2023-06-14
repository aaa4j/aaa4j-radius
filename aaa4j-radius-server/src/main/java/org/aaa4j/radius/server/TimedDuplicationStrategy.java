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

import org.aaa4j.radius.core.packet.Packet;
import org.aaa4j.radius.server.DuplicationStrategy.Result.State;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * An implementation of {@link DuplicationStrategy} with a configurable time-to-live value for the cached responses.
 */
public final class TimedDuplicationStrategy implements DuplicationStrategy {

    private final Map<CacheKey, CacheValue> cacheMap;

    private final long ttlMillis;

    public TimedDuplicationStrategy(Duration ttlDuration) {
        this.cacheMap = new LinkedHashMap<>();
        this.ttlMillis = ttlDuration.toMillis();
    }

    @Override
    public synchronized Result handleRequest(InetSocketAddress clientAddress, Packet requestPacket) {
        long currentEpochMillis = Instant.now().toEpochMilli();

        // Remove the expired cache entries
        Iterator<Map.Entry<CacheKey, CacheValue>> cacheMapIterator = cacheMap.entrySet().iterator();

        while (cacheMapIterator.hasNext()) {
            Map.Entry<CacheKey, CacheValue> entry = cacheMapIterator.next();

            if (entry.getValue().insertionEpochMillis + ttlMillis < currentEpochMillis) {
                cacheMapIterator.remove();
            }
            else {
                // If this element isn't expired then neither will the subsequent elements (since oldest first)
                break;
            }
        }

        CacheKey cacheKey = new CacheKey(clientAddress, requestPacket.getReceivedFields().getIdentifier());

        if (!cacheMap.containsKey(cacheKey)) {
            // It's a new, unseen request; add it to the cache
            cacheMap.put(cacheKey, new CacheValue(currentEpochMillis, requestPacket));

            return new Result(State.NEW_REQUEST, null);
        }

        CacheValue cacheValue = cacheMap.get(cacheKey);

        if (!Arrays.equals(cacheValue.requestPacket.getReceivedFields().getAuthenticator(),
                requestPacket.getReceivedFields().getAuthenticator()))
        {
            // If the request authenticator field is different from what we have in the cache then we must clear it
            cacheMap.replace(cacheKey, new CacheValue(currentEpochMillis, requestPacket));
        }

        if (cacheValue.responsePacket != null) {
            return new Result(State.CACHED_RESPONSE, cacheValue.responsePacket);
        }
        else {
            return new Result(State.IN_PROGRESS_REQUEST, null);
        }
    }

    @Override
    public synchronized void handleResponse(InetSocketAddress clientAddress, Packet requestPacket,
                                            Packet responsePacket)
    {
        CacheKey cacheKey = new CacheKey(clientAddress, requestPacket.getReceivedFields().getIdentifier());

        if (!cacheMap.containsKey(cacheKey)) {
            return;
        }

        CacheValue cacheValue = cacheMap.get(cacheKey);

        if (!Arrays.equals(cacheValue.requestPacket.getReceivedFields().getAuthenticator(),
                requestPacket.getReceivedFields().getAuthenticator()))
        {
            // Don't save to the cache if this request was replaced by another with a different authenticator field
            return;
        }

        cacheValue.responsePacket = responsePacket;
    }

    @Override
    public synchronized void unhandleRequest(InetSocketAddress clientAddress, Packet requestPacket) {
        CacheKey cacheKey = new CacheKey(clientAddress, requestPacket.getReceivedFields().getIdentifier());

        if (!cacheMap.containsKey(cacheKey)) {
            return;
        }

        CacheValue cacheValue = cacheMap.get(cacheKey);

        if (!Arrays.equals(cacheValue.requestPacket.getReceivedFields().getAuthenticator(),
                requestPacket.getReceivedFields().getAuthenticator()))
        {
            // Don't modify the cache if this request was replaced by another with a different authenticator field
            return;
        }

        cacheMap.remove(cacheKey);
    }

    private static class CacheKey {

        private final InetSocketAddress clientAddress;

        private final int identifier;

        private CacheKey(InetSocketAddress clientAddress, int identifier) {
            this.clientAddress = clientAddress;
            this.identifier = identifier;
        }

        @Override
        public int hashCode() {
            return Objects.hash(clientAddress, identifier);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            CacheKey cacheKey = (CacheKey) obj;

            return Objects.equals(clientAddress, cacheKey.clientAddress) && identifier == cacheKey.identifier;
        }

    }

    private static class CacheValue {

        private final long insertionEpochMillis;

        private final Packet requestPacket;

        private Packet responsePacket;

        private CacheValue(long insertionEpochMillis, Packet requestPacket) {
            this.insertionEpochMillis = insertionEpochMillis;
            this.requestPacket = requestPacket;
        }

    }

}
