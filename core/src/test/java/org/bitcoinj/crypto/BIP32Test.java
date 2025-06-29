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
 * Copyright 2013 Matija Mazi.
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

import org.bitcoinj.base.Base58;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.bitcoinj.base.utils.ByteUtils.HEX;
import static org.junit.Assert.assertEquals;

/**
 * A test with test vectors as per BIP 32 spec: https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki#Test_Vectors
 */
public class BIP32Test {
    private static final Logger log = LoggerFactory.getLogger(BIP32Test.class);
    private static final NetworkParameters MAINNET = MainNetParams.get();

    HDWTestVector[] tvs = {
            new HDWTestVector(
                    "000102030405060708090a0b0c0d0e0f",
                    "xprv9s21ZrQH143K3QTDL4LXw2F7HEK3wJUD2nW2nRk4stbPy6cq3jPPqjiChkVvvNKmPGJxWUtg6LnF5kejMRNNU3TGtRBeJgk33yuGBxrMPHi",
                    "xpub661MyMwAqRbcFtXgS5sYJABqqG9YLmC4Q1Rdap9gSE8NqtwybGhePY2gZ29ESFjqJoCu1Rupje8YtGqsefD265TMg7usUDFdp6W1EGMcet8",
                    Arrays.asList(
                            new HDWTestVector.DerivedTestCase(
                                    "Test1 m/0H",
                                    HDPath.m(new ChildNumber(0, true)),
                                    "xprv9uHRZZhk6KAJC1avXpDAp4MDc3sQKNxDiPvvkX8Br5ngLNv1TxvUxt4cV1rGL5hj6KCesnDYUhd7oWgT11eZG7XnxHrnYeSvkzY7d2bhkJ7",
                                    "xpub68Gmy5EdvgibQVfPdqkBBCHxA5htiqg55crXYuXoQRKfDBFA1WEjWgP6LHhwBZeNK1VTsfTFUHCdrfp1bgwQ9xv5ski8PX9rL2dZXvgGDnw"
                            ),
                            new HDWTestVector.DerivedTestCase(
                                    "Test1 m/0H/1",
                                    HDPath.m(new ChildNumber(0, true), new ChildNumber(1, false)),
                                    "xprv9wTYmMFdV23N2TdNG573QoEsfRrWKQgWeibmLntzniatZvR9BmLnvSxqu53Kw1UmYPxLgboyZQaXwTCg8MSY3H2EU4pWcQDnRnrVA1xe8fs",
                                    "xpub6ASuArnXKPbfEwhqN6e3mwBcDTgzisQN1wXN9BJcM47sSikHjJf3UFHKkNAWbWMiGj7Wf5uMash7SyYq527Hqck2AxYysAA7xmALppuCkwQ"
                            ),
                            new HDWTestVector.DerivedTestCase(
                                    "Test1 m/0H/1/2H",
                                    HDPath.m(new ChildNumber(0, true), new ChildNumber(1, false), new ChildNumber(2, true)),
                                    "xprv9z4pot5VBttmtdRTWfWQmoH1taj2axGVzFqSb8C9xaxKymcFzXBDptWmT7FwuEzG3ryjH4ktypQSAewRiNMjANTtpgP4mLTj34bhnZX7UiM",
                                    "xpub6D4BDPcP2GT577Vvch3R8wDkScZWzQzMMUm3PWbmWvVJrZwQY4VUNgqFJPMM3No2dFDFGTsxxpG5uJh7n7epu4trkrX7x7DogT5Uv6fcLW5"
                            ),
                            new HDWTestVector.DerivedTestCase(
                                    "Test1 m/0H/1/2H/2",
                                    HDPath.m(new ChildNumber(0, true), new ChildNumber(1, false), new ChildNumber(2, true), new ChildNumber(2, false)),
                                    "xprvA2JDeKCSNNZky6uBCviVfJSKyQ1mDYahRjijr5idH2WwLsEd4Hsb2Tyh8RfQMuPh7f7RtyzTtdrbdqqsunu5Mm3wDvUAKRHSC34sJ7in334",
                                    "xpub6FHa3pjLCk84BayeJxFW2SP4XRrFd1JYnxeLeU8EqN3vDfZmbqBqaGJAyiLjTAwm6ZLRQUMv1ZACTj37sR62cfN7fe5JnJ7dh8zL4fiyLHV"
                            ),
                            new HDWTestVector.DerivedTestCase(
                                    "Test1 m/0H/1/2H/2/1000000000",
                                    HDPath.m(new ChildNumber(0, true), new ChildNumber(1, false), new ChildNumber(2, true), new ChildNumber(2, false), new ChildNumber(1000000000, false)),
                                    "xprvA41z7zogVVwxVSgdKUHDy1SKmdb533PjDz7J6N6mV6uS3ze1ai8FHa8kmHScGpWmj4WggLyQjgPie1rFSruoUihUZREPSL39UNdE3BBDu76",
                                    "xpub6H1LXWLaKsWFhvm6RVpEL9P4KfRZSW7abD2ttkWP3SSQvnyA8FSVqNTEcYFgJS2UaFcxupHiYkro49S8yGasTvXEYBVPamhGW6cFJodrTHy"
                            )
                    )
            ),
            new HDWTestVector(
                    "fffcf9f6f3f0edeae7e4e1dedbd8d5d2cfccc9c6c3c0bdbab7b4b1aeaba8a5a29f9c999693908d8a8784817e7b7875726f6c696663605d5a5754514e4b484542",
                    "xprv9s21ZrQH143K31xYSDQpPDxsXRTUcvj2iNHm5NUtrGiGG5e2DtALGdso3pGz6ssrdK4PFmM8NSpSBHNqPqm55Qn3LqFtT2emdEXVYsCzC2U",
                    "xpub661MyMwAqRbcFW31YEwpkMuc5THy2PSt5bDMsktWQcFF8syAmRUapSCGu8ED9W6oDMSgv6Zz8idoc4a6mr8BDzTJY47LJhkJ8UB7WEGuduB",
                    Arrays.asList(
                            new HDWTestVector.DerivedTestCase(
                                    "Test2 m/0",
                                    HDPath.m(new ChildNumber(0, false)),
                                    "xprv9vHkqa6EV4sPZHYqZznhT2NPtPCjKuDKGY38FBWLvgaDx45zo9WQRUT3dKYnjwih2yJD9mkrocEZXo1ex8G81dwSM1fwqWpWkeS3v86pgKt",
                                    "xpub69H7F5d8KSRgmmdJg2KhpAK8SR3DjMwAdkxj3ZuxV27CprR9LgpeyGmXUbC6wb7ERfvrnKZjXoUmmDznezpbZb7ap6r1D3tgFxHmwMkQTPH"
                            ),
                            new HDWTestVector.DerivedTestCase(
                                    "Test2 m/0/2147483647H",
                                    HDPath.m(new ChildNumber(0, false), new ChildNumber(2147483647, true)),
                                    "xprv9wSp6B7kry3Vj9m1zSnLvN3xH8RdsPP1Mh7fAaR7aRLcQMKTR2vidYEeEg2mUCTAwCd6vnxVrcjfy2kRgVsFawNzmjuHc2YmYRmagcEPdU9",
                                    "xpub6ASAVgeehLbnwdqV6UKMHVzgqAG8Gr6riv3Fxxpj8ksbH9ebxaEyBLZ85ySDhKiLDBrQSARLq1uNRts8RuJiHjaDMBU4Zn9h8LZNnBC5y4a"
                            ),
                            new HDWTestVector.DerivedTestCase(
                                    "Test2 m/0/2147483647H/1",
                                    HDPath.m(new ChildNumber(0, false), new ChildNumber(2147483647, true), new ChildNumber(1, false)),
                                    "xprv9zFnWC6h2cLgpmSA46vutJzBcfJ8yaJGg8cX1e5StJh45BBciYTRXSd25UEPVuesF9yog62tGAQtHjXajPPdbRCHuWS6T8XA2ECKADdw4Ef",
                                    "xpub6DF8uhdarytz3FWdA8TvFSvvAh8dP3283MY7p2V4SeE2wyWmG5mg5EwVvmdMVCQcoNJxGoWaU9DCWh89LojfZ537wTfunKau47EL2dhHKon"
                            ),
                            new HDWTestVector.DerivedTestCase(
                                    "Test2 m/0/2147483647H/1/2147483646H",
                                    HDPath.m(new ChildNumber(0, false), new ChildNumber(2147483647, true), new ChildNumber(1, false), new ChildNumber(2147483646, true)),
                                    "xprvA1RpRA33e1JQ7ifknakTFpgNXPmW2YvmhqLQYMmrj4xJXXWYpDPS3xz7iAxn8L39njGVyuoseXzU6rcxFLJ8HFsTjSyQbLYnMpCqE2VbFWc",
                                    "xpub6ERApfZwUNrhLCkDtcHTcxd75RbzS1ed54G1LkBUHQVHQKqhMkhgbmJbZRkrgZw4koxb5JaHWkY4ALHY2grBGRjaDMzQLcgJvLJuZZvRcEL"
                            ),
                            new HDWTestVector.DerivedTestCase(
                                    "Test2 m/0/2147483647H/1/2147483646H/2",
                                    HDPath.m(new ChildNumber(0, false), new ChildNumber(2147483647, true), new ChildNumber(1, false), new ChildNumber(2147483646, true), new ChildNumber(2, false)),
                                    "xprvA2nrNbFZABcdryreWet9Ea4LvTJcGsqrMzxHx98MMrotbir7yrKCEXw7nadnHM8Dq38EGfSh6dqA9QWTyefMLEcBYJUuekgW4BYPJcr9E7j",
                                    "xpub6FnCn6nSzZAw5Tw7cgR9bi15UV96gLZhjDstkXXxvCLsUXBGXPdSnLFbdpq8p9HmGsApME5hQTZ3emM2rnY5agb9rXpVGyy3bdW6EEgAtqt"
                            )
                    )
            ),
            new HDWTestVector(
                    "4b381541583be4423346c643850da4b320e46a87ae3d2a4e6da11eba819cd4acba45d239319ac14f863b8d5ab5a0d0c64d2e8a1e7d1457df2e5a3c51c73235be",
                    "xprv9s21ZrQH143K25QhxbucbDDuQ4naNntJRi4KUfWT7xo4EKsHt2QJDu7KXp1A3u7Bi1j8ph3EGsZ9Xvz9dGuVrtHHs7pXeTzjuxBrCmmhgC6",
                    "xpub661MyMwAqRbcEZVB4dScxMAdx6d4nFc9nvyvH3v4gJL378CSRZiYmhRoP7mBy6gSPSCYk6SzXPTf3ND1cZAceL7SfJ1Z3GC8vBgp2epUt13",
                    Arrays.asList(
                            new HDWTestVector.DerivedTestCase(
                                    "Test3 m/0H",
                                    HDPath.m(new ChildNumber(0, true)),
                                    "xprv9uPDJpEQgRQfDcW7BkF7eTya6RPxXeJCqCJGHuCJ4GiRVLzkTXBAJMu2qaMWPrS7AANYqdq6vcBcBUdJCVVFceUvJFjaPdGZ2y9WACViL4L",
                                    "xpub68NZiKmJWnxxS6aaHmn81bvJeTESw724CRDs6HbuccFQN9Ku14VQrADWgqbhhTHBaohPX4CjNLf9fq9MYo6oDaPPLPxSb7gwQN3ih19Zm4Y"
                            )
                   )
            ),
            new HDWTestVector(
                    "3ddd5602285899a946114506157c7997e5444528f3003f6134712147db19b678",
                    "xprv9s21ZrQH143K48vGoLGRPxgo2JNkJ3J3fqkirQC2zVdk5Dgd5w14S7fRDyHH4dWNHUgkvsvNDCkvAwcSHNAQwhwgNMgZhLtQC63zxwhQmRv",
                    "xpub661MyMwAqRbcGczjuMoRm6dXaLDEhW1u34gKenbeYqAix21mdUKJyuyu5F1rzYGVxyL6tmgBUAEPrEz92mBXjByMRiJdba9wpnN37RLLAXa",
                    Arrays.asList(
                            new HDWTestVector.DerivedTestCase(
                                    "Test4 m/0H",
                                    HDPath.m(new ChildNumber(0, true)),
                                    "xprv9vB7xEWwNp9kh1wQRfCCQMnZUEG21LpbR9NPCNN1dwhiZkjjeGRnaALmPXCX7SgjFTiCTT6bXes17boXtjq3xLpcDjzEuGLQBM5ohqkao9G",
                                    "xpub69AUMk3qDBi3uW1sXgjCmVjJ2G6WQoYSnNHyzkmdCHEhSZ4tBok37xfFEqHd2AddP56Tqp4o56AePAgCjYdvpW2PU2jbUPFKsav5ut6Ch1m"
                            ),
                            new HDWTestVector.DerivedTestCase(
                                    "Test4 m/0H/1H",
                                    HDPath.m(new ChildNumber(0, true), new ChildNumber(1, true)),
                                    "xprv9xJocDuwtYCMNAo3Zw76WENQeAS6WGXQ55RCy7tDJ8oALr4FWkuVoHJeHVAcAqiZLE7Je3vZJHxspZdFHfnBEjHqU5hG1Jaj32dVoS6XLT1",
                                    "xpub6BJA1jSqiukeaesWfxe6sNK9CCGaujFFSJLomWHprUL9DePQ4JDkM5d88n49sMGJxrhpjazuXYWdMf17C9T5XnxkopaeS7jGk1GyyVziaMt"
                            )
                    )
            )
    };

