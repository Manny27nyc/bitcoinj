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

import org.bitcoinj.base.utils.ByteUtils;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;

/**
 * Wraps a {@link Block} object with extra data that can be derived from the block chain but is slow or inconvenient to
 * calculate. By storing it alongside the block header we reduce the amount of work required significantly.
 * Recalculation is slow because the fields are cumulative - to find the chainWork you have to iterate over every
 * block in the chain back to the genesis block, which involves lots of seeking/loading etc. So we just keep a
 * running total: it's a disk space vs cpu/io tradeoff.<p>
 *
 * StoredBlocks are put inside a {@link BlockStore} which saves them to memory or disk.
 */
public class StoredBlock {

    // A BigInteger representing the total amount of work done so far on this chain. As of May 2011 it takes 8
    // bytes to represent this field, so 12 bytes should be plenty for now.
    private static final int CHAIN_WORK_BYTES = 12;
    private static final byte[] EMPTY_BYTES = new byte[CHAIN_WORK_BYTES];
    public static final int COMPACT_SERIALIZED_SIZE = Block.HEADER_SIZE + CHAIN_WORK_BYTES + 4;  // for height

    private final Block header;
    private final BigInteger chainWork;
    private final int height;

    /**
     * Create a StoredBlock from a (header-only) {@link Block}, chain work value, and block height
     *
     * @param header A Block object with only a header (no transactions should be included)
     * @param chainWork Calculated chainWork for this block
     * @param height block height for this block
     */
    public StoredBlock(Block header, BigInteger chainWork, int height) {
        this.header = header;
        this.chainWork = chainWork;
        this.height = height;
    }

    /**
     * The block header this object wraps. The referenced block object must not have any transactions in it.
     */
    public Block getHeader() {
        return header;
    }

    /**
     * The total sum of work done in this block, and all the blocks below it in the chain. Work is a measure of how
     * many tries are needed to solve a block. If the target is set to cover 10% of the total hash value space,
     * then the work represented by a block is 10.
     */
    public BigInteger getChainWork() {
        return chainWork;
    }

    /**
     * Position in the chain for this block. The genesis block has a height of zero.
     */
    public int getHeight() {
        return height;
    }

    /** Returns true if this objects chainWork is higher than the others. */
    public boolean moreWorkThan(StoredBlock other) {
        return chainWork.compareTo(other.chainWork) > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoredBlock other = (StoredBlock) o;
        return header.equals(other.header) && chainWork.equals(other.chainWork) && height == other.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(header, chainWork, height);
    }

    /**
     * Creates a new StoredBlock, calculating the additional fields by adding to the values in this block.
     */
    public StoredBlock build(Block block) throws VerificationException {
        // Stored blocks track total work done in this chain, because the canonical chain is the one that represents
        // the largest amount of work done not the tallest.
        BigInteger chainWork = this.chainWork.add(block.getWork());
        int height = this.height + 1;
        return new StoredBlock(block, chainWork, height);
    }

    /**
     * Given a block store, looks up the previous block in this chain. Convenience method for doing
     * {@code store.get(this.getHeader().getPrevBlockHash())}.
     *
     * @return the previous block in the chain or null if it was not found in the store.
     */
    public StoredBlock getPrev(BlockStore store) throws BlockStoreException {
        return store.get(getHeader().getPrevBlockHash());
    }

    /** Serializes the stored block to a custom packed format. Used by {@link CheckpointManager}. */
    public void serializeCompact(ByteBuffer buffer) {
        byte[] chainWorkBytes = getChainWork().toByteArray();
        checkState(chainWorkBytes.length <= CHAIN_WORK_BYTES, "Ran out of space to store chain work!");
        if (chainWorkBytes.length < CHAIN_WORK_BYTES) {
            // Pad to the right size.
            buffer.put(EMPTY_BYTES, 0, CHAIN_WORK_BYTES - chainWorkBytes.length);
        }
        buffer.put(chainWorkBytes);
        buffer.putInt(getHeight());
        // Using unsafeBitcoinSerialize here can give us direct access to the same bytes we read off the wire,
        // avoiding serialization round-trips.
        byte[] bytes = getHeader().unsafeBitcoinSerialize();
        buffer.put(bytes, 0, Block.HEADER_SIZE);  // Trim the trailing 00 byte (zero transactions).
    }

    /** De-serializes the stored block from a custom packed format. Used by {@link CheckpointManager}. */
    public static StoredBlock deserializeCompact(NetworkParameters params, ByteBuffer buffer) throws ProtocolException {
        byte[] chainWorkBytes = new byte[StoredBlock.CHAIN_WORK_BYTES];
        buffer.get(chainWorkBytes);
        BigInteger chainWork = ByteUtils.bytesToBigInteger(chainWorkBytes);
        int height = buffer.getInt();  // +4 bytes
        byte[] header = new byte[Block.HEADER_SIZE + 1];    // Extra byte for the 00 transactions length.
        buffer.get(header, 0, Block.HEADER_SIZE);
        return new StoredBlock(params.getDefaultSerializer().makeBlock(header), chainWork, height);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "Block %s at height %d: %s",
                getHeader().getHashAsString(), getHeight(), getHeader().toString());
    }
}
