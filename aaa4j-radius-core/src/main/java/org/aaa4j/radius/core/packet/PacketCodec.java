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
import org.aaa4j.radius.core.attribute.AttributeType;
import org.aaa4j.radius.core.attribute.CodecContext;
import org.aaa4j.radius.core.attribute.ContainerData;
import org.aaa4j.radius.core.attribute.RawAttribute;
import org.aaa4j.radius.core.dictionary.AttributeDefinition;
import org.aaa4j.radius.core.dictionary.Dictionary;
import org.aaa4j.radius.core.dictionary.PacketDefinition;
import org.aaa4j.radius.core.util.RandomProvider;
import org.aaa4j.radius.core.util.SecureRandomProvider;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/**
 * Packet encoder and decoder. The packet codec uses a {@link Dictionary} to encode and decode packets and containing
 * attributes.
 */
public final class PacketCodec {

    private static final int MESSAGE_AUTHENTICATOR_TYPE = 80;

    private final Dictionary dictionary;

    private final PacketIdGenerator packetIdGenerator;

    private final RandomProvider randomProvider;

    /**
     * Constructs a packet codec with the given dictionary.
     *
     * @param dictionary the dictionary to use
     */
    public PacketCodec(Dictionary dictionary) {
        this(dictionary, new SecureRandomProvider(), new IncrementingPacketIdGenerator(0));
    }

    /**
     * Constructs a packet codec with the given dictionary and random provider.
     *
     * @param dictionary the dictionary to use
     * @param randomProvider the random provider to use
     */
    public PacketCodec(Dictionary dictionary, RandomProvider randomProvider) {
        this(dictionary, randomProvider, new IncrementingPacketIdGenerator(0));
    }

    /**
     * Constructs a packet codec with the given dictionary, random provider, and packet identifier generator.
     * 
     * @param dictionary the dictionary to use
     * @param randomProvider the random provider to use
     * @param packetIdGenerator the packet identifier generator to use
     */
    public PacketCodec(Dictionary dictionary, RandomProvider randomProvider, PacketIdGenerator packetIdGenerator) {
        this.dictionary = Objects.requireNonNull(dictionary);
        this.randomProvider = Objects.requireNonNull(randomProvider);
        this.packetIdGenerator = Objects.requireNonNull(packetIdGenerator);
    }

    private static MessageDigest getMd5Instance() {
        try {
            return MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            // Shouldn't happen since every JRE Is required to support MD5
            throw new AssertionError(e);
        }
    }

    private static Mac getHmacMd5Instance() {
        try {
            return Mac.getInstance("HmacMD5");
        }
        catch (NoSuchAlgorithmException e) {
            // Shouldn't happen since every JRE Is required to support HmacMD5
            throw new AssertionError(e);
        }
    }

    /**
     * Encodes a request packet into bytes.
     *
     * @param request the request packet to encode
     * @param secret the shared secret
     * 
     * @return byte array of the encoded request packet
     * 
     * @throws PacketCodecException if there's a problem encoding the packet
     */
    public byte[] encodeRequest(Packet request, byte[] secret, byte[] requestAuthenticator)
            throws PacketCodecException {
        CodecContext codecContext = new CodecContext(secret, requestAuthenticator, randomProvider);

        List<RawAttribute> rawAttributes = encodeAttributes(codecContext, request.getAttributes());

        int attributesLength = 0;

        for (RawAttribute rawAttribute : rawAttributes) {
            attributesLength = attributesLength + 2 + rawAttribute.getData().length();
        }

        int packetLength = 20 + attributesLength;

        byte[] bytes = new byte[packetLength];

        int identifier = packetIdGenerator.nextId();

        bytes[0] = (byte) (request.getCode() & 0xff);
        bytes[1] = (byte) (identifier & 0xff);
        bytes[2] = (byte) ((packetLength & 0xff00) >>> 8);
        bytes[3] = (byte) (packetLength & 0xff);

        System.arraycopy(requestAuthenticator, 0, bytes, 4, requestAuthenticator.length);

        int position = 20;

        int messageAuthenticatorPosition = -1;

        for (RawAttribute rawAttribute : rawAttributes) {
            byte[] attrData = rawAttribute.getData().getValue();

            bytes[position] = (byte) (rawAttribute.getType().head() & 0xff);
            bytes[position + 1] = (byte) ((byte) (attrData.length + 2) & 0xff);

            System.arraycopy(attrData, 0, bytes, position + 2, attrData.length);

            if (rawAttribute.getType().head() == MESSAGE_AUTHENTICATOR_TYPE && attrData.length == 16) {
                messageAuthenticatorPosition = position;
            }

            position = position + 2 + attrData.length;
        }

        if (messageAuthenticatorPosition != -1) {
            Arrays.fill(bytes, messageAuthenticatorPosition + 2, messageAuthenticatorPosition + 16, (byte) 0x00);

            Mac hmacMd5 = getHmacMd5Instance();

            try {
                hmacMd5.init(new SecretKeySpec(secret, "HmacMD5"));
            }
            catch (InvalidKeyException e) {
                throw new PacketCodecException(e);
            }

            hmacMd5.update(bytes);
            byte[] messageAuthenticator = hmacMd5.doFinal();

            System.arraycopy(messageAuthenticator, 0, bytes, messageAuthenticatorPosition + 2, 16);
        }

        return bytes;
    }

