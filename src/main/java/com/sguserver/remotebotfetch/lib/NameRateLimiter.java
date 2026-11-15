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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;

public final class NameRateLimiter {
    private final Map<String, Long> lastCallMillisByName = new ConcurrentHashMap<>();

    public synchronized void check(String action, @Nullable String name, int cooldownSeconds) {
        if (cooldownSeconds <= 0) {
            return;
        }

        String key = name == null ? "" : name;
        long now = System.currentTimeMillis();
        long cooldownMillis = cooldownSeconds * 1000L;
        Long last = this.lastCallMillisByName.get(key);
        if (last != null && now - last < cooldownMillis) {
            long elapsed = now - last;
            long waitSeconds = (cooldownMillis - elapsed + 999L) / 1000L;
            throw new RateLimitException(action, key, (int)waitSeconds);
        }

        this.lastCallMillisByName.put(key, now);
    }

    public static final class RateLimitException extends IllegalStateException {
        private final int waitSeconds;

        private RateLimitException(String action, String name, int waitSeconds) {
            super(action + " is rate limited for player \"" + name + "\", wait " + waitSeconds + "s");
            this.waitSeconds = waitSeconds;
        }

        public int waitSeconds() {
            return this.waitSeconds;
        }
    }
}
