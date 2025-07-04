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

package org.bitcoinj.wallet;

import org.bitcoinj.base.Coin;
import org.bitcoinj.core.TransactionOutput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents the results of a {@link CoinSelector#select(Coin, List)} operation. A coin selection represents a list
 * of spendable transaction outputs that sum together to a {@link #totalValue()} value gathered. Different coin selections
 * could be produced by different coin selectors from the same input set, according to their varying policies.
 */
public class CoinSelection {
    /**
     * @deprecated Use {@link #totalValue()}
     */
    @Deprecated
    public final Coin valueGathered;
    /**
     * @deprecated Use {@link #outputs()}
     */
    @Deprecated
    public final List<TransactionOutput> gathered;

    public CoinSelection(List<TransactionOutput> gathered) {
        this.valueGathered = sumOutputValues(gathered);
        this.gathered = gathered;
    }

    /**
     * @deprecated use {@link #CoinSelection(List)}
     */
    @Deprecated
    public CoinSelection(Coin valueGathered, Collection<TransactionOutput> gathered) {
        // ignore valueGathered
        this(new ArrayList<>(gathered));
    }

    private static Coin sumOutputValues(List<TransactionOutput> outputs) {
        return outputs.stream()
                .map(TransactionOutput::getValue)
                .reduce(Coin.ZERO, Coin::add);
    }

    /**
     * @return Total value of gathered outputs.
     */
    public Coin totalValue() {
        return valueGathered;
    }

    /**
     * @return List of gathered outputs
     */
    public List<TransactionOutput> outputs() {
        return gathered;
    }
}
