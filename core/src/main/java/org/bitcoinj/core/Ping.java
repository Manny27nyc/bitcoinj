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
 * Copyright 2011 Noa Resare
 * Copyright 2015 Andreas Schildbach
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

import org.bitcoinj.base.utils.ByteUtils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>Instances of this class are not safe for use by multiple threads.</p>
 */
public class Ping extends Message {
    private long nonce;
    private boolean hasNonce;
    
    public Ping(NetworkParameters params, byte[] payloadBytes) throws ProtocolException {
        super(params, payloadBytes, 0);
    }
    
    /**
     * Create a Ping with a nonce value.
     * Only use this if the remote node has a protocol version greater than 60000
     */
    public Ping(long nonce) {
        this.nonce = nonce;
        this.hasNonce = true;
    }
    
    /**
     * Create a Ping without a nonce value.
     * Only use this if the remote node has a protocol version lower than or equal 60000
     */
    public Ping() {
        this.hasNonce = false;
    }
    
    @Override
    public void bitcoinSerializeToStream(OutputStream stream) throws IOException {
        if (hasNonce)
            ByteUtils.int64ToByteStreamLE(nonce, stream);
    }

    @Override
    protected void parse() throws ProtocolException {
        try {
            nonce = readInt64();
            hasNonce = true;
        } catch(ProtocolException e) {
            hasNonce = false;
        }
        length = hasNonce ? 8 : 0;
    }
    
    public boolean hasNonce() {
        return hasNonce;
    }
    
    public long getNonce() {
        return nonce;
    }
}
