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

import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

// TODO: Rename this class to SPVBlockChain at some point.

/**
 * A BlockChain implements the <i>simplified payment verification</i> mode of the Bitcoin protocol. It is the right
 * choice to use for programs that have limited resources as it won't verify transactions signatures or attempt to store
 * all of the block chain. Really, this class should be called SPVBlockChain but for backwards compatibility it is not.
 */
public class BlockChain extends AbstractBlockChain {
    /** Keeps a map of block hashes to StoredBlocks. */
    protected final BlockStore blockStore;

    /**
     * <p>Constructs a BlockChain connected to the given wallet and store. To obtain a {@link Wallet} you can construct
     * one from scratch, or you can deserialize a saved wallet from disk using
     * {@link Wallet#loadFromFile(File, WalletExtension...)}</p>
     *
     * <p>For the store, you should use {@link SPVBlockStore} or you could also try a
     * {@link MemoryBlockStore} if you want to hold all headers in RAM and don't care about
     * disk serialization (this is rare).</p>
     */
    public BlockChain(NetworkParameters params, Wallet wallet, BlockStore blockStore) throws BlockStoreException {
        this(params, new ArrayList<Wallet>(), blockStore);
        addWallet(wallet);
    }

    /**
     * Constructs a BlockChain that has no wallet at all. This is helpful when you don't actually care about sending
     * and receiving coins but rather, just want to explore the network data structures.
     */
    public BlockChain(NetworkParameters params, BlockStore blockStore) throws BlockStoreException {
        this(params, new ArrayList<Wallet>(), blockStore);
    }

    /**
     * Constructs a BlockChain connected to the given list of listeners and a store.
     */
    public BlockChain(NetworkParameters params, List<? extends Wallet> wallets, BlockStore blockStore) throws BlockStoreException {
        super(params, wallets, blockStore);
        this.blockStore = blockStore;
    }

    @Override
    protected StoredBlock addToBlockStore(StoredBlock storedPrev, Block blockHeader, TransactionOutputChanges txOutChanges)
            throws BlockStoreException, VerificationException {
        StoredBlock newBlock = storedPrev.build(blockHeader);
        blockStore.put(newBlock);
        return newBlock;
    }
    
    @Override
    protected StoredBlock addToBlockStore(StoredBlock storedPrev, Block blockHeader)
            throws BlockStoreException, VerificationException {
        StoredBlock newBlock = storedPrev.build(blockHeader);
        blockStore.put(newBlock);
        return newBlock;
    }

    @Override
    protected void rollbackBlockStore(int height) throws BlockStoreException {
        lock.lock();
        try {
            int currentHeight = getBestChainHeight();
            checkArgument(height >= 0 && height <= currentHeight, "Bad height: %s", height);
            if (height == currentHeight)
                return; // nothing to do

            // Look for the block we want to be the new chain head
            StoredBlock newChainHead = blockStore.getChainHead();
            while (newChainHead.getHeight() > height) {
                newChainHead = newChainHead.getPrev(blockStore);
                if (newChainHead == null)
                    throw new BlockStoreException("Unreachable height");
            }

            // Modify store directly
            blockStore.put(newChainHead);
            this.setChainHead(newChainHead);
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected boolean shouldVerifyTransactions() {
        return false;
    }

    @Override
    protected TransactionOutputChanges connectTransactions(int height, Block block) {
        // Don't have to do anything as this is only called if(shouldVerifyTransactions())
        throw new UnsupportedOperationException();
    }

    @Override
    protected TransactionOutputChanges connectTransactions(StoredBlock newBlock) {
        // Don't have to do anything as this is only called if(shouldVerifyTransactions())
        throw new UnsupportedOperationException();
    }

    @Override
    protected void disconnectTransactions(StoredBlock block) {
        // Don't have to do anything as this is only called if(shouldVerifyTransactions())
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doSetChainHead(StoredBlock chainHead) throws BlockStoreException {
        blockStore.setChainHead(chainHead);
    }

    @Override
    protected void notSettingChainHead() throws BlockStoreException {
        // We don't use DB transactions here, so we don't need to do anything
    }

    @Override
    protected StoredBlock getStoredBlockInCurrentScope(Sha256Hash hash) throws BlockStoreException {
        return blockStore.get(hash);
    }

    @Override
    public boolean add(FilteredBlock block) throws VerificationException, PrunedException {
        boolean success = super.add(block);
        if (success) {
            trackFilteredTransactions(block.getTransactionCount());
        }
        return success;
    }
}
