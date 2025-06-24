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
package org.bitcoinj.utils;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

/**
 * A {@link CompletionStage} with a {@link ListenableFuture}-compatible interface to smooth migration
 * from Guava {@code ListenableFuture} to {@link java.util.concurrent.CompletableFuture}/{@code CompletionStage}.
 * <p>
 * Note that this is much easier to implement than trying to extend {@link com.google.common.util.concurrent.AbstractFuture}
 * to implement {@code CompletionStage}.
 */
public interface ListenableCompletionStage<V> extends CompletionStage<V>, ListenableFuture<V> {
    @Override
    default void addListener(Runnable listener, Executor executor) {
        this.thenRunAsync(listener, executor);
    }
}
