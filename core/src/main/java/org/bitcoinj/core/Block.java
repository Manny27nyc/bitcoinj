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
 * Copyright 2011 Google Inc.
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.base.utils.ByteUtils;
import org.bitcoinj.core.internal.InternalUtils;
import org.bitcoinj.params.BitcoinNetworkParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static com.google.common.base.Preconditions.checkState;
import static org.bitcoinj.base.Coin.FIFTY_COINS;
import static org.bitcoinj.base.Sha256Hash.hashTwice;

/**
 * <p>A block is a group of transactions, and is one of the fundamental data structures of the Bitcoin system.
 * It records a set of {@link Transaction}s together with some data that links it into a place in the global block
 * chain, and proves that a difficult calculation was done over its contents. See
 * <a href="http://www.bitcoin.org/bitcoin.pdf">the Bitcoin technical paper</a> for
 * more detail on blocks.</p>
 *
 * <p>To get a block, you can either build one from the raw bytes you can get from another implementation, or request one
 * specifically using {@link Peer#getBlock(Sha256Hash)}, or grab one from a downloaded {@link BlockChain}.</p>
 * 
 * <p>Instances of this class are not safe for use by multiple threads.</p>
 */
public class Block extends Message {
    /**
     * Flags used to control which elements of block validation are done on
     * received blocks.
     */
    public enum VerifyFlag {
        /** Check that block height is in coinbase transaction (BIP 34). */
        HEIGHT_IN_COINBASE
    }

    private static final Logger log = LoggerFactory.getLogger(Block.class);

    /** How many bytes are required to represent a block header WITHOUT the trailing 00 length byte. */
    public static final int HEADER_SIZE = 80;

    static final long ALLOWED_TIME_DRIFT = 2 * 60 * 60; // Same value as Bitcoin Core.

    /**
     * A constant shared by the entire network: how large in bytes a block is allowed to be. One day we may have to
     * upgrade everyone to change this, so Bitcoin can continue to grow. For now it exists as an anti-DoS measure to
     * avoid somebody creating a titanically huge but valid block and forcing everyone to download/store it forever.
     */
    public static final int MAX_BLOCK_SIZE = 1 * 1000 * 1000;
    /**
     * A "sigop" is a signature verification operation. Because they're expensive we also impose a separate limit on
     * the number in a block to prevent somebody mining a huge block that has way more sigops than normal, so is very
     * expensive/slow to verify.
     */
    public static final int MAX_BLOCK_SIGOPS = MAX_BLOCK_SIZE / 50;

    /** Standard maximum value for difficultyTarget (nBits) (Bitcoin MainNet and TestNet) */
    public static final long STANDARD_MAX_DIFFICULTY_TARGET = 0x1d00ffffL;

    /** A value for difficultyTarget (nBits) that allows (slightly less than) half of all possible hash solutions. Used in unit testing. */
    public static final long EASIEST_DIFFICULTY_TARGET = 0x207fFFFFL;

    /** Value to use if the block height is unknown */
    public static final int BLOCK_HEIGHT_UNKNOWN = -1;
    /** Height of the first block */
    public static final int BLOCK_HEIGHT_GENESIS = 0;

    public static final long BLOCK_VERSION_GENESIS = 1;
    /** Block version introduced in BIP 34: Height in coinbase */
    public static final long BLOCK_VERSION_BIP34 = 2;
    /** Block version introduced in BIP 66: Strict DER signatures */
    public static final long BLOCK_VERSION_BIP66 = 3;
    /** Block version introduced in BIP 65: OP_CHECKLOCKTIMEVERIFY */
    public static final long BLOCK_VERSION_BIP65 = 4;

    // Fields defined as part of the protocol format.
    private long version;
    private Sha256Hash prevBlockHash;
    private Sha256Hash merkleRoot, witnessRoot;
    private long time;
    private long difficultyTarget; // "nBits"
    private long nonce;

    // If null, it means this object holds only the headers.
    @VisibleForTesting
    @Nullable List<Transaction> transactions;

    /** Stores the hash of the block. If null, getHash() will recalculate it. */
    private Sha256Hash hash;

    protected boolean headerBytesValid;
    protected boolean transactionBytesValid;
    
    // Blocks can be encoded in a way that will use more bytes than is optimal (due to VarInts having multiple encodings)
    // MAX_BLOCK_SIZE must be compared to the optimal encoding, not the actual encoding, so when parsing, we keep track
    // of the size of the ideal encoding in addition to the actual message size (which Message needs)
    protected int optimalEncodingMessageSize;

    /** Special case constructor, used for the genesis node, cloneAsHeader and unit tests. */
    Block(NetworkParameters params, long setVersion) {
        super(params);
        // Set up a few basic things. We are not complete after this though.
        version = setVersion;
        difficultyTarget = 0x1d07fff8L;
        time = Utils.currentTimeSeconds();
        prevBlockHash = Sha256Hash.ZERO_HASH;

        length = HEADER_SIZE;
    }

    /**
     * Construct a block object from the Bitcoin wire format.
     * @param params NetworkParameters object.
     * @param payloadBytes the payload to extract the block from.
     * @param serializer the serializer to use for this message.
     * @param length The length of message if known.  Usually this is provided when deserializing of the wire
     * as the length will be provided as part of the header.  If unknown then set to Message.UNKNOWN_LENGTH
     * @throws ProtocolException
     */
    public Block(NetworkParameters params, byte[] payloadBytes, MessageSerializer serializer, int length)
            throws ProtocolException {
        super(params, payloadBytes, 0, serializer, length);
    }

