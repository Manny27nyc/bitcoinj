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

package org.bitcoinj.store;

import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.StoredBlock;

/**
 * An implementor of BlockStore saves StoredBlock objects to disk. Different implementations store them in
 * different ways. An in-memory implementation (MemoryBlockStore) exists for unit testing but real apps will want to
 * use implementations that save to disk.<p>
 *
 * A BlockStore is a map of hashes to StoredBlock. The hash is the double digest of the Bitcoin serialization
 * of the block header, <b>not</b> the header with the extra data as well.<p>
 *
 * BlockStores are thread safe.
 */
public interface BlockStore {
    /**
     * Saves the given block header+extra data. The key isn't specified explicitly as it can be calculated from the
     * StoredBlock directly. Can throw if there is a problem with the underlying storage layer such as running out of
     * disk space.
     */
    void put(StoredBlock block) throws BlockStoreException;

    /**
     * Returns the StoredBlock given a hash. The returned values block.getHash() method will be equal to the
     * parameter. If no such block is found, returns null.
     */
    StoredBlock get(Sha256Hash hash) throws BlockStoreException;

    /**
     * Returns the {@link StoredBlock} that represents the top of the chain of greatest total work. Note that this
     * can be arbitrarily expensive, you probably should use {@link BlockChain#getChainHead()}
     * or perhaps {@link BlockChain#getBestChainHeight()} which will run in constant time and
     * not take any heavyweight locks.
     */
    StoredBlock getChainHead() throws BlockStoreException;

    /**
     * Sets the {@link StoredBlock} that represents the top of the chain of greatest total work.
     */
    void setChainHead(StoredBlock chainHead) throws BlockStoreException;
    
    /** Closes the store. */
    void close() throws BlockStoreException;

    /**
     * Get the {@link NetworkParameters} of this store.
     * @return The network params.
     */
    NetworkParameters getParams();
}
