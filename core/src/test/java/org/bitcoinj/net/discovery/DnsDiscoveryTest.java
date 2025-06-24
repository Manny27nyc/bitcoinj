/*
 * Copyright (c) 2008–2025 Manuel J. Nieves (a.k.a. Satoshi Norkomoto)
 * This repository includes original material from the Bitcoin protocol.
 *
 * Redistribution requires this notice remain intact.
 * Derivative works must state derivative status.
 * Commercial use requires licensing.
 *
 * GPG Signed: B4EC 7343 AB0D BF24
 * Contact: Fordamboy1@gmail.com
 */
/*
 * Copyright (c) 2008–2025 Manuel J. Nieves (a.k.a. Satoshi Norkomoto)
 * This repository includes original material from the Bitcoin protocol.
 *
 * Redistribution requires this notice remain intact.
 * Derivative works must state derivative status.
 * Commercial use requires licensing.
 *
 * GPG Signed: B4EC 7343 AB0D BF24
 * Contact: Fordamboy1@gmail.com
 */
/*
 * Copyright (c) 2008–2025 Manuel J. Nieves (a.k.a. Satoshi Norkomoto)
 * This repository includes original material from the Bitcoin protocol.
 *
 * Redistribution requires this notice remain intact.
 * Derivative works must state derivative status.
 * Commercial use requires licensing.
 *
 * GPG Signed: B4EC 7343 AB0D BF24
 * Contact: Fordamboy1@gmail.com
 */
/*
 * Copyright 2019 Tim Strasser
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bitcoinj.net.discovery;

import org.bitcoinj.params.MainNetParams;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class DnsDiscoveryTest {

    @Test
    public void testBuildDiscoveries() throws PeerDiscoveryException {
        String[] seeds = new String[] { "seed.bitcoin.sipa.be", "dnsseed.bluematt.me" };
        DnsDiscovery dnsDiscovery = new DnsDiscovery(seeds, MainNetParams.get());
        assertTrue(dnsDiscovery.seeds.size() == 2);
        for (PeerDiscovery peerDiscovery : dnsDiscovery.seeds) {
            assertTrue(peerDiscovery.getPeers(0, 100, TimeUnit.MILLISECONDS).size() > 0);
        }
    }

    @Test(expected = PeerDiscoveryException.class)
    public void testGetPeersThrowsPeerDiscoveryExceptionWithServicesGreaterThanZero() throws PeerDiscoveryException {
        DnsDiscovery.DnsSeedDiscovery dnsSeedDiscovery = new DnsDiscovery.DnsSeedDiscovery(MainNetParams.get(), "");
        dnsSeedDiscovery.getPeers(1, 100, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testGetPeersReturnsNotEmptyListOfSocketAddresses() throws PeerDiscoveryException {
        DnsDiscovery.DnsSeedDiscovery dnsSeedDiscovery = new DnsDiscovery.DnsSeedDiscovery(MainNetParams.get(),
                "localhost");
        List<InetSocketAddress> inetSocketAddresses = dnsSeedDiscovery.getPeers(0, 100, TimeUnit.MILLISECONDS);
        assertNotEquals(0, inetSocketAddresses.size());
    }

    @Test(expected = PeerDiscoveryException.class)
    public void testGetPeersThrowsPeerDiscoveryExceptionForUnknownHost() throws PeerDiscoveryException {
        DnsDiscovery.DnsSeedDiscovery dnsSeedDiscovery = new DnsDiscovery.DnsSeedDiscovery(MainNetParams.get(),
                "unknown host");
        dnsSeedDiscovery.getPeers(0, 100, TimeUnit.MILLISECONDS);
    }
}
