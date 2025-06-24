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
 * Copyright 2019 Michael Sean Gilligan
 * Copyright 2019 Tim Strasser
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

import org.bitcoinj.core.Utils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Basic test of AppDataDirectory
 */
public class AppDataDirectoryTest {
    private static final String HOMEDIR = System.getProperty("user.home");
    private static final String WINAPPDATA = System.getenv("APPDATA");

    @Test
    public void worksOnCurrentPlatform() {
        final String appName = "bitcoinj";
        String path = AppDataDirectory.get(appName).toString();
        if (Utils.isWindows()) {
            assertEquals("Path wrong on Windows", winPath(appName), path);
        } else if (Utils.isMac()) {
            assertEquals("Path wrong on Mac",  macPath(appName), path);
        } else if (Utils.isLinux()) {
            assertEquals("Path wrong on Linux",  unixPath(appName), path);
        } else {
            assertEquals("Path wrong on unknown/default",  unixPath(appName), path);
        }
    }

    @Test
    public void worksOnCurrentPlatformForBitcoinCore() {
        final String appName = "Bitcoin";
        String path = AppDataDirectory.get(appName).toString();
        if (Utils.isWindows()) {
            assertEquals("Path wrong on Windows", winPath(appName), path);
        } else if (Utils.isMac()) {
            assertEquals("Path wrong on Mac",  macPath(appName), path);
        } else if (Utils.isLinux()) {
            assertEquals("Path wrong on Linux",  unixPath(appName), path);
        } else {
            assertEquals("Path wrong on unknown/default",  unixPath(appName), path);
        }
    }

    @Test(expected = RuntimeException.class)
    public void throwsIOExceptionIfPathNotFound() {
        // Force exceptions with illegal characters
        if (Utils.isWindows()) {
            AppDataDirectory.get(":");  // Illegal character for Windows
        }
        if (Utils.isMac()) {
            // NUL character
            AppDataDirectory.get("\0"); // Illegal character for Mac
        }
        if (Utils.isLinux()) {
            // NUL character
            AppDataDirectory.get("\0"); // Illegal character for Linux
        }
    }


    private static String winPath(String appName) {
        return WINAPPDATA + "\\" + appName.toLowerCase();
    }

    private static String macPath(String appName) {
        return HOMEDIR + "/Library/Application Support/" + appName;
    }

    private static String unixPath(String appName) {
        return HOMEDIR + "/." + appName.toLowerCase();
    }
}