    /**
     * Encodes a response packet into bytes.
     *
     * @param response the response packet to encode
     * @param secret the shared secret
     * @param requestId the request identifier
     * @param requestAuthenticator the request authenticator
     * 
     * @return byte array of the encoded response packet
     * 
     * @throws PacketCodecException if there's a problem encoding the packet
     */
    public byte[] encodeResponse(Packet response, byte[] secret, int requestId, byte[] requestAuthenticator)
            throws PacketCodecException {
        CodecContext codecContext = new CodecContext(secret, requestAuthenticator, randomProvider);

        List<RawAttribute> rawAttributes = encodeAttributes(codecContext, response.getAttributes());

        int attributesLength = 0;

        for (RawAttribute rawAttribute : rawAttributes) {
            attributesLength = attributesLength + 2 + rawAttribute.getData().length();
        }

        int packetLength = 20 + attributesLength;

        byte[] bytes = new byte[packetLength];

        bytes[0] = (byte) (response.getCode() & 0xff);
        bytes[1] = (byte) (requestId & 0xff);
        bytes[2] = (byte) ((packetLength & 0xff00) >>> 8);
        bytes[3] = (byte) (packetLength & 0xff);

        int position = 20;

        int messageAuthenticatorPosition = -1;

        for (RawAttribute rawAttribute : rawAttributes) {
            byte[] attrData = rawAttribute.getData().getValue();

            bytes[position] = (byte) (rawAttribute.getType().head() & 0xff);
            bytes[position + 1] = (byte) ((byte) (attrData.length + 2) & 0xff);

            System.arraycopy(attrData, 0, bytes, position + 2, attrData.length);

            if (rawAttribute.getType().head() == MESSAGE_AUTHENTICATOR_TYPE && attrData.length == 16) {
                messageAuthenticatorPosition = position;
            }

            position = position + 2 + attrData.length;
        }

        // Temporary place the request authenticator into the response authenticator spot
        System.arraycopy(requestAuthenticator, 0, bytes, 4, 16);

        if (messageAuthenticatorPosition != -1) {
            Arrays.fill(bytes, messageAuthenticatorPosition + 2, messageAuthenticatorPosition + 18, (byte) 0x00);

            Mac hmacMd5 = getHmacMd5Instance();

            try {
                hmacMd5.init(new SecretKeySpec(secret, "HmacMD5"));
            }
            catch (InvalidKeyException e) {
                throw new PacketCodecException(e);
            }

            hmacMd5.update(bytes);
            byte[] messageAuthenticator = hmacMd5.doFinal();

            System.arraycopy(messageAuthenticator, 0, bytes, messageAuthenticatorPosition + 2, 16);
        }

        // Calculate the response authenticator
        MessageDigest md5 = getMd5Instance();
        md5.update(bytes);
        md5.update(secret);
        byte[] responseAuthenticator = md5.digest();

        System.arraycopy(responseAuthenticator, 0, bytes, 4, 16);

        return bytes;
    }

