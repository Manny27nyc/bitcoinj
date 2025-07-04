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
 * Copyright 2012 Google Inc.
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

package org.bitcoinj.store;

import com.google.common.io.ByteStreams;
import com.google.protobuf.ByteString;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.ScriptType;
import org.bitcoinj.base.utils.ByteUtils;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.BlockTest;
import org.bitcoinj.base.Coin;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerAddress;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Transaction.Purpose;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionConfidence.ConfidenceType;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.testing.FakeTxBuilder;
import org.bitcoinj.testing.FooWalletExtension;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.MarriedKeyChain;
import org.bitcoinj.wallet.Protos;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletExtension;
import org.bitcoinj.wallet.WalletProtobufSerializer;
import org.bitcoinj.wallet.WalletTransaction;
import org.bitcoinj.wallet.WalletTransaction.Pool;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.bitcoinj.base.Coin.COIN;
import static org.bitcoinj.base.Coin.FIFTY_COINS;
import static org.bitcoinj.testing.FakeTxBuilder.createFakeTx;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class WalletProtobufSerializerTest {
    private static final NetworkParameters TESTNET = TestNet3Params.get();
    private static final NetworkParameters MAINNET = MainNetParams.get();

    private ECKey myKey;
    private ECKey myWatchedKey;
    private Address myAddress;
    private Wallet myWallet;

    public static String WALLET_DESCRIPTION  = "The quick brown fox lives in \u4f26\u6566"; // Beijing in Chinese
    private long mScriptCreationTime;

    @BeforeClass
    public static void setUpClass() {
        Utils.resetMocking();
        Context.propagate(new Context());
    }

    @Before
    public void setUp() {
        BriefLogFormatter.initVerbose();
        myWatchedKey = new ECKey();
        myKey = new ECKey();
        myKey.setCreationTimeSeconds(123456789L);
        myAddress = myKey.toAddress(ScriptType.P2PKH, BitcoinNetwork.TESTNET);
        myWallet = new Wallet(TESTNET, KeyChainGroup.builder(TESTNET).fromRandom(ScriptType.P2PKH).build());
        myWallet.importKey(myKey);
        mScriptCreationTime = new Date().getTime() / 1000 - 1234;
        myWallet.addWatchedAddress(myWatchedKey.toAddress(ScriptType.P2PKH, BitcoinNetwork.TESTNET), mScriptCreationTime);
        myWallet.setDescription(WALLET_DESCRIPTION);
    }

    @Test
    public void empty() throws Exception {
        // Check the base case of a wallet with one key and no transactions.
        Wallet wallet1 = roundTrip(myWallet);
        assertEquals(0, wallet1.getTransactions(true).size());
        assertEquals(Coin.ZERO, wallet1.getBalance());
        ECKey foundKey = wallet1.findKeyFromPubKeyHash(myKey.getPubKeyHash(), null);
        assertArrayEquals(myKey.getPubKey(), foundKey.getPubKey());
        assertArrayEquals(myKey.getPrivKeyBytes(), foundKey.getPrivKeyBytes());
        assertEquals(myKey.getCreationTimeSeconds(), foundKey.getCreationTimeSeconds());
        assertEquals(mScriptCreationTime,
                wallet1.getWatchedScripts().get(0).getCreationTimeSeconds());
        assertEquals(1, wallet1.getWatchedScripts().size());
        assertEquals(ScriptBuilder.createOutputScript(myWatchedKey.toAddress(ScriptType.P2PKH, BitcoinNetwork.TESTNET)),
                wallet1.getWatchedScripts().get(0));
        assertEquals(WALLET_DESCRIPTION, wallet1.getDescription());
    }

    @Test
    public void oneTx() throws Exception {
        // Check basic tx serialization.
        Coin v1 = COIN;
        Transaction t1 = createFakeTx(TESTNET, v1, myAddress);
        t1.getConfidence().markBroadcastBy(new PeerAddress(TESTNET, InetAddress.getByName("1.2.3.4")));
        t1.getConfidence().markBroadcastBy(new PeerAddress(TESTNET, InetAddress.getByName("5.6.7.8")));
        t1.getConfidence().setSource(TransactionConfidence.Source.NETWORK);
        myWallet.receivePending(t1, null);
        Wallet wallet1 = roundTrip(myWallet);
        assertEquals(1, wallet1.getTransactions(true).size());
        assertEquals(v1, wallet1.getBalance(Wallet.BalanceType.ESTIMATED));
        Transaction t1copy = wallet1.getTransaction(t1.getTxId());
        assertArrayEquals(t1.unsafeBitcoinSerialize(), t1copy.unsafeBitcoinSerialize());
        assertEquals(2, t1copy.getConfidence().numBroadcastPeers());
        assertNotNull(t1copy.getConfidence().getLastBroadcastedAt());
        assertEquals(TransactionConfidence.Source.NETWORK, t1copy.getConfidence().getSource());
        
        Protos.Wallet walletProto = new WalletProtobufSerializer().walletToProto(myWallet);
        assertEquals(Protos.Key.Type.ORIGINAL, walletProto.getKey(0).getType());
        assertEquals(0, walletProto.getExtensionCount());
        assertEquals(1, walletProto.getTransactionCount());
        assertEquals(6, walletProto.getKeyCount());
        
        Protos.Transaction t1p = walletProto.getTransaction(0);
        assertEquals(0, t1p.getBlockHashCount());
        assertArrayEquals(t1.getTxId().getBytes(), t1p.getHash().toByteArray());
        assertEquals(Protos.Transaction.Pool.PENDING, t1p.getPool());
        assertFalse(t1p.hasLockTime());
        assertFalse(t1p.getTransactionInput(0).hasSequence());
        assertArrayEquals(t1.getInputs().get(0).getOutpoint().getHash().getBytes(),
                t1p.getTransactionInput(0).getTransactionOutPointHash().toByteArray());
        assertEquals(0, t1p.getTransactionInput(0).getTransactionOutPointIndex());
        assertEquals(t1p.getTransactionOutput(0).getValue(), v1.value);
    }

    @Test
    public void raiseFeeTx() throws Exception {
        // Check basic tx serialization.
        Coin v1 = COIN;
        Transaction t1 = createFakeTx(TESTNET, v1, myAddress);
        t1.setPurpose(Purpose.RAISE_FEE);
        myWallet.receivePending(t1, null);
        Wallet wallet1 = roundTrip(myWallet);
        Transaction t1copy = wallet1.getTransaction(t1.getTxId());
        assertEquals(Purpose.RAISE_FEE, t1copy.getPurpose());
    }

    @Test
    public void doubleSpend() throws Exception {
        // Check that we can serialize double spends correctly, as this is a slightly tricky case.
        FakeTxBuilder.DoubleSpends doubleSpends = FakeTxBuilder.createFakeDoubleSpendTxns(TESTNET, myAddress);
        // t1 spends to our wallet.
        myWallet.receivePending(doubleSpends.t1, null);
        // t2 rolls back t1 and spends somewhere else.
        myWallet.receiveFromBlock(doubleSpends.t2, null, BlockChain.NewBlockType.BEST_CHAIN, 0);
        Wallet wallet1 = roundTrip(myWallet);
        assertEquals(1, wallet1.getTransactions(true).size());
        Transaction t1 = wallet1.getTransaction(doubleSpends.t1.getTxId());
        assertEquals(ConfidenceType.DEAD, t1.getConfidence().getConfidenceType());
        assertEquals(Coin.ZERO, wallet1.getBalance());

        // TODO: Wallet should store overriding transactions even if they are not wallet-relevant.
        // assertEquals(doubleSpends.t2, t1.getConfidence().getOverridingTransaction());
    }
    
    @Test
    public void testKeys() throws Exception {
        for (int i = 0 ; i < 20 ; i++) {
            myKey = new ECKey();
            myAddress = myKey.toAddress(ScriptType.P2PKH, BitcoinNetwork.TESTNET);
            myWallet = Wallet.createDeterministic(TESTNET, ScriptType.P2PKH);
            myWallet.importKey(myKey);
            Wallet wallet1 = roundTrip(myWallet);
            ECKey foundKey = wallet1.findKeyFromPubKeyHash(myKey.getPubKeyHash(), null);
            assertArrayEquals(myKey.getPubKey(), foundKey.getPubKey());
            assertArrayEquals(myKey.getPrivKeyBytes(), foundKey.getPrivKeyBytes());
        }
    }

    @Test
    public void testLastBlockSeenHash() throws Exception {
        // Test the lastBlockSeenHash field works.

        // LastBlockSeenHash should be empty if never set.
        Wallet wallet = Wallet.createDeterministic(TESTNET, ScriptType.P2PKH);
        Protos.Wallet walletProto = new WalletProtobufSerializer().walletToProto(wallet);
        ByteString lastSeenBlockHash = walletProto.getLastSeenBlockHash();
        assertTrue(lastSeenBlockHash.isEmpty());

        // Create a block.
        Block block = TESTNET.getDefaultSerializer()
                .makeBlock(ByteStreams.toByteArray(BlockTest.class.getResourceAsStream("block_testnet700000.dat")));
        Sha256Hash blockHash = block.getHash();
        wallet.setLastBlockSeenHash(blockHash);
        wallet.setLastBlockSeenHeight(1);

        // Roundtrip the wallet and check it has stored the blockHash.
        Wallet wallet1 = roundTrip(wallet);
        assertEquals(blockHash, wallet1.getLastBlockSeenHash());
        assertEquals(1, wallet1.getLastBlockSeenHeight());

        // Test the Satoshi genesis block (hash of all zeroes) is roundtripped ok.
        Block genesisBlock = MAINNET.getGenesisBlock();
        wallet.setLastBlockSeenHash(genesisBlock.getHash());
        Wallet wallet2 = roundTrip(wallet);
        assertEquals(genesisBlock.getHash(), wallet2.getLastBlockSeenHash());
    }

    @Test
    public void testSequenceNumber() throws Exception {
        Wallet wallet = Wallet.createDeterministic(TESTNET, ScriptType.P2PKH);
        Transaction tx1 = createFakeTx(TESTNET, Coin.COIN, wallet.currentReceiveAddress());
        tx1.getInput(0).setSequenceNumber(TransactionInput.NO_SEQUENCE);
        wallet.receivePending(tx1, null);
        Transaction tx2 = createFakeTx(TESTNET, Coin.COIN, wallet.currentReceiveAddress());
        tx2.getInput(0).setSequenceNumber(TransactionInput.NO_SEQUENCE - 1);
        wallet.receivePending(tx2, null);
        Wallet walletCopy = roundTrip(wallet);
        Transaction tx1copy = checkNotNull(walletCopy.getTransaction(tx1.getTxId()));
        assertEquals(TransactionInput.NO_SEQUENCE, tx1copy.getInput(0).getSequenceNumber());
        Transaction tx2copy = checkNotNull(walletCopy.getTransaction(tx2.getTxId()));
        assertEquals(TransactionInput.NO_SEQUENCE - 1, tx2copy.getInput(0).getSequenceNumber());
    }

    @Test
    public void testAppearedAtChainHeightDepthAndWorkDone() throws Exception {
        // Test the TransactionConfidence appearedAtChainHeight, depth and workDone field are stored.
        Context.propagate(new Context(100, Transaction.DEFAULT_TX_FEE, false, true));
        BlockChain chain = new BlockChain(TESTNET, myWallet, new MemoryBlockStore(TESTNET));

        final ArrayList<Transaction> txns = new ArrayList<>(2);
        myWallet.addCoinsReceivedEventListener((wallet, tx, prevBalance, newBalance) -> txns.add(tx));

        // Start by building two blocks on top of the genesis block.
        Block b1 = TESTNET.getGenesisBlock().createNextBlock(myAddress);
        BigInteger work1 = b1.getWork();
        assertTrue(work1.signum() > 0);

        Block b2 = b1.createNextBlock(myAddress);
        BigInteger work2 = b2.getWork();
        assertTrue(work2.signum() > 0);

        assertTrue(chain.add(b1));
        assertTrue(chain.add(b2));

        // We now have the following chain:
        //     genesis -> b1 -> b2

        // Check the transaction confidence levels are correct before wallet roundtrip.
        Threading.waitForUserCode();
        assertEquals(2, txns.size());

        TransactionConfidence confidence0 = txns.get(0).getConfidence();
        TransactionConfidence confidence1 = txns.get(1).getConfidence();

        assertEquals(1, confidence0.getAppearedAtChainHeight());
        assertEquals(2, confidence1.getAppearedAtChainHeight());

        assertEquals(2, confidence0.getDepthInBlocks());
        assertEquals(1, confidence1.getDepthInBlocks());

        // Roundtrip the wallet and check it has stored the depth and workDone.
        Wallet rebornWallet = roundTrip(myWallet);

        Set<Transaction> rebornTxns = rebornWallet.getTransactions(false);
        assertEquals(2, rebornTxns.size());

        // The transactions are not guaranteed to be in the same order so sort them to be in chain height order if required.
        Iterator<Transaction> it = rebornTxns.iterator();
        Transaction txA = it.next();
        Transaction txB = it.next();

        Transaction rebornTx0, rebornTx1;
         if (txA.getConfidence().getAppearedAtChainHeight() == 1) {
            rebornTx0 = txA;
            rebornTx1 = txB;
        } else {
            rebornTx0 = txB;
            rebornTx1 = txA;
        }

        TransactionConfidence rebornConfidence0 = rebornTx0.getConfidence();
        TransactionConfidence rebornConfidence1 = rebornTx1.getConfidence();

        assertEquals(1, rebornConfidence0.getAppearedAtChainHeight());
        assertEquals(2, rebornConfidence1.getAppearedAtChainHeight());

        assertEquals(2, rebornConfidence0.getDepthInBlocks());
        assertEquals(1, rebornConfidence1.getDepthInBlocks());
    }

    private static Wallet roundTrip(Wallet wallet) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new WalletProtobufSerializer().writeWallet(wallet, output);
        ByteArrayInputStream test = new ByteArrayInputStream(output.toByteArray());
        assertTrue(WalletProtobufSerializer.isWallet(test));
        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        return new WalletProtobufSerializer().readWallet(input);
    }

    @Test
    public void testRoundTripNormalWallet() throws Exception {
        Wallet wallet1 = roundTrip(myWallet);
        assertEquals(0, wallet1.getTransactions(true).size());
        assertEquals(Coin.ZERO, wallet1.getBalance());
        ECKey foundKey = wallet1.findKeyFromPubKeyHash(myKey.getPubKeyHash(), null);
        assertArrayEquals(myKey.getPubKey(), foundKey.getPubKey());
        assertArrayEquals(myKey.getPrivKeyBytes(), foundKey.getPrivKeyBytes());
        assertEquals(myKey.getCreationTimeSeconds(), foundKey.getCreationTimeSeconds());
    }

    @Test
    public void testRoundTripWatchingWallet() throws Exception {
        final String xpub = "tpubD9LrDvFDrB6wYNhbR2XcRRaT4yCa37TjBR3YthBQvrtEwEq6CKeEXUs3TppQd38rfxmxD1qLkC99iP3vKcKwLESSSYdFAftbrpuhSnsw6XM";
        final long creationTimeSeconds = 1457019819;
        Wallet wallet = Wallet.fromWatchingKeyB58(TESTNET, xpub, creationTimeSeconds);
        Wallet wallet2 = roundTrip(wallet);
        Wallet wallet3 = roundTrip(wallet2);
        assertEquals(xpub, wallet.getWatchingKey().serializePubB58(TESTNET));
        assertEquals(creationTimeSeconds, wallet.getWatchingKey().getCreationTimeSeconds());
        assertEquals(creationTimeSeconds, wallet2.getWatchingKey().getCreationTimeSeconds());
        assertEquals(creationTimeSeconds, wallet3.getWatchingKey().getCreationTimeSeconds());
        assertEquals(creationTimeSeconds, wallet.getEarliestKeyCreationTime());
        assertEquals(creationTimeSeconds, wallet2.getEarliestKeyCreationTime());
        assertEquals(creationTimeSeconds, wallet3.getEarliestKeyCreationTime());
    }

    @Test
    public void testRoundTripMarriedWallet() throws Exception {
        // create 2-of-2 married wallet
        myWallet = Wallet.createDeterministic(TESTNET, ScriptType.P2PKH);
        final DeterministicKeyChain partnerChain = DeterministicKeyChain.builder().random(new SecureRandom()).build();
        DeterministicKey partnerKey = DeterministicKey.deserializeB58(null, partnerChain.getWatchingKey().serializePubB58(TESTNET), TESTNET);
        MarriedKeyChain chain = MarriedKeyChain.builder()
                .random(new SecureRandom())
                .followingKeys(partnerKey)
                .threshold(2).build();
        myWallet.addAndActivateHDChain(chain);

        myAddress = myWallet.currentAddress(KeyChain.KeyPurpose.RECEIVE_FUNDS);

        Wallet wallet1 = roundTrip(myWallet);
        assertEquals(0, wallet1.getTransactions(true).size());
        assertEquals(Coin.ZERO, wallet1.getBalance());
        assertEquals(2, wallet1.getActiveKeyChain().getSigsRequiredToSpend());
        assertEquals(myAddress, wallet1.currentAddress(KeyChain.KeyPurpose.RECEIVE_FUNDS));
    }

    @Test
    public void roundtripVersionTwoTransaction() throws Exception {
        Transaction tx = new Transaction(TESTNET, ByteUtils.HEX.decode(
                "0200000001d7902864af9310420c6e606b814c8f89f7902d40c130594e85df2e757a7cc301070000006b483045022100ca1757afa1af85c2bb014382d9ce411e1628d2b3d478df9d5d3e9e93cb25dcdd02206c5d272b31a23baf64e82793ee5c816e2bbef251e733a638b630ff2331fc83ba0121026ac2316508287761befbd0f7495ea794b396dbc5b556bf276639f56c0bd08911feffffff0274730700000000001976a91456da2d038a098c42390c77ef163e1cc23aedf24088ac91062300000000001976a9148ebf3467b9a8d7ae7b290da719e61142793392c188ac22e00600"));
        assertEquals(tx.getVersion(), 2);
        assertEquals(tx.getTxId().toString(), "0321b1413ed9048199815bd6bc2650cab1a9e8d543f109a42c769b1f18df4174");
        myWallet.addWalletTransaction(new WalletTransaction(Pool.UNSPENT, tx));
        Wallet wallet1 = roundTrip(myWallet);
        Transaction tx2 = wallet1.getTransaction(tx.getTxId());
        assertEquals(checkNotNull(tx2).getVersion(), 2);
    }

    @Test
    public void coinbaseTxns() throws Exception {
        // Covers issue 420 where the outpoint index of a coinbase tx input was being mis-serialized.
        Context.propagate(new Context(100, Transaction.DEFAULT_TX_FEE, false, true));
        Block b = TESTNET.getGenesisBlock().createNextBlockWithCoinbase(Block.BLOCK_VERSION_GENESIS, myKey.getPubKey(), FIFTY_COINS, Block.BLOCK_HEIGHT_GENESIS);
        Transaction coinbase = b.getTransactions().get(0);
        assertTrue(coinbase.isCoinBase());
        BlockChain chain = new BlockChain(TESTNET, myWallet, new MemoryBlockStore(TESTNET));
        assertTrue(chain.add(b));
        // Wallet now has a coinbase tx in it.
        assertEquals(1, myWallet.getTransactions(true).size());
        assertTrue(myWallet.getTransaction(coinbase.getTxId()).isCoinBase());
        Wallet wallet2 = roundTrip(myWallet);
        assertEquals(1, wallet2.getTransactions(true).size());
        assertTrue(wallet2.getTransaction(coinbase.getTxId()).isCoinBase());
    }

    @Test
    public void tags() throws Exception {
        myWallet.setTag("foo", ByteString.copyFromUtf8("bar"));
        assertEquals("bar", myWallet.getTag("foo").toStringUtf8());
        myWallet = roundTrip(myWallet);
        assertEquals("bar", myWallet.getTag("foo").toStringUtf8());
    }

    @Test
    public void extensions() throws Exception {
        myWallet.addExtension(new FooWalletExtension("com.whatever.required", true));
        Protos.Wallet proto = new WalletProtobufSerializer().walletToProto(myWallet);
        // Initial extension is mandatory: try to read it back into a wallet that doesn't know about it.
        try {
            new WalletProtobufSerializer().readWallet(TESTNET, null, proto);
            fail();
        } catch (UnreadableWalletException e) {
            assertTrue(e.getMessage().contains("mandatory"));
        }
        Wallet wallet = new WalletProtobufSerializer().readWallet(TESTNET,
                new WalletExtension[]{ new FooWalletExtension("com.whatever.required", true) },
                proto);
        assertTrue(wallet.getExtensions().containsKey("com.whatever.required"));

        // Non-mandatory extensions are ignored if the wallet doesn't know how to read them.
        Wallet wallet2 = Wallet.createDeterministic(TESTNET, ScriptType.P2PKH);
        wallet2.addExtension(new FooWalletExtension("com.whatever.optional", false));
        Protos.Wallet proto2 = new WalletProtobufSerializer().walletToProto(wallet2);
        Wallet wallet5 = new WalletProtobufSerializer().readWallet(TESTNET, null, proto2);
        assertEquals(0, wallet5.getExtensions().size());
    }

    @Test
    public void extensionsWithError() throws Exception {
        WalletExtension extension = new WalletExtension() {
            @Override
            public String getWalletExtensionID() {
                return "test";
            }

            @Override
            public boolean isWalletExtensionMandatory() {
                return false;
            }

            @Override
            public byte[] serializeWalletExtension() {
                return new byte[0];
            }

            @Override
            public void deserializeWalletExtension(Wallet containingWallet, byte[] data) throws Exception {
                throw new NullPointerException();  // Something went wrong!
            }
        };
        myWallet.addExtension(extension);
        Protos.Wallet proto = new WalletProtobufSerializer().walletToProto(myWallet);
        Wallet wallet = new WalletProtobufSerializer().readWallet(TESTNET, new WalletExtension[]{extension}, proto);
        assertEquals(0, wallet.getExtensions().size());
    }

    @Test(expected = UnreadableWalletException.FutureVersion.class)
    public void versions() throws Exception {
        Protos.Wallet.Builder proto = Protos.Wallet.newBuilder(new WalletProtobufSerializer().walletToProto(myWallet));
        proto.setVersion(2);
        new WalletProtobufSerializer().readWallet(TESTNET, null, proto.build());
    }

    @Test
    public void storeWitnessTransactions() throws Exception {
        // 3 inputs, inputs 0 and 2 have witnesses but not input 1
        Transaction tx = new Transaction(TESTNET, ByteUtils.HEX.decode(
                "02000000000103fc8a5bea59392369e8a1b635395e507a5cbaeffd926e6967a00d17c669aef1d3010000001716001403c80a334ed6a92cf400d8c708522ea0d6fa5593ffffffffc0166d2218a2613b5384fc2c31238b1b6fa337080a1384220734e1bfd3629d3f0100000000ffffffffc0166d2218a2613b5384fc2c31238b1b6fa337080a1384220734e1bfd3629d3f0200000000ffffffff01a086010000000000220020eb72e573a9513d982a01f0e6a6b53e92764db81a0c26d2be94c5fc5b69a0db7d02473044022048e895b7af715303ce273a2be03d6110ed69b5700679f4f036000f8ba6eddd2802205f780423fcce9b3632ed41681b0a86f5d123766b71f303558c39c1be5fe43e2601210259eb16169df80dbe5856d082a226d84a97d191c895f8046c3544df525028a874000220c0166d2218a2613b5384fc2c31238b1b6fa337080a1384220734e1bfd3629d3f20c0166d2218a2613b5384fc2c31238b1b6fa337080a1384220734e1bfd3629d3f00000000"));
        assertTrue(tx.hasWitnesses());
        assertEquals(tx.getTxId().toString(), "1c687396f4710f26206dbdd8bf07a28c76398be6750226ddfaf05a1a80d30034");
        myWallet.addWalletTransaction(new WalletTransaction(Pool.UNSPENT, tx));
        Wallet wallet1 = roundTrip(myWallet);
        Transaction tx2 = wallet1.getTransaction(tx.getTxId());
        assertEquals(tx.getInput(0).getWitness(), tx2.getInput(0).getWitness());
    }
}