    @Test
    public void testVector1() {
        testVector(0);
    }

    @Test
    public void testVector2() {
        testVector(1);
    }

    @Test
    public void testVector3() {
        testVector(2);
    }

    @Test
    public void testVector4() {
        testVector(3);
    }

    private void testVector(int testCase) {
        log.info("=======  Test vector {}", testCase);
        HDWTestVector tv = tvs[testCase];
        DeterministicKey masterPrivateKey = HDKeyDerivation.createMasterPrivateKey(HEX.decode(tv.seed));
        assertEquals(testEncode(tv.priv), testEncode(masterPrivateKey.serializePrivB58(MAINNET)));
        assertEquals(testEncode(tv.pub), testEncode(masterPrivateKey.serializePubB58(MAINNET)));
        DeterministicHierarchy dh = new DeterministicHierarchy(masterPrivateKey);
        for (int i = 0; i < tv.derived.size(); i++) {
            HDWTestVector.DerivedTestCase tc = tv.derived.get(i);
            log.info("{}", tc.name);
            assertEquals(tc.name, String.format(Locale.US, "Test%d %s", testCase + 1, tc.path));
            int depth = tc.path.size() - 1;
            DeterministicKey ehkey = dh.deriveChild(tc.path.subList(0, depth), false, true, tc.path.get(depth));
            assertEquals(testEncode(tc.priv), testEncode(ehkey.serializePrivB58(MAINNET)));
            assertEquals(testEncode(tc.pub), testEncode(ehkey.serializePubB58(MAINNET)));
        }
    }

    private String testEncode(String what) {
        return HEX.encode(Base58.decodeChecked(what));
    }

    static class HDWTestVector {
        final String seed;
        final String priv;
        final String pub;
        final List<DerivedTestCase> derived;

        HDWTestVector(String seed, String priv, String pub, List<DerivedTestCase> derived) {
            this.seed = seed;
            this.priv = priv;
            this.pub = pub;
            this.derived = derived;
        }

        static class DerivedTestCase {
            final String name;
            final HDPath path;
            final String pub;
            final String priv;

            DerivedTestCase(String name, HDPath path, String priv, String pub) {
                this.name = name;
                this.path = path;
                this.pub = pub;
                this.priv = priv;
            }
        }
    }
}
