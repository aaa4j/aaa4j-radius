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

package org.aaa4j.radius.core.dictionary.dictionaries;

import org.aaa4j.radius.core.attribute.AttributeType;
import org.aaa4j.radius.core.attribute.ConcatAttribute;
import org.aaa4j.radius.core.attribute.ConcatData;
import org.aaa4j.radius.core.attribute.ExtendedAttribute;
import org.aaa4j.radius.core.attribute.ExtendedData;
import org.aaa4j.radius.core.attribute.IntegerData;
import org.aaa4j.radius.core.attribute.Ipv4AddrData;
import org.aaa4j.radius.core.attribute.Ipv4PrefixData;
import org.aaa4j.radius.core.attribute.Ipv6AddrData;
import org.aaa4j.radius.core.attribute.Ipv6PrefixData;
import org.aaa4j.radius.core.attribute.LongExtendedAttribute;
import org.aaa4j.radius.core.attribute.LongExtendedData;
import org.aaa4j.radius.core.attribute.StandardAttribute;
import org.aaa4j.radius.core.attribute.StringData;
import org.aaa4j.radius.core.attribute.TextData;
import org.aaa4j.radius.core.attribute.TlvData;
import org.aaa4j.radius.core.attribute.UserPasswordDataCodec;
import org.aaa4j.radius.core.attribute.VsaData;
import org.aaa4j.radius.core.attribute.attributes.EapMessage;
import org.aaa4j.radius.core.attribute.attributes.ExtendedAttribute1;
import org.aaa4j.radius.core.attribute.attributes.ExtendedAttribute2;
import org.aaa4j.radius.core.attribute.attributes.ExtendedAttribute3;
import org.aaa4j.radius.core.attribute.attributes.ExtendedAttribute4;
import org.aaa4j.radius.core.attribute.attributes.ExtendedAttribute5;
import org.aaa4j.radius.core.attribute.attributes.ExtendedAttribute6;
import org.aaa4j.radius.core.attribute.attributes.FilterId;
import org.aaa4j.radius.core.attribute.attributes.FragStatus;
import org.aaa4j.radius.core.attribute.attributes.IpPortForwardingMap;
import org.aaa4j.radius.core.attribute.attributes.IpPortLimitInfo;
import org.aaa4j.radius.core.attribute.attributes.IpPortRange;
import org.aaa4j.radius.core.attribute.attributes.Ipv66rdConfiguration;
import org.aaa4j.radius.core.attribute.attributes.MessageAuthenticator;
import org.aaa4j.radius.core.attribute.attributes.NasIdentifier;
import org.aaa4j.radius.core.attribute.attributes.NasIpAddress;
import org.aaa4j.radius.core.attribute.attributes.NasPort;
import org.aaa4j.radius.core.attribute.attributes.ProxyState;
import org.aaa4j.radius.core.attribute.attributes.SamlAssertion;
import org.aaa4j.radius.core.attribute.attributes.Softwire46Configuration;
import org.aaa4j.radius.core.attribute.attributes.Softwire46Multicast;
import org.aaa4j.radius.core.attribute.attributes.Softwire46Priority;
import org.aaa4j.radius.core.attribute.attributes.State;
import org.aaa4j.radius.core.attribute.attributes.UserName;
import org.aaa4j.radius.core.attribute.attributes.UserPassword;
import org.aaa4j.radius.core.attribute.attributes.VendorSpecific;
import org.aaa4j.radius.core.dictionary.AttributeDefinition;
import org.aaa4j.radius.core.dictionary.Dictionary;
import org.aaa4j.radius.core.dictionary.PacketDefinition;
import org.aaa4j.radius.core.dictionary.TlvDefinition;
import org.aaa4j.radius.core.packet.packets.AccessAccept;
import org.aaa4j.radius.core.packet.packets.AccessChallenge;
import org.aaa4j.radius.core.packet.packets.AccessReject;
import org.aaa4j.radius.core.packet.packets.AccessRequest;
import org.aaa4j.radius.core.packet.packets.AccountingRequest;
import org.aaa4j.radius.core.packet.packets.AccountingResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Standard dictionary containing definitions for all the standard packets defined in
 * {@link org.aaa4j.radius.core.packet.packets} and all standard attributes defined in
 * {@link org.aaa4j.radius.core.attribute.attributes}.
 */
