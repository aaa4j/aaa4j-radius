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
import org.aaa4j.radius.core.attribute.EnumData;
import org.aaa4j.radius.core.attribute.EvsData;
import org.aaa4j.radius.core.attribute.ExtendedAttribute;
import org.aaa4j.radius.core.attribute.ExtendedData;
import org.aaa4j.radius.core.attribute.IfidData;
import org.aaa4j.radius.core.attribute.Integer64Data;
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
import org.aaa4j.radius.core.attribute.TimeData;
import org.aaa4j.radius.core.attribute.TlvData;
import org.aaa4j.radius.core.attribute.UserPasswordDataCodec;
import org.aaa4j.radius.core.attribute.VsaData;
import org.aaa4j.radius.core.attribute.attributes.AcctAuthentic;
import org.aaa4j.radius.core.attribute.attributes.AcctDelayTime;
import org.aaa4j.radius.core.attribute.attributes.AcctInputGigawords;
import org.aaa4j.radius.core.attribute.attributes.AcctInputOctets;
import org.aaa4j.radius.core.attribute.attributes.AcctInputPackets;
import org.aaa4j.radius.core.attribute.attributes.AcctInterimInterval;
import org.aaa4j.radius.core.attribute.attributes.AcctLinkCount;
import org.aaa4j.radius.core.attribute.attributes.AcctMultiSessionId;
import org.aaa4j.radius.core.attribute.attributes.AcctOutputGigawords;
import org.aaa4j.radius.core.attribute.attributes.AcctOutputOctets;
import org.aaa4j.radius.core.attribute.attributes.AcctOutputPackets;
import org.aaa4j.radius.core.attribute.attributes.AcctSessionId;
import org.aaa4j.radius.core.attribute.attributes.AcctSessionTime;
import org.aaa4j.radius.core.attribute.attributes.AcctStatusType;
import org.aaa4j.radius.core.attribute.attributes.AcctTerminateCause;
import org.aaa4j.radius.core.attribute.attributes.AcctTunnelConnection;
import org.aaa4j.radius.core.attribute.attributes.AcctTunnelPacketsLost;
import org.aaa4j.radius.core.attribute.attributes.AllowedCalledStationId;
import org.aaa4j.radius.core.attribute.attributes.ArapChallengeResponse;
import org.aaa4j.radius.core.attribute.attributes.ArapFeatures;
import org.aaa4j.radius.core.attribute.attributes.ArapPassword;
import org.aaa4j.radius.core.attribute.attributes.ArapSecurity;
import org.aaa4j.radius.core.attribute.attributes.ArapSecurityData;
import org.aaa4j.radius.core.attribute.attributes.ArapZoneAccess;
import org.aaa4j.radius.core.attribute.attributes.BasicLocationPolicyRules;
import org.aaa4j.radius.core.attribute.attributes.CallbackId;
import org.aaa4j.radius.core.attribute.attributes.CallbackNumber;
import org.aaa4j.radius.core.attribute.attributes.CalledStationId;
import org.aaa4j.radius.core.attribute.attributes.CallingStationId;
import org.aaa4j.radius.core.attribute.attributes.ChapChallenge;
import org.aaa4j.radius.core.attribute.attributes.ChapPassword;
import org.aaa4j.radius.core.attribute.attributes.Class;
import org.aaa4j.radius.core.attribute.attributes.ConfigurationToken;
import org.aaa4j.radius.core.attribute.attributes.ConnectInfo;
import org.aaa4j.radius.core.attribute.attributes.Cui;
import org.aaa4j.radius.core.attribute.attributes.DelegatedIpv6Prefix;
import org.aaa4j.radius.core.attribute.attributes.DelegatedIpv6PrefixPool;
import org.aaa4j.radius.core.attribute.attributes.DhcpV4Options;
import org.aaa4j.radius.core.attribute.attributes.DhcpV6Options;
import org.aaa4j.radius.core.attribute.attributes.DigestAkaAuts;
import org.aaa4j.radius.core.attribute.attributes.DigestAlgorithm;
import org.aaa4j.radius.core.attribute.attributes.DigestAuthParam;
import org.aaa4j.radius.core.attribute.attributes.DigestCnonce;
import org.aaa4j.radius.core.attribute.attributes.DigestDomain;
import org.aaa4j.radius.core.attribute.attributes.DigestEntityBodyHash;
import org.aaa4j.radius.core.attribute.attributes.DigestHa1;
import org.aaa4j.radius.core.attribute.attributes.DigestMethod;
import org.aaa4j.radius.core.attribute.attributes.DigestNextnonce;
import org.aaa4j.radius.core.attribute.attributes.DigestNonce;
import org.aaa4j.radius.core.attribute.attributes.DigestNonceCount;
import org.aaa4j.radius.core.attribute.attributes.DigestOpaque;
import org.aaa4j.radius.core.attribute.attributes.DigestQop;
import org.aaa4j.radius.core.attribute.attributes.DigestRealm;
import org.aaa4j.radius.core.attribute.attributes.DigestResponse;
import org.aaa4j.radius.core.attribute.attributes.DigestResponseAuth;
import org.aaa4j.radius.core.attribute.attributes.DigestStale;
import org.aaa4j.radius.core.attribute.attributes.DigestUri;
import org.aaa4j.radius.core.attribute.attributes.DigestUsername;
import org.aaa4j.radius.core.attribute.attributes.DnsServerIpv6Address;
import org.aaa4j.radius.core.attribute.attributes.DsLiteTunnelName;
import org.aaa4j.radius.core.attribute.attributes.EapKeyName;
import org.aaa4j.radius.core.attribute.attributes.EapLowerLayer;
import org.aaa4j.radius.core.attribute.attributes.EapMessage;
import org.aaa4j.radius.core.attribute.attributes.EapOLAnnouncement;
import org.aaa4j.radius.core.attribute.attributes.EapPeerId;
import org.aaa4j.radius.core.attribute.attributes.EapServerId;
import org.aaa4j.radius.core.attribute.attributes.EgressVlanName;
import org.aaa4j.radius.core.attribute.attributes.EgressVlanid;
import org.aaa4j.radius.core.attribute.attributes.ErrorCause;
import org.aaa4j.radius.core.attribute.attributes.EventTimestamp;
import org.aaa4j.radius.core.attribute.attributes.ExtendedAttribute1;
import org.aaa4j.radius.core.attribute.attributes.ExtendedAttribute2;
import org.aaa4j.radius.core.attribute.attributes.ExtendedAttribute3;
import org.aaa4j.radius.core.attribute.attributes.ExtendedAttribute4;
import org.aaa4j.radius.core.attribute.attributes.ExtendedAttribute5;
import org.aaa4j.radius.core.attribute.attributes.ExtendedAttribute6;
import org.aaa4j.radius.core.attribute.attributes.ExtendedLocationPolicyRules;
import org.aaa4j.radius.core.attribute.attributes.ExtendedVendorSpecific1;
import org.aaa4j.radius.core.attribute.attributes.ExtendedVendorSpecific2;
import org.aaa4j.radius.core.attribute.attributes.ExtendedVendorSpecific3;
import org.aaa4j.radius.core.attribute.attributes.ExtendedVendorSpecific4;
import org.aaa4j.radius.core.attribute.attributes.ExtendedVendorSpecific5;
import org.aaa4j.radius.core.attribute.attributes.ExtendedVendorSpecific6;
import org.aaa4j.radius.core.attribute.attributes.FilterId;
import org.aaa4j.radius.core.attribute.attributes.FragStatus;
import org.aaa4j.radius.core.attribute.attributes.FramedAppleTalkLink;
import org.aaa4j.radius.core.attribute.attributes.FramedAppleTalkNetwork;
import org.aaa4j.radius.core.attribute.attributes.FramedAppleTalkZone;
import org.aaa4j.radius.core.attribute.attributes.FramedCompression;
import org.aaa4j.radius.core.attribute.attributes.FramedInterfaceId;
import org.aaa4j.radius.core.attribute.attributes.FramedIpAddress;
import org.aaa4j.radius.core.attribute.attributes.FramedIpNetmask;
import org.aaa4j.radius.core.attribute.attributes.FramedIpv6Address;
import org.aaa4j.radius.core.attribute.attributes.FramedIpv6Pool;
import org.aaa4j.radius.core.attribute.attributes.FramedIpv6Prefix;
import org.aaa4j.radius.core.attribute.attributes.FramedIpv6Route;
import org.aaa4j.radius.core.attribute.attributes.FramedIpxNetwork;
import org.aaa4j.radius.core.attribute.attributes.FramedManagementProtocol;
import org.aaa4j.radius.core.attribute.attributes.FramedMtu;
import org.aaa4j.radius.core.attribute.attributes.FramedPool;
import org.aaa4j.radius.core.attribute.attributes.FramedProtocol;
import org.aaa4j.radius.core.attribute.attributes.FramedRoute;
import org.aaa4j.radius.core.attribute.attributes.FramedRouting;
import org.aaa4j.radius.core.attribute.attributes.GssAcceptorHostName;
import org.aaa4j.radius.core.attribute.attributes.GssAcceptorRealmName;
import org.aaa4j.radius.core.attribute.attributes.GssAcceptorServiceName;
import org.aaa4j.radius.core.attribute.attributes.GssAcceptorServiceSpecifics;
import org.aaa4j.radius.core.attribute.attributes.IdleTimeout;
import org.aaa4j.radius.core.attribute.attributes.IngressFilters;
import org.aaa4j.radius.core.attribute.attributes.IpPortForwardingMap;
import org.aaa4j.radius.core.attribute.attributes.IpPortLimitInfo;
import org.aaa4j.radius.core.attribute.attributes.IpPortRange;
import org.aaa4j.radius.core.attribute.attributes.Ipv66rdConfiguration;
import org.aaa4j.radius.core.attribute.attributes.LocationCapable;
import org.aaa4j.radius.core.attribute.attributes.LocationData;
import org.aaa4j.radius.core.attribute.attributes.LocationInformation;
import org.aaa4j.radius.core.attribute.attributes.LoginIpHost;
import org.aaa4j.radius.core.attribute.attributes.LoginIpv6Host;
import org.aaa4j.radius.core.attribute.attributes.LoginLatGroup;
import org.aaa4j.radius.core.attribute.attributes.LoginLatNode;
import org.aaa4j.radius.core.attribute.attributes.LoginLatPort;
import org.aaa4j.radius.core.attribute.attributes.LoginLatService;
import org.aaa4j.radius.core.attribute.attributes.LoginService;
import org.aaa4j.radius.core.attribute.attributes.LoginTcpPort;
import org.aaa4j.radius.core.attribute.attributes.ManagementPolicyId;
import org.aaa4j.radius.core.attribute.attributes.ManagementPrivilegeLevel;
import org.aaa4j.radius.core.attribute.attributes.ManagementTransportProtection;
import org.aaa4j.radius.core.attribute.attributes.MessageAuthenticator;
import org.aaa4j.radius.core.attribute.attributes.Mip6FeatureVector;
import org.aaa4j.radius.core.attribute.attributes.Mip6HomeLinkPrefix;
import org.aaa4j.radius.core.attribute.attributes.MobileNodeIdentifier;
import org.aaa4j.radius.core.attribute.attributes.MobilityDomainId;
import org.aaa4j.radius.core.attribute.attributes.NasFilterRule;
import org.aaa4j.radius.core.attribute.attributes.NasIdentifier;
import org.aaa4j.radius.core.attribute.attributes.NasIpAddress;
import org.aaa4j.radius.core.attribute.attributes.NasIpv6Address;
import org.aaa4j.radius.core.attribute.attributes.NasPort;
import org.aaa4j.radius.core.attribute.attributes.NasPortId;
import org.aaa4j.radius.core.attribute.attributes.NasPortType;
import org.aaa4j.radius.core.attribute.attributes.NetworkIdName;
import org.aaa4j.radius.core.attribute.attributes.OperatorName;
import org.aaa4j.radius.core.attribute.attributes.OperatorNasIdentifier;
import org.aaa4j.radius.core.attribute.attributes.OriginalPacketCode;
import org.aaa4j.radius.core.attribute.attributes.OriginatingLineInfo;
import org.aaa4j.radius.core.attribute.attributes.PasswordRetry;
import org.aaa4j.radius.core.attribute.attributes.PkmAuthKey;
import org.aaa4j.radius.core.attribute.attributes.PkmCaCert;
import org.aaa4j.radius.core.attribute.attributes.PkmConfigSettings;
import org.aaa4j.radius.core.attribute.attributes.PkmCryptosuiteList;
import org.aaa4j.radius.core.attribute.attributes.PkmSaDescriptor;
import org.aaa4j.radius.core.attribute.attributes.PkmSaid;
import org.aaa4j.radius.core.attribute.attributes.PkmSsCert;
import org.aaa4j.radius.core.attribute.attributes.Pmip6HomeDhcp4ServerAddress;
import org.aaa4j.radius.core.attribute.attributes.Pmip6HomeDhcp6ServerAddress;
import org.aaa4j.radius.core.attribute.attributes.Pmip6HomeHnPrefix;
import org.aaa4j.radius.core.attribute.attributes.Pmip6HomeInterfaceId;
import org.aaa4j.radius.core.attribute.attributes.Pmip6HomeIpv4Gateway;
import org.aaa4j.radius.core.attribute.attributes.Pmip6HomeIpv4HoA;
import org.aaa4j.radius.core.attribute.attributes.Pmip6HomeLmaIpv4Address;
import org.aaa4j.radius.core.attribute.attributes.Pmip6HomeLmaIpv6Address;
import org.aaa4j.radius.core.attribute.attributes.Pmip6VisitedDhcp4ServerAddress;
import org.aaa4j.radius.core.attribute.attributes.Pmip6VisitedDhcp6ServerAddress;
import org.aaa4j.radius.core.attribute.attributes.Pmip6VisitedHnPrefix;
import org.aaa4j.radius.core.attribute.attributes.Pmip6VisitedInterfaceId;
import org.aaa4j.radius.core.attribute.attributes.Pmip6VisitedIpv4Gateway;
import org.aaa4j.radius.core.attribute.attributes.Pmip6VisitedIpv4HoA;
import org.aaa4j.radius.core.attribute.attributes.Pmip6VisitedLmaIpv4Address;
import org.aaa4j.radius.core.attribute.attributes.Pmip6VisitedLmaIpv6Address;
import org.aaa4j.radius.core.attribute.attributes.PortLimit;
import org.aaa4j.radius.core.attribute.attributes.PreauthTimeout;
import org.aaa4j.radius.core.attribute.attributes.Prompt;
import org.aaa4j.radius.core.attribute.attributes.ProxyState;
import org.aaa4j.radius.core.attribute.attributes.ProxyStateLength;
import org.aaa4j.radius.core.attribute.attributes.ReplyMessage;
import org.aaa4j.radius.core.attribute.attributes.RequestedLocationInfo;
import org.aaa4j.radius.core.attribute.attributes.ResponseLength;
import org.aaa4j.radius.core.attribute.attributes.RouteIpv6Information;
import org.aaa4j.radius.core.attribute.attributes.SamlAssertion;
import org.aaa4j.radius.core.attribute.attributes.SamlProtocol;
import org.aaa4j.radius.core.attribute.attributes.ServiceSelection;
import org.aaa4j.radius.core.attribute.attributes.ServiceType;
import org.aaa4j.radius.core.attribute.attributes.SessionTimeout;
import org.aaa4j.radius.core.attribute.attributes.SipAor;
import org.aaa4j.radius.core.attribute.attributes.Softwire46Configuration;
import org.aaa4j.radius.core.attribute.attributes.Softwire46Multicast;
import org.aaa4j.radius.core.attribute.attributes.Softwire46Priority;
import org.aaa4j.radius.core.attribute.attributes.State;
import org.aaa4j.radius.core.attribute.attributes.StatefulIpv6AddressPool;
import org.aaa4j.radius.core.attribute.attributes.TerminationAction;
import org.aaa4j.radius.core.attribute.attributes.TunnelAssignmentId;
import org.aaa4j.radius.core.attribute.attributes.TunnelClientAuthId;
import org.aaa4j.radius.core.attribute.attributes.TunnelClientEndpoint;
import org.aaa4j.radius.core.attribute.attributes.TunnelMediumType;
import org.aaa4j.radius.core.attribute.attributes.TunnelPassword;
import org.aaa4j.radius.core.attribute.attributes.TunnelPreference;
import org.aaa4j.radius.core.attribute.attributes.TunnelPrivateGroupId;
import org.aaa4j.radius.core.attribute.attributes.TunnelServerAuthId;
import org.aaa4j.radius.core.attribute.attributes.TunnelServerEndpoint;
import org.aaa4j.radius.core.attribute.attributes.TunnelType;
import org.aaa4j.radius.core.attribute.attributes.UserName;
import org.aaa4j.radius.core.attribute.attributes.UserPassword;
import org.aaa4j.radius.core.attribute.attributes.UserPriorityTable;
import org.aaa4j.radius.core.attribute.attributes.VendorSpecific;
import org.aaa4j.radius.core.attribute.attributes.WlanAkmSuite;
import org.aaa4j.radius.core.attribute.attributes.WlanGroupCipher;
import org.aaa4j.radius.core.attribute.attributes.WlanGroupMgmtCipher;
import org.aaa4j.radius.core.attribute.attributes.WlanHessid;
import org.aaa4j.radius.core.attribute.attributes.WlanPairwiseCipher;
import org.aaa4j.radius.core.attribute.attributes.WlanReasonCode;
import org.aaa4j.radius.core.attribute.attributes.WlanRfBand;
import org.aaa4j.radius.core.attribute.attributes.WlanVenueInfo;
import org.aaa4j.radius.core.attribute.attributes.WlanVenueLanguage;
import org.aaa4j.radius.core.attribute.attributes.WlanVenueName;
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Standard dictionary containing definitions for all the standard packets defined in
 * {@link org.aaa4j.radius.core.packet.packets} and all standard attributes defined in
 * {@link org.aaa4j.radius.core.attribute.attributes}.
 */
