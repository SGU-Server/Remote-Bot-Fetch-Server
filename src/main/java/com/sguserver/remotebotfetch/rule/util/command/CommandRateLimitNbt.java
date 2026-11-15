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
package com.sguserver.remotebotfetch.rule.util.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;

import com.sguserver.remotebotfetch.lib.MessageComponent;
import com.sguserver.remotebotfetch.lib.NameRateLimiter;

public final class CommandRateLimitNbt {
    private CommandRateLimitNbt() {}

    public static boolean sendIfRateLimited(CommandSourceStack source, Throwable throwable) {
        NameRateLimiter.RateLimitException rateLimitException = findRateLimitException(throwable);
        if (rateLimitException == null) {
            return false;
        }
        sendWaitSecond(source, rateLimitException.waitSeconds());
        return true;
    }

    public static void sendWaitSecond(CommandSourceStack source, int waitSecond) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("waitSecond", waitSecond);
        new MessageComponent(tag).sendFailure(source);
    }

    private static NameRateLimiter.RateLimitException findRateLimitException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof NameRateLimiter.RateLimitException rateLimitException) {
                return rateLimitException;
            }
            current = current.getCause();
        }
        return null;
    }
}