public final class StandardDictionary implements Dictionary {

    static private final PacketDefinition[] packetDefinitions = new PacketDefinition[256];

    static private final Map<AttributeType, AttributeDefinition> attributeDefinitionsMap = new HashMap<>();

    static private final Map<AttributeType, TlvDefinition> tlvDefinitionsMap = new HashMap<>();

    static {
        packetDefinitions[AccessRequest.CODE] = new PacketDefinition(AccessRequest.CODE,
                AccessRequest.NAME, AccessRequest.class,
                (code, attributes, receivedFields) -> new AccessRequest(attributes, receivedFields));

        packetDefinitions[AccessAccept.CODE] = new PacketDefinition(AccessAccept.CODE,
                AccessAccept.NAME, AccessAccept.class,
                (code, attributes, receivedFields) -> new AccessAccept(attributes, receivedFields));

        packetDefinitions[AccessReject.CODE] = new PacketDefinition(AccessReject.CODE,
                AccessReject.NAME, AccessReject.class,
                (code, attributes, receivedFields) -> new AccessReject(attributes, receivedFields));

        packetDefinitions[AccountingRequest.CODE] = new PacketDefinition(AccountingRequest.CODE,
                AccountingRequest.NAME, AccountingRequest.class,
                (code, attributes, receivedFields) -> new AccountingRequest(attributes, receivedFields));

        packetDefinitions[AccountingResponse.CODE] = new PacketDefinition(AccountingResponse.CODE,
                AccountingResponse.NAME, AccountingResponse.class,
                (code, attributes, receivedFields) -> new AccountingResponse(attributes, receivedFields));

        packetDefinitions[AccessChallenge.CODE] = new PacketDefinition(AccessChallenge.CODE,
                AccessChallenge.NAME, AccessChallenge.class,
                (code, attributes, receivedFields) -> new AccessChallenge(attributes, receivedFields));
    }

    static {
        attributeDefinitionsMap.put(UserName.TYPE,
                new AttributeDefinition(
                        UserName.TYPE,
                        UserName.NAME,
                        UserName.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new UserName(data))));

