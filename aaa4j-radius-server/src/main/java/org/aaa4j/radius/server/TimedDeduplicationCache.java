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

import org.aaa4j.radius.server.DeduplicationCache.Result.State;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * An implementation of {@link DeduplicationCache} with a configurable time-to-live value for the cached responses.
 * Caches all requests using the entire request packet bytes.
 */
public final class TimedDeduplicationCache implements DeduplicationCache {

    private final Map<CacheKey, CacheValue> cacheMap;

    private final long ttlMillis;

    public TimedDeduplicationCache(Duration ttlDuration) {
        this.cacheMap = new LinkedHashMap<>();
        this.ttlMillis = ttlDuration.toMillis();
    }

    @Override
    public synchronized Result handleRequest(InetSocketAddress clientAddress, byte[] requestPacketBytes) {
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

        CacheKey cacheKey = new CacheKey(clientAddress, requestPacketBytes);

        if (!cacheMap.containsKey(cacheKey)) {
            // It's a new, unseen request; add it to the cache
            cacheMap.put(cacheKey, new CacheValue(currentEpochMillis, requestPacketBytes));

            return new Result(State.NEW_REQUEST, null);
        }

        CacheValue cacheValue = cacheMap.get(cacheKey);

        if (cacheValue.responsePacketBytes != null) {
            return new Result(State.CACHED_RESPONSE, cacheValue.responsePacketBytes);
        }
        else {
            return new Result(State.IN_PROGRESS_REQUEST, null);
        }
    }

    @Override
    public synchronized void handleResponse(InetSocketAddress clientAddress, byte[] requestPacketBytes,
                                            byte[] responsePacketBytes)
    {
        CacheKey cacheKey = new CacheKey(clientAddress, requestPacketBytes);

        if (!cacheMap.containsKey(cacheKey)) {
            return;
        }

        CacheValue cacheValue = cacheMap.get(cacheKey);

        cacheValue.responsePacketBytes = responsePacketBytes;
    }

    @Override
    public synchronized void unhandleRequest(InetSocketAddress clientAddress, byte[] requestPacketBytes) {
        CacheKey cacheKey = new CacheKey(clientAddress, requestPacketBytes);

        if (!cacheMap.containsKey(cacheKey)) {
            return;
        }

        cacheMap.remove(cacheKey);
    }

    @Override
    public synchronized void clear() {
        cacheMap.clear();
    }

    private static class CacheKey {

        private final InetSocketAddress clientAddress;

        private final byte[] requestBytes;

        private CacheKey(InetSocketAddress clientAddress, byte[] requestBytes) {
            this.clientAddress = clientAddress;
            this.requestBytes = requestBytes;
        }

        @Override
        public int hashCode() {
            return Objects.hash(clientAddress, Arrays.hashCode(requestBytes));
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

            return Objects.equals(clientAddress, cacheKey.clientAddress)
                    && Arrays.equals(requestBytes, cacheKey.requestBytes);
        }

    }

    private static class CacheValue {

        private final long insertionEpochMillis;

        private final byte[] requestPacketBytes;

        private byte[] responsePacketBytes;

        private CacheValue(long insertionEpochMillis, byte[] requestPacketBytes) {
            this.insertionEpochMillis = insertionEpochMillis;
            this.requestPacketBytes = requestPacketBytes;
        }

    }

}
