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

package org.bitcoinj.core;

import org.bitcoinj.base.Sha256Hash;

import java.util.List;

/**
 * Contains minimal data necessary to disconnect/connect the transactions
 * in the stored block at will. Can either store the full set of
 * transactions (if the inputs for the block have not been tested to work)
 * or the set of transaction outputs created/destroyed when the block is
 * connected.
 */
public class StoredUndoableBlock {
    
    Sha256Hash blockHash;
    
    // Only one of either txOutChanges or transactions will be set
    private TransactionOutputChanges txOutChanges;
    private List<Transaction> transactions;
    
    public StoredUndoableBlock(Sha256Hash hash, TransactionOutputChanges txOutChanges) {
        this.blockHash = hash;
        this.transactions = null;
        this.txOutChanges = txOutChanges;
    }
    
    public StoredUndoableBlock(Sha256Hash hash, List<Transaction> transactions) {
        this.blockHash = hash;
        this.txOutChanges = null;
        this.transactions = transactions;
    }
    
    /**
     * Get the transaction output changes if they have been calculated, otherwise null.
     * Only one of this and getTransactions() will return a non-null value.
     */
    public TransactionOutputChanges getTxOutChanges() {
        return txOutChanges;
    }
    
    /**
     * Get the full list of transactions if it is stored, otherwise null.
     * Only one of this and getTxOutChanges() will return a non-null value.
     */
    public List<Transaction> getTransactions() {
        return transactions;
    }
    
    /**
     * Get the hash of the represented block
     */
    public Sha256Hash getHash() {
        return blockHash;
    }

    @Override
    public int hashCode() {
        return blockHash.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return getHash().equals(((StoredUndoableBlock)o).getHash());
    }

    @Override
    public String toString() {
        return "Undoable Block " + blockHash;
    }
}
