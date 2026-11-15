/*
 * Copyright (C) 2025  Remote-Bot-Fetch-Server contributors
 * https://github.com/SGU-Server/Remote-Bot-Fetch-Server

 * Remote Bot Fetch Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.

 * Remote Bot Fetch Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Remote Bot Fetch Server.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.sguserver.remotebotfetch.lib;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import com.sguserver.remotebotfetch.Mod;

public final class AsyncTasks {
    private static volatile ExecutorService executor;

    private AsyncTasks() {}

    public static synchronized void init() {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newCachedThreadPool(namedDaemonFactory(Mod.COMPACT_NAME));
        }
    }

    public static <T> CompletableFuture<T> supply(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, requireExecutor());
    }

    public static CompletableFuture<Void> run(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, requireExecutor());
    }

    public static ExecutorService executor() {
        return requireExecutor();
    }

    public static synchronized void shutdown() {
        ExecutorService current = executor;
        if (current == null) {
            return;
        }
        current.shutdownNow();
        executor = null;
    }

    private static ExecutorService requireExecutor() {
        ExecutorService current = executor;
        if (current == null || current.isShutdown()) {
            throw new IllegalStateException("AsyncTasks is not initialized for current server lifecycle");
        }
        return current;
    }

    private static ThreadFactory namedDaemonFactory(String namePrefix) {
        AtomicInteger id = new AtomicInteger(1);
        return runnable -> {
            Thread thread = new Thread(runnable, namePrefix + "-" + id.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
    }
}
