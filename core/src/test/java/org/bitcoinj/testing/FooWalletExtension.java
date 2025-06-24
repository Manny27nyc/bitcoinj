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

package org.bitcoinj.testing;

import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletExtension;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;

public class FooWalletExtension implements WalletExtension {
    private final byte[] data = {1, 2, 3};

    private final boolean isMandatory;
    private final String id;

    public FooWalletExtension(String id, boolean isMandatory) {
        this.isMandatory = isMandatory;
        this.id = id;
    }

    @Override
    public String getWalletExtensionID() {
        return id;
    }

    @Override
    public boolean isWalletExtensionMandatory() {
        return isMandatory;
    }

    @Override
    public byte[] serializeWalletExtension() {
        return data;
    }

    @Override
    public void deserializeWalletExtension(Wallet wallet, byte[] data) {
        checkArgument(Arrays.equals(this.data, data));
    }
}
