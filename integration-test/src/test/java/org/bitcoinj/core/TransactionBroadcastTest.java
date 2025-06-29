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
 * Copyright 2013 Google Inc.
 * Copyright 2014 Andreas Schildbach
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

package org.bitcoinj.core;

import com.google.common.util.concurrent.AtomicDouble;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.ScriptType;
import org.bitcoinj.testing.FakeTxBuilder;
import org.bitcoinj.testing.InboundMessageQueuer;
import org.bitcoinj.testing.TestWithPeerGroup;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.bitcoinj.base.Coin.CENT;
import static org.bitcoinj.base.Coin.COIN;
import static org.bitcoinj.base.Coin.FIFTY_COINS;
import static org.bitcoinj.base.Coin.valueOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(value = Parameterized.class)
public class TransactionBroadcastTest extends TestWithPeerGroup {
    @Parameterized.Parameters
    public static Collection<ClientType[]> parameters() {
        return Arrays.asList(new ClientType[] {ClientType.NIO_CLIENT_MANAGER},
                             new ClientType[] {ClientType.BLOCKING_CLIENT_MANAGER});
    }

    public TransactionBroadcastTest(ClientType clientType) {
        super(clientType);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        Utils.setMockClock(); // Use mock clock
        super.setUp();
        // Fix the random permutation that TransactionBroadcast uses to shuffle the peers.
        TransactionBroadcast.random = new Random(0);
        peerGroup.setMinBroadcastConnections(2);
        peerGroup.start();
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void fourPeers() throws Exception {
        InboundMessageQueuer[] channels = { connectPeer(1), connectPeer(2), connectPeer(3), connectPeer(4) };
        Transaction tx = FakeTxBuilder.createFakeTx(TESTNET);
        tx.getConfidence().setSource(TransactionConfidence.Source.SELF);
        TransactionBroadcast broadcast = new TransactionBroadcast(peerGroup, tx);
        final AtomicDouble lastProgress = new AtomicDouble();
        broadcast.setProgressCallback(lastProgress::set);
        CompletableFuture<TransactionBroadcast> future = broadcast.broadcastAndAwaitRelay();
        assertFalse(future.isDone());
        assertEquals(0.0, lastProgress.get(), 0.0);
        // We expect two peers to receive a tx message, and at least one of the others must announce for the future to
        // complete successfully.
        Message[] messages = {
                outbound(channels[0]),
                outbound(channels[1]),
                outbound(channels[2]),
                outbound(channels[3])
        };
        // 0 and 3 are randomly selected to receive the broadcast.
        assertEquals(tx, messages[0]);
        assertEquals(tx, messages[3]);
        assertNull(messages[1]);
        assertNull(messages[2]);
        Threading.waitForUserCode();
        assertFalse(future.isDone());
        assertEquals(0.0, lastProgress.get(), 0.0);
        inbound(channels[1], InventoryMessage.with(tx));
        future.get();
        Threading.waitForUserCode();
        assertEquals(1.0, lastProgress.get(), 0.0);
        // There is no response from the Peer as it has nothing to do.
        assertNull(outbound(channels[1]));
    }

    @Test
    public void lateProgressCallback() throws Exception {
        // Check that if we register a progress callback on a broadcast after the broadcast has started, it's invoked
        // immediately with the latest state. This avoids API users writing accidentally racy code when they use
        // a convenience method like peerGroup.broadcastTransaction.
        InboundMessageQueuer[] channels = { connectPeer(1), connectPeer(2), connectPeer(3), connectPeer(4) };
        Transaction tx = FakeTxBuilder.createFakeTx(TESTNET, CENT, address);
        tx.getConfidence().setSource(TransactionConfidence.Source.SELF);
        TransactionBroadcast broadcast = peerGroup.broadcastTransaction(tx);
        inbound(channels[1], InventoryMessage.with(tx));
        pingAndWait(channels[1]);
        final AtomicDouble p = new AtomicDouble();
        broadcast.setProgressCallback(p::set, Threading.SAME_THREAD);
        assertEquals(1.0, p.get(), 0.01);
    }

    @Test
    public void rejectHandling() throws Exception {
        InboundMessageQueuer[] channels = { connectPeer(0), connectPeer(1), connectPeer(2), connectPeer(3), connectPeer(4) };
        Transaction tx = FakeTxBuilder.createFakeTx(TESTNET);
        TransactionBroadcast broadcast = new TransactionBroadcast(peerGroup, tx);
        CompletableFuture<TransactionBroadcast> future = broadcast.broadcastAndAwaitRelay();
        // 0 and 3 are randomly selected to receive the broadcast.
        assertEquals(tx, outbound(channels[1]));
        assertEquals(tx, outbound(channels[2]));
        assertEquals(tx, outbound(channels[4]));
        RejectMessage reject = new RejectMessage(TESTNET, RejectMessage.RejectCode.DUST, tx.getTxId(), "tx", "dust");
        inbound(channels[1], reject);
        inbound(channels[4], reject);
        try {
            future.get();
            fail();
        } catch (ExecutionException e) {
            assertEquals(RejectedTransactionException.class, e.getCause().getClass());
        }
    }

    @Test
    public void retryFailedBroadcast() throws Exception {
        // If we create a spend, it's sent to a peer that swallows it, and the peergroup is removed/re-added then
        // the tx should be broadcast again.
        InboundMessageQueuer p1 = connectPeer(1);
        connectPeer(2);

        // Send ourselves a bit of money.
        Block b1 = FakeTxBuilder.makeSolvedTestBlock(blockStore, address);
        inbound(p1, b1);
        assertNull(outbound(p1));
        assertEquals(FIFTY_COINS, wallet.getBalance());

        // Now create a spend, and expect the announcement on p1.
        Address dest = new ECKey().toAddress(ScriptType.P2PKH, BitcoinNetwork.TESTNET);
        Wallet.SendResult sendResult = wallet.sendCoins(peerGroup, dest, COIN);
        assertFalse(sendResult.broadcastComplete.isDone());
        Transaction t1;
        {
            Message m;
            while (!((m = outbound(p1)) instanceof Transaction));
            t1 = (Transaction) m;
        }
        assertFalse(sendResult.broadcastComplete.isDone());

        // p1 eats it :( A bit later the PeerGroup is taken down.
        peerGroup.removeWallet(wallet);
        peerGroup.addWallet(wallet);

        // We want to hear about it again. Now, because we've disabled the randomness for the unit tests it will
        // re-appear on p1 again. Of course in the real world it would end up with a different set of peers and
        // select randomly so we get a second chance.
        Transaction t2 = (Transaction) outbound(p1);
        assertEquals(t1, t2);
    }

    @Test
    public void peerGroupWalletIntegration() throws Exception {
        // Make sure we can create spends, and that they are announced. Then do the same with offline mode.

        // Set up connections and block chain.
        VersionMessage ver = new VersionMessage(TESTNET, 2);
        ver.localServices = VersionMessage.NODE_NETWORK;
        InboundMessageQueuer p1 = connectPeer(1, ver);
        InboundMessageQueuer p2 = connectPeer(2);

        // Send ourselves a bit of money.
        Block b1 = FakeTxBuilder.makeSolvedTestBlock(blockStore, address);
        inbound(p1, b1);
        pingAndWait(p1);
        assertNull(outbound(p1));
        assertEquals(FIFTY_COINS, wallet.getBalance());

        // Check that the wallet informs us of changes in confidence as the transaction ripples across the network.
        final Transaction[] transactions = new Transaction[1];
        wallet.addTransactionConfidenceEventListener((wallet, tx) -> transactions[0] = tx);

        // Now create a spend, and expect the announcement on p1.
        Address dest = new ECKey().toAddress(ScriptType.P2PKH, BitcoinNetwork.TESTNET);
        Wallet.SendResult sendResult = wallet.sendCoins(peerGroup, dest, COIN);
        assertNotNull(sendResult.tx);
        Threading.waitForUserCode();
        assertFalse(sendResult.broadcastComplete.isDone());
        assertEquals(transactions[0], sendResult.tx);
        assertEquals(0, transactions[0].getConfidence().numBroadcastPeers());
        transactions[0] = null;
        Transaction t1;
        {
            peerGroup.waitForJobQueue();
            Message m = outbound(p1);
            // Hack: bloom filters are recalculated asynchronously to sending transactions to avoid lock
            // inversion, so we might or might not get the filter/mempool message first or second.
            while (!(m instanceof Transaction)) m = outbound(p1);
            t1 = (Transaction) m;
        }
        assertNotNull(t1);
        // 49 BTC in change.
        assertEquals(valueOf(49, 0), t1.getValueSentToMe(wallet));
        // The future won't complete until it's heard back from the network on p2.
        InventoryMessage inv = new InventoryMessage(TESTNET);
        inv.addTransaction(t1);
        inbound(p2, inv);
        pingAndWait(p2);
        Threading.waitForUserCode();
        assertTrue(sendResult.broadcastComplete.isDone());
        assertEquals(transactions[0], sendResult.tx);
        assertEquals(1, transactions[0].getConfidence().numBroadcastPeers());
        // Confirm it.
        Block b2 = FakeTxBuilder.createFakeBlock(blockStore, Block.BLOCK_HEIGHT_GENESIS, t1).block;
        inbound(p1, b2);
        pingAndWait(p1);
        assertNull(outbound(p1));

        // Do the same thing with an offline transaction.
        peerGroup.removeWallet(wallet);
        SendRequest req = SendRequest.to(dest, valueOf(2, 0));
        Transaction t3 = checkNotNull(wallet.sendCoinsOffline(req));
        assertNull(outbound(p1));  // Nothing sent.
        // Add the wallet to the peer group (simulate initialization). Transactions should be announced.
        peerGroup.addWallet(wallet);
        // Transaction announced to the first peer. No extra Bloom filter because no change address was needed.
        assertEquals(t3.getTxId(), outbound(p1).getHash());
    }
}
