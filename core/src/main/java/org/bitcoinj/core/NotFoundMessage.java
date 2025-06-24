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
 * Copyright 2012 Google Inc.
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

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Sent by a peer when a getdata request doesn't find the requested data in the mempool. It has the same format
 * as an inventory message and lists the hashes of the missing items.</p>
 * 
 * <p>Instances of this class are not safe for use by multiple threads.</p>
 */
public class NotFoundMessage extends InventoryMessage {
    public static int MIN_PROTOCOL_VERSION = 70001;

    public NotFoundMessage(NetworkParameters params) {
        super(params);
    }

    public NotFoundMessage(NetworkParameters params, byte[] payloadBytes) throws ProtocolException {
        super(params, payloadBytes);
    }

    public NotFoundMessage(NetworkParameters params, List<InventoryItem> items) {
        super(params);
        this.items = new ArrayList<>(items);
    }
}
