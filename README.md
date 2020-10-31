# AAA4J-RADIUS

Java RADIUS library for building RADIUS clients and RADIUS servers.

## Features

* RADIUS client implementation
* RADIUS server implementation
* Standard RADIUS data types (`integer`, `text`, `string`, `concat`, `ipv4addr`, `vsa`)
* Standard RADIUS dictionary (packets, attributes)
* Custom dictionary support
* Java 8+ support
* Java 9+ modules (JPMS)

## Usage

### Client

RADIUS client example:

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

RADIUS server example:

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
                Optional<UserName> userNameAttribute = requestPacket.getAttribute(UserName.class);
                Optional<UserPassword> userPasswordAttribute = requestPacket.getAttribute(UserPassword.class);

                if (userNameAttribute.isPresent() && userPasswordAttribute.isPresent()) {
                    String username = userNameAttribute.get().getData().getValue();
                    String password = new String(userPasswordAttribute.get().getData().getValue(), UTF_8);

                    if (username.equals("john.doe") && password.equals("hunter2")) {
                        return new AccessAccept();
                    }
                }

                return new AccessReject();
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