    /**
     * Construct a block object from the Bitcoin wire format.
     * @param params NetworkParameters object.
     * @param payloadBytes the payload to extract the block from.
     * @param offset The location of the first payload byte within the array.
     * @param serializer the serializer to use for this message.
     * @param length The length of message if known.  Usually this is provided when deserializing of the wire
     * as the length will be provided as part of the header.  If unknown then set to Message.UNKNOWN_LENGTH
     * @throws ProtocolException
     */
    public Block(NetworkParameters params, byte[] payloadBytes, int offset, MessageSerializer serializer, int length)
            throws ProtocolException {
        super(params, payloadBytes, offset, serializer, length);
    }

    /**
     * Construct a block object from the Bitcoin wire format. Used in the case of a block
     * contained within another message (i.e. for AuxPoW header).
     *
     * @param params NetworkParameters object.
     * @param payloadBytes Bitcoin protocol formatted byte array containing message content.
     * @param offset The location of the first payload byte within the array.
     * @param parent The message element which contains this block, maybe null for no parent.
     * @param serializer the serializer to use for this block.
     * @param length The length of message if known.  Usually this is provided when deserializing of the wire
     * as the length will be provided as part of the header.  If unknown then set to Message.UNKNOWN_LENGTH
     * @throws ProtocolException
     */
    public Block(NetworkParameters params, byte[] payloadBytes, int offset, @Nullable Message parent, MessageSerializer serializer, int length)
            throws ProtocolException {
        // TODO: Keep the parent
        super(params, payloadBytes, offset, serializer, length);
    }

    /**
     * Construct a block initialized with all the given fields.
     * @param params Which network the block is for.
     * @param version This should usually be set to 1 or 2, depending on if the height is in the coinbase input.
     * @param prevBlockHash Reference to previous block in the chain or {@link Sha256Hash#ZERO_HASH} if genesis.
     * @param merkleRoot The root of the merkle tree formed by the transactions.
     * @param time UNIX time when the block was mined.
     * @param difficultyTarget Number which this block hashes lower than.
     * @param nonce Arbitrary number to make the block hash lower than the target.
     * @param transactions List of transactions including the coinbase.
     */
    public Block(NetworkParameters params, long version, Sha256Hash prevBlockHash, Sha256Hash merkleRoot, long time,
                 long difficultyTarget, long nonce, List<Transaction> transactions) {
        super(params);
        this.version = version;
        this.prevBlockHash = prevBlockHash;
        this.merkleRoot = merkleRoot;
        this.time = time;
        this.difficultyTarget = difficultyTarget;
        this.nonce = nonce;
        this.transactions = new LinkedList<>();
        this.transactions.addAll(transactions);
    }

    /** @deprecated Use {@link BitcoinNetworkParams#getBlockInflation(int)} */
    @Deprecated
    public Coin getBlockInflation(int height) {
        return ((BitcoinNetworkParams) params).getBlockInflation(height);
    }

    /**
     * Parse transactions from the block.
     * 
     * @param transactionsOffset Offset of the transactions within the block.
     * Useful for non-Bitcoin chains where the block header may not be a fixed
     * size.
     */
    protected void parseTransactions(final int transactionsOffset) throws ProtocolException {
        cursor = transactionsOffset;
        optimalEncodingMessageSize = HEADER_SIZE;
        if (payload.length == cursor) {
            // This message is just a header, it has no transactions.
            transactionBytesValid = false;
            return;
        }

        VarInt numTransactionsVarInt = readVarInt();
        optimalEncodingMessageSize += numTransactionsVarInt.getSizeInBytes();
        int numTransactions = numTransactionsVarInt.intValue();
        transactions = new ArrayList<>(Math.min(numTransactions, Utils.MAX_INITIAL_ARRAY_LENGTH));
        for (int i = 0; i < numTransactions; i++) {
            Transaction tx = new Transaction(params, payload, cursor, this, serializer, UNKNOWN_LENGTH, null);
            // Label the transaction as coming from the P2P network, so code that cares where we first saw it knows.
            tx.getConfidence().setSource(TransactionConfidence.Source.NETWORK);
            transactions.add(tx);
            cursor += tx.getMessageSize();
            optimalEncodingMessageSize += tx.getOptimalEncodingMessageSize();
        }
        transactionBytesValid = serializer.isParseRetainMode();
    }

    @Override
    protected void parse() throws ProtocolException {
        // header
        cursor = offset;
        version = readUint32();
        prevBlockHash = readHash();
        merkleRoot = readHash();
        time = readUint32();
        difficultyTarget = readUint32();
        nonce = readUint32();
        hash = Sha256Hash.wrapReversed(Sha256Hash.hashTwice(payload, offset, cursor - offset));
        headerBytesValid = serializer.isParseRetainMode();

        // transactions
        parseTransactions(offset + HEADER_SIZE);
        length = cursor - offset;
    }

