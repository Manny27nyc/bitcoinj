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
 * Copyright 2018 Andreas Schildbach
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

import org.bitcoinj.base.Network;

import java.util.Arrays;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Some form of string-encoded private key. This form is useful for noting them down, e.g. on paper wallets.
 */
public abstract class EncodedPrivateKey {
    protected final Network network;
    protected final byte[] bytes;

    protected EncodedPrivateKey(Network network, byte[] bytes) {
        this.network = checkNotNull(network);
        this.bytes = checkNotNull(bytes);
    }

    @Deprecated
    protected EncodedPrivateKey(NetworkParameters params, byte[] bytes) {
        this(checkNotNull(params).network(), checkNotNull(bytes));
    }

    /**
     * Get the network this data is prefixed with.
     * @return the Network.
     */
    public Network network() {
        return network;
    }

    /**
     * @return network this data is valid for
     * @deprecated Use {@link #network()}
     */
    @Deprecated
    public final NetworkParameters getParameters() {
        return NetworkParameters.of(network);
    }

    @Override
    public int hashCode() {
        return Objects.hash(network, Arrays.hashCode(bytes));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncodedPrivateKey other = (EncodedPrivateKey) o;
        return this.network.equals(other.network) && Arrays.equals(this.bytes, other.bytes);
    }
}
