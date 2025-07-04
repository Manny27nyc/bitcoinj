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
 * Copyright 2014 devrandom
 * Copyright 2019 Andreas Schildbach
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

package org.bitcoinj.wallet;

import org.bitcoinj.base.ScriptType;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.KeyCrypter;

import java.util.List;

/**
 * Factory interface for creation keychains while de-serializing a wallet.
 */
public interface KeyChainFactory {
    /**
     * Make a keychain (but not a watching one) with the specified account path
     *
     * @param seed the seed
     * @param crypter the encrypted/decrypter
     * @param isMarried whether the keychain is leading in a marriage
     * @param outputScriptType type of addresses (aka output scripts) to generate for receiving
     * @param accountPath account path to generate receiving addresses on
     */
    DeterministicKeyChain makeKeyChain(DeterministicSeed seed, KeyCrypter crypter, boolean isMarried,
                                       ScriptType outputScriptType, List<ChildNumber> accountPath);

    /**
     * Make a watching keychain.
     *
     * <p>isMarried and isFollowingKey must not be true at the same time.
     *
     * @param accountKey the account extended public key
     * @param isFollowingKey whether the keychain is following in a marriage
     * @param isMarried whether the keychain is leading in a marriage
     * @param outputScriptType type of addresses (aka output scripts) to generate for watching
     */
    DeterministicKeyChain makeWatchingKeyChain(DeterministicKey accountKey, boolean isFollowingKey, boolean isMarried,
            ScriptType outputScriptType) throws UnreadableWalletException;

    /**
     * Make a spending keychain.
     *
     * <p>isMarried and isFollowingKey must not be true at the same time.
     *
     * @param accountKey the account extended public key
     * @param isMarried whether the keychain is leading in a marriage
     * @param outputScriptType type of addresses (aka output scripts) to generate for spending
     */
    DeterministicKeyChain makeSpendingKeyChain(DeterministicKey accountKey, boolean isMarried,
            ScriptType outputScriptType) throws UnreadableWalletException;
}
