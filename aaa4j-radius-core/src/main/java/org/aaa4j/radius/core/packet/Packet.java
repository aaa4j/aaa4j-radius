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

package org.aaa4j.radius.core.packet;

import org.aaa4j.radius.core.attribute.Attribute;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A packet. A packet may be a request to a server or a response to a client. A packet will contain extra fields
 * ({@link #getReceivedFields()} when it has been received.
 */
public class Packet {

    private final int code;

    private final List<Attribute<?>> attributes;

    private final ReceivedFields receivedFields;

    /**
     * Constructs a packet with no attributes.
     *
     * @param code the packet code
     */
    public Packet(int code) {
        this(code, Collections.emptyList());
    }

    /**
     * Constructs a packet with attributes.
     *
     * @param code (integer in range [0, 255])
     * @param attributes list of attributes
     */
    public Packet(int code, List<Attribute<?>> attributes) {
        if (code < 0 || code > 255) {
            throw new IllegalArgumentException("Code must be in range [0, 255]");
        }

        this.code = code;
        this.attributes = Objects.requireNonNull(attributes);
        this.receivedFields = null;
    }

    /**
     * Constructs a packet with attributes and received fields from an incoming packet.
     *
     * @param code (integer in range [0, 255])
     * @param attributes list of attributes
     * @param receivedFields ReceivedFields object
     */
    public Packet(int code, List<Attribute<?>> attributes, ReceivedFields receivedFields) {
        if (code < 0 || code > 255) {
            throw new IllegalArgumentException("Code must be in range [0, 255]");
        }

        this.code = code;
        this.attributes = Objects.requireNonNull(attributes);
        this.receivedFields = Objects.requireNonNull(receivedFields);
    }

    /**
     * Gets the packet code.
     *
     * @return the packet code in range [0, 255]
     */
    public int getCode() {
        return code;
    }

    /**
     * Gets the list of attributes.
     *
     * @return list of attributes
     */
    public List<Attribute<?>> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    /**
     * Gets the first attribute of a specific attribute class.
     *
     * @param aClass attribute type class
     * @param <A> attribute type for {@code aClass} parameter
     * 
     * @return optional with the first attribute of the specified class (or empty optional)
     */
    public <A extends Attribute<?>> Optional<A> getAttribute(Class<A> aClass) {
        return attributes.stream()
                .filter(aClass::isInstance)
                .findFirst()
                .map(aClass::cast);
    }

    /**
     * Gets all the attributes of a specific attribute class.
     *
     * @param aClass attribute type class
     * @param <A> attribute type for {@code aClass} parameter
     * 
     * @return list of attributes of the given class
     */
    public <A extends Attribute<?>> List<A> getAttributes(Class<A> aClass) {
        return attributes.stream()
                .filter(aClass::isInstance)
                .map(aClass::cast)
                .collect(Collectors.toList());
    }

    /**
     * Gets the received fields.
     *
     * @return the received fields or null if this packet was not received (i.e., it will be transmitted).
     */
    public ReceivedFields getReceivedFields() {
        return receivedFields;
    }

    /**
     * Packet fields received from an incoming packet.
     */
    public static final class ReceivedFields {

        private final int identifier;

        private final byte[] authenticator;

        /**
         * Constructs received fields.
         *
         * @param identifier the incoming packet identifier (integer in range [0, 255])
         * @param authenticator the incoming packet authenticator (byte array of length 16)
         */
        public ReceivedFields(int identifier, byte[] authenticator) {
            if (identifier < 0 || identifier > 255) {
                throw new IllegalArgumentException("Identifier must be in range [0, 255]");
            }

            if (authenticator.length != 16) {
                throw new IllegalArgumentException("Authenticator length must be 16");
            }

            this.identifier = identifier;
            this.authenticator = Objects.requireNonNull(authenticator);
        }

        /**
         * Gets the incoming packet identifier.
         *
         * @return identifier integer in the range [0, 255]
         */
        public int getIdentifier() {
            return identifier;
        }

        /**
         * Gets the packet authenticator (request or response authenticator).
         *
         * @return authenticator byte array of length 16
         */
        public byte[] getAuthenticator() {
            return authenticator;
        }

    }

}