    public static Block createGenesis(NetworkParameters n) {
        Block genesisBlock = new Block(n, BLOCK_VERSION_GENESIS);
        Transaction t = createGenesisTransaction(n, genesisTxInputScriptBytes, FIFTY_COINS, genesisTxScriptPubKeyBytes);
        genesisBlock.addTransaction(t);
        return genesisBlock;
    }

    private static Transaction createGenesisTransaction(NetworkParameters n, byte[] inputScriptBytes, Coin amount, byte[] scriptPubKeyBytes) {
        Transaction t = new Transaction(n);
        t.addInput(new TransactionInput(n, t, inputScriptBytes));
        t.addOutput(new TransactionOutput(n, t, amount, scriptPubKeyBytes));
        return t;
    }

    // A script containing the difficulty bits and the following message:
    //
    //   "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks"
    private static final byte[] genesisTxInputScriptBytes = ByteUtils.HEX.decode
                ("04ffff001d0104455468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73");

    private static final byte[] genesisTxScriptPubKeyBytes;
    static {
        ByteArrayOutputStream scriptPubKeyBytes = new ByteArrayOutputStream();
        try {
            Script.writeBytes(scriptPubKeyBytes, ByteUtils.HEX.decode
                    ("04678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5f"));
        } catch (IOException e) {
            throw new RuntimeException(e); // Cannot happen.
        }
        scriptPubKeyBytes.write(ScriptOpCodes.OP_CHECKSIG);
        genesisTxScriptPubKeyBytes = scriptPubKeyBytes.toByteArray();
    }

    public int getOptimalEncodingMessageSize() {
        if (optimalEncodingMessageSize != 0)
            return optimalEncodingMessageSize;
        optimalEncodingMessageSize = bitcoinSerialize().length;
        return optimalEncodingMessageSize;
    }

    // default for testing
    void writeHeader(OutputStream stream) throws IOException {
        // try for cached write first
        if (headerBytesValid && payload != null && payload.length >= offset + HEADER_SIZE) {
            stream.write(payload, offset, HEADER_SIZE);
            return;
        }
        // fall back to manual write
        ByteUtils.uint32ToByteStreamLE(version, stream);
        stream.write(prevBlockHash.getReversedBytes());
        stream.write(getMerkleRoot().getReversedBytes());
        ByteUtils.uint32ToByteStreamLE(time, stream);
        ByteUtils.uint32ToByteStreamLE(difficultyTarget, stream);
        ByteUtils.uint32ToByteStreamLE(nonce, stream);
    }

    private void writeTransactions(OutputStream stream) throws IOException {
        // check for no transaction conditions first
        // must be a more efficient way to do this but I'm tired atm.
        if (transactions == null) {
            return;
        }

        // confirmed we must have transactions either cached or as objects.
        if (transactionBytesValid && payload != null && payload.length >= offset + length) {
            stream.write(payload, offset + HEADER_SIZE, length - HEADER_SIZE);
            return;
        }

        stream.write(new VarInt(transactions.size()).encode());
        for (Transaction tx : transactions) {
            tx.bitcoinSerialize(stream);
        }
    }

    /**
     * Special handling to check if we have a valid byte array for both header
     * and transactions
     */
    @Override
    public byte[] bitcoinSerialize() {
        // we have completely cached byte array.
        if (headerBytesValid && transactionBytesValid) {
            Preconditions.checkNotNull(payload, "Bytes should never be null if headerBytesValid && transactionBytesValid");
            if (length == payload.length) {
                return payload;
            } else {
                // byte array is offset so copy out the correct range.
                byte[] buf = new byte[length];
                System.arraycopy(payload, offset, buf, 0, length);
                return buf;
            }
        }

        // At least one of the two cacheable components is invalid
        // so fall back to stream write since we can't be sure of the length.
        ByteArrayOutputStream stream = new UnsafeByteArrayOutputStream(length == UNKNOWN_LENGTH ? HEADER_SIZE + guessTransactionsLength() : length);
        try {
            writeHeader(stream);
            writeTransactions(stream);
        } catch (IOException e) {
            // Cannot happen, we are serializing to a memory stream.
        }
        return stream.toByteArray();
    }

    @Override
    protected void bitcoinSerializeToStream(OutputStream stream) throws IOException {
        writeHeader(stream);
        // We may only have enough data to write the header.
        writeTransactions(stream);
    }

    /**
     * Provides a reasonable guess at the byte length of the transactions part of the block.
     * The returned value will be accurate in 99% of cases and in those cases where not will probably slightly
     * oversize.
     *
     * This is used to preallocate the underlying byte array for a ByteArrayOutputStream.  If the size is under the
     * real value the only penalty is resizing of the underlying byte array.
     */
    private int guessTransactionsLength() {
        if (transactionBytesValid)
            return payload.length - HEADER_SIZE;
        if (transactions == null)
            return 0;
        int len = VarInt.sizeOf(transactions.size());
        for (Transaction tx : transactions) {
            // 255 is just a guess at an average tx length
            len += tx.length == UNKNOWN_LENGTH ? 255 : tx.length;
        }
        return len;
    }

    @Override
    protected void unCache() {
        // Since we have alternate uncache methods to use internally this will only ever be called by a child
        // transaction so we only need to invalidate that part of the cache.
        unCacheTransactions();
    }

    private void unCacheHeader() {
        headerBytesValid = false;
        if (!transactionBytesValid)
            payload = null;
        hash = null;
    }

