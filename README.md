# AAA4J-RADIUS

![GitHub](https://img.shields.io/github/license/aaa4j/aaa4j-radius)
[![Maven Central](https://img.shields.io/maven-central/v/org.aaa4j.radius/aaa4j-radius)](https://search.maven.org/search?q=org.aaa4j.radius)
[![GitHub Workflow Status (branch)](https://img.shields.io/github/workflow/status/aaa4j/aaa4j-radius/Build/master)](https://github.com/aaa4j/aaa4j-radius/actions?query=workflow%3ABuild+branch%3Amaster)
[![GitHub Repo stars](https://img.shields.io/github/stars/aaa4j/aaa4j-radius)](https://github.com/aaa4j/aaa4j-radius)

Java library for building RADIUS clients and RADIUS servers.

## Features

* RADIUS client implementation
* RADIUS server implementation
* Standard RADIUS data types
* Standard RADIUS dictionary
* Custom dictionary support
* FreeRADIUS dictionary support
* Java 8+ support
* Apache-2.0 license

## Usage

### Client

Add `aaa4j-radius-client` dependency from [Maven Central](https://central.sonatype.com/artifact/org.aaa4j.radius/aaa4j-radius-client):

```xml
<dependency>
    <groupId>org.aaa4j.radius</groupId>
    <artifactId>aaa4j-radius-client</artifactId>
    <version>0.3.1</version>
</dependency>
```

Build a `RadiusClient` using `UdpRadiusClient.newBuilder()` and send a request packet using `send()`:

```java
package org.aaa4j.radius.examples;

import org.aaa4j.radius.client.RadiusClient;
import org.aaa4j.radius.client.RadiusClientException;
import org.aaa4j.radius.client.clients.UdpRadiusClient;
import org.aaa4j.radius.core.attribute.StringData;
import org.aaa4j.radius.core.attribute.TextData;
import org.aaa4j.radius.core.attribute.attributes.NasIdentifier;
import org.aaa4j.radius.core.attribute.attributes.UserName;
import org.aaa4j.radius.core.attribute.attributes.UserPassword;
import org.aaa4j.radius.core.packet.Packet;
import org.aaa4j.radius.core.packet.packets.AccessAccept;
import org.aaa4j.radius.core.packet.packets.AccessRequest;

import java.net.InetSocketAddress;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Main {

    public static void main(String[] args) {
        RadiusClient radiusClient = UdpRadiusClient.newBuilder()
                .secret("sharedsecret".getBytes(UTF_8))
                .address(new InetSocketAddress("10.1.1.10", 1812))
                .build();

        AccessRequest accessRequest = new AccessRequest(List.of(
                new MessageAuthenticator(),
                new UserName(new TextData("john.doe")),
                new UserPassword(new StringData("hunter2".getBytes(UTF_8))),
                new NasIdentifier(new TextData("SSID1"))
        ));

        try {
            Packet responsePacket = radiusClient.send(accessRequest);

            if (responsePacket instanceof AccessAccept) {
                System.out.println("Accepted");
            }
            else {
                System.out.println("Rejected");
            }
        }
        catch (RadiusClientException e) {
            e.printStackTrace();
        }
    }

}
```

### Server

Add `aaa4j-radius-server` dependency  from [Maven Central](https://central.sonatype.com/artifact/org.aaa4j.radius/aaa4j-radius-server):

```xml
<dependency>
    <groupId>org.aaa4j.radius</groupId>
    <artifactId>aaa4j-radius-server</artifactId>
    <version>0.3.1</version>
</dependency>
```

Implement `RadiusServer.Handler` to handle RADIUS clients and packets, build a `RadiusServer` using `UdpRadiusServer.newBuilder()`, and start the server using `start()`:

```java
package org.aaa4j.radius.examples;

import org.aaa4j.radius.core.attribute.attributes.UserName;
import org.aaa4j.radius.core.attribute.attributes.UserPassword;
import org.aaa4j.radius.core.packet.Packet;
import org.aaa4j.radius.core.packet.packets.AccessAccept;
import org.aaa4j.radius.core.packet.packets.AccessReject;
import org.aaa4j.radius.core.packet.packets.AccessRequest;
import org.aaa4j.radius.server.RadiusServer;
import org.aaa4j.radius.server.servers.UdpRadiusServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Main {

    public static void main(String[] args) throws Exception {
        RadiusServer radiusServer = UdpRadiusServer.newBuilder()
                .bindAddress(new InetSocketAddress(1812))
                .handler(new RadiusHandler())
                .build();

        radiusServer.start();
    }

    private static class RadiusHandler implements RadiusServer.Handler {

        @Override
        public byte[] handleClient(InetAddress clientAddress) {
            if (clientAddress.getHostAddress().equals("10.5.5.50")) {
                return "sharedsecret".getBytes(UTF_8);
            }

            return null;
        }

        @Override
        public Packet handlePacket(InetAddress clientAddress, Packet requestPacket) {
            if (requestPacket instanceof AccessRequest) {
                if (requestPacket.getAttribute(MessageAuthenticator.class).isEmpty()) {
                    // Require Message-Authenticator to mitigate Blast-RADIUS
                    return null;
                }
                
                Optional<UserName> userNameAttribute = requestPacket.getAttribute(UserName.class);
                Optional<UserPassword> userPasswordAttribute = requestPacket.getAttribute(UserPassword.class);

                if (userNameAttribute.isPresent() && userPasswordAttribute.isPresent()) {
                    String username = userNameAttribute.get().getData().getValue();
                    String password = new String(userPasswordAttribute.get().getData().getValue(), UTF_8);

                    if (username.equals("john.doe") && password.equals("hunter2")) {
                        return new AccessAccept(List.of(new MessageAuthenticator()));
                    }
                }

                return new AccessReject(List.of(new MessageAuthenticator()));
            }

            return null;
        }

    }

}
```

## License

```
Copyright 2020 The AAA4J-RADIUS Authors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
