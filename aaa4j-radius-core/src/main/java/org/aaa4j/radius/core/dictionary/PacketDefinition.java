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

package org.aaa4j.radius.core.dictionary;

import org.aaa4j.radius.core.attribute.Attribute;
import org.aaa4j.radius.core.packet.Packet;

import java.util.List;
import java.util.Objects;

/**
 * A dictionary packet definition. The packet definition contains the 1-byte packet code ({@link #getCode()}), a
 * human-readable name ({@link #getName()}), a class that extends {@link Packet} ({@link #getPacketClass()}) and a
 * factory (see {@link Factory}) object for instantiating a concrete packet object ({@link #getFactory()}).
 */
public final class PacketDefinition {

    private final int code;

    private final String name;

    private final Class<? extends Packet> packetClass;

    private final Factory<? extends Packet> factory;

    /**
     * Constructs a packet definition given the packet parameters.
     *
     * @param code the packet code (integer in range [0, 255])
     * @param name the packet name
     * @param packetClass the packet class
     * @param factory the packet instance factory
     */
    public <P extends Packet> PacketDefinition(int code, String name, Class<P> packetClass, Factory<P> factory) {
        if (code < 0 || code > 255) {
            throw new IllegalArgumentException("Code must be in range [0, 255]");
        }

        this.code = code;
        this.name = Objects.requireNonNull(name);
        this.packetClass = Objects.requireNonNull(packetClass);
        this.factory = Objects.requireNonNull(factory);
    }

    /**
     * Gets the packet code.
     *
     * @return the packet code
     */
    public int getCode() {
        return code;
    }

    /**
     * Gets the packet name.
     *
     * @return the packet name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the packet class.
     *
     * @return the packet class
     */
    public Class<? extends Packet> getPacketClass() {
        return packetClass;
    }

    /**
     * Gets the packet instance factory.
     *
     * @return the packet instance factory
     */
    public Factory<? extends Packet> getFactory() {
        return factory;
    }

    /**
     * Packet factory that returns a concrete instance of {@link Packet}.
     *
     * @param <P> the packet type that's constructed
     */
    @FunctionalInterface
    public interface Factory<P extends Packet> {

        /**
         * Builds a concrete instance of {@link Packet}.
         *
         * @param code the packet code (integer in range [0, 255])
         * @param attributes the received attributes
         * @param receivedFields the received fields
         *
         * @return an instance of {@link Packet} or its subtypes with the provided attributes and received fields)
         */
        P build(int code, List<Attribute<?>> attributes, Packet.ReceivedFields receivedFields);

    }

}