    private void unCacheTransactions() {
        transactionBytesValid = false;
        if (!headerBytesValid)
            payload = null;
        // Current implementation has to uncache headers as well as any change to a tx will alter the merkle root. In
        // future we can go more granular and cache merkle root separately so rest of the header does not need to be
        // rewritten.
        unCacheHeader();
        // Clear merkleRoot last as it may end up being parsed during unCacheHeader().
        merkleRoot = null;
    }

    /**
     * Calculates the block hash by serializing the block and hashing the
     * resulting bytes.
     */
    private Sha256Hash calculateHash() {
        try {
            ByteArrayOutputStream bos = new UnsafeByteArrayOutputStream(HEADER_SIZE);
            writeHeader(bos);
            return Sha256Hash.wrapReversed(Sha256Hash.hashTwice(bos.toByteArray()));
        } catch (IOException e) {
            throw new RuntimeException(e); // Cannot happen.
        }
    }

    /**
     * Returns the hash of the block (which for a valid, solved block should be below the target) in the form seen on
     * the block explorer. If you call this on block 1 in the mainnet chain
     * you will get "00000000839a8e6886ab5951d76f411475428afc90947ee320161bbf18eb6048".
     */
    public String getHashAsString() {
        return getHash().toString();
    }

    /**
     * Returns the hash of the block (which for a valid, solved block should be
     * below the target). Big endian.
     */
    @Override
    public Sha256Hash getHash() {
        if (hash == null)
            hash = calculateHash();
        return hash;
    }

    /**
     * The number that is one greater than the largest representable SHA-256
     * hash.
     */
    private static BigInteger LARGEST_HASH = BigInteger.ONE.shiftLeft(256);

    /**
     * Returns the work represented by this block.<p>
     *
     * Work is defined as the number of tries needed to solve a block in the
     * average case. Consider a difficulty target that covers 5% of all possible
     * hash values. Then the work of the block will be 20. As the target gets
     * lower, the amount of work goes up.
     */
    public BigInteger getWork() throws VerificationException {
        BigInteger target = getDifficultyTargetAsInteger();
        return LARGEST_HASH.divide(target.add(BigInteger.ONE));
    }

    /**
     * Returns a copy of the block, but without any transactions.
     * @return new, header-only {@code Block}
     */
    public Block cloneAsHeader() {
        Block block = new Block(params, version);
        block.difficultyTarget = difficultyTarget;
        block.time = time;
        block.nonce = nonce;
        block.prevBlockHash = prevBlockHash;
        block.merkleRoot = getMerkleRoot();
        block.hash = getHash();
        block.transactions = null;
        return block;
    }

    /**
     * Returns a multi-line string containing a description of the contents of
     * the block. Use for debugging purposes only.
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(" block: \n");
        s.append("   hash: ").append(getHashAsString()).append('\n');
        s.append("   version: ").append(version);
        String bips = InternalUtils.commaJoin(isBIP34() ? "BIP34" : null, isBIP66() ? "BIP66" : null, isBIP65() ? "BIP65" : null);
        if (!bips.isEmpty())
            s.append(" (").append(bips).append(')');
        s.append('\n');
        s.append("   previous block: ").append(getPrevBlockHash()).append("\n");
        s.append("   time: ").append(time).append(" (").append(Utils.dateTimeFormat(time * 1000)).append(")\n");
        s.append("   difficulty target (nBits): ").append(difficultyTarget).append("\n");
        s.append("   nonce: ").append(nonce).append("\n");
        if (transactions != null && transactions.size() > 0) {
            s.append("   merkle root: ").append(getMerkleRoot()).append("\n");
            s.append("   witness root: ").append(getWitnessRoot()).append("\n");
            s.append("   with ").append(transactions.size()).append(" transaction(s):\n");
            for (Transaction tx : transactions) {
                s.append(tx).append('\n');
            }
        }
        return s.toString();
    }

    /**
     * <p>Finds a value of nonce that makes the blocks hash lower than the difficulty target. This is called mining, but
     * solve() is far too slow to do real mining with. It exists only for unit testing purposes.
     *
     * <p>This can loop forever if a solution cannot be found solely by incrementing nonce. It doesn't change
     * extraNonce.</p>
     */
    @VisibleForTesting
    public void solve() {
        while (true) {
            try {
                // Is our proof of work valid yet?
                if (checkProofOfWork(false))
                    return;
                // No, so increment the nonce and try again.
                setNonce(getNonce() + 1);
            } catch (VerificationException e) {
                throw new RuntimeException(e); // Cannot happen.
            }
        }
    }

    /**
     * Returns the difficulty target as a 256 bit value that can be compared to a SHA-256 hash. Inside a block the
     * target is represented using a compact form. If this form decodes to a value that is out of bounds, an exception
     * is thrown.
     */
    public BigInteger getDifficultyTargetAsInteger() throws VerificationException {
        BigInteger target = ByteUtils.decodeCompactBits(difficultyTarget);
        if (target.signum() <= 0 || target.compareTo(params.maxTarget) > 0)
            throw new VerificationException("Difficulty target is bad: " + target.toString());
        return target;
    }

