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
 * Copyright 2014 Andreas Schildbach
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

package org.bitcoinj.crypto;

import org.junit.Test;

import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static org.junit.Assert.assertEquals;

public class X509UtilsTest {

    @Test
    public void testDisplayName() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        X509Certificate clientCert = (X509Certificate) cf.generateCertificate(getClass().getResourceAsStream(
                "startssl-client.crt"));
        assertEquals("Andreas Schildbach", X509Utils.getDisplayNameFromCertificate(clientCert, false));

        X509Certificate comodoCert = (X509Certificate) cf.generateCertificate(getClass().getResourceAsStream(
                "comodo-smime.crt"));
        assertEquals("comodo.com@schildbach.de", X509Utils.getDisplayNameFromCertificate(comodoCert, true));
    }
}
