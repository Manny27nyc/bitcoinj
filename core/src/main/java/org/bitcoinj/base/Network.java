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
 * Copyright by the original author or authors.
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

package org.bitcoinj.base;

/**
 * Interface for a generic Bitcoin-like cryptocurrency network. See {@link BitcoinNetwork} for the Bitcoin implementation.
 */
public interface Network {
    /**
     * The dot-seperated string id for this network. For example {@code "org.bitcoin.production"}
     * @return String ID for network
     */
    String id();

    /**
     * The URI scheme for this network. See {@link BitcoinNetwork#uriScheme()}.
     * @return The URI scheme for this network
     */
    String uriScheme();

    /**
     * Does this network have a fixed maximum number of coins
     * @return {@code true} if this network has a fixed maximum number of coins
     */
    boolean hasMaxMoney();

    /**
     * Maximum number of coins for this network as a {@link Monetary} value.
     * Where not applicable, a very large number of coins is returned instead (e.g. the main coin issue for Dogecoin).
     * @return Maximum number of coins for this network
     */
    Monetary maxMoney();

    /**
     * Check if an amount exceeds the maximum allowed for a network (if the network has one)
     * @param monetary A monetary amount
     * @return true if too big, false if an allowed amount
     */
    boolean exceedsMaxMoney(Monetary monetary);
}
