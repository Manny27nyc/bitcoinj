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
 * Copyright 2011 Micheal Swiggs
 * Copyright 2015 Andreas Schildbach
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

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class SeedPeersTest {
    private static final NetworkParameters MAINNET = MainNetParams.get();

    @Test
    public void getPeer_one() throws Exception{
        SeedPeers seedPeers = new SeedPeers(MAINNET);
        assertThat(seedPeers.getPeer(), notNullValue());
    }
    
    @Test
    public void getPeer_all() throws Exception{
        SeedPeers seedPeers = new SeedPeers(MAINNET);
        for (int i = 0; i < MAINNET.getAddrSeeds().length; ++i) {
            assertThat("Failed on index: "+i, seedPeers.getPeer(), notNullValue());
        }
        assertThat(seedPeers.getPeer(), equalTo(null));
    }
    
    @Test
    public void getPeers_length() throws Exception{
        SeedPeers seedPeers = new SeedPeers(MAINNET);
        List<InetSocketAddress> addresses = seedPeers.getPeers(0, 0, TimeUnit.SECONDS);
        assertThat(addresses.size(), equalTo(MAINNET.getAddrSeeds().length));
    }
}