    /** Returns true if the hash of the block is OK (lower than difficulty target). */
    protected boolean checkProofOfWork(boolean throwException) throws VerificationException {
        // shortcut for unit-testing
        if (Context.get().isRelaxProofOfWork())
            return true;

        // This part is key - it is what proves the block was as difficult to make as it claims
        // to be. Note however that in the context of this function, the block can claim to be
        // as difficult as it wants to be .... if somebody was able to take control of our network
        // connection and fork us onto a different chain, they could send us valid blocks with
        // ridiculously easy difficulty and this function would accept them.
        //
        // To prevent this attack from being possible, elsewhere we check that the difficultyTarget
        // field is of the right value. This requires us to have the preceding blocks.
        BigInteger target = getDifficultyTargetAsInteger();

        BigInteger h = getHash().toBigInteger();
        if (h.compareTo(target) > 0) {
            // Proof of work check failed!
            if (throwException)
                throw new VerificationException("Hash is higher than target: " + getHashAsString() + " vs "
                        + target.toString(16));
            else
                return false;
        }
        return true;
    }

    private void checkTimestamp() throws VerificationException {
        final long allowedTime = Utils.currentTimeSeconds() + ALLOWED_TIME_DRIFT;
        if (time > allowedTime)
            throw new VerificationException(String.format(Locale.US,
                    "Block too far in future: %s (%d) vs allowed %s (%d)", Utils.dateTimeFormat(time * 1000), time,
                    Utils.dateTimeFormat(allowedTime * 1000), allowedTime));
    }

    private void checkSigOps() throws VerificationException {
        // Check there aren't too many signature verifications in the block. This is an anti-DoS measure, see the
        // comments for MAX_BLOCK_SIGOPS.
        int sigOps = 0;
        for (Transaction tx : transactions) {
            sigOps += tx.getSigOpCount();
        }
        if (sigOps > MAX_BLOCK_SIGOPS)
            throw new VerificationException("Block had too many Signature Operations");
    }

    private void checkMerkleRoot() throws VerificationException {
        Sha256Hash calculatedRoot = calculateMerkleRoot();
        if (!calculatedRoot.equals(merkleRoot)) {
            log.error("Merkle tree did not verify");
            throw new VerificationException("Merkle hashes do not match: " + calculatedRoot + " vs " + merkleRoot);
        }
    }

    @VisibleForTesting
    void checkWitnessRoot() throws VerificationException {
        Transaction coinbase = transactions.get(0);
        checkState(coinbase.isCoinBase());
        Sha256Hash witnessCommitment = coinbase.findWitnessCommitment();
        if (witnessCommitment != null) {
            byte[] witnessReserved = null;
            TransactionWitness witness = coinbase.getInput(0).getWitness();
            if (witness.getPushCount() != 1)
                throw new VerificationException("Coinbase witness reserved invalid: push count");
            witnessReserved = witness.getPush(0);
            if (witnessReserved.length != 32)
                throw new VerificationException("Coinbase witness reserved invalid: length");

            Sha256Hash witnessRootHash = Sha256Hash.twiceOf(getWitnessRoot().getReversedBytes(), witnessReserved);
            if (!witnessRootHash.equals(witnessCommitment))
                throw new VerificationException("Witness merkle root invalid. Expected " + witnessCommitment.toString()
                        + " but got " + witnessRootHash.toString());
        } else {
            for (Transaction tx : transactions) {
                if (tx.hasWitnesses())
                    throw new VerificationException("Transaction witness found but no witness commitment present");
            }
        }
    }

    private Sha256Hash calculateMerkleRoot() {
        List<byte[]> tree = buildMerkleTree(false);
        return Sha256Hash.wrap(tree.get(tree.size() - 1));
    }

    private Sha256Hash calculateWitnessRoot() {
        List<byte[]> tree = buildMerkleTree(true);
        return Sha256Hash.wrap(tree.get(tree.size() - 1));
    }

    private List<byte[]> buildMerkleTree(boolean useWTxId) {
        // The Merkle root is based on a tree of hashes calculated from the transactions:
        //
        //     root
        //      / \
        //   A      B
        //  / \    / \
        // t1 t2 t3 t4
        //
        // The tree is represented as a list: t1,t2,t3,t4,A,B,root where each
        // entry is a hash.
        //
        // The hashing algorithm is double SHA-256. The leaves are a hash of the serialized contents of the transaction.
        // The interior nodes are hashes of the concatenation of the two child hashes.
        //
        // This structure allows the creation of proof that a transaction was included into a block without having to
        // provide the full block contents. Instead, you can provide only a Merkle branch. For example to prove tx2 was
        // in a block you can just provide tx2, the hash(tx1) and B. Now the other party has everything they need to
        // derive the root, which can be checked against the block header. These proofs aren't used right now but
        // will be helpful later when we want to download partial block contents.
        //
        // Note that if the number of transactions is not even the last tx is repeated to make it so (see
        // tx3 above). A tree with 5 transactions would look like this:
        //
        //         root
        //        /     \
        //       1        5
        //     /   \     / \
        //    2     3    4  4
        //  / \   / \   / \
        // t1 t2 t3 t4 t5 t5
        ArrayList<byte[]> tree = new ArrayList<>(transactions.size());
        // Start by adding all the hashes of the transactions as leaves of the tree.
        for (Transaction tx : transactions) {
            final Sha256Hash id;
            if (useWTxId && tx.isCoinBase())
                id = Sha256Hash.ZERO_HASH;
            else
                id = useWTxId ? tx.getWTxId() : tx.getTxId();
            tree.add(id.getBytes());
        }
        int levelOffset = 0; // Offset in the list where the currently processed level starts.
        // Step through each level, stopping when we reach the root (levelSize == 1).
        for (int levelSize = transactions.size(); levelSize > 1; levelSize = (levelSize + 1) / 2) {
            // For each pair of nodes on that level:
            for (int left = 0; left < levelSize; left += 2) {
                // The right hand node can be the same as the left hand, in the case where we don't have enough
                // transactions.
                int right = Math.min(left + 1, levelSize - 1);
                byte[] leftBytes = ByteUtils.reverseBytes(tree.get(levelOffset + left));
                byte[] rightBytes = ByteUtils.reverseBytes(tree.get(levelOffset + right));
                tree.add(ByteUtils.reverseBytes(hashTwice(leftBytes, rightBytes)));
            }
            // Move to the next level.
            levelOffset += levelSize;
        }
        return tree;
    }

