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

package org.bitcoinj.utils;

import com.google.common.util.concurrent.CycleDetectingLockFactory;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Uninterruptibles;
import org.bitcoinj.core.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Various threading related utilities. Provides a wrapper around explicit lock creation that lets you control whether
 * bitcoinj performs cycle detection or not. Cycle detection is useful to detect bugs but comes with a small cost.
 * Also provides a worker thread that is designed for event listeners to be dispatched on.
 */
public class Threading {

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // User thread/event handling utilities
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * An executor with one thread that is intended for running event listeners on. This ensures all event listener code
     * runs without any locks being held. It's intended for the API user to run things on. Callbacks registered by
     * bitcoinj internally shouldn't normally run here, although currently there are a few exceptions.
     */
    public static Executor USER_THREAD;

    /**
     * A dummy executor that just invokes the runnable immediately. Use this over more complex executors
     * (e.g. those extending {@link ExecutorService}), which are overkill for our needs.
     */
    public static final Executor SAME_THREAD;

    /**
     * Put a dummy task into the queue and wait for it to be run. Because it's single threaded, this means all
     * tasks submitted before this point are now completed. Usually you won't want to use this method - it's a
     * convenience primarily used in unit testing. If you want to wait for an event to be called the right thing
     * to do is usually to create a {@link CompletableFuture} and then call {@link CompletableFuture#complete(Object)}
     * on it. For example:
     * <pre>{@code
     * CompletableFuture f = CompletableFuture.supplyAsync(() -> event, USER_THREAD)
     * }</pre>
     * You can then either block on that future, compose it, add listeners to it and so on.
     */
    public static void waitForUserCode() {
        CompletableFuture.runAsync(() -> {}, USER_THREAD).join();
    }

    /**
     * An exception handler that will be invoked for any exceptions that occur in the user thread, and
     * any unhandled exceptions that are caught whilst the framework is processing network traffic or doing other
     * background tasks. The purpose of this is to allow you to report back unanticipated crashes from your users
     * to a central collection center for analysis and debugging. You should configure this <b>before</b> any
     * bitcoinj library code is run, setting it after you started network traffic and other forms of processing
     * may result in the change not taking effect.
     */
    @Nullable
    public static volatile Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    public static class UserThread extends Thread implements Executor {
        private static final Logger log = LoggerFactory.getLogger(UserThread.class);
        // 10,000 pending tasks is entirely arbitrary and may or may not be appropriate for the device we're
        // running on.
        public static int WARNING_THRESHOLD = 10000;
        private final LinkedBlockingQueue<Runnable> tasks;

        public UserThread() {
            super("bitcoinj user thread");
            setDaemon(true);
            tasks = new LinkedBlockingQueue<>();
            start();
        }

        @SuppressWarnings("InfiniteLoopStatement") @Override
        public void run() {
            while (true) {
                Runnable task = Uninterruptibles.takeUninterruptibly(tasks);
                try {
                    task.run();
                } catch (Throwable throwable) {
                    log.warn("Exception in user thread", throwable);
                    Thread.UncaughtExceptionHandler handler = uncaughtExceptionHandler;
                    if (handler != null)
                        handler.uncaughtException(this, throwable);
                }
            }
        }

        @Override
        public void execute(Runnable command) {
            final int size = tasks.size();
            if (size == WARNING_THRESHOLD) {
                log.warn(
                    "User thread has {} pending tasks, memory exhaustion may occur.\n" +
                    "If you see this message, check your memory consumption and see if it's problematic or excessively spikey.\n" +
                    "If it is, check for deadlocked or slow event handlers. If it isn't, try adjusting the constant \n" +
                    "Threading.UserThread.WARNING_THRESHOLD upwards until it's a suitable level for your app, or Integer.MAX_VALUE to disable." , size);
            }
            Uninterruptibles.putUninterruptibly(tasks, command);
        }
    }

    static {
        // Default policy goes here. If you want to change this, use one of the static methods before
        // instantiating any bitcoinj objects. The policy change will take effect only on new objects
        // from that point onwards.
        throwOnLockCycles();

        USER_THREAD = new UserThread();
        SAME_THREAD = Runnable::run;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Cycle detecting lock factories
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static CycleDetectingLockFactory.Policy policy;
    public static CycleDetectingLockFactory factory;

    public static ReentrantLock lock(Class clazz) {
        return lock(clazz.getSimpleName() + " lock");
    }

    public static ReentrantLock lock(String name) {
        if (Utils.isAndroidRuntime())
            return new ReentrantLock(true);
        else
            return factory.newReentrantLock(name);
    }

    public static void warnOnLockCycles() {
        setPolicy(CycleDetectingLockFactory.Policies.WARN);
    }

    public static void throwOnLockCycles() {
        setPolicy(CycleDetectingLockFactory.Policies.THROW);
    }

    public static void ignoreLockCycles() {
        setPolicy(CycleDetectingLockFactory.Policies.DISABLED);
    }

    public static void setPolicy(CycleDetectingLockFactory.Policy policy) {
        Threading.policy = policy;
        factory = CycleDetectingLockFactory.newInstance(policy);
    }

    public static CycleDetectingLockFactory.Policy getPolicy() {
        return policy;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Generic worker pool.
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** A caching thread pool that creates daemon threads, which won't keep the JVM alive waiting for more work. */
    public static ListeningExecutorService THREAD_POOL = MoreExecutors.listeningDecorator(
            Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r);
                t.setName("Threading.THREAD_POOL worker");
                t.setDaemon(true);
                return t;
            })
    );
}
