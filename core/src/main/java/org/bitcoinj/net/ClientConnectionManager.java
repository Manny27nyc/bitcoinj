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
 * Copyright 2013 Google Inc.
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

package org.bitcoinj.net;

import com.google.common.util.concurrent.Service;
import org.bitcoinj.utils.ListenableCompletableFuture;

import java.net.SocketAddress;

/**
 * <p>A generic interface for an object which keeps track of a set of open client connections, creates new ones and
 * ensures they are serviced properly.</p>
 *
 * <p>When the service is stopped via {@link com.google.common.util.concurrent.Service#stopAsync()}, all connections will be closed and
 * the appropriate connectionClosed() calls must be made.</p>
 */
public interface ClientConnectionManager extends Service {
    /**
     * Creates a new connection to the given address, with the given connection used to handle incoming data. Any errors
     * that occur during connection will be returned in the given future, including errors that can occur immediately.
     */
    ListenableCompletableFuture<SocketAddress> openConnection(SocketAddress serverAddress, StreamConnection connection);

    /** Gets the number of connected peers */
    int getConnectedClientCount();

    /** Closes n peer connections */
    void closeConnections(int n);
}