    /**
     * Verify the transactions on a block.
     *
     * @param height block height, if known, or -1 otherwise. If provided, used
     * to validate the coinbase input script of v2 and above blocks.
     * @throws VerificationException if there was an error verifying the block.
     */
    private void checkTransactions(final int height, final EnumSet<VerifyFlag> flags)
            throws VerificationException {
        // The first transaction in a block must always be a coinbase transaction.
        if (!transactions.get(0).isCoinBase())
            throw new VerificationException("First tx is not coinbase");
        if (flags.contains(Block.VerifyFlag.HEIGHT_IN_COINBASE) && height >= BLOCK_HEIGHT_GENESIS) {
            transactions.get(0).checkCoinBaseHeight(height);
        }
        // The rest must not be.
        for (int i = 1; i < transactions.size(); i++) {
            if (transactions.get(i).isCoinBase())
                throw new VerificationException("TX " + i + " is coinbase when it should not be.");
        }
    }

    /**
     * Checks the block data to ensure it follows the rules laid out in the network parameters. Specifically,
     * throws an exception if the proof of work is invalid, or if the timestamp is too far from what it should be.
     * This is <b>not</b> everything that is required for a block to be valid, only what is checkable independent
     * of the chain and without a transaction index.
     *
     * @throws VerificationException
     */
    public void verifyHeader() throws VerificationException {
        // Prove that this block is OK. It might seem that we can just ignore most of these checks given that the
        // network is also verifying the blocks, but we cannot as it'd open us to a variety of obscure attacks.
        //
        // Firstly we need to ensure this block does in fact represent real work done. If the difficulty is high
        // enough, it's probably been done by the network.
        checkProofOfWork(true);
        checkTimestamp();
    }

    /**
     * Checks the block contents
     *
     * @param height block height, if known, or -1 otherwise. If valid, used
     * to validate the coinbase input script of v2 and above blocks.
     * @param flags flags to indicate which tests should be applied (i.e.
     * whether to test for height in the coinbase transaction).
     * @throws VerificationException if there was an error verifying the block.
     */
    public void verifyTransactions(final int height, final EnumSet<VerifyFlag> flags) throws VerificationException {
        // Now we need to check that the body of the block actually matches the headers. The network won't generate
        // an invalid block, but if we didn't validate this then an untrusted man-in-the-middle could obtain the next
        // valid block from the network and simply replace the transactions in it with their own fictional
        // transactions that reference spent or non-existent inputs.
        if (transactions.isEmpty())
            throw new VerificationException("Block had no transactions");
        if (this.getOptimalEncodingMessageSize() > MAX_BLOCK_SIZE)
            throw new VerificationException("Block larger than MAX_BLOCK_SIZE");
        checkTransactions(height, flags);
        checkMerkleRoot();
        checkSigOps();
        for (Transaction transaction : transactions)
            transaction.verify();
        }

