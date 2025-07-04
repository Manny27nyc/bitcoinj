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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VarIntTest {

    @Test
    public void testBytes() {
        VarInt a = new VarInt(10); // with widening conversion
        assertEquals(1, a.getSizeInBytes());
        assertEquals(1, a.encode().length);
        assertEquals(10, new VarInt(a.encode(), 0).intValue());
    }

    @Test
    public void testShorts() {
        VarInt a = new VarInt(64000); // with widening conversion
        assertEquals(3, a.getSizeInBytes());
        assertEquals(3, a.encode().length);
        assertEquals(64000, new VarInt(a.encode(), 0).intValue());
    }

    @Test
    public void testShortFFFF() {
        VarInt a = new VarInt(0xFFFFL);
        assertEquals(3, a.getSizeInBytes());
        assertEquals(3, a.encode().length);
        assertEquals(0xFFFFL, new VarInt(a.encode(), 0).intValue());
    }

    @Test
    public void testInts() {
        VarInt a = new VarInt(0xAABBCCDDL);
        assertEquals(5, a.getSizeInBytes());
        assertEquals(5, a.encode().length);
        byte[] bytes = a.encode();
        assertEquals(0xAABBCCDDL, new VarInt(bytes, 0).longValue());
    }

    @Test
    public void testIntFFFFFFFF() {
        VarInt a = new VarInt(0xFFFFFFFFL);
        assertEquals(5, a.getSizeInBytes());
        assertEquals(5, a.encode().length);
        byte[] bytes = a.encode();
        assertEquals(0xFFFFFFFFL, new VarInt(bytes, 0).longValue());
    }

    @Test
    public void testLong() {
        VarInt a = new VarInt(0xCAFEBABEDEADBEEFL);
        assertEquals(9, a.getSizeInBytes());
        assertEquals(9, a.encode().length);
        byte[] bytes = a.encode();
        assertEquals(0xCAFEBABEDEADBEEFL, new VarInt(bytes, 0).longValue());
    }

    @Test
    public void testSizeOfNegativeInt() {
        // shouldn't normally be passed, but at least stay consistent (bug regression test)
        assertEquals(VarInt.sizeOf(-1), new VarInt(-1).encode().length);
    }
}
