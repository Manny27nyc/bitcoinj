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
# A rough overview of a modular bitcoinj architecture

Note: this diagram somewhat blurs the distinctions between modules/JARs and packages. Some of the "modules" would remain packages in the short-run.
Other packages that are not significant or are not changing are not shown (and are generally expected to remain in the `core` module). The difference between API and implementation dependencies is (for now) omitted.

````mermaid
graph TD
    B[base] --> G[guava]
    B --> S[slf4j]
    B --> A[jcip-annotations]
    CR[crypto] --> BC[Bouncy Castle]
    CR --> B
    CO[core] --> CR
    W --> P[ProtoBuf]
    W[wallet] --> CO
    I[integration-test] --> W
    WT[wallet-tool] --> W
    FX[wallettemplate] --> W
    E[examples] --> W
classDef external fill:#999;
class G,S,A,BC,P external;
````