    /**
     * Decodes a request packet from bytes into a {@link Packet} object.
     *
     * @param bytes byte array of the encoded request packet
     * @param secret the shared secret
     * 
     * @return a packet object
     * 
     * @throws PacketCodecException if the packet is malformed or there's a problem decoding the request packet
     */
    public Packet decodeRequest(byte[] bytes, byte[] secret) throws PacketCodecException {
        if (bytes.length < 20) {
            throw new PacketCodecException("Invalid packet length: " + bytes.length);
        }

        int length = ((bytes[2] & 0xff) << 8) | (bytes[3] & 0xff);

        if (bytes.length != length) {
            throw new PacketCodecException("Packet length " + length + " doesn't match actual length " + bytes.length);
        }

        int code = bytes[0] & 0xff;
        int identifier = bytes[1] & 0xff;

        byte[] authenticatorBytes = new byte[16];
        System.arraycopy(bytes, 4, authenticatorBytes, 0, 16);

        CodecContext codecContext = new CodecContext(secret, authenticatorBytes, randomProvider);

        List<RawAttribute> rawAttributes = new ArrayList<>();

        int position = 20;

        int messageAuthenticatorPosition = -1;
        byte[] messageAuthenticator = null;

        while (position < length) {
            if (position + 2 > length) {
                throw new PacketCodecException("Malformed attribute at position " + position);
            }

            int attrType = bytes[position] & 0xff;
            int attrLength = bytes[position + 1] & 0xff;

            if (attrLength < 2) {
                throw new PacketCodecException("Malformed attribute at position " + position);
            }

            if (position + attrLength > length) {
                throw new PacketCodecException("Malformed attribute at position " + position);
            }

            byte[] attrData = new byte[attrLength - 2];

            System.arraycopy(bytes, position + 2, attrData, 0, attrData.length);

            RawAttribute rawAttribute = new RawAttribute(attrType, attrData);
            rawAttributes.add(rawAttribute);

            if (attrType == MESSAGE_AUTHENTICATOR_TYPE && attrData.length == 16) {
                messageAuthenticatorPosition = position;
                messageAuthenticator = attrData;
            }

            position = position + attrLength;
        }

        if (messageAuthenticatorPosition != -1) {
            byte[] messageAuthenticatorWorkingBytes = Arrays.copyOf(bytes, bytes.length);
            Arrays.fill(messageAuthenticatorWorkingBytes, messageAuthenticatorPosition + 2,
                    messageAuthenticatorPosition + 18, (byte) 0x00);

            Mac hmacMd5 = getHmacMd5Instance();

            try {
                hmacMd5.init(new SecretKeySpec(secret, "HmacMD5"));
            }
            catch (InvalidKeyException e) {
                throw new PacketCodecException(e);
            }

            hmacMd5.update(messageAuthenticatorWorkingBytes);
            byte[] calculatedMessageAuthenticator = hmacMd5.doFinal();

            if (!Arrays.equals(messageAuthenticator, calculatedMessageAuthenticator)) {
                throw new PacketCodecException("Packet contains an invalid message authenticator");
            }
        }

        List<Attribute<?>> attributes = decodeAttributes(codecContext, rawAttributes);

        Packet.ReceivedFields receivedFields = new Packet.ReceivedFields(identifier, authenticatorBytes);

        PacketDefinition packetDefinition = dictionary.getPacketDefinition(code);

        if (packetDefinition != null) {
            return packetDefinition.getFactory().build(code, attributes, receivedFields);
        }

        return new Packet(code, attributes, receivedFields);
    }

    /**
     * Decodes a response packet from bytes into a {@link Packet} object.
     *
     * @param bytes byte array of the encoded request packet
     * @param secret the shared secret
     * @param requestAuthenticator the request authenticator
     * 
     * @return a packet object
     * 
     * @throws PacketCodecException if the packet is malformed or there's a problem decoding the request packet
     */
    public Packet decodeResponse(byte[] bytes, byte[] secret, byte[] requestAuthenticator) throws PacketCodecException {
        if (bytes.length < 20) {
            throw new PacketCodecException("Invalid packet length: " + bytes.length);
        }

        int length = ((bytes[2] & 0xff) << 8) | (bytes[3] & 0xff);

        if (bytes.length != length) {
            throw new PacketCodecException("Packet length " + length + " doesn't match actual length " + bytes.length);
        }

        int code = bytes[0] & 0xff;
        int identifier = bytes[1] & 0xff;

        byte[] authenticatorBytes = new byte[16];
        System.arraycopy(bytes, 4, authenticatorBytes, 0, 16);

        byte[] authenticatorWorkingBytes = Arrays.copyOf(bytes, bytes.length);

        System.arraycopy(requestAuthenticator, 0, authenticatorWorkingBytes, 4, 16);

        MessageDigest md5 = getMd5Instance();
        md5.update(authenticatorWorkingBytes);
        md5.update(secret);
        byte[] calculatedAuthenticatorBytes = md5.digest();

        if (!Arrays.equals(authenticatorBytes, calculatedAuthenticatorBytes)) {
            throw new PacketCodecException("Invalid response packet authenticator");
        }

        CodecContext codecContext = new CodecContext(secret, requestAuthenticator, randomProvider);

        List<RawAttribute> rawAttributes = new ArrayList<>();

        int position = 20;

        int messageAuthenticatorPosition = -1;
        byte[] messageAuthenticator = null;

        while (position < length) {
            if (position + 2 > length) {
                throw new PacketCodecException("Malformed attribute at position " + position);
            }

            int attrType = bytes[position] & 0xff;
            int attrLength = bytes[position + 1] & 0xff;

            if (attrLength < 2) {
                throw new PacketCodecException("Malformed attribute at position " + position);
            }

            if (position + attrLength > length) {
                throw new PacketCodecException("Malformed attribute at position " + position);
            }

            byte[] attrData = new byte[attrLength - 2];
            System.arraycopy(bytes, position + 2, attrData, 0, attrData.length);

            RawAttribute rawAttribute = new RawAttribute(attrType, attrData);
            rawAttributes.add(rawAttribute);

            if (attrType == MESSAGE_AUTHENTICATOR_TYPE && attrData.length == 16) {
                messageAuthenticatorPosition = position;
                messageAuthenticator = attrData;
            }

            position = position + attrLength;
        }

        if (messageAuthenticatorPosition != -1) {
            byte[] messageAuthenticatorWorkingBytes = Arrays.copyOf(bytes, bytes.length);
            System.arraycopy(requestAuthenticator, 0, messageAuthenticatorWorkingBytes, 4, 16);

            Arrays.fill(messageAuthenticatorWorkingBytes, messageAuthenticatorPosition + 2,
                    messageAuthenticatorPosition + 18, (byte) 0x00);

            Mac hmacMd5 = getHmacMd5Instance();

            try {
                hmacMd5.init(new SecretKeySpec(secret, "HmacMD5"));
            }
            catch (InvalidKeyException e) {
                throw new PacketCodecException(e);
            }

            hmacMd5.update(messageAuthenticatorWorkingBytes);
            byte[] calculatedMessageAuthenticator = hmacMd5.doFinal();

            if (!Arrays.equals(messageAuthenticator, calculatedMessageAuthenticator)) {
                throw new PacketCodecException("Packet contains an invalid message authenticator");
            }
        }

        List<Attribute<?>> attributes = decodeAttributes(codecContext, rawAttributes);

        Packet.ReceivedFields receivedFields = new Packet.ReceivedFields(identifier, authenticatorBytes);

        PacketDefinition packetDefinition = dictionary.getPacketDefinition(code);

        if (packetDefinition != null) {
            return packetDefinition.getFactory().build(code, attributes, receivedFields);
        }

        return new Packet(code, attributes, receivedFields);
    }

