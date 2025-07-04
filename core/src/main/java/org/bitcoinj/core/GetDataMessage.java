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

package org.bitcoinj.core;

import org.bitcoinj.base.Sha256Hash;

/**
 * <p>Represents the "getdata" P2P network message, which requests the contents of blocks or transactions given their
 * hashes.</p>
 * 
 * <p>Instances of this class are not safe for use by multiple threads.</p>
 */
public class GetDataMessage extends ListMessage {

    public GetDataMessage(NetworkParameters params, byte[] payloadBytes) throws ProtocolException {
        super(params, payloadBytes);
    }

    /**
     * Deserializes a 'getdata' message.
     * @param params NetworkParameters object.
     * @param payload Bitcoin protocol formatted byte array containing message content.
     * @param serializer the serializer to use for this message.
     * @param length The length of message if known.  Usually this is provided when deserializing of the wire
     * as the length will be provided as part of the header.  If unknown then set to Message.UNKNOWN_LENGTH
     * @throws ProtocolException
     */
    public GetDataMessage(NetworkParameters params, byte[] payload, MessageSerializer serializer, int length)
            throws ProtocolException {
        super(params, payload, serializer, length);
    }

    public GetDataMessage(NetworkParameters params) {
        super(params);
    }

    public void addTransaction(Sha256Hash hash, boolean includeWitness) {
        addItem(new InventoryItem(
                includeWitness ? InventoryItem.Type.WITNESS_TRANSACTION : InventoryItem.Type.TRANSACTION, hash));
    }

    public void addBlock(Sha256Hash hash, boolean includeWitness) {
        addItem(new InventoryItem(includeWitness ? InventoryItem.Type.WITNESS_BLOCK : InventoryItem.Type.BLOCK, hash));
    }

    public void addFilteredBlock(Sha256Hash hash) {
        addItem(new InventoryItem(InventoryItem.Type.FILTERED_BLOCK, hash));
    }

    public Sha256Hash getHashOf(int i) {
        return getItems().get(i).hash;
    }
}
