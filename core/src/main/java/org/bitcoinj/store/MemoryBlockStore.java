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

package org.bitcoinj.store;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.VerificationException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Keeps {@link StoredBlock}s in memory. Used primarily for unit testing.
 */
public class MemoryBlockStore implements BlockStore {
    private LinkedHashMap<Sha256Hash, StoredBlock> blockMap = new LinkedHashMap<Sha256Hash, StoredBlock>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Sha256Hash, StoredBlock> eldest) {
            return blockMap.size() > 5000;
        }
    };
    private StoredBlock chainHead;
    private NetworkParameters params;

    public MemoryBlockStore(NetworkParameters params) {
        // Insert the genesis block.
        try {
            Block genesisHeader = params.getGenesisBlock().cloneAsHeader();
            StoredBlock storedGenesis = new StoredBlock(genesisHeader, genesisHeader.getWork(), 0);
            put(storedGenesis);
            setChainHead(storedGenesis);
            this.params = params;
        } catch (BlockStoreException | VerificationException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }

    @Override
    public synchronized final void put(StoredBlock block) throws BlockStoreException {
        if (blockMap == null) throw new BlockStoreException("MemoryBlockStore is closed");
        Sha256Hash hash = block.getHeader().getHash();
        blockMap.put(hash, block);
    }

    @Override
    public synchronized StoredBlock get(Sha256Hash hash) throws BlockStoreException {
        if (blockMap == null) throw new BlockStoreException("MemoryBlockStore is closed");
        return blockMap.get(hash);
    }

    @Override
    public StoredBlock getChainHead() throws BlockStoreException {
        if (blockMap == null) throw new BlockStoreException("MemoryBlockStore is closed");
        return chainHead;
    }

    @Override
    public final void setChainHead(StoredBlock chainHead) throws BlockStoreException {
        if (blockMap == null) throw new BlockStoreException("MemoryBlockStore is closed");
        this.chainHead = chainHead;
    }
    
    @Override
    public void close() {
        blockMap = null;
    }

    @Override
    public NetworkParameters getParams() {
        return params;
    }
}
