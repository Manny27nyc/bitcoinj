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
 * Copyright 2012 Matt Corallo.
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

package org.bitcoinj.testing;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.bitcoinj.core.BloomFilter;
import org.bitcoinj.core.MemoryPoolMessage;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.SendAddrV2Message;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.VersionAck;
import org.bitcoinj.core.VersionMessage;
import org.bitcoinj.net.BlockingClientManager;
import org.bitcoinj.net.ClientConnectionManager;
import org.bitcoinj.net.NioClientManager;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.utils.ContextPropagatingThreadFactory;
import org.junit.Rule;
import org.junit.rules.Timeout;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * You can derive from this class and call peerGroup.start() in your tests to get a functional PeerGroup that can be
 * used with loopback peers created using connectPeer. This involves real TCP connections so is a pretty accurate
 * mock, but means unit tests cannot be run simultaneously.
 */
public class TestWithPeerGroup extends TestWithNetworkConnections {
    protected PeerGroup peerGroup;

    protected VersionMessage remoteVersionMessage;
    private final ClientType clientType;

    public TestWithPeerGroup(ClientType clientType) {
        super(clientType);
        if (clientType != ClientType.NIO_CLIENT_MANAGER && clientType != ClientType.BLOCKING_CLIENT_MANAGER)
            throw new RuntimeException();
        this.clientType = clientType;
    }

    @Rule
    public Timeout globalTimeout = Timeout.seconds(15);

    @Override
    public void setUp() throws Exception {
        setUp(new MemoryBlockStore(UNITTEST));
    }

    @Override
    public void setUp(BlockStore blockStore) throws Exception {
        super.setUp(blockStore);

        remoteVersionMessage = new VersionMessage(UNITTEST, 1);
        remoteVersionMessage.localServices =
                VersionMessage.NODE_NETWORK | VersionMessage.NODE_BLOOM | VersionMessage.NODE_WITNESS;
        remoteVersionMessage.clientVersion =
                NetworkParameters.ProtocolVersion.WITNESS_VERSION.getBitcoinProtocolVersion();
        blockJobs = false;
        initPeerGroup();
    }

    @Override
    public void tearDown() {
        try {
            super.tearDown();
            blockJobs = false;
            if (peerGroup.isRunning())
                peerGroup.stopAsync();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void initPeerGroup() {
        if (clientType == ClientType.NIO_CLIENT_MANAGER)
            peerGroup = createPeerGroup(new NioClientManager());
        else
            peerGroup = createPeerGroup(new BlockingClientManager());
        peerGroup.setPingIntervalMsec(0);  // Disable the pings as they just get in the way of most tests.
        peerGroup.addWallet(wallet);
        peerGroup.setUseLocalhostPeerWhenPossible(false); // Prevents from connecting to bitcoin nodes on localhost.
    }

    protected boolean blockJobs = false;
    protected final Semaphore jobBlocks = new Semaphore(0);

    private PeerGroup createPeerGroup(final ClientConnectionManager manager) {
        return new PeerGroup(UNITTEST, blockChain, manager) {
            @Override
            protected ListeningScheduledExecutorService createPrivateExecutor() {
                return MoreExecutors.listeningDecorator(new ScheduledThreadPoolExecutor(1, new ContextPropagatingThreadFactory("PeerGroup test thread")) {
                    @Override
                    public ScheduledFuture<?> schedule(final Runnable command, final long delay, final TimeUnit unit) {
                        if (!blockJobs)
                            return super.schedule(command, delay, unit);
                        return super.schedule(() -> {
                            Utils.rollMockClockMillis(unit.toMillis(delay));
                            command.run();
                            jobBlocks.acquireUninterruptibly();
                        }, 0 /* immediate */, unit);
                    }
                });
            }
        };
    }

    protected InboundMessageQueuer connectPeerWithoutVersionExchange(int id) throws Exception {
        Preconditions.checkArgument(id < PEER_SERVERS);
        InetSocketAddress remoteAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), 2000 + id);
        Peer peer = peerGroup.connectTo(remoteAddress).getConnectionOpenFuture().get();
        InboundMessageQueuer writeTarget = newPeerWriteTargetQueue.take();
        writeTarget.peer = peer;
        return writeTarget;
    }
    
    protected InboundMessageQueuer connectPeer(int id) throws Exception {
        return connectPeer(id, remoteVersionMessage);
    }

    protected InboundMessageQueuer connectPeer(int id, VersionMessage versionMessage) throws Exception {
        checkArgument(versionMessage.hasBlockChain());
        InboundMessageQueuer writeTarget = connectPeerWithoutVersionExchange(id);
        // Complete handshake with the peer - send/receive version(ack)s, receive bloom filter
        writeTarget.sendMessage(versionMessage);
        writeTarget.sendMessage(new VersionAck());
        stepThroughInit(versionMessage, writeTarget);
        return writeTarget;
    }

    // handle peer discovered by PeerGroup
    protected InboundMessageQueuer handleConnectToPeer(int id) throws Exception {
        return handleConnectToPeer(id, remoteVersionMessage);
    }

    // handle peer discovered by PeerGroup
    protected InboundMessageQueuer handleConnectToPeer(int id, VersionMessage versionMessage) throws Exception {
        InboundMessageQueuer writeTarget = newPeerWriteTargetQueue.take();
        checkArgument(versionMessage.hasBlockChain());
        // Complete handshake with the peer - send/receive version(ack)s, receive bloom filter
        writeTarget.sendMessage(versionMessage);
        writeTarget.sendMessage(new VersionAck());
        stepThroughInit(versionMessage, writeTarget);
        return writeTarget;
    }

    private void stepThroughInit(VersionMessage versionMessage, InboundMessageQueuer writeTarget) throws InterruptedException {
        checkState(writeTarget.nextMessageBlocking() instanceof VersionMessage);
        checkState(writeTarget.nextMessageBlocking() instanceof SendAddrV2Message);
        checkState(writeTarget.nextMessageBlocking() instanceof VersionAck);
        if (versionMessage.isBloomFilteringSupported()) {
            checkState(writeTarget.nextMessageBlocking() instanceof BloomFilter);
            checkState(writeTarget.nextMessageBlocking() instanceof MemoryPoolMessage);
        }
    }
}