    /**
     * Verifies both the header and that the transactions hash to the merkle root.
     *
     * @param height block height, if known, or -1 otherwise.
     * @param flags flags to indicate which tests should be applied (i.e.
     * whether to test for height in the coinbase transaction).
     * @throws VerificationException if there was an error verifying the block.
     */
    public void verify(final int height, final EnumSet<VerifyFlag> flags) throws VerificationException {
        verifyHeader();
        verifyTransactions(height, flags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return getHash().equals(((Block)o).getHash());
    }

    @Override
    public int hashCode() {
        return getHash().hashCode();
    }

    /**
     * Returns the merkle root in big endian form, calculating it from transactions if necessary.
     */
    public Sha256Hash getMerkleRoot() {
        if (merkleRoot == null) {
            //TODO check if this is really necessary.
            unCacheHeader();
            merkleRoot = calculateMerkleRoot();
        }
        return merkleRoot;
    }

    /** Exists only for unit testing. */
    @VisibleForTesting
    void setMerkleRoot(Sha256Hash value) {
        unCacheHeader();
        merkleRoot = value;
        hash = null;
    }

    /**
     * Returns the witness root in big endian form, calculating it from transactions if necessary.
     */
    public Sha256Hash getWitnessRoot() {
        if (witnessRoot == null)
            witnessRoot = calculateWitnessRoot();
        return witnessRoot;
    }

    /** Adds a transaction to this block. The nonce and merkle root are invalid after this. */
    public void addTransaction(Transaction t) {
        addTransaction(t, true);
    }

    /** Adds a transaction to this block, with or without checking the sanity of doing so */
    void addTransaction(Transaction t, boolean runSanityChecks) {
        unCacheTransactions();
        if (transactions == null) {
            transactions = new ArrayList<>();
        }
        t.setParent(this);
        if (runSanityChecks && transactions.size() == 0 && !t.isCoinBase())
            throw new RuntimeException("Attempted to add a non-coinbase transaction as the first transaction: " + t);
        else if (runSanityChecks && transactions.size() > 0 && t.isCoinBase())
            throw new RuntimeException("Attempted to add a coinbase transaction when there already is one: " + t);
        transactions.add(t);
        adjustLength(transactions.size(), t.length);
        // Force a recalculation next time the values are needed.
        merkleRoot = null;
        hash = null;
    }

    /** Returns the version of the block data structure as defined by the Bitcoin protocol. */
    public long getVersion() {
        return version;
    }

    /**
     * Returns the hash of the previous block in the chain, as defined by the block header.
     */
    public Sha256Hash getPrevBlockHash() {
        return prevBlockHash;
    }

    @VisibleForTesting
    void setPrevBlockHash(Sha256Hash prevBlockHash) {
        unCacheHeader();
        this.prevBlockHash = prevBlockHash;
        this.hash = null;
    }

    /**
     * Returns the time at which the block was solved and broadcast, according to the clock of the solving node. This
     * is measured in seconds since the UNIX epoch (midnight Jan 1st 1970).
     */
    public long getTimeSeconds() {
        return time;
    }

    /**
     * Returns the time at which the block was solved and broadcast, according to the clock of the solving node.
     */
    public Date getTime() {
        return new Date(getTimeSeconds()*1000);
    }

    @VisibleForTesting
    public void setTime(long time) {
        unCacheHeader();
        this.time = time;
        this.hash = null;
    }

    /**
     * Returns the difficulty of the proof of work that this block should meet encoded <b>in compact form</b>. The {@link
     * BlockChain} verifies that this is not too easy by looking at the length of the chain when the block is added.
     * To find the actual value the hash should be compared against, use
     * {@link Block#getDifficultyTargetAsInteger()}. Note that this is <b>not</b> the same as
     * the difficulty value reported by the Bitcoin "getdifficulty" RPC that you may see on various block explorers.
     * That number is the result of applying a formula to the underlying difficulty to normalize the minimum to 1.
     * Calculating the difficulty that way is currently unsupported.
     */
    public long getDifficultyTarget() {
        return difficultyTarget;
    }

    /** Sets the difficulty target in compact form. */
    @VisibleForTesting
    public void setDifficultyTarget(long compactForm) {
        unCacheHeader();
        this.difficultyTarget = compactForm;
        this.hash = null;
    }

    /**
     * Returns the nonce, an arbitrary value that exists only to make the hash of the block header fall below the
     * difficulty target.
     */
    public long getNonce() {
        return nonce;
    }

    /** Sets the nonce and clears any cached data. */
    @VisibleForTesting
    public void setNonce(long nonce) {
        unCacheHeader();
        this.nonce = nonce;
        this.hash = null;
    }

    /** Returns an unmodifiable list of transactions held in this block, or null if this object represents just a header. */
    @Nullable
    public List<Transaction> getTransactions() {
        return transactions == null ? null : Collections.unmodifiableList(transactions);
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////
    // Unit testing related methods.

    // Used to make transactions unique.
    private static int txCounter;

    /** Adds a coinbase transaction to the block. This exists for unit tests.
     * 
     * @param height block height, if known, or -1 otherwise.
     */
    @VisibleForTesting
    void addCoinbaseTransaction(byte[] pubKeyTo, Coin value, final int height) {
        unCacheTransactions();
        transactions = new ArrayList<>();
        Transaction coinbase = new Transaction(params);
        final ScriptBuilder inputBuilder = new ScriptBuilder();

        if (height >= Block.BLOCK_HEIGHT_GENESIS) {
            inputBuilder.number(height);
        }
        inputBuilder.data(new byte[]{(byte) txCounter, (byte) (txCounter++ >> 8)});

        // A real coinbase transaction has some stuff in the scriptSig like the extraNonce and difficulty. The
        // transactions are distinguished by every TX output going to a different key.
        //
        // Here we will do things a bit differently so a new address isn't needed every time. We'll put a simple
        // counter in the scriptSig so every transaction has a different hash.
        coinbase.addInput(new TransactionInput(params, coinbase,
                inputBuilder.build().getProgram()));
        coinbase.addOutput(new TransactionOutput(params, coinbase, value,
                ScriptBuilder.createP2PKOutputScript(ECKey.fromPublicOnly(pubKeyTo)).getProgram()));
        transactions.add(coinbase);
        coinbase.setParent(this);
        coinbase.length = coinbase.unsafeBitcoinSerialize().length;
        adjustLength(transactions.size(), coinbase.length);
    }

    private static final byte[] EMPTY_BYTES = new byte[32];

    // It's pretty weak to have this around at runtime: fix later.
    private static final byte[] pubkeyForTesting = new ECKey().getPubKey();

    /**
     * Returns a solved block that builds on top of this one. This exists for unit tests.
     */
    @VisibleForTesting
    public Block createNextBlock(Address to, long version, long time, int blockHeight) {
        return createNextBlock(to, version, null, time, pubkeyForTesting, FIFTY_COINS, blockHeight);
    }

    /**
     * Returns a solved block that builds on top of this one. This exists for unit tests.
     * In this variant you can specify a public key (pubkey) for use in generating coinbase blocks.
     * 
     * @param height block height, if known, or -1 otherwise.
     */
    @VisibleForTesting
    Block createNextBlock(@Nullable final Address to, final long version,
                          @Nullable TransactionOutPoint prevOut, final long time,
                          final byte[] pubKey, final Coin coinbaseValue,
                          final int height) {
        Block b = new Block(params, version);
        b.setDifficultyTarget(difficultyTarget);
        b.addCoinbaseTransaction(pubKey, coinbaseValue, height);

        if (to != null) {
            // Add a transaction paying 50 coins to the "to" address.
            Transaction t = new Transaction(params);
            t.addOutput(new TransactionOutput(params, t, FIFTY_COINS, to));
            // The input does not really need to be a valid signature, as long as it has the right general form.
            TransactionInput input;
            if (prevOut == null) {
                prevOut = new TransactionOutPoint(params, 0, nextTestOutPointHash());
            }
            input = new TransactionInput(params, t, Script.createInputScript(EMPTY_BYTES, EMPTY_BYTES), prevOut);
            t.addInput(input);
            b.addTransaction(t);
        }

        b.setPrevBlockHash(getHash());
        // Don't let timestamp go backwards
        if (getTimeSeconds() >= time)
            b.setTime(getTimeSeconds() + 1);
        else
            b.setTime(time);
        b.solve();
        try {
            b.verifyHeader();
        } catch (VerificationException e) {
            throw new RuntimeException(e); // Cannot happen.
        }
        if (b.getVersion() != version) {
            throw new RuntimeException();
        }
        return b;
    }

    // Importantly the outpoint hash cannot be zero as that's how we detect a coinbase transaction in isolation
    // but it must be unique to avoid 'different' transactions looking the same.
    private Sha256Hash nextTestOutPointHash() {
        byte[] counter = new byte[32];
        counter[0] = (byte) txCounter;
        counter[1] = (byte) (txCounter++ >> 8);
        return Sha256Hash.wrap(counter);
    }

    @VisibleForTesting
    public Block createNextBlock(@Nullable Address to, TransactionOutPoint prevOut) {
        return createNextBlock(to, BLOCK_VERSION_GENESIS, prevOut, getTimeSeconds() + 5, pubkeyForTesting, FIFTY_COINS, BLOCK_HEIGHT_UNKNOWN);
    }

    @VisibleForTesting
    public Block createNextBlock(@Nullable Address to, Coin value) {
        return createNextBlock(to, BLOCK_VERSION_GENESIS, null, getTimeSeconds() + 5, pubkeyForTesting, value, BLOCK_HEIGHT_UNKNOWN);
    }

    @VisibleForTesting
    public Block createNextBlock(@Nullable Address to) {
        return createNextBlock(to, FIFTY_COINS);
    }

    @VisibleForTesting
    public Block createNextBlockWithCoinbase(long version, byte[] pubKey, Coin coinbaseValue, final int height) {
        return createNextBlock(null, version, (TransactionOutPoint) null,
                               Utils.currentTimeSeconds(), pubKey, coinbaseValue, height);
    }

    /**
     * Create a block sending 50BTC as a coinbase transaction to the public key specified.
     * This method is intended for test use only.
     */
    @VisibleForTesting
    Block createNextBlockWithCoinbase(long version, byte[] pubKey, final int height) {
        return createNextBlock(null, version, (TransactionOutPoint) null,
                               Utils.currentTimeSeconds(), pubKey, FIFTY_COINS, height);
    }

    @VisibleForTesting
    boolean isHeaderBytesValid() {
        return headerBytesValid;
    }

    @VisibleForTesting
    boolean isTransactionBytesValid() {
        return transactionBytesValid;
    }

    /**
     * Return whether this block contains any transactions.
     * 
     * @return  true if the block contains transactions, false otherwise (is
     * purely a header).
     */
    public boolean hasTransactions() {
        return (this.transactions != null) && !this.transactions.isEmpty();
    }

    /**
     * Returns whether this block conforms to
     * <a href="https://github.com/bitcoin/bips/blob/master/bip-0034.mediawiki">BIP34: Height in Coinbase</a>.
     */
    public boolean isBIP34() {
        return version >= BLOCK_VERSION_BIP34;
    }

    /**
     * Returns whether this block conforms to
     * <a href="https://github.com/bitcoin/bips/blob/master/bip-0066.mediawiki">BIP66: Strict DER signatures</a>.
     */
    public boolean isBIP66() {
        return version >= BLOCK_VERSION_BIP66;
    }

    /**
     * Returns whether this block conforms to
     * <a href="https://github.com/bitcoin/bips/blob/master/bip-0065.mediawiki">BIP65: OP_CHECKLOCKTIMEVERIFY</a>.
     */
    public boolean isBIP65() {
        return version >= BLOCK_VERSION_BIP65;
    }
}
