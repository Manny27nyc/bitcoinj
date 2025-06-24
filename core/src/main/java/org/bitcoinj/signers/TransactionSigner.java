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
 * Copyright 2014 Kosta Korenkov
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

package org.bitcoinj.signers;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.KeyBag;
import org.bitcoinj.wallet.Wallet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Implementations of this interface are intended to sign inputs of the given transaction. Given transaction may already
 * be partially signed or somehow altered by other signers.</p>
 * <p>To make use of the signer, you need to add it into the  wallet by
 * calling {@link Wallet#addTransactionSigner(TransactionSigner)}. Signer will be serialized
 * along with the wallet data. In order for a wallet to recreate signer after deserialization, each signer
 * should have no-args constructor</p>
 */
public interface TransactionSigner {

    /**
     * This class wraps transaction proposed to complete keeping a metadata that may be updated, used and effectively
     * shared by transaction signers.
     */
    class ProposedTransaction {

        public final Transaction partialTx;

        /**
         * HD key paths used for each input to derive a signing key. It's useful for multisig inputs only.
         * The keys used to create a single P2SH address have the same derivation path, so to use a correct key each signer
         * has to know a derivation path of signing keys used by previous signers. For each input signers will use the
         * same derivation path and we need to store only one key path per input. As TransactionInput is mutable, inputs
         * are identified by their scriptPubKeys (keys in this map).
         */
        public final Map<Script, List<ChildNumber>> keyPaths;

        public ProposedTransaction(Transaction partialTx) {
            this.partialTx = partialTx;
            this.keyPaths = new HashMap<>();
        }
    }

    class MissingSignatureException extends RuntimeException {
    }

    /**
     * Returns true if this signer is ready to be used.
     */
    boolean isReady();

    /**
     * Signs given transaction's inputs.
     * Returns true if signer is compatible with given transaction (can do something meaningful with it).
     * Otherwise this method returns false
     */
    boolean signInputs(ProposedTransaction propTx, KeyBag keyBag);

}