public final class StandardDictionary implements Dictionary {

    static private final PacketDefinition[] packetDefinitions = new PacketDefinition[256];

    static private final Map<AttributeType, AttributeDefinition<?, ?>> typeAttributeDefinitionsMap = new HashMap<>();

    static private final Map<String, AttributeDefinition<?, ?>> nameAttributeDefinitionsMap = new HashMap<>();

    static private final Map<AttributeType, Map<String, Integer>> numericAttributeValueMap = new HashMap<>();

    static private final Map<AttributeType, TlvDefinition> tlvDefinitionsMap = new HashMap<>();

    static {
        packetDefinitions[AccessRequest.CODE] = new PacketDefinition(AccessRequest.CODE,
                AccessRequest.NAME,
                AccessRequest.class,
                (code, attributes, receivedFields) -> new AccessRequest(attributes, receivedFields));

        packetDefinitions[AccessAccept.CODE] = new PacketDefinition(AccessAccept.CODE,
                AccessAccept.NAME,
                AccessAccept.class,
                (code, attributes, receivedFields) -> new AccessAccept(attributes, receivedFields));

        packetDefinitions[AccessReject.CODE] = new PacketDefinition(AccessReject.CODE,
                AccessReject.NAME,
                AccessReject.class,
                (code, attributes, receivedFields) -> new AccessReject(attributes, receivedFields));

        packetDefinitions[AccountingRequest.CODE] = new PacketDefinition(AccountingRequest.CODE,
                AccountingRequest.NAME,
                AccountingRequest.class,
                (code, attributes, receivedFields) -> new AccountingRequest(attributes, receivedFields));

        packetDefinitions[AccountingResponse.CODE] = new PacketDefinition(AccountingResponse.CODE,
                AccountingResponse.NAME,
                AccountingResponse.class,
                (code, attributes, receivedFields) -> new AccountingResponse(attributes, receivedFields));

        packetDefinitions[AccessChallenge.CODE] = new PacketDefinition(AccessChallenge.CODE,
                AccessChallenge.NAME,
                AccessChallenge.class,
                (code, attributes, receivedFields) -> new AccessChallenge(attributes, receivedFields));
    }

    static {
        typeAttributeDefinitionsMap.put(UserName.TYPE,
                new AttributeDefinition<>(
                        UserName.TYPE,
                        UserName.NAME,
                        UserName.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new UserName(data)),
                        UserName::new));