    private List<RawAttribute> encodeAttributes(CodecContext codecContext, List<Attribute<?>> attributes)
            throws PacketCodecException {
        List<RawAttribute> rawAttributes = new ArrayList<>();

        Deque<Attribute<?>> attributeStack = new ArrayDeque<>(attributes);

        while (attributeStack.size() > 0) {
            Attribute<?> nextAttribute = attributeStack.peekFirst();

            if (nextAttribute instanceof RawAttribute) {
                rawAttributes.add((RawAttribute) attributeStack.removeFirst());

                continue;
            }

            AttributeDefinition attributeDefinition = dictionary.getAttributeDefinition(nextAttribute.getType());

            if (attributeDefinition == null) {
                throw new PacketCodecException("Unable to encode attribute with identifier " + nextAttribute.getType());
            }

            attributeDefinition.getAttributeCodec().encode(codecContext, attributeStack);
        }

        return rawAttributes;
    }

    private List<Attribute<?>> decodeAttributes(CodecContext codecContext, List<RawAttribute> rawAttributes) {
        Deque<Attribute<?>> attributeStack = new ArrayDeque<>(rawAttributes);
        List<Attribute<?>> attributes = new ArrayList<>();

        while (attributeStack.size() > 0) {
            RawAttribute rawAttribute = (RawAttribute) attributeStack.peekFirst();
            AttributeType nextType = rawAttribute.getType();

            AttributeDefinition attributeDefinition = dictionary.getAttributeDefinition(nextType);

            if (attributeDefinition != null) {
                attributeDefinition.getAttributeCodec().decode(codecContext, attributeStack);
            }

            attributes.add(attributeStack.removeFirst());
        }

        attributeStack.addAll(attributes);
        attributes.clear();

        while (attributeStack.size() > 0) {
            Attribute<?> nextAttribute = attributeStack.peekFirst();

            if (nextAttribute instanceof RawAttribute) {
                attributes.add(attributeStack.removeFirst());

                continue;
            }

            if (nextAttribute.getData() instanceof ContainerData) {
                ContainerData containerData = (ContainerData) nextAttribute.getData();
                AttributeType nextType = nextAttribute.getType().with(containerData.getContainedType());

                AttributeDefinition attributeDefinition = dictionary.getAttributeDefinition(nextType);

                int numComplete = attributeDefinition.getAttributeCodec().decode(codecContext, attributeStack);

                for (int i = 0; i < numComplete; i++) {
                    attributes.add(attributeStack.removeFirst());
                }

                continue;
            }

            attributes.add(attributeStack.removeFirst());
        }

        return attributes;
    }

}