        attributeDefinitionsMap.put(UserPassword.TYPE,
                new AttributeDefinition(
                        UserPassword.TYPE,
                        UserPassword.NAME,
                        UserPassword.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                new UserPasswordDataCodec<>(StringData.Codec.INSTANCE),
                                (type, data) -> new UserPassword(data))));

        attributeDefinitionsMap.put(NasIpAddress.TYPE,
                new AttributeDefinition(
                        NasIpAddress.TYPE,
                        NasIpAddress.NAME,
                        NasIpAddress.class,
                        Ipv4AddrData.class,
                        new StandardAttribute.Codec<>(
                                Ipv4AddrData.Codec.INSTANCE,
                                (type, data) -> new NasIpAddress(data))));

        attributeDefinitionsMap.put(NasPort.TYPE,
                new AttributeDefinition(
                        NasPort.TYPE,
                        NasPort.NAME,
                        NasPort.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new NasPort(data))));

        attributeDefinitionsMap.put(FilterId.TYPE,
                new AttributeDefinition(
                        FilterId.TYPE,
                        FilterId.NAME,
                        FilterId.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new FilterId(data))));

        attributeDefinitionsMap.put(State.TYPE,
                new AttributeDefinition(
                        State.TYPE,
                        State.NAME,
                        State.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new State(data))));

        attributeDefinitionsMap.put(VendorSpecific.TYPE,
                new AttributeDefinition(
                        VendorSpecific.TYPE,
                        VendorSpecific.NAME,
                        VendorSpecific.class,
                        VsaData.class,
                        new StandardAttribute.Codec<>(
                                VsaData.Codec.INSTANCE,
                                (type, data) -> new VendorSpecific(data))));

        attributeDefinitionsMap.put(NasIdentifier.TYPE,
                new AttributeDefinition(
                        NasIdentifier.TYPE,
                        NasIdentifier.NAME,
                        NasIdentifier.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new NasIdentifier(data))));

        attributeDefinitionsMap.put(ProxyState.TYPE,
                new AttributeDefinition(
                        ProxyState.TYPE,
                        ProxyState.NAME,
                        ProxyState.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new ProxyState(data))));

        attributeDefinitionsMap.put(EapMessage.TYPE,
                new AttributeDefinition(
                        EapMessage.TYPE,
                        EapMessage.NAME,
                        EapMessage.class,
                        ConcatData.class,
                        new ConcatAttribute.Codec(
                                (type, data) -> new EapMessage(data))));

        attributeDefinitionsMap.put(MessageAuthenticator.TYPE,
                new AttributeDefinition(
                        MessageAuthenticator.TYPE,
                        MessageAuthenticator.NAME,
                        MessageAuthenticator.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE, (type, data) -> new MessageAuthenticator(data))));

        attributeDefinitionsMap.put(Ipv66rdConfiguration.TYPE,
                new AttributeDefinition(
                        Ipv66rdConfiguration.TYPE,
                        Ipv66rdConfiguration.NAME,
                        Ipv66rdConfiguration.class,
                        TlvData.class,
                        new StandardAttribute.Codec<>(
                                TlvData.Codec.INSTANCE, (type, data) -> new Ipv66rdConfiguration(data))));

        attributeDefinitionsMap.put(ExtendedAttribute1.TYPE,
                new AttributeDefinition(
                        ExtendedAttribute1.TYPE,
                        ExtendedAttribute1.NAME,
                        ExtendedAttribute1.class,
                        ExtendedData.class,
                        new StandardAttribute.Codec<>(
                                ExtendedData.Codec.INSTANCE, (type, data) -> new ExtendedAttribute1(data))));

        attributeDefinitionsMap.put(FragStatus.TYPE,
                new AttributeDefinition(
                        FragStatus.TYPE,
                        FragStatus.NAME,
                        FragStatus.class,
                        IntegerData.class,
                        new ExtendedAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE, (type, extendedType, data) -> new FragStatus(data))));

        attributeDefinitionsMap.put(IpPortLimitInfo.TYPE,
                new AttributeDefinition(
                        IpPortLimitInfo.TYPE,
                        IpPortLimitInfo.NAME,
                        IpPortLimitInfo.class,
                        TlvData.class,
                        new ExtendedAttribute.Codec<>(
                                TlvData.Codec.INSTANCE, (type, extendedType, data) -> new IpPortLimitInfo(data))));

        attributeDefinitionsMap.put(IpPortRange.TYPE,
                new AttributeDefinition(
                        IpPortRange.TYPE,
                        IpPortRange.NAME,
                        IpPortRange.class,
                        TlvData.class,
                        new ExtendedAttribute.Codec<>(
                                TlvData.Codec.INSTANCE, (type, extendedType, data) -> new IpPortRange(data))));

        attributeDefinitionsMap.put(IpPortForwardingMap.TYPE,
                new AttributeDefinition(
                        IpPortForwardingMap.TYPE,
                        IpPortForwardingMap.NAME,
                        IpPortForwardingMap.class,
                        TlvData.class,
                        new ExtendedAttribute.Codec<>(
                                TlvData.Codec.INSTANCE, (type, extendedType, data) -> new IpPortForwardingMap(data))));

        attributeDefinitionsMap.put(Softwire46Configuration.TYPE,
                new AttributeDefinition(
                        Softwire46Configuration.TYPE,
                        Softwire46Configuration.NAME,
                        Softwire46Configuration.class,
                        TlvData.class,
                        new ExtendedAttribute.Codec<>(
                                TlvData.Codec.INSTANCE,
                                (type, extendedType, data) -> new Softwire46Configuration(data))));

        attributeDefinitionsMap.put(Softwire46Priority.TYPE,
                new AttributeDefinition(
                        Softwire46Priority.TYPE,
                        Softwire46Priority.NAME,
                        Softwire46Priority.class,
                        TlvData.class,
                        new ExtendedAttribute.Codec<>(
                                TlvData.Codec.INSTANCE, (type, extendedType, data) -> new Softwire46Priority(data))));

        attributeDefinitionsMap.put(Softwire46Multicast.TYPE,
                new AttributeDefinition(
                        Softwire46Multicast.TYPE,
                        Softwire46Multicast.NAME,
                        Softwire46Multicast.class,
                        TlvData.class,
                        new ExtendedAttribute.Codec<>(
                                TlvData.Codec.INSTANCE, (type, extendedType, data) -> new Softwire46Multicast(data))));

        attributeDefinitionsMap.put(ExtendedAttribute2.TYPE,
                new AttributeDefinition(
                        ExtendedAttribute2.TYPE,
                        ExtendedAttribute2.NAME,
                        ExtendedAttribute2.class,
                        ExtendedData.class,
                        new StandardAttribute.Codec<>(
                                ExtendedData.Codec.INSTANCE, (type, data) -> new ExtendedAttribute2(data))));

        attributeDefinitionsMap.put(ExtendedAttribute3.TYPE,
                new AttributeDefinition(
                        ExtendedAttribute3.TYPE,
                        ExtendedAttribute3.NAME,
                        ExtendedAttribute3.class,
                        ExtendedData.class,
                        new StandardAttribute.Codec<>(
                                ExtendedData.Codec.INSTANCE, (type, data) -> new ExtendedAttribute3(data))));

        attributeDefinitionsMap.put(ExtendedAttribute4.TYPE,
                new AttributeDefinition(
                        ExtendedAttribute4.TYPE,
                        ExtendedAttribute4.NAME,
                        ExtendedAttribute4.class,
                        ExtendedData.class,
                        new StandardAttribute.Codec<>(
                                ExtendedData.Codec.INSTANCE, (type, data) -> new ExtendedAttribute4(data))));

        attributeDefinitionsMap.put(ExtendedAttribute5.TYPE,
                new AttributeDefinition(
                        ExtendedAttribute5.TYPE,
                        ExtendedAttribute5.NAME,
                        ExtendedAttribute5.class,
                        LongExtendedData.class,
                        new StandardAttribute.Codec<>(
                                LongExtendedData.Codec.INSTANCE, (type, data) -> new ExtendedAttribute5(data))));

        attributeDefinitionsMap.put(SamlAssertion.TYPE,
                new AttributeDefinition(
                        SamlAssertion.TYPE,
                        SamlAssertion.NAME,
                        SamlAssertion.class,
                        TextData.class,
                        new LongExtendedAttribute.Codec<>(
                                TextData.Codec.INSTANCE, (type, extendedType, data) -> new SamlAssertion(data))));

        attributeDefinitionsMap.put(ExtendedAttribute6.TYPE,
                new AttributeDefinition(
                        ExtendedAttribute6.TYPE,
                        ExtendedAttribute6.NAME,
                        ExtendedAttribute6.class,
                        LongExtendedData.class,
                        new StandardAttribute.Codec<>(
                                LongExtendedData.Codec.INSTANCE, (type, data) -> new ExtendedAttribute6(data))));
    }

    static {
        tlvDefinitionsMap.put(new AttributeType(173, 1),
                new TlvDefinition(
                        new AttributeType(173, 1),
                        "IPv4MaskLen",
                        IntegerData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(173, 2),
                new TlvDefinition(
                        new AttributeType(173, 2),
                        "6rdPrefixLen",
                        Ipv6PrefixData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(173, 3),
                new TlvDefinition(
                        new AttributeType(173, 3),
                        "6rdBRIPv4Address",
                        Ipv4AddrData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 5, 1),
                new TlvDefinition(
                        new AttributeType(241, 5, 1),
                        "IP-Port-Type",
                        IntegerData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 5, 2),
                new TlvDefinition(
                        new AttributeType(241, 5, 2),
                        "IP-Port-Limit",
                        IntegerData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 5, 3),
                new TlvDefinition(
                        new AttributeType(241, 5, 3),
                        "IP-Port-Ext-IPv4-Addr",
                        Ipv4AddrData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 6, 1),
                new TlvDefinition(
                        new AttributeType(241, 6, 1),
                        "IP-Port-Type",
                        IntegerData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 6, 3),
                new TlvDefinition(
                        new AttributeType(241, 6, 3),
                        "IP-Port-Ext-IPv4-Addr",
                        Ipv4AddrData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 6, 8),
                new TlvDefinition(
                        new AttributeType(241, 6, 8),
                        "IP-Port-Alloc",
                        IntegerData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 6, 9),
                new TlvDefinition(
                        new AttributeType(241, 6, 9),
                        "IP-Port-Range-Start",
                        IntegerData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 6, 10),
                new TlvDefinition(
                        new AttributeType(241, 6, 10),
                        "IP-Port-Range-End",
                        IntegerData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 6, 11),
                new TlvDefinition(
                        new AttributeType(241, 6, 11),
                        "IP-Port-Local-Id",
                        StringData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 7, 1),
                new TlvDefinition(
                        new AttributeType(241, 7, 1),
                        "IP-Port-Type",
                        IntegerData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 7, 3),
                new TlvDefinition(
                        new AttributeType(241, 7, 3),
                        "IP-Port-Ext-IPv4-Addr",
                        Ipv4AddrData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 7, 4),
                new TlvDefinition(
                        new AttributeType(241, 7, 4),
                        "IP-Port-Int-IPv4-Addr",
                        Ipv4AddrData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 7, 5),
                new TlvDefinition(
                        new AttributeType(241, 7, 5),
                        "IP-Port-Int-IPv6-Addr",
                        Ipv6AddrData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 7, 6),
                new TlvDefinition(
                        new AttributeType(241, 7, 6),
                        "IP-Port-Int-Port",
                        IntegerData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 7, 7),
                new TlvDefinition(
                        new AttributeType(241, 7, 7),
                        "IP-Port-Ext-Port",
                        IntegerData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 7, 11),
                new TlvDefinition(
                        new AttributeType(241, 7, 11),
                        "IP-Port-Local-Id",
                        StringData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 1),
                new TlvDefinition(
                        new AttributeType(241, 9, 1),
                        "Softwire46-MAP-E",
                        TlvData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 1, 4),
                new TlvDefinition(
                        new AttributeType(241, 9, 1, 4),
                        "Softwire46-Rule",
                        TlvData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 1, 4, 10),
                new TlvDefinition(
                        new AttributeType(241, 9, 1, 4, 10),
                        "Rule-IPv6-Prefix",
                        Ipv6PrefixData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 1, 4, 11),
                new TlvDefinition(
                        new AttributeType(241, 9, 1, 4, 11),
                        "Rule-IPv4-Prefix",
                        Ipv4PrefixData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 1, 4, 12),
                new TlvDefinition(
                        new AttributeType(241, 9, 1, 4, 12),
                        "EA-Length",
                        IntegerData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 1, 5),
                new TlvDefinition(
                        new AttributeType(241, 9, 1, 5),
                        "Softwire46-Rule",
                        TlvData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 1, 5, 10),
                new TlvDefinition(
                        new AttributeType(241, 9, 1, 5, 10),
                        "Rule-IPv6-Prefix",
                        Ipv6PrefixData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 1, 5, 11),
                new TlvDefinition(
                        new AttributeType(241, 9, 1, 5, 11),
                        "Rule-IPv4-Prefix",
                        Ipv4PrefixData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 1, 5, 12),
                new TlvDefinition(
                        new AttributeType(241, 9, 1, 5, 12),
                        "EA-Length",
                        IntegerData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 1, 6),
                new TlvDefinition(
                        new AttributeType(241, 9, 1, 6),
                        "Softwire46-BR",
                        Ipv6AddrData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 1, 9),
                new TlvDefinition(
                        new AttributeType(241, 9, 1, 9),
                        "Softwire46-PORTPARAMS",
                        TlvData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 1, 9, 15),
                new TlvDefinition(
                        new AttributeType(241, 9, 1, 9, 15),
                        "PSID-Offset",
                        IntegerData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 1, 9, 16),
                new TlvDefinition(
                        new AttributeType(241, 9, 1, 9, 16),
                        "PSID-Len",
                        IntegerData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 1, 9, 17),
                new TlvDefinition(
                        new AttributeType(241, 9, 1, 9, 17),
                        "PSID",
                        IntegerData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 2),
                new TlvDefinition(
                        new AttributeType(241, 9, 2),
                        "Softwire46-MAP-T",
                        TlvData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 2, 4),
                new TlvDefinition(
                        new AttributeType(241, 9, 2, 4),
                        "Softwire46-Rule",
                        TlvData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 2, 4, 10),
                new TlvDefinition(
                        new AttributeType(241, 9, 2, 4, 10),
                        "Rule-IPv6-Prefix",
                        Ipv6PrefixData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 2, 4, 11),
                new TlvDefinition(
                        new AttributeType(241, 9, 2, 4, 11),
                        "Rule-IPv4-Prefix",
                        Ipv4PrefixData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 2, 4, 12),
                new TlvDefinition(
                        new AttributeType(241, 9, 2, 4, 12),
                        "EA-Length",
                        IntegerData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 2, 5),
                new TlvDefinition(
                        new AttributeType(241, 9, 2, 5),
                        "Softwire46-Rule",
                        TlvData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 2, 5, 10),
                new TlvDefinition(
                        new AttributeType(241, 9, 2, 5, 10),
                        "Rule-IPv6-Prefix",
                        Ipv6PrefixData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 2, 5, 11),
                new TlvDefinition(
                        new AttributeType(241, 9, 2, 5, 11),
                        "Rule-IPv4-Prefix",
                        Ipv4PrefixData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 2, 5, 12),
                new TlvDefinition(
                        new AttributeType(241, 9, 2, 5, 12),
                        "EA-Length",
                        IntegerData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 2, 7),
                new TlvDefinition(
                        new AttributeType(241, 9, 2, 7),
                        "Softwire46-DMR",
                        Ipv6PrefixData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 2, 9),
                new TlvDefinition(
                        new AttributeType(241, 9, 2, 9),
                        "Softwire46-PORTPARAMS",
                        TlvData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 2, 9, 15),
                new TlvDefinition(
                        new AttributeType(241, 9, 2, 9, 15),
                        "PSID-Offset",
                        IntegerData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 2, 9, 16),
                new TlvDefinition(
                        new AttributeType(241, 9, 2, 9, 16),
                        "PSID-Len",
                        IntegerData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 2, 9, 17),
                new TlvDefinition(
                        new AttributeType(241, 9, 2, 9, 17),
                        "PSID",
                        IntegerData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 3),
                new TlvDefinition(
                        new AttributeType(241, 9, 3),
                        "Softwire46-Lightweight-4over6",
                        TlvData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 3, 6),
                new TlvDefinition(
                        new AttributeType(241, 9, 3, 6),
                        "Softwire46-BR",
                        Ipv6AddrData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 3, 8),
                new TlvDefinition(
                        new AttributeType(241, 9, 3, 8),
                        "Softwire46-V4V6Bind",
                        TlvData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 3, 8, 13),
                new TlvDefinition(
                        new AttributeType(241, 9, 3, 8, 13),
                        "IPv4-Address",
                        Ipv4AddrData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 3, 8, 14),
                new TlvDefinition(
                        new AttributeType(241, 9, 3, 8, 14),
                        "Bind-IPv6-Prefix",
                        Ipv4AddrData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 3, 9),
                new TlvDefinition(
                        new AttributeType(241, 9, 3, 9),
                        "Softwire46-PORTPARAMS",
                        TlvData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 3, 9, 15),
                new TlvDefinition(
                        new AttributeType(241, 9, 3, 9, 15),
                        "PSID-Offset",
                        IntegerData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 3, 9, 16),
                new TlvDefinition(
                        new AttributeType(241, 9, 2, 9, 16),
                        "PSID-Len",
                        IntegerData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 9, 3, 9, 17),
                new TlvDefinition(
                        new AttributeType(241, 9, 2, 9, 17),
                        "PSID",
                        IntegerData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 10, 18),
                new TlvDefinition(
                        new AttributeType(241, 10, 18),
                        "Softwire46-Option-Code",
                        IntegerData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 11, 19),
                new TlvDefinition(
                        new AttributeType(241, 11, 19),
                        "ASM-Prefix64",
                        Ipv6PrefixData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 11, 20),
                new TlvDefinition(
                        new AttributeType(241, 11, 20),
                        "SSM-Prefix64",
                        Ipv6PrefixData.Codec.INSTANCE));

        tlvDefinitionsMap.put(new AttributeType(241, 11, 21),
                new TlvDefinition(
                        new AttributeType(241, 11, 21),
                        "U-Prefix64",
                        Ipv6PrefixData.Codec.INSTANCE));
    }

    @Override
    public PacketDefinition getPacketDefinition(int code) {
        return packetDefinitions[code];
    }

    @Override
    public AttributeDefinition getAttributeDefinition(AttributeType type) {
        Objects.requireNonNull(type);

        return attributeDefinitionsMap.getOrDefault(type, null);
    }

    @Override
    public TlvDefinition getTlvDefinition(AttributeType type) {
        Objects.requireNonNull(type);

        return tlvDefinitionsMap.getOrDefault(type, null);
    }

}