        typeAttributeDefinitionsMap.put(UserPassword.TYPE,
                new AttributeDefinition<>(
                        UserPassword.TYPE,
                        UserPassword.NAME,
                        UserPassword.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                new UserPasswordDataCodec<>(StringData.Codec.INSTANCE),
                                (type, data) -> new UserPassword(data)),
                        UserPassword::new));

        typeAttributeDefinitionsMap.put(ChapPassword.TYPE,
                new AttributeDefinition<>(
                        ChapPassword.TYPE,
                        ChapPassword.NAME,
                        ChapPassword.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new ChapPassword(data)),
                        ChapPassword::new));

        typeAttributeDefinitionsMap.put(NasIpAddress.TYPE,
                new AttributeDefinition<>(
                        NasIpAddress.TYPE,
                        NasIpAddress.NAME,
                        NasIpAddress.class,
                        Ipv4AddrData.class,
                        new StandardAttribute.Codec<>(
                                Ipv4AddrData.Codec.INSTANCE,
                                (type, data) -> new NasIpAddress(data)),
                        NasIpAddress::new));

        typeAttributeDefinitionsMap.put(NasPort.TYPE,
                new AttributeDefinition<>(
                        NasPort.TYPE,
                        NasPort.NAME,
                        NasPort.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new NasPort(data)),
                        NasPort::new));

        typeAttributeDefinitionsMap.put(ServiceType.TYPE,
                new AttributeDefinition<>(
                        ServiceType.TYPE,
                        ServiceType.NAME,
                        ServiceType.class,
                        EnumData.class,
                        new StandardAttribute.Codec<>(
                                EnumData.Codec.INSTANCE,
                                (type, data) -> new ServiceType(data)),
                        ServiceType::new));

        typeAttributeDefinitionsMap.put(FramedProtocol.TYPE,
                new AttributeDefinition<>(
                        FramedProtocol.TYPE,
                        FramedProtocol.NAME,
                        FramedProtocol.class,
                        EnumData.class,
                        new StandardAttribute.Codec<>(
                                EnumData.Codec.INSTANCE,
                                (type, data) -> new FramedProtocol(data)),
                        FramedProtocol::new));

        typeAttributeDefinitionsMap.put(FramedIpAddress.TYPE,
                new AttributeDefinition<>(
                        FramedIpAddress.TYPE,
                        FramedIpAddress.NAME,
                        FramedIpAddress.class,
                        Ipv4AddrData.class,
                        new StandardAttribute.Codec<>(
                                Ipv4AddrData.Codec.INSTANCE,
                                (type, data) -> new FramedIpAddress(data)),
                        FramedIpAddress::new));

        typeAttributeDefinitionsMap.put(FramedIpNetmask.TYPE,
                new AttributeDefinition<>(
                        FramedIpNetmask.TYPE,
                        FramedIpNetmask.NAME,
                        FramedIpNetmask.class,
                        Ipv4AddrData.class,
                        new StandardAttribute.Codec<>(
                                Ipv4AddrData.Codec.INSTANCE,
                                (type, data) -> new FramedIpNetmask(data)),
                        FramedIpNetmask::new));

        typeAttributeDefinitionsMap.put(FramedRouting.TYPE,
                new AttributeDefinition<>(
                        FramedRouting.TYPE,
                        FramedRouting.NAME,
                        FramedRouting.class,
                        EnumData.class,
                        new StandardAttribute.Codec<>(
                                EnumData.Codec.INSTANCE,
                                (type, data) -> new FramedRouting(data)),
                        FramedRouting::new));

        typeAttributeDefinitionsMap.put(FilterId.TYPE,
                new AttributeDefinition<>(
                        FilterId.TYPE,
                        FilterId.NAME,
                        FilterId.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new FilterId(data)),
                        FilterId::new));

        typeAttributeDefinitionsMap.put(FramedMtu.TYPE,
                new AttributeDefinition<>(
                        FramedMtu.TYPE,
                        FramedMtu.NAME,
                        FramedMtu.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new FramedMtu(data)),
                        FramedMtu::new));

        typeAttributeDefinitionsMap.put(FramedCompression.TYPE,
                new AttributeDefinition<>(
                        FramedCompression.TYPE,
                        FramedCompression.NAME,
                        FramedCompression.class,
                        EnumData.class,
                        new StandardAttribute.Codec<>(
                                EnumData.Codec.INSTANCE,
                                (type, data) -> new FramedCompression(data)),
                        FramedCompression::new));

        typeAttributeDefinitionsMap.put(LoginIpHost.TYPE,
                new AttributeDefinition<>(
                        LoginIpHost.TYPE,
                        LoginIpHost.NAME,
                        LoginIpHost.class,
                        Ipv4AddrData.class,
                        new StandardAttribute.Codec<>(
                                Ipv4AddrData.Codec.INSTANCE,
                                (type, data) -> new LoginIpHost(data)),
                        LoginIpHost::new));

        typeAttributeDefinitionsMap.put(LoginService.TYPE,
                new AttributeDefinition<>(
                        LoginService.TYPE,
                        LoginService.NAME,
                        LoginService.class,
                        EnumData.class,
                        new StandardAttribute.Codec<>(
                                EnumData.Codec.INSTANCE,
                                (type, data) -> new LoginService(data)),
                        LoginService::new));

        typeAttributeDefinitionsMap.put(LoginTcpPort.TYPE,
                new AttributeDefinition<>(
                        LoginTcpPort.TYPE,
                        LoginTcpPort.NAME,
                        LoginTcpPort.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new LoginTcpPort(data)),
                        LoginTcpPort::new));

        typeAttributeDefinitionsMap.put(ReplyMessage.TYPE,
                new AttributeDefinition<>(
                        ReplyMessage.TYPE,
                        ReplyMessage.NAME,
                        ReplyMessage.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new ReplyMessage(data)),
                        ReplyMessage::new));

        typeAttributeDefinitionsMap.put(CallbackNumber.TYPE,
                new AttributeDefinition<>(
                        CallbackNumber.TYPE,
                        CallbackNumber.NAME,
                        CallbackNumber.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new CallbackNumber(data)),
                        CallbackNumber::new));

        typeAttributeDefinitionsMap.put(CallbackId.TYPE,
                new AttributeDefinition<>(
                        CallbackId.TYPE,
                        CallbackId.NAME,
                        CallbackId.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new CallbackId(data)),
                        CallbackId::new));

        typeAttributeDefinitionsMap.put(FramedRoute.TYPE,
                new AttributeDefinition<>(
                        FramedRoute.TYPE,
                        FramedRoute.NAME,
                        FramedRoute.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new FramedRoute(data)),
                        FramedRoute::new));

        typeAttributeDefinitionsMap.put(FramedIpxNetwork.TYPE,
                new AttributeDefinition<>(
                        FramedIpxNetwork.TYPE,
                        FramedIpxNetwork.NAME,
                        FramedIpxNetwork.class,
                        Ipv4AddrData.class,
                        new StandardAttribute.Codec<>(
                                Ipv4AddrData.Codec.INSTANCE,
                                (type, data) -> new FramedIpxNetwork(data)),
                        FramedIpxNetwork::new));

        typeAttributeDefinitionsMap.put(State.TYPE,
                new AttributeDefinition<>(
                        State.TYPE,
                        State.NAME,
                        State.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new State(data)),
                        State::new));

        typeAttributeDefinitionsMap.put(Class.TYPE,
                new AttributeDefinition<>(
                        Class.TYPE,
                        Class.NAME,
                        Class.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new Class(data)),
                        Class::new));

        typeAttributeDefinitionsMap.put(VendorSpecific.TYPE,
                new AttributeDefinition<>(
                        VendorSpecific.TYPE,
                        VendorSpecific.NAME,
                        VendorSpecific.class,
                        VsaData.class,
                        new StandardAttribute.Codec<>(
                                VsaData.Codec.INSTANCE,
                                (type, data) -> new VendorSpecific(data)),
                        VendorSpecific::new));

        typeAttributeDefinitionsMap.put(SessionTimeout.TYPE,
                new AttributeDefinition<>(
                        SessionTimeout.TYPE,
                        SessionTimeout.NAME,
                        SessionTimeout.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new SessionTimeout(data)),
                        SessionTimeout::new));

        typeAttributeDefinitionsMap.put(IdleTimeout.TYPE,
                new AttributeDefinition<>(
                        IdleTimeout.TYPE,
                        IdleTimeout.NAME,
                        IdleTimeout.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new IdleTimeout(data)),
                        IdleTimeout::new));

        typeAttributeDefinitionsMap.put(TerminationAction.TYPE,
                new AttributeDefinition<>(
                        TerminationAction.TYPE,
                        TerminationAction.NAME,
                        TerminationAction.class,
                        EnumData.class,
                        new StandardAttribute.Codec<>(
                                EnumData.Codec.INSTANCE,
                                (type, data) -> new TerminationAction(data)),
                        TerminationAction::new));

        typeAttributeDefinitionsMap.put(CalledStationId.TYPE,
                new AttributeDefinition<>(
                        CalledStationId.TYPE,
                        CalledStationId.NAME,
                        CalledStationId.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new CalledStationId(data)),
                        CalledStationId::new));

        typeAttributeDefinitionsMap.put(CallingStationId.TYPE,
                new AttributeDefinition<>(
                        CallingStationId.TYPE,
                        CallingStationId.NAME,
                        CallingStationId.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new CallingStationId(data)),
                        CallingStationId::new));

        typeAttributeDefinitionsMap.put(NasIdentifier.TYPE,
                new AttributeDefinition<>(
                        NasIdentifier.TYPE,
                        NasIdentifier.NAME,
                        NasIdentifier.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new NasIdentifier(data)),
                        NasIdentifier::new));

        typeAttributeDefinitionsMap.put(ProxyState.TYPE,
                new AttributeDefinition<>(
                        ProxyState.TYPE,
                        ProxyState.NAME,
                        ProxyState.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new ProxyState(data)),
                        ProxyState::new));

        typeAttributeDefinitionsMap.put(LoginLatService.TYPE,
                new AttributeDefinition<>(
                        LoginLatService.TYPE,
                        LoginLatService.NAME,
                        LoginLatService.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new LoginLatService(data)),
                        LoginLatService::new));

        typeAttributeDefinitionsMap.put(LoginLatNode.TYPE,
                new AttributeDefinition<>(
                        LoginLatNode.TYPE,
                        LoginLatNode.NAME,
                        LoginLatNode.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new LoginLatNode(data)),
                        LoginLatNode::new));

        typeAttributeDefinitionsMap.put(LoginLatGroup.TYPE,
                new AttributeDefinition<>(
                        LoginLatGroup.TYPE,
                        LoginLatGroup.NAME,
                        LoginLatGroup.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new LoginLatGroup(data)),
                        LoginLatGroup::new));

        typeAttributeDefinitionsMap.put(FramedAppleTalkLink.TYPE,
                new AttributeDefinition<>(
                        FramedAppleTalkLink.TYPE,
                        FramedAppleTalkLink.NAME,
                        FramedAppleTalkLink.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new FramedAppleTalkLink(data)),
                        FramedAppleTalkLink::new));

        typeAttributeDefinitionsMap.put(FramedAppleTalkNetwork.TYPE,
                new AttributeDefinition<>(
                        FramedAppleTalkNetwork.TYPE,
                        FramedAppleTalkNetwork.NAME,
                        FramedAppleTalkNetwork.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new FramedAppleTalkNetwork(data)),
                        FramedAppleTalkNetwork::new));

        typeAttributeDefinitionsMap.put(FramedAppleTalkZone.TYPE,
                new AttributeDefinition<>(
                        FramedAppleTalkZone.TYPE,
                        FramedAppleTalkZone.NAME,
                        FramedAppleTalkZone.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new FramedAppleTalkZone(data)),
                        FramedAppleTalkZone::new));

        typeAttributeDefinitionsMap.put(AcctStatusType.TYPE,
                new AttributeDefinition<>(
                        AcctStatusType.TYPE,
                        AcctStatusType.NAME,
                        AcctStatusType.class,
                        EnumData.class,
                        new StandardAttribute.Codec<>(
                                EnumData.Codec.INSTANCE,
                                (type, data) -> new AcctStatusType(data)),
                        AcctStatusType::new));

        typeAttributeDefinitionsMap.put(AcctDelayTime.TYPE,
                new AttributeDefinition<>(
                        AcctDelayTime.TYPE,
                        AcctDelayTime.NAME,
                        AcctDelayTime.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new AcctDelayTime(data)),
                        AcctDelayTime::new));

        typeAttributeDefinitionsMap.put(AcctInputOctets.TYPE,
                new AttributeDefinition<>(
                        AcctInputOctets.TYPE,
                        AcctInputOctets.NAME,
                        AcctInputOctets.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new AcctInputOctets(data)),
                        AcctInputOctets::new));

        typeAttributeDefinitionsMap.put(AcctOutputOctets.TYPE,
                new AttributeDefinition<>(
                        AcctOutputOctets.TYPE,
                        AcctOutputOctets.NAME,
                        AcctOutputOctets.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new AcctOutputOctets(data)),
                        AcctOutputOctets::new));

        typeAttributeDefinitionsMap.put(AcctSessionId.TYPE,
                new AttributeDefinition<>(
                        AcctSessionId.TYPE,
                        AcctSessionId.NAME,
                        AcctSessionId.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new AcctSessionId(data)),
                        AcctSessionId::new));

        typeAttributeDefinitionsMap.put(AcctAuthentic.TYPE,
                new AttributeDefinition<>(
                        AcctAuthentic.TYPE,
                        AcctAuthentic.NAME,
                        AcctAuthentic.class,
                        EnumData.class,
                        new StandardAttribute.Codec<>(
                                EnumData.Codec.INSTANCE,
                                (type, data) -> new AcctAuthentic(data)),
                        AcctAuthentic::new));

        typeAttributeDefinitionsMap.put(AcctSessionTime.TYPE,
                new AttributeDefinition<>(
                        AcctSessionTime.TYPE,
                        AcctSessionTime.NAME,
                        AcctSessionTime.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new AcctSessionTime(data)),
                        AcctSessionTime::new));

        typeAttributeDefinitionsMap.put(AcctInputPackets.TYPE,
                new AttributeDefinition<>(
                        AcctInputPackets.TYPE,
                        AcctInputPackets.NAME,
                        AcctInputPackets.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new AcctInputPackets(data)),
                        AcctInputPackets::new));

        typeAttributeDefinitionsMap.put(AcctOutputPackets.TYPE,
                new AttributeDefinition<>(
                        AcctOutputPackets.TYPE,
                        AcctOutputPackets.NAME,
                        AcctOutputPackets.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new AcctOutputPackets(data)),
                        AcctOutputPackets::new));

        typeAttributeDefinitionsMap.put(AcctTerminateCause.TYPE,
                new AttributeDefinition<>(
                        AcctTerminateCause.TYPE,
                        AcctTerminateCause.NAME,
                        AcctTerminateCause.class,
                        EnumData.class,
                        new StandardAttribute.Codec<>(
                                EnumData.Codec.INSTANCE,
                                (type, data) -> new AcctTerminateCause(data)),
                        AcctTerminateCause::new));

        typeAttributeDefinitionsMap.put(AcctMultiSessionId.TYPE,
                new AttributeDefinition<>(
                        AcctMultiSessionId.TYPE,
                        AcctMultiSessionId.NAME,
                        AcctMultiSessionId.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new AcctMultiSessionId(data)),
                        AcctMultiSessionId::new));

        typeAttributeDefinitionsMap.put(AcctLinkCount.TYPE,
                new AttributeDefinition<>(
                        AcctLinkCount.TYPE,
                        AcctLinkCount.NAME,
                        AcctLinkCount.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new AcctLinkCount(data)),
                        AcctLinkCount::new));

        typeAttributeDefinitionsMap.put(AcctInputGigawords.TYPE,
                new AttributeDefinition<>(
                        AcctInputGigawords.TYPE,
                        AcctInputGigawords.NAME,
                        AcctInputGigawords.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new AcctInputGigawords(data)),
                        AcctInputGigawords::new));

        typeAttributeDefinitionsMap.put(AcctOutputGigawords.TYPE,
                new AttributeDefinition<>(
                        AcctOutputGigawords.TYPE,
                        AcctOutputGigawords.NAME,
                        AcctOutputGigawords.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new AcctOutputGigawords(data)),
                        AcctOutputGigawords::new));

        typeAttributeDefinitionsMap.put(EventTimestamp.TYPE,
                new AttributeDefinition<>(
                        EventTimestamp.TYPE,
                        EventTimestamp.NAME,
                        EventTimestamp.class,
                        TimeData.class,
                        new StandardAttribute.Codec<>(
                                TimeData.Codec.INSTANCE,
                                (type, data) -> new EventTimestamp(data)),
                        EventTimestamp::new));

        typeAttributeDefinitionsMap.put(EgressVlanid.TYPE,
                new AttributeDefinition<>(
                        EgressVlanid.TYPE,
                        EgressVlanid.NAME,
                        EgressVlanid.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new EgressVlanid(data)),
                        EgressVlanid::new));

        typeAttributeDefinitionsMap.put(IngressFilters.TYPE,
                new AttributeDefinition<>(
                        IngressFilters.TYPE,
                        IngressFilters.NAME,
                        IngressFilters.class,
                        EnumData.class,
                        new StandardAttribute.Codec<>(
                                EnumData.Codec.INSTANCE,
                                (type, data) -> new IngressFilters(data)),
                        IngressFilters::new));

        typeAttributeDefinitionsMap.put(EgressVlanName.TYPE,
                new AttributeDefinition<>(
                        EgressVlanName.TYPE,
                        EgressVlanName.NAME,
                        EgressVlanName.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new EgressVlanName(data)),
                        EgressVlanName::new));

        typeAttributeDefinitionsMap.put(UserPriorityTable.TYPE,
                new AttributeDefinition<>(
                        UserPriorityTable.TYPE,
                        UserPriorityTable.NAME,
                        UserPriorityTable.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new UserPriorityTable(data)),
                        UserPriorityTable::new));

        typeAttributeDefinitionsMap.put(ChapChallenge.TYPE,
                new AttributeDefinition<>(
                        ChapChallenge.TYPE,
                        ChapChallenge.NAME,
                        ChapChallenge.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new ChapChallenge(data)),
                        ChapChallenge::new));

        typeAttributeDefinitionsMap.put(NasPortType.TYPE,
                new AttributeDefinition<>(
                        NasPortType.TYPE,
                        NasPortType.NAME,
                        NasPortType.class,
                        EnumData.class,
                        new StandardAttribute.Codec<>(
                                EnumData.Codec.INSTANCE,
                                (type, data) -> new NasPortType(data)),
                        NasPortType::new));

        typeAttributeDefinitionsMap.put(PortLimit.TYPE,
                new AttributeDefinition<>(
                        PortLimit.TYPE,
                        PortLimit.NAME,
                        PortLimit.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new PortLimit(data)),
                        PortLimit::new));

        typeAttributeDefinitionsMap.put(LoginLatPort.TYPE,
                new AttributeDefinition<>(
                        LoginLatPort.TYPE,
                        LoginLatPort.NAME,
                        LoginLatPort.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new LoginLatPort(data)),
                        LoginLatPort::new));

        typeAttributeDefinitionsMap.put(TunnelType.TYPE,
                new AttributeDefinition<>(
                        TunnelType.TYPE,
                        TunnelType.NAME,
                        TunnelType.class,
                        EnumData.class,
                        new StandardAttribute.Codec<>(
                                EnumData.Codec.INSTANCE,
                                (type, data) -> new TunnelType(data)),
                        TunnelType::new));

        typeAttributeDefinitionsMap.put(TunnelMediumType.TYPE,
                new AttributeDefinition<>(
                        TunnelMediumType.TYPE,
                        TunnelMediumType.NAME,
                        TunnelMediumType.class,
                        EnumData.class,
                        new StandardAttribute.Codec<>(
                                EnumData.Codec.INSTANCE,
                                (type, data) -> new TunnelMediumType(data)),
                        TunnelMediumType::new));

        typeAttributeDefinitionsMap.put(TunnelClientEndpoint.TYPE,
                new AttributeDefinition<>(
                        TunnelClientEndpoint.TYPE,
                        TunnelClientEndpoint.NAME,
                        TunnelClientEndpoint.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new TunnelClientEndpoint(data)),
                        TunnelClientEndpoint::new));

        typeAttributeDefinitionsMap.put(TunnelServerEndpoint.TYPE,
                new AttributeDefinition<>(
                        TunnelServerEndpoint.TYPE,
                        TunnelServerEndpoint.NAME,
                        TunnelServerEndpoint.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new TunnelServerEndpoint(data)),
                        TunnelServerEndpoint::new));

        typeAttributeDefinitionsMap.put(AcctTunnelConnection.TYPE,
                new AttributeDefinition<>(
                        AcctTunnelConnection.TYPE,
                        AcctTunnelConnection.NAME,
                        AcctTunnelConnection.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new AcctTunnelConnection(data)),
                        AcctTunnelConnection::new));

        typeAttributeDefinitionsMap.put(TunnelPassword.TYPE,
                new AttributeDefinition<>(
                        TunnelPassword.TYPE,
                        TunnelPassword.NAME,
                        TunnelPassword.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new TunnelPassword(data)),
                        TunnelPassword::new));

        typeAttributeDefinitionsMap.put(ArapPassword.TYPE,
                new AttributeDefinition<>(
                        ArapPassword.TYPE,
                        ArapPassword.NAME,
                        ArapPassword.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new ArapPassword(data)),
                        ArapPassword::new));

        typeAttributeDefinitionsMap.put(ArapFeatures.TYPE,
                new AttributeDefinition<>(
                        ArapFeatures.TYPE,
                        ArapFeatures.NAME,
                        ArapFeatures.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new ArapFeatures(data)),
                        ArapFeatures::new));

        typeAttributeDefinitionsMap.put(ArapZoneAccess.TYPE,
                new AttributeDefinition<>(
                        ArapZoneAccess.TYPE,
                        ArapZoneAccess.NAME,
                        ArapZoneAccess.class,
                        EnumData.class,
                        new StandardAttribute.Codec<>(
                                EnumData.Codec.INSTANCE,
                                (type, data) -> new ArapZoneAccess(data)),
                        ArapZoneAccess::new));

        typeAttributeDefinitionsMap.put(ArapSecurity.TYPE,
                new AttributeDefinition<>(
                        ArapSecurity.TYPE,
                        ArapSecurity.NAME,
                        ArapSecurity.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new ArapSecurity(data)),
                        ArapSecurity::new));

        typeAttributeDefinitionsMap.put(ArapSecurityData.TYPE,
                new AttributeDefinition<>(
                        ArapSecurityData.TYPE,
                        ArapSecurityData.NAME,
                        ArapSecurityData.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new ArapSecurityData(data)),
                        ArapSecurityData::new));

        typeAttributeDefinitionsMap.put(PasswordRetry.TYPE,
                new AttributeDefinition<>(
                        PasswordRetry.TYPE,
                        PasswordRetry.NAME,
                        PasswordRetry.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new PasswordRetry(data)),
                        PasswordRetry::new));

        typeAttributeDefinitionsMap.put(Prompt.TYPE,
                new AttributeDefinition<>(
                        Prompt.TYPE,
                        Prompt.NAME,
                        Prompt.class,
                        EnumData.class,
                        new StandardAttribute.Codec<>(
                                EnumData.Codec.INSTANCE,
                                (type, data) -> new Prompt(data)),
                        Prompt::new));

        typeAttributeDefinitionsMap.put(ConnectInfo.TYPE,
                new AttributeDefinition<>(
                        ConnectInfo.TYPE,
                        ConnectInfo.NAME,
                        ConnectInfo.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new ConnectInfo(data)),
                        ConnectInfo::new));

        typeAttributeDefinitionsMap.put(ConfigurationToken.TYPE,
                new AttributeDefinition<>(
                        ConfigurationToken.TYPE,
                        ConfigurationToken.NAME,
                        ConfigurationToken.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new ConfigurationToken(data)),
                        ConfigurationToken::new));

        typeAttributeDefinitionsMap.put(EapMessage.TYPE,
                new AttributeDefinition<>(
                        EapMessage.TYPE,
                        EapMessage.NAME,
                        EapMessage.class,
                        ConcatData.class,
                        new ConcatAttribute.Codec(
                                (type, data) -> new EapMessage(data)),
                        EapMessage::new));

        typeAttributeDefinitionsMap.put(MessageAuthenticator.TYPE,
                new AttributeDefinition<>(
                        MessageAuthenticator.TYPE,
                        MessageAuthenticator.NAME,
                        MessageAuthenticator.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new MessageAuthenticator(data)),
                        MessageAuthenticator::new));

        typeAttributeDefinitionsMap.put(TunnelPrivateGroupId.TYPE,
                new AttributeDefinition<>(
                        TunnelPrivateGroupId.TYPE,
                        TunnelPrivateGroupId.NAME,
                        TunnelPrivateGroupId.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new TunnelPrivateGroupId(data)),
                        TunnelPrivateGroupId::new));

        typeAttributeDefinitionsMap.put(TunnelAssignmentId.TYPE,
                new AttributeDefinition<>(
                        TunnelAssignmentId.TYPE,
                        TunnelAssignmentId.NAME,
                        TunnelAssignmentId.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new TunnelAssignmentId(data)),
                        TunnelAssignmentId::new));

        typeAttributeDefinitionsMap.put(TunnelPreference.TYPE,
                new AttributeDefinition<>(
                        TunnelPreference.TYPE,
                        TunnelPreference.NAME,
                        TunnelPreference.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new TunnelPreference(data)),
                        TunnelPreference::new));

        typeAttributeDefinitionsMap.put(ArapChallengeResponse.TYPE,
                new AttributeDefinition<>(
                        ArapChallengeResponse.TYPE,
                        ArapChallengeResponse.NAME,
                        ArapChallengeResponse.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new ArapChallengeResponse(data)),
                        ArapChallengeResponse::new));

        typeAttributeDefinitionsMap.put(AcctInterimInterval.TYPE,
                new AttributeDefinition<>(
                        AcctInterimInterval.TYPE,
                        AcctInterimInterval.NAME,
                        AcctInterimInterval.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new AcctInterimInterval(data)),
                        AcctInterimInterval::new));

        typeAttributeDefinitionsMap.put(AcctTunnelPacketsLost.TYPE,
                new AttributeDefinition<>(
                        AcctTunnelPacketsLost.TYPE,
                        AcctTunnelPacketsLost.NAME,
                        AcctTunnelPacketsLost.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new AcctTunnelPacketsLost(data)),
                        AcctTunnelPacketsLost::new));

        typeAttributeDefinitionsMap.put(NasPortId.TYPE,
                new AttributeDefinition<>(
                        NasPortId.TYPE,
                        NasPortId.NAME,
                        NasPortId.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new NasPortId(data)),
                        NasPortId::new));

        typeAttributeDefinitionsMap.put(FramedPool.TYPE,
                new AttributeDefinition<>(
                        FramedPool.TYPE,
                        FramedPool.NAME,
                        FramedPool.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new FramedPool(data)),
                        FramedPool::new));

        typeAttributeDefinitionsMap.put(Cui.TYPE,
                new AttributeDefinition<>(
                        Cui.TYPE,
                        Cui.NAME,
                        Cui.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new Cui(data)),
                        Cui::new));

        typeAttributeDefinitionsMap.put(TunnelClientAuthId.TYPE,
                new AttributeDefinition<>(
                        TunnelClientAuthId.TYPE,
                        TunnelClientAuthId.NAME,
                        TunnelClientAuthId.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new TunnelClientAuthId(data)),
                        TunnelClientAuthId::new));

        typeAttributeDefinitionsMap.put(TunnelServerAuthId.TYPE,
                new AttributeDefinition<>(
                        TunnelServerAuthId.TYPE,
                        TunnelServerAuthId.NAME,
                        TunnelServerAuthId.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new TunnelServerAuthId(data)),
                        TunnelServerAuthId::new));

        typeAttributeDefinitionsMap.put(NasFilterRule.TYPE,
                new AttributeDefinition<>(
                        NasFilterRule.TYPE,
                        NasFilterRule.NAME,
                        NasFilterRule.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new NasFilterRule(data)),
                        NasFilterRule::new));

        typeAttributeDefinitionsMap.put(OriginatingLineInfo.TYPE,
                new AttributeDefinition<>(
                        OriginatingLineInfo.TYPE,
                        OriginatingLineInfo.NAME,
                        OriginatingLineInfo.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new OriginatingLineInfo(data)),
                        OriginatingLineInfo::new));

        typeAttributeDefinitionsMap.put(NasIpv6Address.TYPE,
                new AttributeDefinition<>(
                        NasIpv6Address.TYPE,
                        NasIpv6Address.NAME,
                        NasIpv6Address.class,
                        Ipv6AddrData.class,
                        new StandardAttribute.Codec<>(
                                Ipv6AddrData.Codec.INSTANCE,
                                (type, data) -> new NasIpv6Address(data)),
                        NasIpv6Address::new));

        typeAttributeDefinitionsMap.put(FramedInterfaceId.TYPE,
                new AttributeDefinition<>(
                        FramedInterfaceId.TYPE,
                        FramedInterfaceId.NAME,
                        FramedInterfaceId.class,
                        IfidData.class,
                        new StandardAttribute.Codec<>(
                                IfidData.Codec.INSTANCE,
                                (type, data) -> new FramedInterfaceId(data)),
                        FramedInterfaceId::new));

        typeAttributeDefinitionsMap.put(FramedIpv6Prefix.TYPE,
                new AttributeDefinition<>(
                        FramedIpv6Prefix.TYPE,
                        FramedIpv6Prefix.NAME,
                        FramedIpv6Prefix.class,
                        Ipv6PrefixData.class,
                        new StandardAttribute.Codec<>(
                                Ipv6PrefixData.Codec.INSTANCE,
                                (type, data) -> new FramedIpv6Prefix(data)),
                        FramedIpv6Prefix::new));

        typeAttributeDefinitionsMap.put(LoginIpv6Host.TYPE,
                new AttributeDefinition<>(
                        LoginIpv6Host.TYPE,
                        LoginIpv6Host.NAME,
                        LoginIpv6Host.class,
                        Ipv6AddrData.class,
                        new StandardAttribute.Codec<>(
                                Ipv6AddrData.Codec.INSTANCE,
                                (type, data) -> new LoginIpv6Host(data)),
                        LoginIpv6Host::new));

        typeAttributeDefinitionsMap.put(FramedIpv6Route.TYPE,
                new AttributeDefinition<>(
                        FramedIpv6Route.TYPE,
                        FramedIpv6Route.NAME,
                        FramedIpv6Route.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new FramedIpv6Route(data)),
                        FramedIpv6Route::new));

        typeAttributeDefinitionsMap.put(FramedIpv6Pool.TYPE,
                new AttributeDefinition<>(
                        FramedIpv6Pool.TYPE,
                        FramedIpv6Pool.NAME,
                        FramedIpv6Pool.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new FramedIpv6Pool(data)),
                        FramedIpv6Pool::new));

        typeAttributeDefinitionsMap.put(ErrorCause.TYPE,
                new AttributeDefinition<>(
                        ErrorCause.TYPE,
                        ErrorCause.NAME,
                        ErrorCause.class,
                        EnumData.class,
                        new StandardAttribute.Codec<>(
                                EnumData.Codec.INSTANCE,
                                (type, data) -> new ErrorCause(data)),
                        ErrorCause::new));

        typeAttributeDefinitionsMap.put(EapKeyName.TYPE,
                new AttributeDefinition<>(
                        EapKeyName.TYPE,
                        EapKeyName.NAME,
                        EapKeyName.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new EapKeyName(data)),
                        EapKeyName::new));

        typeAttributeDefinitionsMap.put(DigestResponse.TYPE,
                new AttributeDefinition<>(
                        DigestResponse.TYPE,
                        DigestResponse.NAME,
                        DigestResponse.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new DigestResponse(data)),
                        DigestResponse::new));

        typeAttributeDefinitionsMap.put(DigestRealm.TYPE,
                new AttributeDefinition<>(
                        DigestRealm.TYPE,
                        DigestRealm.NAME,
                        DigestRealm.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new DigestRealm(data)),
                        DigestRealm::new));

        typeAttributeDefinitionsMap.put(DigestNonce.TYPE,
                new AttributeDefinition<>(
                        DigestNonce.TYPE,
                        DigestNonce.NAME,
                        DigestNonce.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new DigestNonce(data)),
                        DigestNonce::new));

        typeAttributeDefinitionsMap.put(DigestResponseAuth.TYPE,
                new AttributeDefinition<>(
                        DigestResponseAuth.TYPE,
                        DigestResponseAuth.NAME,
                        DigestResponseAuth.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new DigestResponseAuth(data)),
                        DigestResponseAuth::new));

        typeAttributeDefinitionsMap.put(DigestNextnonce.TYPE,
                new AttributeDefinition<>(
                        DigestNextnonce.TYPE,
                        DigestNextnonce.NAME,
                        DigestNextnonce.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new DigestNextnonce(data)),
                        DigestNextnonce::new));

        typeAttributeDefinitionsMap.put(DigestMethod.TYPE,
                new AttributeDefinition<>(
                        DigestMethod.TYPE,
                        DigestMethod.NAME,
                        DigestMethod.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new DigestMethod(data)),
                        DigestMethod::new));

        typeAttributeDefinitionsMap.put(DigestUri.TYPE,
                new AttributeDefinition<>(
                        DigestUri.TYPE,
                        DigestUri.NAME,
                        DigestUri.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new DigestUri(data)),
                        DigestUri::new));

        typeAttributeDefinitionsMap.put(DigestQop.TYPE,
                new AttributeDefinition<>(
                        DigestQop.TYPE,
                        DigestQop.NAME,
                        DigestQop.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new DigestQop(data)),
                        DigestQop::new));

        typeAttributeDefinitionsMap.put(DigestAlgorithm.TYPE,
                new AttributeDefinition<>(
                        DigestAlgorithm.TYPE,
                        DigestAlgorithm.NAME,
                        DigestAlgorithm.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new DigestAlgorithm(data)),
                        DigestAlgorithm::new));

        typeAttributeDefinitionsMap.put(DigestEntityBodyHash.TYPE,
                new AttributeDefinition<>(
                        DigestEntityBodyHash.TYPE,
                        DigestEntityBodyHash.NAME,
                        DigestEntityBodyHash.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new DigestEntityBodyHash(data)),
                        DigestEntityBodyHash::new));

        typeAttributeDefinitionsMap.put(DigestCnonce.TYPE,
                new AttributeDefinition<>(
                        DigestCnonce.TYPE,
                        DigestCnonce.NAME,
                        DigestCnonce.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new DigestCnonce(data)),
                        DigestCnonce::new));

        typeAttributeDefinitionsMap.put(DigestNonceCount.TYPE,
                new AttributeDefinition<>(
                        DigestNonceCount.TYPE,
                        DigestNonceCount.NAME,
                        DigestNonceCount.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new DigestNonceCount(data)),
                        DigestNonceCount::new));

        typeAttributeDefinitionsMap.put(DigestUsername.TYPE,
                new AttributeDefinition<>(
                        DigestUsername.TYPE,
                        DigestUsername.NAME,
                        DigestUsername.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new DigestUsername(data)),
                        DigestUsername::new));

        typeAttributeDefinitionsMap.put(DigestOpaque.TYPE,
                new AttributeDefinition<>(
                        DigestOpaque.TYPE,
                        DigestOpaque.NAME,
                        DigestOpaque.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new DigestOpaque(data)),
                        DigestOpaque::new));

        typeAttributeDefinitionsMap.put(DigestAuthParam.TYPE,
                new AttributeDefinition<>(
                        DigestAuthParam.TYPE,
                        DigestAuthParam.NAME,
                        DigestAuthParam.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new DigestAuthParam(data)),
                        DigestAuthParam::new));

        typeAttributeDefinitionsMap.put(DigestAkaAuts.TYPE,
                new AttributeDefinition<>(
                        DigestAkaAuts.TYPE,
                        DigestAkaAuts.NAME,
                        DigestAkaAuts.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new DigestAkaAuts(data)),
                        DigestAkaAuts::new));

        typeAttributeDefinitionsMap.put(DigestDomain.TYPE,
                new AttributeDefinition<>(
                        DigestDomain.TYPE,
                        DigestDomain.NAME,
                        DigestDomain.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new DigestDomain(data)),
                        DigestDomain::new));

        typeAttributeDefinitionsMap.put(DigestStale.TYPE,
                new AttributeDefinition<>(
                        DigestStale.TYPE,
                        DigestStale.NAME,
                        DigestStale.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new DigestStale(data)),
                        DigestStale::new));

        typeAttributeDefinitionsMap.put(DigestHa1.TYPE,
                new AttributeDefinition<>(
                        DigestHa1.TYPE,
                        DigestHa1.NAME,
                        DigestHa1.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new DigestHa1(data)),
                        DigestHa1::new));

        typeAttributeDefinitionsMap.put(SipAor.TYPE,
                new AttributeDefinition<>(
                        SipAor.TYPE,
                        SipAor.NAME,
                        SipAor.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new SipAor(data)),
                        SipAor::new));

        typeAttributeDefinitionsMap.put(DelegatedIpv6Prefix.TYPE,
                new AttributeDefinition<>(
                        DelegatedIpv6Prefix.TYPE,
                        DelegatedIpv6Prefix.NAME,
                        DelegatedIpv6Prefix.class,
                        Ipv6PrefixData.class,
                        new StandardAttribute.Codec<>(
                                Ipv6PrefixData.Codec.INSTANCE,
                                (type, data) -> new DelegatedIpv6Prefix(data)),
                        DelegatedIpv6Prefix::new));

        typeAttributeDefinitionsMap.put(Mip6FeatureVector.TYPE,
                new AttributeDefinition<>(
                        Mip6FeatureVector.TYPE,
                        Mip6FeatureVector.NAME,
                        Mip6FeatureVector.class,
                        Integer64Data.class,
                        new StandardAttribute.Codec<>(
                                Integer64Data.Codec.INSTANCE,
                                (type, data) -> new Mip6FeatureVector(data)),
                        Mip6FeatureVector::new));

        typeAttributeDefinitionsMap.put(Mip6HomeLinkPrefix.TYPE,
                new AttributeDefinition<>(
                        Mip6HomeLinkPrefix.TYPE,
                        Mip6HomeLinkPrefix.NAME,
                        Mip6HomeLinkPrefix.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new Mip6HomeLinkPrefix(data)),
                        Mip6HomeLinkPrefix::new));

        typeAttributeDefinitionsMap.put(OperatorName.TYPE,
                new AttributeDefinition<>(
                        OperatorName.TYPE,
                        OperatorName.NAME,
                        OperatorName.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new OperatorName(data)),
                        OperatorName::new));

        typeAttributeDefinitionsMap.put(LocationInformation.TYPE,
                new AttributeDefinition<>(
                        LocationInformation.TYPE,
                        LocationInformation.NAME,
                        LocationInformation.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new LocationInformation(data)),
                        LocationInformation::new));

        typeAttributeDefinitionsMap.put(LocationData.TYPE,
                new AttributeDefinition<>(
                        LocationData.TYPE,
                        LocationData.NAME,
                        LocationData.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new LocationData(data)),
                        LocationData::new));

        typeAttributeDefinitionsMap.put(BasicLocationPolicyRules.TYPE,
                new AttributeDefinition<>(
                        BasicLocationPolicyRules.TYPE,
                        BasicLocationPolicyRules.NAME,
                        BasicLocationPolicyRules.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new BasicLocationPolicyRules(data)),
                        BasicLocationPolicyRules::new));

        typeAttributeDefinitionsMap.put(ExtendedLocationPolicyRules.TYPE,
                new AttributeDefinition<>(
                        ExtendedLocationPolicyRules.TYPE,
                        ExtendedLocationPolicyRules.NAME,
                        ExtendedLocationPolicyRules.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new ExtendedLocationPolicyRules(data)),
                        ExtendedLocationPolicyRules::new));

        typeAttributeDefinitionsMap.put(LocationCapable.TYPE,
                new AttributeDefinition<>(
                        LocationCapable.TYPE,
                        LocationCapable.NAME,
                        LocationCapable.class,
                        EnumData.class,
                        new StandardAttribute.Codec<>(
                                EnumData.Codec.INSTANCE,
                                (type, data) -> new LocationCapable(data)),
                        LocationCapable::new));

        typeAttributeDefinitionsMap.put(RequestedLocationInfo.TYPE,
                new AttributeDefinition<>(
                        RequestedLocationInfo.TYPE,
                        RequestedLocationInfo.NAME,
                        RequestedLocationInfo.class,
                        EnumData.class,
                        new StandardAttribute.Codec<>(
                                EnumData.Codec.INSTANCE,
                                (type, data) -> new RequestedLocationInfo(data)),
                        RequestedLocationInfo::new));

        typeAttributeDefinitionsMap.put(FramedManagementProtocol.TYPE,
                new AttributeDefinition<>(
                        FramedManagementProtocol.TYPE,
                        FramedManagementProtocol.NAME,
                        FramedManagementProtocol.class,
                        EnumData.class,
                        new StandardAttribute.Codec<>(
                                EnumData.Codec.INSTANCE,
                                (type, data) -> new FramedManagementProtocol(data)),
                        FramedManagementProtocol::new));

        typeAttributeDefinitionsMap.put(ManagementTransportProtection.TYPE,
                new AttributeDefinition<>(
                        ManagementTransportProtection.TYPE,
                        ManagementTransportProtection.NAME,
                        ManagementTransportProtection.class,
                        EnumData.class,
                        new StandardAttribute.Codec<>(
                                EnumData.Codec.INSTANCE,
                                (type, data) -> new ManagementTransportProtection(data)),
                        ManagementTransportProtection::new));

        typeAttributeDefinitionsMap.put(ManagementPolicyId.TYPE,
                new AttributeDefinition<>(
                        ManagementPolicyId.TYPE,
                        ManagementPolicyId.NAME,
                        ManagementPolicyId.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new ManagementPolicyId(data)),
                        ManagementPolicyId::new));

        typeAttributeDefinitionsMap.put(ManagementPrivilegeLevel.TYPE,
                new AttributeDefinition<>(
                        ManagementPrivilegeLevel.TYPE,
                        ManagementPrivilegeLevel.NAME,
                        ManagementPrivilegeLevel.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new ManagementPrivilegeLevel(data)),
                        ManagementPrivilegeLevel::new));

        typeAttributeDefinitionsMap.put(PkmSsCert.TYPE,
                new AttributeDefinition<>(
                        PkmSsCert.TYPE,
                        PkmSsCert.NAME,
                        PkmSsCert.class,
                        ConcatData.class,
                        new ConcatAttribute.Codec(
                                (type, data) -> new PkmSsCert(data)),
                        PkmSsCert::new));

        typeAttributeDefinitionsMap.put(PkmCaCert.TYPE,
                new AttributeDefinition<>(
                        PkmCaCert.TYPE,
                        PkmCaCert.NAME,
                        PkmCaCert.class,
                        ConcatData.class,
                        new ConcatAttribute.Codec(
                                (type, data) -> new PkmCaCert(data)),
                        PkmCaCert::new));

        typeAttributeDefinitionsMap.put(PkmConfigSettings.TYPE,
                new AttributeDefinition<>(
                        PkmConfigSettings.TYPE,
                        PkmConfigSettings.NAME,
                        PkmConfigSettings.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new PkmConfigSettings(data)),
                        PkmConfigSettings::new));

        typeAttributeDefinitionsMap.put(PkmCryptosuiteList.TYPE,
                new AttributeDefinition<>(
                        PkmCryptosuiteList.TYPE,
                        PkmCryptosuiteList.NAME,
                        PkmCryptosuiteList.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new PkmCryptosuiteList(data)),
                        PkmCryptosuiteList::new));

        typeAttributeDefinitionsMap.put(PkmSaid.TYPE,
                new AttributeDefinition<>(
                        PkmSaid.TYPE,
                        PkmSaid.NAME,
                        PkmSaid.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new PkmSaid(data)),
                        PkmSaid::new));

        typeAttributeDefinitionsMap.put(PkmSaDescriptor.TYPE,
                new AttributeDefinition<>(
                        PkmSaDescriptor.TYPE,
                        PkmSaDescriptor.NAME,
                        PkmSaDescriptor.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new PkmSaDescriptor(data)),
                        PkmSaDescriptor::new));

        typeAttributeDefinitionsMap.put(PkmAuthKey.TYPE,
                new AttributeDefinition<>(
                        PkmAuthKey.TYPE,
                        PkmAuthKey.NAME,
                        PkmAuthKey.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new PkmAuthKey(data)),
                        PkmAuthKey::new));

        typeAttributeDefinitionsMap.put(DsLiteTunnelName.TYPE,
                new AttributeDefinition<>(
                        DsLiteTunnelName.TYPE,
                        DsLiteTunnelName.NAME,
                        DsLiteTunnelName.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new DsLiteTunnelName(data)),
                        DsLiteTunnelName::new));

        typeAttributeDefinitionsMap.put(MobileNodeIdentifier.TYPE,
                new AttributeDefinition<>(
                        MobileNodeIdentifier.TYPE,
                        MobileNodeIdentifier.NAME,
                        MobileNodeIdentifier.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new MobileNodeIdentifier(data)),
                        MobileNodeIdentifier::new));

        typeAttributeDefinitionsMap.put(ServiceSelection.TYPE,
                new AttributeDefinition<>(
                        ServiceSelection.TYPE,
                        ServiceSelection.NAME,
                        ServiceSelection.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new ServiceSelection(data)),
                        ServiceSelection::new));

        typeAttributeDefinitionsMap.put(Pmip6HomeLmaIpv6Address.TYPE,
                new AttributeDefinition<>(
                        Pmip6HomeLmaIpv6Address.TYPE,
                        Pmip6HomeLmaIpv6Address.NAME,
                        Pmip6HomeLmaIpv6Address.class,
                        Ipv6AddrData.class,
                        new StandardAttribute.Codec<>(
                                Ipv6AddrData.Codec.INSTANCE,
                                (type, data) -> new Pmip6HomeLmaIpv6Address(data)),
                        Pmip6HomeLmaIpv6Address::new));

        typeAttributeDefinitionsMap.put(Pmip6VisitedLmaIpv6Address.TYPE,
                new AttributeDefinition<>(
                        Pmip6VisitedLmaIpv6Address.TYPE,
                        Pmip6VisitedLmaIpv6Address.NAME,
                        Pmip6VisitedLmaIpv6Address.class,
                        Ipv6AddrData.class,
                        new StandardAttribute.Codec<>(
                                Ipv6AddrData.Codec.INSTANCE,
                                (type, data) -> new Pmip6VisitedLmaIpv6Address(data)),
                        Pmip6VisitedLmaIpv6Address::new));

        typeAttributeDefinitionsMap.put(Pmip6HomeLmaIpv4Address.TYPE,
                new AttributeDefinition<>(
                        Pmip6HomeLmaIpv4Address.TYPE,
                        Pmip6HomeLmaIpv4Address.NAME,
                        Pmip6HomeLmaIpv4Address.class,
                        Ipv4AddrData.class,
                        new StandardAttribute.Codec<>(
                                Ipv4AddrData.Codec.INSTANCE,
                                (type, data) -> new Pmip6HomeLmaIpv4Address(data)),
                        Pmip6HomeLmaIpv4Address::new));

        typeAttributeDefinitionsMap.put(Pmip6VisitedLmaIpv4Address.TYPE,
                new AttributeDefinition<>(
                        Pmip6VisitedLmaIpv4Address.TYPE,
                        Pmip6VisitedLmaIpv4Address.NAME,
                        Pmip6VisitedLmaIpv4Address.class,
                        Ipv4AddrData.class,
                        new StandardAttribute.Codec<>(
                                Ipv4AddrData.Codec.INSTANCE,
                                (type, data) -> new Pmip6VisitedLmaIpv4Address(data)),
                        Pmip6VisitedLmaIpv4Address::new));

        typeAttributeDefinitionsMap.put(Pmip6HomeHnPrefix.TYPE,
                new AttributeDefinition<>(
                        Pmip6HomeHnPrefix.TYPE,
                        Pmip6HomeHnPrefix.NAME,
                        Pmip6HomeHnPrefix.class,
                        Ipv6PrefixData.class,
                        new StandardAttribute.Codec<>(
                                Ipv6PrefixData.Codec.INSTANCE,
                                (type, data) -> new Pmip6HomeHnPrefix(data)),
                        Pmip6HomeHnPrefix::new));

        typeAttributeDefinitionsMap.put(Pmip6VisitedHnPrefix.TYPE,
                new AttributeDefinition<>(
                        Pmip6VisitedHnPrefix.TYPE,
                        Pmip6VisitedHnPrefix.NAME,
                        Pmip6VisitedHnPrefix.class,
                        Ipv6PrefixData.class,
                        new StandardAttribute.Codec<>(
                                Ipv6PrefixData.Codec.INSTANCE,
                                (type, data) -> new Pmip6VisitedHnPrefix(data)),
                        Pmip6VisitedHnPrefix::new));

        typeAttributeDefinitionsMap.put(Pmip6HomeInterfaceId.TYPE,
                new AttributeDefinition<>(
                        Pmip6HomeInterfaceId.TYPE,
                        Pmip6HomeInterfaceId.NAME,
                        Pmip6HomeInterfaceId.class,
                        IfidData.class,
                        new StandardAttribute.Codec<>(
                                IfidData.Codec.INSTANCE,
                                (type, data) -> new Pmip6HomeInterfaceId(data)),
                        Pmip6HomeInterfaceId::new));

        typeAttributeDefinitionsMap.put(Pmip6VisitedInterfaceId.TYPE,
                new AttributeDefinition<>(
                        Pmip6VisitedInterfaceId.TYPE,
                        Pmip6VisitedInterfaceId.NAME,
                        Pmip6VisitedInterfaceId.class,
                        IfidData.class,
                        new StandardAttribute.Codec<>(
                                IfidData.Codec.INSTANCE,
                                (type, data) -> new Pmip6VisitedInterfaceId(data)),
                        Pmip6VisitedInterfaceId::new));

        typeAttributeDefinitionsMap.put(Pmip6HomeIpv4HoA.TYPE,
                new AttributeDefinition<>(
                        Pmip6HomeIpv4HoA.TYPE,
                        Pmip6HomeIpv4HoA.NAME,
                        Pmip6HomeIpv4HoA.class,
                        Ipv4PrefixData.class,
                        new StandardAttribute.Codec<>(
                                Ipv4PrefixData.Codec.INSTANCE,
                                (type, data) -> new Pmip6HomeIpv4HoA(data)),
                        Pmip6HomeIpv4HoA::new));

        typeAttributeDefinitionsMap.put(Pmip6VisitedIpv4HoA.TYPE,
                new AttributeDefinition<>(
                        Pmip6VisitedIpv4HoA.TYPE,
                        Pmip6VisitedIpv4HoA.NAME,
                        Pmip6VisitedIpv4HoA.class,
                        Ipv4PrefixData.class,
                        new StandardAttribute.Codec<>(
                                Ipv4PrefixData.Codec.INSTANCE,
                                (type, data) -> new Pmip6VisitedIpv4HoA(data)),
                        Pmip6VisitedIpv4HoA::new));

        typeAttributeDefinitionsMap.put(Pmip6HomeDhcp4ServerAddress.TYPE,
                new AttributeDefinition<>(
                        Pmip6HomeDhcp4ServerAddress.TYPE,
                        Pmip6HomeDhcp4ServerAddress.NAME,
                        Pmip6HomeDhcp4ServerAddress.class,
                        Ipv4AddrData.class,
                        new StandardAttribute.Codec<>(
                                Ipv4AddrData.Codec.INSTANCE,
                                (type, data) -> new Pmip6HomeDhcp4ServerAddress(data)),
                        Pmip6HomeDhcp4ServerAddress::new));

        typeAttributeDefinitionsMap.put(Pmip6VisitedDhcp4ServerAddress.TYPE,
                new AttributeDefinition<>(
                        Pmip6VisitedDhcp4ServerAddress.TYPE,
                        Pmip6VisitedDhcp4ServerAddress.NAME,
                        Pmip6VisitedDhcp4ServerAddress.class,
                        Ipv4AddrData.class,
                        new StandardAttribute.Codec<>(
                                Ipv4AddrData.Codec.INSTANCE,
                                (type, data) -> new Pmip6VisitedDhcp4ServerAddress(data)),
                        Pmip6VisitedDhcp4ServerAddress::new));

        typeAttributeDefinitionsMap.put(Pmip6HomeDhcp6ServerAddress.TYPE,
                new AttributeDefinition<>(
                        Pmip6HomeDhcp6ServerAddress.TYPE,
                        Pmip6HomeDhcp6ServerAddress.NAME,
                        Pmip6HomeDhcp6ServerAddress.class,
                        Ipv6AddrData.class,
                        new StandardAttribute.Codec<>(
                                Ipv6AddrData.Codec.INSTANCE,
                                (type, data) -> new Pmip6HomeDhcp6ServerAddress(data)),
                        Pmip6HomeDhcp6ServerAddress::new));

        typeAttributeDefinitionsMap.put(Pmip6VisitedDhcp6ServerAddress.TYPE,
                new AttributeDefinition<>(
                        Pmip6VisitedDhcp6ServerAddress.TYPE,
                        Pmip6VisitedDhcp6ServerAddress.NAME,
                        Pmip6VisitedDhcp6ServerAddress.class,
                        Ipv6AddrData.class,
                        new StandardAttribute.Codec<>(
                                Ipv6AddrData.Codec.INSTANCE,
                                (type, data) -> new Pmip6VisitedDhcp6ServerAddress(data)),
                        Pmip6VisitedDhcp6ServerAddress::new));

        typeAttributeDefinitionsMap.put(Pmip6HomeIpv4Gateway.TYPE,
                new AttributeDefinition<>(
                        Pmip6HomeIpv4Gateway.TYPE,
                        Pmip6HomeIpv4Gateway.NAME,
                        Pmip6HomeIpv4Gateway.class,
                        Ipv4AddrData.class,
                        new StandardAttribute.Codec<>(
                                Ipv4AddrData.Codec.INSTANCE,
                                (type, data) -> new Pmip6HomeIpv4Gateway(data)),
                        Pmip6HomeIpv4Gateway::new));

        typeAttributeDefinitionsMap.put(Pmip6VisitedIpv4Gateway.TYPE,
                new AttributeDefinition<>(
                        Pmip6VisitedIpv4Gateway.TYPE,
                        Pmip6VisitedIpv4Gateway.NAME,
                        Pmip6VisitedIpv4Gateway.class,
                        Ipv4AddrData.class,
                        new StandardAttribute.Codec<>(
                                Ipv4AddrData.Codec.INSTANCE,
                                (type, data) -> new Pmip6VisitedIpv4Gateway(data)),
                        Pmip6VisitedIpv4Gateway::new));

        typeAttributeDefinitionsMap.put(EapLowerLayer.TYPE,
                new AttributeDefinition<>(
                        EapLowerLayer.TYPE,
                        EapLowerLayer.NAME,
                        EapLowerLayer.class,
                        EnumData.class,
                        new StandardAttribute.Codec<>(
                                EnumData.Codec.INSTANCE,
                                (type, data) -> new EapLowerLayer(data)),
                        EapLowerLayer::new));

        typeAttributeDefinitionsMap.put(GssAcceptorServiceName.TYPE,
                new AttributeDefinition<>(
                        GssAcceptorServiceName.TYPE,
                        GssAcceptorServiceName.NAME,
                        GssAcceptorServiceName.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new GssAcceptorServiceName(data)),
                        GssAcceptorServiceName::new));

        typeAttributeDefinitionsMap.put(GssAcceptorHostName.TYPE,
                new AttributeDefinition<>(
                        GssAcceptorHostName.TYPE,
                        GssAcceptorHostName.NAME,
                        GssAcceptorHostName.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new GssAcceptorHostName(data)),
                        GssAcceptorHostName::new));

        typeAttributeDefinitionsMap.put(GssAcceptorServiceSpecifics.TYPE,
                new AttributeDefinition<>(
                        GssAcceptorServiceSpecifics.TYPE,
                        GssAcceptorServiceSpecifics.NAME,
                        GssAcceptorServiceSpecifics.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new GssAcceptorServiceSpecifics(data)),
                        GssAcceptorServiceSpecifics::new));

        typeAttributeDefinitionsMap.put(GssAcceptorRealmName.TYPE,
                new AttributeDefinition<>(
                        GssAcceptorRealmName.TYPE,
                        GssAcceptorRealmName.NAME,
                        GssAcceptorRealmName.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new GssAcceptorRealmName(data)),
                        GssAcceptorRealmName::new));

        typeAttributeDefinitionsMap.put(FramedIpv6Address.TYPE,
                new AttributeDefinition<>(
                        FramedIpv6Address.TYPE,
                        FramedIpv6Address.NAME,
                        FramedIpv6Address.class,
                        Ipv6AddrData.class,
                        new StandardAttribute.Codec<>(
                                Ipv6AddrData.Codec.INSTANCE,
                                (type, data) -> new FramedIpv6Address(data)),
                        FramedIpv6Address::new));

        typeAttributeDefinitionsMap.put(DnsServerIpv6Address.TYPE,
                new AttributeDefinition<>(
                        DnsServerIpv6Address.TYPE,
                        DnsServerIpv6Address.NAME,
                        DnsServerIpv6Address.class,
                        Ipv6AddrData.class,
                        new StandardAttribute.Codec<>(
                                Ipv6AddrData.Codec.INSTANCE,
                                (type, data) -> new DnsServerIpv6Address(data)),
                        DnsServerIpv6Address::new));

        typeAttributeDefinitionsMap.put(RouteIpv6Information.TYPE,
                new AttributeDefinition<>(
                        RouteIpv6Information.TYPE,
                        RouteIpv6Information.NAME,
                        RouteIpv6Information.class,
                        Ipv6PrefixData.class,
                        new StandardAttribute.Codec<>(
                                Ipv6PrefixData.Codec.INSTANCE,
                                (type, data) -> new RouteIpv6Information(data)),
                        RouteIpv6Information::new));

        typeAttributeDefinitionsMap.put(DelegatedIpv6PrefixPool.TYPE,
                new AttributeDefinition<>(
                        DelegatedIpv6PrefixPool.TYPE,
                        DelegatedIpv6PrefixPool.NAME,
                        DelegatedIpv6PrefixPool.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new DelegatedIpv6PrefixPool(data)),
                        DelegatedIpv6PrefixPool::new));

        typeAttributeDefinitionsMap.put(StatefulIpv6AddressPool.TYPE,
                new AttributeDefinition<>(
                        StatefulIpv6AddressPool.TYPE,
                        StatefulIpv6AddressPool.NAME,
                        StatefulIpv6AddressPool.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new StatefulIpv6AddressPool(data)),
                        StatefulIpv6AddressPool::new));

        typeAttributeDefinitionsMap.put(Ipv66rdConfiguration.TYPE,
                new AttributeDefinition<>(
                        Ipv66rdConfiguration.TYPE,
                        Ipv66rdConfiguration.NAME,
                        Ipv66rdConfiguration.class,
                        TlvData.class,
                        new StandardAttribute.Codec<>(
                                TlvData.Codec.INSTANCE,
                                (type, data) -> new Ipv66rdConfiguration(data)),
                        Ipv66rdConfiguration::new));

        typeAttributeDefinitionsMap.put(AllowedCalledStationId.TYPE,
                new AttributeDefinition<>(
                        AllowedCalledStationId.TYPE,
                        AllowedCalledStationId.NAME,
                        AllowedCalledStationId.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new AllowedCalledStationId(data)),
                        AllowedCalledStationId::new));

        typeAttributeDefinitionsMap.put(EapPeerId.TYPE,
                new AttributeDefinition<>(
                        EapPeerId.TYPE,
                        EapPeerId.NAME,
                        EapPeerId.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new EapPeerId(data)),
                        EapPeerId::new));

        typeAttributeDefinitionsMap.put(EapServerId.TYPE,
                new AttributeDefinition<>(
                        EapServerId.TYPE,
                        EapServerId.NAME,
                        EapServerId.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new EapServerId(data)),
                        EapServerId::new));

        typeAttributeDefinitionsMap.put(MobilityDomainId.TYPE,
                new AttributeDefinition<>(
                        MobilityDomainId.TYPE,
                        MobilityDomainId.NAME,
                        MobilityDomainId.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new MobilityDomainId(data)),
                        MobilityDomainId::new));

        typeAttributeDefinitionsMap.put(PreauthTimeout.TYPE,
                new AttributeDefinition<>(
                        PreauthTimeout.TYPE,
                        PreauthTimeout.NAME,
                        PreauthTimeout.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new PreauthTimeout(data)),
                        PreauthTimeout::new));

        typeAttributeDefinitionsMap.put(NetworkIdName.TYPE,
                new AttributeDefinition<>(
                        NetworkIdName.TYPE,
                        NetworkIdName.NAME,
                        NetworkIdName.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new NetworkIdName(data)),
                        NetworkIdName::new));

        typeAttributeDefinitionsMap.put(EapOLAnnouncement.TYPE,
                new AttributeDefinition<>(
                        EapOLAnnouncement.TYPE,
                        EapOLAnnouncement.NAME,
                        EapOLAnnouncement.class,
                        ConcatData.class,
                        new ConcatAttribute.Codec(
                                (type, data) -> new EapOLAnnouncement(data)),
                        EapOLAnnouncement::new));

        typeAttributeDefinitionsMap.put(WlanHessid.TYPE,
                new AttributeDefinition<>(
                        WlanHessid.TYPE,
                        WlanHessid.NAME,
                        WlanHessid.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new WlanHessid(data)),
                        WlanHessid::new));

        typeAttributeDefinitionsMap.put(WlanVenueInfo.TYPE,
                new AttributeDefinition<>(
                        WlanVenueInfo.TYPE,
                        WlanVenueInfo.NAME,
                        WlanVenueInfo.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new WlanVenueInfo(data)),
                        WlanVenueInfo::new));

        typeAttributeDefinitionsMap.put(WlanVenueLanguage.TYPE,
                new AttributeDefinition<>(
                        WlanVenueLanguage.TYPE,
                        WlanVenueLanguage.NAME,
                        WlanVenueLanguage.class,
                        StringData.class,
                        new StandardAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, data) -> new WlanVenueLanguage(data)),
                        WlanVenueLanguage::new));

        typeAttributeDefinitionsMap.put(WlanVenueName.TYPE,
                new AttributeDefinition<>(
                        WlanVenueName.TYPE,
                        WlanVenueName.NAME,
                        WlanVenueName.class,
                        TextData.class,
                        new StandardAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, data) -> new WlanVenueName(data)),
                        WlanVenueName::new));

        typeAttributeDefinitionsMap.put(WlanReasonCode.TYPE,
                new AttributeDefinition<>(
                        WlanReasonCode.TYPE,
                        WlanReasonCode.NAME,
                        WlanReasonCode.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new WlanReasonCode(data)),
                        WlanReasonCode::new));

        typeAttributeDefinitionsMap.put(WlanPairwiseCipher.TYPE,
                new AttributeDefinition<>(
                        WlanPairwiseCipher.TYPE,
                        WlanPairwiseCipher.NAME,
                        WlanPairwiseCipher.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new WlanPairwiseCipher(data)),
                        WlanPairwiseCipher::new));

        typeAttributeDefinitionsMap.put(WlanGroupCipher.TYPE,
                new AttributeDefinition<>(
                        WlanGroupCipher.TYPE,
                        WlanGroupCipher.NAME,
                        WlanGroupCipher.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new WlanGroupCipher(data)),
                        WlanGroupCipher::new));

        typeAttributeDefinitionsMap.put(WlanAkmSuite.TYPE,
                new AttributeDefinition<>(
                        WlanAkmSuite.TYPE,
                        WlanAkmSuite.NAME,
                        WlanAkmSuite.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new WlanAkmSuite(data)),
                        WlanAkmSuite::new));

        typeAttributeDefinitionsMap.put(WlanGroupMgmtCipher.TYPE,
                new AttributeDefinition<>(
                        WlanGroupMgmtCipher.TYPE,
                        WlanGroupMgmtCipher.NAME,
                        WlanGroupMgmtCipher.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new WlanGroupMgmtCipher(data)),
                        WlanGroupMgmtCipher::new));

        typeAttributeDefinitionsMap.put(WlanRfBand.TYPE,
                new AttributeDefinition<>(
                        WlanRfBand.TYPE,
                        WlanRfBand.NAME,
                        WlanRfBand.class,
                        IntegerData.class,
                        new StandardAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, data) -> new WlanRfBand(data)),
                        WlanRfBand::new));

        typeAttributeDefinitionsMap.put(ExtendedAttribute1.TYPE,
                new AttributeDefinition<>(
                        ExtendedAttribute1.TYPE,
                        ExtendedAttribute1.NAME,
                        ExtendedAttribute1.class,
                        ExtendedData.class,
                        new StandardAttribute.Codec<>(
                                ExtendedData.Codec.INSTANCE,
                                (type, data) -> new ExtendedAttribute1(data)),
                        ExtendedAttribute1::new));

        typeAttributeDefinitionsMap.put(FragStatus.TYPE,
                new AttributeDefinition<>(
                        FragStatus.TYPE,
                        FragStatus.NAME,
                        FragStatus.class,
                        IntegerData.class,
                        new ExtendedAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, extendedType, data) -> new FragStatus(data)),
                        FragStatus::new));

        typeAttributeDefinitionsMap.put(ProxyStateLength.TYPE,
                new AttributeDefinition<>(
                        ProxyStateLength.TYPE,
                        ProxyStateLength.NAME,
                        ProxyStateLength.class,
                        IntegerData.class,
                        new ExtendedAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, extendedType, data) -> new ProxyStateLength(data)),
                        ProxyStateLength::new));

        typeAttributeDefinitionsMap.put(ResponseLength.TYPE,
                new AttributeDefinition<>(
                        ResponseLength.TYPE,
                        ResponseLength.NAME,
                        ResponseLength.class,
                        IntegerData.class,
                        new ExtendedAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, extendedType, data) -> new ResponseLength(data)),
                        ResponseLength::new));

        typeAttributeDefinitionsMap.put(OriginalPacketCode.TYPE,
                new AttributeDefinition<>(
                        OriginalPacketCode.TYPE,
                        OriginalPacketCode.NAME,
                        OriginalPacketCode.class,
                        IntegerData.class,
                        new ExtendedAttribute.Codec<>(
                                IntegerData.Codec.INSTANCE,
                                (type, extendedType, data) -> new OriginalPacketCode(data)),
                        OriginalPacketCode::new));

        typeAttributeDefinitionsMap.put(IpPortLimitInfo.TYPE,
                new AttributeDefinition<>(
                        IpPortLimitInfo.TYPE,
                        IpPortLimitInfo.NAME,
                        IpPortLimitInfo.class,
                        TlvData.class,
                        new ExtendedAttribute.Codec<>(
                                TlvData.Codec.INSTANCE,
                                (type, extendedType, data) -> new IpPortLimitInfo(data)),
                        IpPortLimitInfo::new));

        typeAttributeDefinitionsMap.put(IpPortRange.TYPE,
                new AttributeDefinition<>(
                        IpPortRange.TYPE,
                        IpPortRange.NAME,
                        IpPortRange.class,
                        TlvData.class,
                        new ExtendedAttribute.Codec<>(
                                TlvData.Codec.INSTANCE,
                                (type, extendedType, data) -> new IpPortRange(data)),
                        IpPortRange::new));

        typeAttributeDefinitionsMap.put(IpPortForwardingMap.TYPE,
                new AttributeDefinition<>(
                        IpPortForwardingMap.TYPE,
                        IpPortForwardingMap.NAME,
                        IpPortForwardingMap.class,
                        TlvData.class,
                        new ExtendedAttribute.Codec<>(
                                TlvData.Codec.INSTANCE,
                                (type, extendedType, data) -> new IpPortForwardingMap(data)),
                        IpPortForwardingMap::new));

        typeAttributeDefinitionsMap.put(OperatorNasIdentifier.TYPE,
                new AttributeDefinition<>(
                        OperatorNasIdentifier.TYPE,
                        OperatorNasIdentifier.NAME,
                        OperatorNasIdentifier.class,
                        StringData.class,
                        new ExtendedAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, extendedType, data) -> new OperatorNasIdentifier(data)),
                        OperatorNasIdentifier::new));

        typeAttributeDefinitionsMap.put(Softwire46Configuration.TYPE,
                new AttributeDefinition<>(
                        Softwire46Configuration.TYPE,
                        Softwire46Configuration.NAME,
                        Softwire46Configuration.class,
                        TlvData.class,
                        new ExtendedAttribute.Codec<>(
                                TlvData.Codec.INSTANCE,
                                (type, extendedType, data) -> new Softwire46Configuration(data)),
                        Softwire46Configuration::new));

        typeAttributeDefinitionsMap.put(Softwire46Priority.TYPE,
                new AttributeDefinition<>(
                        Softwire46Priority.TYPE,
                        Softwire46Priority.NAME,
                        Softwire46Priority.class,
                        TlvData.class,
                        new ExtendedAttribute.Codec<>(
                                TlvData.Codec.INSTANCE,
                                (type, extendedType, data) -> new Softwire46Priority(data)),
                        Softwire46Priority::new));

        typeAttributeDefinitionsMap.put(Softwire46Multicast.TYPE,
                new AttributeDefinition<>(
                        Softwire46Multicast.TYPE,
                        Softwire46Multicast.NAME,
                        Softwire46Multicast.class,
                        TlvData.class,
                        new ExtendedAttribute.Codec<>(
                                TlvData.Codec.INSTANCE,
                                (type, extendedType, data) -> new Softwire46Multicast(data)),
                        Softwire46Multicast::new));

        typeAttributeDefinitionsMap.put(ExtendedVendorSpecific1.TYPE,
                new AttributeDefinition<>(
                        ExtendedVendorSpecific1.TYPE,
                        ExtendedVendorSpecific1.NAME,
                        ExtendedVendorSpecific1.class,
                        EvsData.class,
                        new ExtendedAttribute.Codec<>(
                                EvsData.Codec.INSTANCE,
                                (type, extendedType, data) -> new ExtendedVendorSpecific1(data)),
                        ExtendedVendorSpecific1::new));

        typeAttributeDefinitionsMap.put(ExtendedAttribute2.TYPE,
                new AttributeDefinition<>(
                        ExtendedAttribute2.TYPE,
                        ExtendedAttribute2.NAME,
                        ExtendedAttribute2.class,
                        ExtendedData.class,
                        new StandardAttribute.Codec<>(
                                ExtendedData.Codec.INSTANCE,
                                (type, data) -> new ExtendedAttribute2(data)),
                        ExtendedAttribute2::new));

        typeAttributeDefinitionsMap.put(ExtendedVendorSpecific2.TYPE,
                new AttributeDefinition<>(
                        ExtendedVendorSpecific2.TYPE,
                        ExtendedVendorSpecific2.NAME,
                        ExtendedVendorSpecific2.class,
                        EvsData.class,
                        new ExtendedAttribute.Codec<>(
                                EvsData.Codec.INSTANCE,
                                (type, extendedType, data) -> new ExtendedVendorSpecific2(data)),
                        ExtendedVendorSpecific2::new));

        typeAttributeDefinitionsMap.put(ExtendedAttribute3.TYPE,
                new AttributeDefinition<>(
                        ExtendedAttribute3.TYPE,
                        ExtendedAttribute3.NAME,
                        ExtendedAttribute3.class,
                        ExtendedData.class,
                        new StandardAttribute.Codec<>(
                                ExtendedData.Codec.INSTANCE,
                                (type, data) -> new ExtendedAttribute3(data)),
                        ExtendedAttribute3::new));

        typeAttributeDefinitionsMap.put(ExtendedVendorSpecific3.TYPE,
                new AttributeDefinition<>(
                        ExtendedVendorSpecific3.TYPE,
                        ExtendedVendorSpecific3.NAME,
                        ExtendedVendorSpecific3.class,
                        EvsData.class,
                        new ExtendedAttribute.Codec<>(
                                EvsData.Codec.INSTANCE,
                                (type, extendedType, data) -> new ExtendedVendorSpecific3(data)),
                        ExtendedVendorSpecific3::new));

        typeAttributeDefinitionsMap.put(ExtendedAttribute4.TYPE,
                new AttributeDefinition<>(
                        ExtendedAttribute4.TYPE,
                        ExtendedAttribute4.NAME,
                        ExtendedAttribute4.class,
                        ExtendedData.class,
                        new StandardAttribute.Codec<>(
                                ExtendedData.Codec.INSTANCE,
                                (type, data) -> new ExtendedAttribute4(data)),
                        ExtendedAttribute4::new));

        typeAttributeDefinitionsMap.put(ExtendedVendorSpecific4.TYPE,
                new AttributeDefinition<>(
                        ExtendedVendorSpecific4.TYPE,
                        ExtendedVendorSpecific4.NAME,
                        ExtendedVendorSpecific4.class,
                        EvsData.class,
                        new ExtendedAttribute.Codec<>(
                                EvsData.Codec.INSTANCE,
                                (type, extendedType, data) -> new ExtendedVendorSpecific4(data)),
                        ExtendedVendorSpecific4::new));

        typeAttributeDefinitionsMap.put(ExtendedAttribute5.TYPE,
                new AttributeDefinition<>(
                        ExtendedAttribute5.TYPE,
                        ExtendedAttribute5.NAME,
                        ExtendedAttribute5.class,
                        LongExtendedData.class,
                        new StandardAttribute.Codec<>(
                                LongExtendedData.Codec.INSTANCE,
                                (type, data) -> new ExtendedAttribute5(data)),
                        ExtendedAttribute5::new));

        typeAttributeDefinitionsMap.put(SamlAssertion.TYPE,
                new AttributeDefinition<>(
                        SamlAssertion.TYPE,
                        SamlAssertion.NAME,
                        SamlAssertion.class,
                        TextData.class,
                        new LongExtendedAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, extendedType, data) -> new SamlAssertion(data)),
                        SamlAssertion::new));

        typeAttributeDefinitionsMap.put(SamlProtocol.TYPE,
                new AttributeDefinition<>(
                        SamlProtocol.TYPE,
                        SamlProtocol.NAME,
                        SamlProtocol.class,
                        TextData.class,
                        new LongExtendedAttribute.Codec<>(
                                TextData.Codec.INSTANCE,
                                (type, extendedType, data) -> new SamlProtocol(data)),
                        SamlProtocol::new));

        typeAttributeDefinitionsMap.put(DhcpV6Options.TYPE,
                new AttributeDefinition<>(
                        DhcpV6Options.TYPE,
                        DhcpV6Options.NAME,
                        DhcpV6Options.class,
                        StringData.class,
                        new LongExtendedAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, extendedType, data) -> new DhcpV6Options(data)),
                        DhcpV6Options::new));

        typeAttributeDefinitionsMap.put(DhcpV4Options.TYPE,
                new AttributeDefinition<>(
                        DhcpV4Options.TYPE,
                        DhcpV4Options.NAME,
                        DhcpV4Options.class,
                        StringData.class,
                        new LongExtendedAttribute.Codec<>(
                                StringData.Codec.INSTANCE,
                                (type, extendedType, data) -> new DhcpV4Options(data)),
                        DhcpV4Options::new));

        typeAttributeDefinitionsMap.put(ExtendedVendorSpecific5.TYPE,
                new AttributeDefinition<>(
                        ExtendedVendorSpecific5.TYPE,
                        ExtendedVendorSpecific5.NAME,
                        ExtendedVendorSpecific5.class,
                        EvsData.class,
                        new LongExtendedAttribute.Codec<>(
                                EvsData.Codec.INSTANCE,
                                (type, extendedType, data) -> new ExtendedVendorSpecific5(data)),
                        ExtendedVendorSpecific5::new));

        typeAttributeDefinitionsMap.put(ExtendedAttribute6.TYPE,
                new AttributeDefinition<>(
                        ExtendedAttribute6.TYPE,
                        ExtendedAttribute6.NAME,
                        ExtendedAttribute6.class,
                        LongExtendedData.class,
                        new StandardAttribute.Codec<>(
                                LongExtendedData.Codec.INSTANCE,
                                (type, data) -> new ExtendedAttribute6(data)),
                        ExtendedAttribute6::new));

        typeAttributeDefinitionsMap.put(ExtendedVendorSpecific6.TYPE,
                new AttributeDefinition<>(
                        ExtendedVendorSpecific6.TYPE,
                        ExtendedVendorSpecific6.NAME,
                        ExtendedVendorSpecific6.class,
                        EvsData.class,
                        new LongExtendedAttribute.Codec<>(
                                EvsData.Codec.INSTANCE,
                                (type, extendedType, data) -> new ExtendedVendorSpecific6(data)),
                        ExtendedVendorSpecific6::new));

        typeAttributeDefinitionsMap.forEach((attributeType, attributeDefinition) ->
                nameAttributeDefinitionsMap.put(attributeDefinition.getName().toLowerCase(Locale.ROOT),
                        attributeDefinition));
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
    public AttributeDefinition<?, ?> getAttributeDefinition(AttributeType type) {
        Objects.requireNonNull(type);

        return typeAttributeDefinitionsMap.getOrDefault(type, null);
    }

    @Override
    public AttributeDefinition<?, ?> getAttributeDefinition(String name) {
        Objects.requireNonNull(name);

        return nameAttributeDefinitionsMap.getOrDefault(name.toLowerCase(Locale.ROOT), null);
    }

    @Override
    public Integer getNumericAttributeValue(AttributeType type, String name) {
        Map<String, Integer> valueMap = numericAttributeValueMap.get(type);

        if (valueMap == null) {
            return null;
        }

        return valueMap.getOrDefault(name.toLowerCase(Locale.ROOT), null);
    }

    @Override
    public TlvDefinition getTlvDefinition(AttributeType type) {
        Objects.requireNonNull(type);

        return tlvDefinitionsMap.getOrDefault(type, null);
    }

}
