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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>A protocol message that contains a repeated series of block headers, sent in response to the "getheaders" command.
 * This is useful when you want to traverse the chain but know you don't care about the block contents, for example,
 * because you have a freshly created wallet with no keys.</p>
 * 
 * <p>Instances of this class are not safe for use by multiple threads.</p>
 */
public class HeadersMessage extends Message {
    private static final Logger log = LoggerFactory.getLogger(HeadersMessage.class);

    // The main client will never send us more than this number of headers.
    public static final int MAX_HEADERS = 2000;

    private List<Block> blockHeaders;

    public HeadersMessage(NetworkParameters params, byte[] payload) throws ProtocolException {
        super(params, payload, 0);
    }

    public HeadersMessage(NetworkParameters params, Block... headers) throws ProtocolException {
        super(params);
        blockHeaders = Arrays.asList(headers);
    }

    public HeadersMessage(NetworkParameters params, List<Block> headers) throws ProtocolException {
        super(params);
        blockHeaders = headers;
    }

    @Override
    public void bitcoinSerializeToStream(OutputStream stream) throws IOException {
        stream.write(new VarInt(blockHeaders.size()).encode());
        for (Block header : blockHeaders) {
            header.cloneAsHeader().bitcoinSerializeToStream(stream);
            stream.write(0);
        }
    }

    @Override
    protected void parse() throws ProtocolException {
        int numHeaders = readVarInt().intValue();
        if (numHeaders > MAX_HEADERS)
            throw new ProtocolException("Too many headers: got " + numHeaders + " which is larger than " +
                                         MAX_HEADERS);

        blockHeaders = new ArrayList<>();
        final BitcoinSerializer serializer = this.params.getSerializer(true);

        for (int i = 0; i < numHeaders; ++i) {
            final Block newBlockHeader = serializer.makeBlock(payload, cursor, UNKNOWN_LENGTH);
            if (newBlockHeader.hasTransactions()) {
                throw new ProtocolException("Block header does not end with a null byte");
            }
            cursor += newBlockHeader.optimalEncodingMessageSize;
            blockHeaders.add(newBlockHeader);
        }

        if (length == UNKNOWN_LENGTH) {
            length = cursor - offset;
        }

        if (log.isDebugEnabled()) {
            for (int i = 0; i < numHeaders; ++i) {
                log.debug(this.blockHeaders.get(i).toString());
            }
        }
    }

    public List<Block> getBlockHeaders() {
        return blockHeaders;
    }
}
