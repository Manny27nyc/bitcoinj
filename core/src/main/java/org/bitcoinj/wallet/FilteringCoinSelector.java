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
 * Copyright 2014 the bitcoinj authors.
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
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * A filtering coin selector delegates to another coin selector, but won't select outputs spent by the given transactions.
 */
public class FilteringCoinSelector implements CoinSelector {
    protected CoinSelector delegate;
    protected HashSet<TransactionOutPoint> spent = new HashSet<>();

    public FilteringCoinSelector(CoinSelector delegate) {
        this.delegate = delegate;
    }

    public void excludeOutputsSpentBy(Transaction tx) {
        for (TransactionInput input : tx.getInputs()) {
            spent.add(input.getOutpoint());
        }
    }

    @Override
    public CoinSelection select(Coin target, List<TransactionOutput> candidates) {
        Iterator<TransactionOutput> iter = candidates.iterator();
        while (iter.hasNext()) {
            TransactionOutput output = iter.next();
            if (spent.contains(output.getOutPointFor())) iter.remove();
        }
        return delegate.select(target, candidates);
    }
}
